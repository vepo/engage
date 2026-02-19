package dev.vepo.engage.model;

import java.time.Instant;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "yt_comment_id", unique = true, nullable = false)
    private String youtubeCommentId;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_channel_id")
    private String authorChannelId;

    @Column(length = 10000)
    private String text;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sync_at", nullable = false)
    private Instant syncAt;

    public Comment() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYoutubeCommentId() {
        return youtubeCommentId;
    }

    public void setYoutubeCommentId(String youtubeCommentId) {
        this.youtubeCommentId = youtubeCommentId;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorChannelId() {
        return authorChannelId;
    }

    public void setAuthorChannelId(String authorChannelId) {
        this.authorChannelId = authorChannelId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getSyncAt() {
        return syncAt;
    }

    public void setSyncAt(Instant syncAt) {
        this.syncAt = syncAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        var other = (Comment) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Comment[id=%d, youtubeCommentId=%s, authorName=%s]".formatted(id, youtubeCommentId, authorName);
    }
}