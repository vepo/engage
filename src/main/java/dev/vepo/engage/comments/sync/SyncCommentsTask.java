package dev.vepo.engage.comments.sync;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.CommentThread;

import dev.vepo.engage.comments.CommentRepository;
import dev.vepo.engage.model.Comment;
import dev.vepo.engage.model.Video;
import dev.vepo.engage.shared.youtube.YoutubeService;
import dev.vepo.engage.video.VideoRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SyncCommentsTask {
    private static final Logger logger = LoggerFactory.getLogger(SyncCommentsTask.class);

    private final YoutubeService youtubeService;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    @Inject
    public SyncCommentsTask(YoutubeService youtubeService,
                            VideoRepository videoRepository,
                            CommentRepository commentRepository) {
        this.youtubeService = youtubeService;
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
    }

    @Scheduled(every = "1m")
    @Transactional
    void loadNewComments() {
        logger.info("Starting comments sync...");
        videoRepository.findAll()
                       .forEach(video -> this.youtubeService.loadNewCommentsForVideo(video.getYoutubeId(),
                                                                                     comment -> processCommentThread(comment, video)));
    }

    private void processCommentThread(CommentThread commentThread, Video video) {
        try {
            var snippet = commentThread.getSnippet();
            var topLevelComment = snippet.getTopLevelComment().getSnippet();
            var commentId = commentThread.getId();

            commentRepository.findByYoutubeCommentId(commentId)
                             .ifPresentOrElse(existingComment -> updateExistingComment(existingComment, topLevelComment),
                                              () -> createNewComment(commentId, video, topLevelComment));

            // Process replies if any
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
                         .ifPresentOrElse(
                                          existingComment -> updateExistingComment(existingComment, snippet),
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
        logger.debug("Created new comment: {}", commentId);
    }

    private void updateExistingComment(Comment existingComment,
                                       com.google.api.services.youtube.model.CommentSnippet snippet) {
        existingComment.setText(snippet.getTextDisplay());
        existingComment.setLikeCount(snippet.getLikeCount().intValue());
        existingComment.setSyncAt(Instant.now());

        commentRepository.save(existingComment);
        logger.debug("Updated comment: {}", existingComment.getYoutubeCommentId());
    }
}