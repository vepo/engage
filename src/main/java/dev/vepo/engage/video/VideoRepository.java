package dev.vepo.engage.video;

import java.util.List;
import java.util.Optional;

import dev.vepo.engage.model.Video;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

@ApplicationScoped
public class VideoRepository {

    private final EntityManager entityManager;

    @Inject
    public VideoRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Video> findAll() {
        return this.entityManager.createQuery("FROM Video v ORDER BY v.publishedAt DESC NULLS LAST, v.id DESC", Video.class)
                                 .getResultStream()
                                 .toList();
    }

    public long count(String search) {
        var hasSearch = hasSearch(search);
        var jpql = "SELECT COUNT(v) FROM Video v" + (hasSearch ? " WHERE LOWER(v.title) LIKE :pattern OR LOWER(v.youtubeId) LIKE :pattern" : "");
        var query = this.entityManager.createQuery(jpql, Long.class);
        if (hasSearch) {
            query.setParameter("pattern", searchPattern(search));
        }
        return query.getSingleResult();
    }

    public List<VideoWithCommentCount> findPage(int page, int pageSize, String search) {
        var hasSearch = hasSearch(search);
        var jpql = """
                   SELECT v, (SELECT COUNT(c) FROM Comment c WHERE c.video.id = v.id)
                   FROM Video v
                   """ + (hasSearch ? " WHERE LOWER(v.title) LIKE :pattern OR LOWER(v.youtubeId) LIKE :pattern" : "")
                + " ORDER BY v.publishedAt DESC NULLS LAST, v.id DESC";
        var query = this.entityManager.createQuery(jpql, Object[].class);
        if (hasSearch) {
            query.setParameter("pattern", searchPattern(search));
        }
        return query.setFirstResult(page * pageSize)
                    .setMaxResults(pageSize)
                    .getResultStream()
                    .map(row -> new VideoWithCommentCount((Video) row[0], (Long) row[1]))
                    .toList();
    }

    private boolean hasSearch(String search) {
        return search != null && !search.isBlank();
    }

    private String searchPattern(String search) {
        return "%" + search.trim().toLowerCase() + "%";
    }

    public Optional<Video> findByYoutubeId(String youtubeId) {
        return this.entityManager.createQuery("FROM Video WHERE youtubeId = :youtubeId", Video.class)
                                 .setParameter("youtubeId", youtubeId)
                                 .getResultStream()
                                 .limit(1)
                                 .findFirst();
    }

    public Optional<Video> findById(Long id) {
        try {
            return Optional.of(this.entityManager.createQuery("FROM Video WHERE id = :id", Video.class)
                                                 .setParameter("id", id)
                                                 .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
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
