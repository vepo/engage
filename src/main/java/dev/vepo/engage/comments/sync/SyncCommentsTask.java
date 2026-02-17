package dev.vepo.engage.comments.sync;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SyncCommentsTask {
    private static final Logger logger = LoggerFactory.getLogger(SyncCommentsTask.class);

    private static final String APPLICATION_NAME = "engage";
    private String apiKey;

    public SyncCommentsTask(@ConfigProperty(name = "youtube.api.key") String apiKey) {
        this.apiKey = apiKey;
    }

    public YouTube getServiceWithApiKey() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport,
                                   GsonFactory.getDefaultInstance(),
                                   null)
                                        .setApplicationName(APPLICATION_NAME)
                                        .build();
    }

    // @Scheduled(every = "10s")
    void loadNewComments() {
        logger.info("Loading new comments...");
        try {
            YouTube youtubeService = getServiceWithApiKey();
            YouTube.CommentThreads.List commentRequest = youtubeService.commentThreads()
                                                                       .list("snippet,replies");

            commentRequest.setKey(apiKey);
            commentRequest.setVideoId("ELf3KgIqjLA");
            commentRequest.setMaxResults(100L);
            commentRequest.setChannelId("APPLICATION_NAME");

            logger.info("Sending request....");
            // Execute and process comments
            var commentResponse = commentRequest.execute();
            logger.info("Response: {}", commentResponse);
            // Process comments...
        } catch (Exception ex) {
            logger.error("Error fetching comments for video: {}", "ELf3KgIqjLA", ex);
        }
    }
}
