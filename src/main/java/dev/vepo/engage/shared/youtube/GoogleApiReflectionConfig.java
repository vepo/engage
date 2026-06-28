package dev.vepo.engage.shared.youtube;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.GenericData;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.CommentThreadReplies;
import com.google.api.services.youtube.model.CommentThreadSnippet;
import com.google.api.services.youtube.model.PageInfo;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Google API client uses reflection for JSON models and for
 * {@link YouTubeRequest} query parameters ({@code key}, {@code part}, …) via
 * {@code UriTemplate} / {@code DataMap}. Without registration, native image
 * builds succeed but runtime calls omit the API key and YouTube returns 403
 * "unregistered callers".
 */
@RegisterForReflection(targets = { GenericData.class, GenericJson.class, GoogleJsonError.class, GoogleJsonError.ErrorInfo.class, GoogleJsonError.Details.class, GoogleJsonError.ParameterViolations.class, GoogleJsonErrorContainer.class, YouTube.class, YouTubeRequest.class, ChannelListResponse.class, Channel.class, ChannelSnippet.class, ChannelStatistics.class, ThumbnailDetails.class, Thumbnail.class, PageInfo.class, SearchListResponse.class, SearchResult.class, SearchResultSnippet.class, CommentThreadListResponse.class, CommentThread.class, CommentThreadSnippet.class, CommentThreadReplies.class, Comment.class, CommentSnippet.class, VideoListResponse.class, Video.class, VideoSnippet.class, VideoStatistics.class
}, registerFullHierarchy = true)
public final class GoogleApiReflectionConfig {
    private GoogleApiReflectionConfig() {}
}
