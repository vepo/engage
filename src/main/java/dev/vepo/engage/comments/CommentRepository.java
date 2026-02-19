package dev.vepo.engage.comments;

import java.util.List;
import java.util.Optional;

import dev.vepo.engage.model.Comment;
import dev.vepo.engage.model.Video;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class CommentRepository {

    private final EntityManager entityManager;

    @Inject
    public CommentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Comment> findByVideo(Video video) {
        return this.entityManager.createQuery("FROM Comment WHERE video = :video ORDER BY publishedAt DESC", Comment.class)
                                 .setParameter("video", video)
                                 .getResultStream()
                                 .toList();
    }

    public List<Comment> findByVideoId(Long videoId) {
        return this.entityManager.createQuery("FROM Comment WHERE video.id = :videoId ORDER BY publishedAt DESC", Comment.class)
                                 .setParameter("videoId", videoId)
                                 .getResultStream()
                                 .toList();
    }

    public Optional<Comment> findByYoutubeCommentId(String youtubeCommentId) {
        return this.entityManager.createQuery("FROM Comment WHERE youtubeCommentId = :youtubeCommentId", Comment.class)
                                 .setParameter("youtubeCommentId", youtubeCommentId)
                                 .getResultStream()
                                 .limit(1)
                                 .findFirst();
    }

    public Comment save(Comment comment) {
        this.entityManager.persist(comment);
        return comment;
    }
}