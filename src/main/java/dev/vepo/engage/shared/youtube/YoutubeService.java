package dev.vepo.engage.shared.youtube;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    public SearchListResponse loadNewVideos(String channelId, Instant lastSync) {
        try {
            var youtubeService = getServiceWithApiKey();
            var videosRequest = youtubeService.search()
                                              .list("snippet")
                                              .setChannelId(channelId)
                                              .setType("video")
                                              .setPublishedAfter(Optional.ofNullable(lastSync)
                                                                         .map(sync -> new DateTime(sync.toEpochMilli()))
                                                                         .orElseGet(() -> new DateTime(0l)))
                                                                         .setMaxResults(10L)
                                                                         .setOrder("date")
                                                                         .setKey(apiKey);


             logger.info("Sending request....");

            var videosResponse = videosRequest.execute();
            logger.info("Response: {}", videosResponse);
            return videosResponse;
        } catch (IOException ioe) {
            logger.error("Error connecting with Youtube!", ioe);
            throw new RuntimeException("Cannot load Youtube Service!", ioe);
        }
    }
}
