package dev.vepo.engage.shared.youtube;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import dev.vepo.engage.shared.notification.SyncReportFields;
import dev.vepo.engage.shared.notification.SyncRunReport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

/**
 * Facade for YouTube Data API v3. Every call receives the channel-specific API
 * key so quota is attributed per connected channel and sync can skip channels
 * without credentials.
 */
@ApplicationScoped
public class YoutubeApiFacade {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeApiFacade.class);
    private static final String APPLICATION_NAME = "engage";
    private static final long MAX_SEARCH_RESULTS = 50L;
    private static final long MAX_PLAYLIST_RESULTS = 50L;
    private static final long MAX_COMMENT_RESULTS = 100L;

    private final AtomicReference<YouTube> youtubeClient = new AtomicReference<>();

    public void validateChannelExists(String apiKey, String youtubeChannelId) {
        fetchChannelStatistics(apiKey, youtubeChannelId);
    }

    public String fetchUploadsPlaylistId(String apiKey, String youtubeChannelId) {
        requireApiKey(apiKey);
        try {
            ChannelListResponse response = client().channels()
                                                   .list("contentDetails")
                                                   .setId(youtubeChannelId)
                                                   .setKey(apiKey)
                                                   .execute();

            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new IllegalStateException("YouTube channel not found: %s".formatted(youtubeChannelId));
            }

            var uploadsPlaylistId = response.getItems().getFirst().getContentDetails().getRelatedPlaylists().getUploads();
            if (uploadsPlaylistId == null || uploadsPlaylistId.isBlank()) {
                throw new IllegalStateException("YouTube uploads playlist not found for channel %s".formatted(youtubeChannelId));
            }
            return uploadsPlaylistId;
        } catch (GoogleJsonResponseException gjre) {
            logger.error("YouTube API error loading uploads playlist for {}", youtubeChannelId, gjre);
            throw youtubeRequestFailed("Cannot load YouTube uploads playlist", gjre);
        } catch (IOException ioe) {
            logger.error("IO error loading uploads playlist for {}", youtubeChannelId, ioe);
            throw new IllegalStateException("Cannot load YouTube uploads playlist", ioe);
        }
    }

    public PlaylistVideoPage fetchUploadsPlaylistPage(String apiKey,
                                                      String uploadsPlaylistId,
                                                      String pageToken,
                                                      SyncRunReport report) {
        requireApiKey(apiKey);
        try {
            var request = client().playlistItems()
                                  .list("snippet,contentDetails")
                                  .setPlaylistId(uploadsPlaylistId)
                                  .setMaxResults(MAX_PLAYLIST_RESULTS)
                                  .setKey(apiKey)
                                  .setPageToken(pageToken);

            PlaylistItemListResponse response = request.execute();
            var items = response.getItems() == null ? List.<PlaylistItem>of() : response.getItems();
            var snippets = items.stream().map(this::toVideoSnippet).toList();
            var nextToken = response.getNextPageToken();
            var lastPage = items.isEmpty() || nextToken == null;
            recordApiCall(report,
                          "youtube.playlistItems.list",
                          "Vídeos da playlist de uploads",
                          "success",
                          200,
                          snippets.size(),
                          null,
                          null,
                          null);
            return new PlaylistVideoPage(snippets, nextToken, lastPage);
        } catch (GoogleJsonResponseException gjre) {
            logger.error("YouTube playlist items failed for playlist {}", uploadsPlaylistId, gjre);
            recordApiCall(report,
                          "youtube.playlistItems.list",
                          "Vídeos da playlist de uploads",
                          gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode() ? "forbidden" : "error",
                          gjre.getStatusCode(),
                          0,
                          null,
                          null,
                          gjre.getMessage());
            if (gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
                return new PlaylistVideoPage(List.of(), pageToken, true);
            }
            throw new IllegalStateException("Cannot load uploads playlist videos for %s".formatted(uploadsPlaylistId), gjre);
        } catch (IOException ioe) {
            logger.error("IO error loading playlist items for {}", uploadsPlaylistId, ioe);
            recordApiCall(report,
                          "youtube.playlistItems.list",
                          "Vídeos da playlist de uploads",
                          "error",
                          0,
                          0,
                          null,
                          null,
                          ioe.getMessage());
            throw new IllegalStateException("Cannot load uploads playlist videos for %s".formatted(uploadsPlaylistId), ioe);
        }
    }

    public VideoSearchPage fetchVideoSearchPage(String apiKey,
                                                String youtubeChannelId,
                                                Instant publishedAfter,
                                                String pageToken) {
        return fetchVideoSearchPage(apiKey, youtubeChannelId, publishedAfter, pageToken, null);
    }

    public VideoSearchPage fetchVideoSearchPage(String apiKey,
                                                String youtubeChannelId,
                                                Instant publishedAfter,
                                                String pageToken,
                                                SyncRunReport report) {
        requireApiKey(apiKey);
        try {
            var request = client().search()
                                  .list("snippet")
                                  .setChannelId(youtubeChannelId)
                                  .setType("video")
                                  .setPublishedAfter(Optional.ofNullable(publishedAfter)
                                                             .map(sync -> new DateTime(sync.toEpochMilli()))
                                                             .orElseGet(() -> new DateTime(0L)))
                                  .setMaxResults(MAX_SEARCH_RESULTS)
                                  .setOrder("date")
                                  .setKey(apiKey)
                                  .setPageToken(pageToken);

            var response = request.execute();
            var items = response.getItems() == null ? List.<SearchResult>of() : response.getItems();
            var nextToken = response.getNextPageToken();
            var lastPage = items.isEmpty() || nextToken == null;
            recordApiCall(report,
                          "youtube.search.list",
                          "Busca de vídeos do canal",
                          "success",
                          200,
                          items.size(),
                          youtubeChannelId,
                          null,
                          null);
            return new VideoSearchPage(items, nextToken, lastPage);
        } catch (GoogleJsonResponseException gjre) {
            logger.error("YouTube search failed for channel {}", youtubeChannelId, gjre);
            recordApiCall(report,
                          "youtube.search.list",
                          "Busca de vídeos do canal",
                          gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode() ? "forbidden" : "error",
                          gjre.getStatusCode(),
                          0,
                          youtubeChannelId,
                          null,
                          gjre.getMessage());
            if (gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
                return new VideoSearchPage(List.of(), pageToken, true);
            }
            throw new IllegalStateException("Cannot search YouTube videos for channel %s".formatted(youtubeChannelId), gjre);
        } catch (IOException ioe) {
            logger.error("IO error searching videos for channel {}", youtubeChannelId, ioe);
            recordApiCall(report,
                          "youtube.search.list",
                          "Busca de vídeos do canal",
                          "error",
                          0,
                          0,
                          youtubeChannelId,
                          null,
                          ioe.getMessage());
            throw new IllegalStateException("Cannot search YouTube videos for channel %s".formatted(youtubeChannelId), ioe);
        }
    }

    public CommentThreadPage fetchCommentThreadPage(String apiKey, String youtubeVideoId, String pageToken) {
        return fetchCommentThreadPage(apiKey, youtubeVideoId, pageToken, null);
    }

    public CommentThreadPage fetchCommentThreadPage(String apiKey,
                                                    String youtubeVideoId,
                                                    String pageToken,
                                                    SyncRunReport report) {
        requireApiKey(apiKey);
        try {
            var request = client().commentThreads()
                                  .list("snippet,replies")
                                  .setKey(apiKey)
                                  .setVideoId(youtubeVideoId)
                                  .setMaxResults(MAX_COMMENT_RESULTS)
                                  .setOrder("time")
                                  .setPageToken(pageToken);

            var response = request.execute();
            var items = response.getItems() == null ? List.<CommentThread>of() : response.getItems();
            var nextToken = response.getNextPageToken();
            var lastPage = items.isEmpty() || nextToken == null;
            recordApiCall(report,
                          "youtube.commentThreads.list",
                          "Comentários do vídeo",
                          "success",
                          200,
                          items.size(),
                          null,
                          youtubeVideoId,
                          null);
            return new CommentThreadPage(items, nextToken, lastPage);
        } catch (GoogleJsonResponseException gjre) {
            logger.error("YouTube comments failed for video {}", youtubeVideoId, gjre);
            recordApiCall(report,
                          "youtube.commentThreads.list",
                          "Comentários do vídeo",
                          gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode() ? "forbidden" : "error",
                          gjre.getStatusCode(),
                          0,
                          null,
                          youtubeVideoId,
                          gjre.getMessage());
            if (gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
                return new CommentThreadPage(List.of(), pageToken, true);
            }
            throw new IllegalStateException("Cannot load comments for video %s".formatted(youtubeVideoId), gjre);
        } catch (IOException ioe) {
            logger.error("IO error loading comments for video {}", youtubeVideoId, ioe);
            recordApiCall(report,
                          "youtube.commentThreads.list",
                          "Comentários do vídeo",
                          "error",
                          0,
                          0,
                          null,
                          youtubeVideoId,
                          ioe.getMessage());
            throw new IllegalStateException("Cannot load comments for video %s".formatted(youtubeVideoId), ioe);
        }
    }

    public void loadAllCommentsForVideo(String apiKey, String youtubeVideoId, Consumer<CommentThread> consumer) {
        String nextPageToken = null;
        do {
            var page = fetchCommentThreadPage(apiKey, youtubeVideoId, nextPageToken);
            page.items().forEach(consumer);
            nextPageToken = page.lastPage() ? null : page.nextPageToken();
        } while (nextPageToken != null);
    }

    public YoutubeChannelStatistics fetchChannelStatistics(String apiKey, String youtubeChannelId) {
        requireApiKey(apiKey);
        try {
            ChannelListResponse response = client().channels()
                                                   .list("snippet,statistics")
                                                   .setId(youtubeChannelId)
                                                   .setKey(apiKey)
                                                   .execute();

            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new IllegalStateException("YouTube channel not found: %s".formatted(youtubeChannelId));
            }

            Channel channel = response.getItems().getFirst();
            var snippet = channel.getSnippet();
            var statistics = channel.getStatistics();

            return new YoutubeChannelStatistics(snippet.getTitle(),
                                                snippet.getThumbnails().getDefault().getUrl(),
                                                statistics.getSubscriberCount().longValue(),
                                                statistics.getViewCount().longValue(),
                                                statistics.getVideoCount().longValue(),
                                                Boolean.TRUE.equals(statistics.getHiddenSubscriberCount()));
        } catch (GoogleJsonResponseException gjre) {
            logger.error("YouTube API error loading channel statistics for {}", youtubeChannelId, gjre);
            throw youtubeRequestFailed("Cannot load YouTube channel statistics", gjre);
        } catch (IllegalArgumentException ex) {
            if (isGoogleApiErrorParsingFailure(ex)) {
                logger.error("YouTube API rejected request for channel {}", youtubeChannelId, ex);
                throw new IllegalStateException(
                                                "YouTube API rejected the request — verify the API key, enable YouTube Data API v3, and enable billing",
                                                ex);
            }
            throw ex;
        } catch (IOException ioe) {
            logger.error("IO error loading channel statistics for {}", youtubeChannelId, ioe);
            throw new IllegalStateException("Cannot load YouTube channel statistics", ioe);
        }
    }

    public List<YoutubeVideoStatistics> fetchVideoStatistics(String apiKey, List<String> youtubeVideoIds) {
        if (youtubeVideoIds == null || youtubeVideoIds.isEmpty()) {
            return Collections.emptyList();
        }

        requireApiKey(apiKey);
        var results = new ArrayList<YoutubeVideoStatistics>();

        for (int index = 0; index < youtubeVideoIds.size(); index += 50) {
            var batch = youtubeVideoIds.subList(index, Math.min(index + 50, youtubeVideoIds.size()));
            var joinedIds = String.join(",", batch);

            try {
                VideoListResponse response = client().videos()
                                                     .list("snippet,statistics")
                                                     .setId(joinedIds)
                                                     .setKey(apiKey)
                                                     .execute();

                if (response.getItems() == null) {
                    continue;
                }

                for (Video video : response.getItems()) {
                    var snippet = video.getSnippet();
                    var statistics = video.getStatistics();
                    results.add(new YoutubeVideoStatistics(video.getId(),
                                                           snippet.getTitle(),
                                                           snippet.getThumbnails().getDefault().getUrl(),
                                                           parseCount(statistics.getViewCount()),
                                                           parseCount(statistics.getLikeCount()),
                                                           parseCount(statistics.getCommentCount())));
                }
            } catch (GoogleJsonResponseException gjre) {
                logger.error("YouTube API error loading video statistics", gjre);
                throw new IllegalStateException("Cannot load YouTube video statistics", gjre);
            } catch (IOException ioe) {
                logger.error("IO error loading video statistics", ioe);
                throw new IllegalStateException("Cannot load YouTube video statistics", ioe);
            }
        }

        return results;
    }

    private YouTube client() {
        return youtubeClient.updateAndGet(prev -> {
            if (Objects.nonNull(prev)) {
                return prev;
            }
            try {
                return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                                           GsonFactory.getDefaultInstance(),
                                           null)
                                                .setApplicationName(APPLICATION_NAME)
                                                .build();
            } catch (Exception ex) {
                logger.error("Error connecting with YouTube", ex);
                throw new IllegalStateException("Cannot load YouTube client", ex);
            }
        });
    }

    private void requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("YouTube API key is required for this channel");
        }
    }

    private IllegalStateException youtubeRequestFailed(String message, GoogleJsonResponseException gjre) {
        if (gjre.getStatusCode() == Status.BAD_REQUEST.getStatusCode()
                || gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
            return new IllegalStateException(
                                             "%s — verify the API key, enable YouTube Data API v3, and check key restrictions".formatted(message),
                                             gjre);
        }
        return new IllegalStateException(message, gjre);
    }

    private boolean isGoogleApiErrorParsingFailure(Throwable ex) {
        for (var current = ex; current != null; current = current.getCause()) {
            if (current instanceof InstantiationException || current instanceof NoSuchMethodException) {
                return true;
            }
            var className = current.getClass().getName();
            if (className.contains("GoogleJsonError") || className.contains("GenericJson")) {
                return true;
            }
        }
        return ex.getMessage() != null && ex.getMessage().contains("key error");
    }

    private long parseCount(java.math.BigInteger value) {
        return value == null ? 0L : value.longValue();
    }

    private YoutubeVideoSnippet toVideoSnippet(PlaylistItem item) {
        var snippet = item.getSnippet();
        var videoId = item.getContentDetails().getVideoId();
        var thumbnail = snippet.getThumbnails() != null && snippet.getThumbnails().getHigh() != null
                               ? snippet.getThumbnails().getHigh().getUrl()
                               : snippet.getThumbnails() != null && snippet.getThumbnails().getDefault() != null
                                        ? snippet.getThumbnails().getDefault().getUrl()
                               : null;
        var publishedAt = snippet.getPublishedAt() == null
                                                           ? null
                                                           : Instant.ofEpochMilli(snippet.getPublishedAt().getValue());
        return new YoutubeVideoSnippet(videoId,
                                       snippet.getTitle(),
                                       snippet.getDescription(),
                                       thumbnail,
                                       publishedAt);
    }

    private void recordApiCall(SyncRunReport report,
                               String operation,
                               String description,
                               String status,
                               int httpStatus,
                               int itemCount,
                               String channelId,
                               String videoId,
                               String error) {
        if (report == null) {
            return;
        }
        report.addItem(operation,
                       description,
                       SyncReportFields.apiCall(operation, status, httpStatus, itemCount, channelId, videoId, error));
    }
}
