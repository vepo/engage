package dev.vepo.engage.video.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.model.Channel;
import dev.vepo.engage.shared.youtube.YoutubeService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SyncVideoTask {
    private static final Logger logger = LoggerFactory.getLogger(SyncVideoTask.class);
    private final YoutubeService youtubeService;
    private final ChannelRepository channelRepository;

    @Inject
    public SyncVideoTask(YoutubeService youtubeService, ChannelRepository channelRepository) {
        this.youtubeService = youtubeService;
        this.channelRepository = channelRepository;
    }

    @Scheduled(every = "10s")
    public void loadNewVideos() {
        this.channelRepository.findAll()
                              .forEach(this::loadVideoForChannel);
    }

    private void loadVideoForChannel(Channel channel) {
        logger.info("Loading videos for channel={}", channel);
        this.youtubeService.loadNewVideos(channel.getYoutubeId(), channel.getSyncAt())
                           .getItems()
                           .forEach(video -> {
                               logger.info("video={}", video);
                           });
    }

}
