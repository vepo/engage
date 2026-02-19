package dev.vepo.engage.shared.youtube;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class YoutubeService {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeService.class);
    private static final String APPLICATION_NAME = "engage";
    private String apiKey;
    private AtomicReference<YouTube> refYoutube;

    @Inject
    public YoutubeService(@ConfigProperty(name = "youtube.api.key") String apiKey) {
        this.apiKey = apiKey;
        this.refYoutube = new AtomicReference<>();
    }

    public YouTube getServiceWithApiKey() {
        return this.refYoutube.updateAndGet(prev -> {
            if (Objects.isNull(prev)) {
                try {
                    return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                                               GsonFactory.getDefaultInstance(),
                                               null)
                                                    .setApplicationName(APPLICATION_NAME)
                                                    .build();
                } catch (Exception ex) {
                    logger.error("Error connecting with Youtube!", ex);
                    throw new RuntimeException("Cannot load Youtube Service!", ex);
                }
            } else {
                return prev;
            }
        });
    }

    public String loadNewVideos(String channelId, Instant lastSync, String nextPageToken, Consumer<SearchResult> consumer) {
        boolean done = false;
        try {
            var youtubeService = getServiceWithApiKey();
            do {
                var videosRequest = youtubeService.search()
                                                  .list("snippet")
                                                  .setChannelId(channelId)
                                                  .setType("video")
                                                  .setPublishedAfter(Optional.ofNullable(lastSync)
                                                                             .map(sync -> new DateTime(sync.toEpochMilli()))
                                                                             .orElseGet(() -> new DateTime(0l)))
                                                  .setMaxResults(10L)
                                                  .setOrder("date")
                                                  .setKey(apiKey)
                                                  .setPageToken(nextPageToken);

                logger.info("Sending request....");

                var videosResponse = videosRequest.execute();
                done = videosResponse.isEmpty();
                logger.info("Response: {}", videosResponse);
                videosResponse.getItems()
                              .forEach(consumer);
                nextPageToken = videosResponse.getNextPageToken();
            } while (!done && Objects.nonNull(nextPageToken));
        } catch (GoogleJsonResponseException gjre) {
            logger.error("Error connecting with Youtube!", gjre);
            if (gjre.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
                return nextPageToken;
            }
            throw new RuntimeException("Cannot load Youtube Service!", gjre);
        } catch (IOException ioe) {
            logger.error("Error connecting with Youtube!", ioe);
            throw new RuntimeException("Cannot load Youtube Service!", ioe);
        }
        return null;
    }
}
