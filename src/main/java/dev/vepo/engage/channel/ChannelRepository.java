package dev.vepo.engage.channel;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.model.Channel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

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

}
