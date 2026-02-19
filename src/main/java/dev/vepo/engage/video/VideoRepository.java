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

    public Video save(Video video) {
        this.entityManager.persist(video);
        return video;
    }
}
