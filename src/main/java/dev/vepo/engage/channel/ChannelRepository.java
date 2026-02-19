package dev.vepo.engage.channel;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.model.Channel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

@ApplicationScoped
public class ChannelRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRepository.class);
    private final EntityManager entityManager;

    @Inject
    public ChannelRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Channel> findAll() {
        logger.info("Listing all channels from db...");
        return this.entityManager.createQuery("FROM Channel", Channel.class)
                                 .getResultStream()
                                 .toList();
    }

    public void delete(Channel channel) {
        if (this.entityManager.contains(channel)) {
            this.entityManager.remove(channel);
        } else {
            this.entityManager.remove(this.entityManager.merge(channel));
        }
        logger.info("Deleted channel: {}", channel);
    }

    public Optional<Channel> findById(Long id) {
        try {
            Channel channel = this.entityManager.createQuery("FROM Channel WHERE id = :id", Channel.class)
                                                .setParameter("id", id)
                                                .getSingleResult();
            return Optional.ofNullable(channel);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<Channel> findByYoutubeId(String youtubeId) {
        try {
            Channel channel = this.entityManager.createQuery("FROM Channel WHERE youtubeId = :youtubeId", Channel.class)
                                                .setParameter("youtubeId", youtubeId)
                                                .getSingleResult();
            return Optional.ofNullable(channel);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Channel save(Channel channel) {
        this.entityManager.persist(channel);
        return channel;
    }

    public boolean existsByYoutubeId(String youtubeId) {
        Long count = this.entityManager.createQuery("SELECT COUNT(c) FROM Channel c WHERE c.youtubeId = :youtubeId", Long.class)
                                       .setParameter("youtubeId", youtubeId)
                                       .getSingleResult();
        return count > 0;
    }
}
