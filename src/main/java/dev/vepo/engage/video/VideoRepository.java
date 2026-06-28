package dev.vepo.engage.video;

import java.util.List;
import java.util.Optional;

import dev.vepo.engage.model.Video;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class VideoRepository {

    private final EntityManager entityManager;

    @Inject
    public VideoRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Video> findAll() {
        return this.entityManager.createQuery("FROM Video", Video.class)
                                 .getResultStream()
                                 .toList();
    }

    public Optional<Video> findByYoutubeId(String youtubeId) {
        return this.entityManager.createQuery("FROM Video WHERE youtubeId = :youtubeId", Video.class)
                                 .setParameter("youtubeId", youtubeId)
                                 .getResultStream()
                                 .limit(1)
                                 .findFirst();
    }

    public Optional<Video> findById(long id) {
        return this.entityManager.createQuery("FROM Video WHERE id = :id", Video.class)
                                 .setParameter("id", id)
                                 .getResultStream()
                                 .limit(1)
                                 .findFirst();
    }

    public List<Video> findDueForCommentSync(int limit) {
        return this.entityManager.createQuery("""
                                              FROM Video v
                                              JOIN v.channel c
                                              WHERE c.connected = true
                                                AND c.youtubeApiKey IS NOT NULL
                                              ORDER BY v.commentsSyncAt ASC NULLS FIRST, v.id ASC
                                              """, Video.class)
                                 .setMaxResults(limit)
                                 .getResultStream()
                                 .toList();
    }

    public List<Video> findByChannelId(Long channelId) {
        return this.entityManager.createQuery("FROM Video v WHERE v.channel.id = :channelId ORDER BY v.publishedAt DESC", Video.class)
                                 .setParameter("channelId", channelId)
                                 .getResultStream()
                                 .toList();
    }

    public Video save(Video video) {
        if (video.getId() == null) {
            this.entityManager.persist(video);
            return video;
        }
        return this.entityManager.merge(video);
    }
}
