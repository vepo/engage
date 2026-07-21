package dev.vepo.engage.comments.sync;

import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.CommentThread;

import dev.vepo.engage.comments.CommentRepository;
import dev.vepo.engage.model.Comment;
import dev.vepo.engage.model.Video;
import dev.vepo.engage.shared.notification.PassportNotificationPublisher;
import dev.vepo.engage.shared.notification.SyncRunReport;
import dev.vepo.engage.shared.youtube.YoutubeApiFacade;
import dev.vepo.engage.video.VideoRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SyncCommentsTask {
    private static final Logger logger = LoggerFactory.getLogger(SyncCommentsTask.class);

    private final YoutubeApiFacade youtubeApiFacade;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final PassportNotificationPublisher passportNotificationPublisher;
    private final int videosPerRun;

    @Inject
    public SyncCommentsTask(YoutubeApiFacade youtubeApiFacade,
                            VideoRepository videoRepository,
                            CommentRepository commentRepository,
                            PassportNotificationPublisher passportNotificationPublisher,
                            @ConfigProperty(name = "engage.sync.comments.videos-per-run", defaultValue = "3") int videosPerRun) {
        this.youtubeApiFacade = youtubeApiFacade;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.passportNotificationPublisher = passportNotificationPublisher;
        this.videosPerRun = Math.max(1, videosPerRun);
    }

    @Scheduled(every = "${engage.sync.comments.interval:10m}", delayed = "45s")
    @Transactional
    void syncCommentsForDueVideos() {
        var videos = videoRepository.findDueForCommentSync(videosPerRun);
        if (videos.isEmpty()) {
            logger.debug("No videos due for comment sync");
            return;
        }

        logger.info("Starting comment sync for {} video(s)", videos.size());
        videos.forEach(this::syncCommentsForVideo);
    }

    private void syncCommentsForVideo(Video video) {
        var channel = video.getChannel();
        if (!channel.isReadyForSync()) {
            logger.debug("Skipping comment sync for video {} — channel not connected", video.getYoutubeId());
            return;
        }

        var report = new SyncRunReport("comment_sync",
                                       channel.getId(),
                                       "Sincronização de comentários",
                                       "Vídeo %s".formatted(video.getYoutubeId()));
        report.putSummary("youtubeVideoId", video.getYoutubeId());
        report.putSummary("youtubeChannelId", channel.getYoutubeId());

        var threadsProcessed = 0;
        try {
            var page = youtubeApiFacade.fetchCommentThreadPage(channel.getYoutubeApiKey(),
                                                               video.getYoutubeId(),
                                                               video.getCommentsNextPageToken(),
                                                               report);
            page.items().forEach(thread -> processCommentThread(thread, video));
            threadsProcessed = page.items().size();

            if (page.lastPage()) {
                video.setCommentsNextPageToken(null);
                video.setCommentsSyncAt(Instant.now());
            } else {
                video.setCommentsNextPageToken(page.nextPageToken());
            }
            videoRepository.save(video);

            report.putSummary("status", "completed");
            report.putSummary("threadsProcessed", threadsProcessed);
            report.setDescription("Vídeo %s — %d thread(s) processada(s)".formatted(video.getYoutubeId(), threadsProcessed));
        } catch (Exception ex) {
            logger.error("Error syncing comments for video {}", video.getYoutubeId(), ex);
            report.markFailed(ex.getMessage());
            report.setDescription("Falha na sincronização de comentários do vídeo %s".formatted(video.getYoutubeId()));
        } finally {
            if (report.isFailed()) {
                passportNotificationPublisher.publishSyncReport(report);
            }
        }
    }

    private void processCommentThread(CommentThread commentThread, Video video) {
        try {
            var snippet = commentThread.getSnippet();
            var topLevelComment = snippet.getTopLevelComment().getSnippet();
            var commentId = commentThread.getId();

            commentRepository.findByYoutubeCommentId(commentId)
                             .ifPresentOrElse(existingComment -> updateExistingComment(existingComment, topLevelComment),
                                              () -> createNewComment(commentId, video, topLevelComment));

            if (commentThread.getReplies() != null) {
                for (var reply : commentThread.getReplies().getComments()) {
                    processReply(reply, video);
                }
            }
        } catch (Exception ex) {
            logger.error("Error processing comment thread", ex);
        }
    }

    private void processReply(com.google.api.services.youtube.model.Comment reply, Video video) {
        var snippet = reply.getSnippet();
        var replyId = reply.getId();

        commentRepository.findByYoutubeCommentId(replyId)
                         .ifPresentOrElse(existingComment -> updateExistingComment(existingComment, snippet),
                                          () -> createNewComment(replyId, video, snippet));
    }

    private void createNewComment(String commentId, Video video,
                                  com.google.api.services.youtube.model.CommentSnippet snippet) {
        var comment = new Comment();
        comment.setYoutubeCommentId(commentId);
        comment.setVideo(video);
        comment.setAuthorName(snippet.getAuthorDisplayName());
        comment.setAuthorChannelId(snippet.getAuthorChannelId().toString());
        comment.setText(snippet.getTextDisplay());
        comment.setLikeCount(snippet.getLikeCount().intValue());
        comment.setPublishedAt(Instant.ofEpochMilli(snippet.getPublishedAt().getValue()));
        if (snippet.getUpdatedAt() != null) {
            comment.setUpdatedAt(Instant.ofEpochMilli(snippet.getUpdatedAt().getValue()));
        }
        comment.setSyncAt(Instant.now());

        commentRepository.save(comment);
    }

    private void updateExistingComment(Comment existingComment,
                                       com.google.api.services.youtube.model.CommentSnippet snippet) {
        existingComment.setText(snippet.getTextDisplay());
        existingComment.setLikeCount(snippet.getLikeCount().intValue());
        existingComment.setSyncAt(Instant.now());
        commentRepository.save(existingComment);
    }
}
