package dev.vepo.engage.comments;

import java.time.Instant;

import dev.vepo.engage.model.Comment;

public record CommentResponse(long id,
                              String youtubeCommentId,
                              Long videoId,
                              String authorName,
                              String authorChannelId,
                              String text,
                              Integer likeCount,
                              Instant publishedAt,
                              Instant updatedAt,
                              Instant syncAt) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(),
                                   comment.getYoutubeCommentId(),
                                   comment.getVideo().getId(),
                                   comment.getAuthorName(),
                                   comment.getAuthorChannelId(),
                                   comment.getText(),
                                   comment.getLikeCount(),
                                   comment.getPublishedAt(),
                                   comment.getUpdatedAt(),
                                   comment.getSyncAt());
    }
}