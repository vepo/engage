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
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_channels")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "yt_id", length = 50)
    private String youtubeId;

    @Column(name = "next_page_token")
    private String nextPageToken;

    @Column(name = "uploads_playlist_id", length = 50)
    private String uploadsPlaylistId;

    @Column(name = "backfill_completed", nullable = false)
    private boolean backfillCompleted;

    @Column(name = "youtube_api_key")
    private String youtubeApiKey;

    @Column(name = "connected", nullable = false)
    private boolean connected;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "sync_at", nullable = false)
    private Instant syncAt;

    public Channel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getUploadsPlaylistId() {
        return uploadsPlaylistId;
    }

    public void setUploadsPlaylistId(String uploadsPlaylistId) {
        this.uploadsPlaylistId = uploadsPlaylistId;
    }

    public boolean isBackfillCompleted() {
        return backfillCompleted;
    }

    public void setBackfillCompleted(boolean backfillCompleted) {
        this.backfillCompleted = backfillCompleted;
    }

    public String getYoutubeApiKey() {
        return youtubeApiKey;
    }

    public void setYoutubeApiKey(String youtubeApiKey) {
        this.youtubeApiKey = youtubeApiKey;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isReadyForSync() {
        return connected && youtubeApiKey != null && !youtubeApiKey.isBlank();
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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
        var other = (Channel) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "Channel[id=%d, youtubeId=%s, createdAt=%s, updatedAt=%s, syncAt=%s]".formatted(id, youtubeId, createdAt, updatedAt, syncAt);
    }

}
