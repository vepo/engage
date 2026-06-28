package dev.vepo.engage.video.sync;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.model.SearchResult;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.model.Channel;
import dev.vepo.engage.model.Video;
import dev.vepo.engage.shared.notification.PassportNotificationPublisher;
import dev.vepo.engage.shared.notification.SyncRunReport;
import dev.vepo.engage.shared.youtube.YoutubeApiFacade;
import dev.vepo.engage.video.VideoRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SyncVideoTask {
    private static final Logger logger = LoggerFactory.getLogger(SyncVideoTask.class);

    private final YoutubeApiFacade youtubeApiFacade;
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final PassportNotificationPublisher passportNotificationPublisher;
    private final AtomicInteger channelCursor = new AtomicInteger();
    private final int videoPagesPerRun;

    @Inject
    public SyncVideoTask(YoutubeApiFacade youtubeApiFacade,
                         ChannelRepository channelRepository,
                         VideoRepository videoRepository,
                         PassportNotificationPublisher passportNotificationPublisher,
                         @ConfigProperty(name = "engage.sync.video.pages-per-run", defaultValue = "1") int videoPagesPerRun) {
        this.youtubeApiFacade = youtubeApiFacade;
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.passportNotificationPublisher = passportNotificationPublisher;
        this.videoPagesPerRun = Math.max(1, videoPagesPerRun);
    }

    @Scheduled(every = "${engage.sync.video.interval:5m}", delayed = "30s")
    @Transactional
    public void syncVideosForNextChannel() {
        List<Channel> channels = channelRepository.findConnectedReadyForSync();
        if (channels.isEmpty()) {
            logger.debug("No connected channels ready for video sync");
            return;
        }

        int index = Math.floorMod(channelCursor.getAndIncrement(), channels.size());
        var channel = channels.get(index);
        logger.info("Syncing videos for channel id={} youtubeId={}", channel.getId(), channel.getYoutubeId());
        syncOneChannel(channel);
    }

    private void syncOneChannel(Channel channel) {
        var report = new SyncRunReport("video_sync",
                                       channel.getId(),
                                       "Sincronização de vídeos",
                                       "Canal %s".formatted(channel.getYoutubeId()));
        report.putSummary("youtubeChannelId", channel.getYoutubeId());
        report.putSummary("pagesPerRun", videoPagesPerRun);

        var pageToken = channel.getNextPageToken();
        var publishedAfter = channel.getSyncAt();
        var videosProcessed = 0;

        try {
            for (int page = 0; page < videoPagesPerRun; page++) {
                var searchPage = youtubeApiFacade.fetchVideoSearchPage(channel.getYoutubeApiKey(),
                                                                       channel.getYoutubeId(),
                                                                       publishedAfter,
                                                                       pageToken,
                                                                       report);
                searchPage.items().forEach(item -> upsertVideo(item, channel));
                videosProcessed += searchPage.items().size();

                pageToken = searchPage.nextPageToken();
                if (searchPage.lastPage()) {
                    pageToken = null;
                    break;
                }
            }

            channel.setSyncAt(Instant.now());
            channel.setNextPageToken(pageToken);
            channelRepository.merge(channel);

            report.putSummary("status", "completed");
            report.putSummary("videosProcessed", videosProcessed);
            report.setDescription("Canal %s — %d vídeo(s) processado(s)".formatted(channel.getYoutubeId(), videosProcessed));
        } catch (Exception ex) {
            logger.error("Video sync failed for channel {}", channel.getYoutubeId(), ex);
            report.putSummary("status", "failed");
            report.putSummary("error", ex.getMessage());
            report.setDescription("Falha na sincronização de vídeos do canal %s".formatted(channel.getYoutubeId()));
        } finally {
            passportNotificationPublisher.publishSyncReport(report);
        }
    }

    private void upsertVideo(SearchResult video, Channel channel) {
        videoRepository.findByYoutubeId(video.getId().getVideoId())
                       .ifPresentOrElse(dbVideo -> updateVideo(dbVideo, video, channel),
                                        () -> createVideo(video, channel));
    }

    private void updateVideo(Video dbVideo, SearchResult video, Channel channel) {
        dbVideo.setChannel(channel);
        dbVideo.setDescription(video.getSnippet().getDescription());
        dbVideo.setTitle(video.getSnippet().getTitle());
        dbVideo.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
        dbVideo.setSyncAt(Instant.now());
        dbVideo.setPublishedAt(Instant.ofEpochMilli(video.getSnippet().getPublishedAt().getValue()));
        videoRepository.save(dbVideo);
    }

    private void createVideo(SearchResult video, Channel channel) {
        var dbVideo = new Video();
        dbVideo.setChannel(channel);
        dbVideo.setYoutubeId(video.getId().getVideoId());
        dbVideo.setDescription(video.getSnippet().getDescription());
        dbVideo.setTitle(video.getSnippet().getTitle());
        dbVideo.setThumbnail(video.getSnippet().getThumbnails().getHigh().getUrl());
        dbVideo.setPublishedAt(Instant.ofEpochMilli(video.getSnippet().getPublishedAt().getValue()));
        dbVideo.setSyncAt(Instant.now());
        videoRepository.save(dbVideo);
    }
}
