package dev.vepo.engage.video.sync;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchResult;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.model.Channel;
import dev.vepo.engage.model.Video;
import dev.vepo.engage.shared.youtube.YoutubeService;
import dev.vepo.engage.video.VideoRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SyncVideoTask {
    private static final Logger logger = LoggerFactory.getLogger(SyncVideoTask.class);
    private final YoutubeService youtubeService;
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private long lastSyncDone;

    @Inject
    public SyncVideoTask(YoutubeService youtubeService,
                         ChannelRepository channelRepository,
                         VideoRepository videoRepository) {
        this.youtubeService = youtubeService;
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.lastSyncDone = System.nanoTime();
    }

    @Scheduled(every = "30s")
    @Transactional
    public void loadNewVideos() {
        if ((System.nanoTime() - this.lastSyncDone) > Duration.ofSeconds(30).toNanos()) {
            logger.info("Syncing...");
            this.channelRepository.findAll()
                                  .forEach(this::loadVideoForChannel);
        }
    }

    private void loadVideoForChannel(Channel channel) {
        logger.info("Loading videos for channel={}", channel);
        var nextPageToken = this.youtubeService.loadNewVideos(channel.getYoutubeId(),
                                                              channel.getSyncAt(),
                                                              channel.getNextPageToken(),
                                                              video -> updateVideo(video, channel));
        channel.setSyncAt(Instant.now());
        channel.setNextPageToken(nextPageToken);
        this.channelRepository.save(channel);
    }

    private void updateVideo(SearchResult video, Channel channel) {
        logger.info("Trying to find: video={}", video);
        this.videoRepository.findByYoutubeId(video.getId().getVideoId())
                            .ifPresentOrElse(dbVideo -> {
                                logger.info("Updating video {}", video);
                                dbVideo.setChannel(channel);
                                dbVideo.setDescription(video.getSnippet().getDescription());
                                dbVideo.setTitle(video.getSnippet().getTitle());
                                dbVideo.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
                                dbVideo.setSyncAt(Instant.now());
                                dbVideo.setPublishedAt(Instant.ofEpochMilli(video.getSnippet().getPublishedAt().getValue()));
                                this.videoRepository.save(dbVideo);
                            }, () -> {
                                logger.info("creating video {}", video);
                                var dbVideo = new Video();
                                dbVideo.setChannel(channel);
                                dbVideo.setYoutubeId(video.getId().getVideoId());
                                dbVideo.setDescription(video.getSnippet().getDescription());
                                dbVideo.setTitle(video.getSnippet().getTitle());
                                dbVideo.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
                                dbVideo.setPublishedAt(Instant.ofEpochMilli(video.getSnippet().getPublishedAt().getValue()));
                                dbVideo.setSyncAt(Instant.now());
                                this.videoRepository.save(dbVideo);
                            });
    }

}
