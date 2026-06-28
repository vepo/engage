package dev.vepo.engage.statistics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.model.Channel;
import dev.vepo.engage.model.Video;
import dev.vepo.engage.shared.youtube.YoutubeApiFacade;
import dev.vepo.engage.shared.youtube.YoutubeVideoStatistics;
import dev.vepo.engage.video.VideoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatisticsService {
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    private final YoutubeApiFacade youtubeApiFacade;

    @Inject
    public StatisticsService(ChannelRepository channelRepository,
                             VideoRepository videoRepository,
                             YoutubeApiFacade youtubeApiFacade) {
        this.channelRepository = channelRepository;
        this.videoRepository = videoRepository;
        this.youtubeApiFacade = youtubeApiFacade;
    }

    public PlatformStatisticsResponse loadPlatformStatistics() {
        var channels = channelRepository.findAll()
                                        .stream()
                                        .map(this::loadChannelStatistics)
                                        .toList();
        var videos = loadVideoStatistics(videoRepository.findAll());
        return new PlatformStatisticsResponse(channels, videos, Instant.now());
    }

    private ChannelStatisticsResponse loadChannelStatistics(Channel channel) {
        if (!channel.isReadyForSync()) {
            return new ChannelStatisticsResponse(channel.getId(),
                                                 channel.getYoutubeId(),
                                                 channel.getYoutubeId(),
                                                 null,
                                                 0L,
                                                 0L,
                                                 0L,
                                                 false,
                                                 channel.getSyncAt());
        }

        var statistics = youtubeApiFacade.fetchChannelStatistics(channel.getYoutubeApiKey(), channel.getYoutubeId());
        return new ChannelStatisticsResponse(channel.getId(),
                                             channel.getYoutubeId(),
                                             statistics.title(),
                                             statistics.thumbnailUrl(),
                                             statistics.subscriberCount(),
                                             statistics.viewCount(),
                                             statistics.videoCount(),
                                             statistics.hiddenSubscriberCount(),
                                             channel.getSyncAt());
    }

    private List<VideoStatisticsResponse> loadVideoStatistics(List<Video> videos) {
        if (videos.isEmpty()) {
            return List.of();
        }

        var videosByChannel = videos.stream().collect(Collectors.groupingBy(video -> video.getChannel().getId()));
        var results = new ArrayList<VideoStatisticsResponse>();

        for (var entry : videosByChannel.entrySet()) {
            var channelVideos = entry.getValue();
            var channel = channelVideos.getFirst().getChannel();
            if (!channel.isReadyForSync()) {
                channelVideos.forEach(video -> results.add(toVideoStatistics(video, null)));
                continue;
            }

            var youtubeIds = channelVideos.stream().map(Video::getYoutubeId).toList();
            Map<String, YoutubeVideoStatistics> statisticsByYoutubeId = youtubeApiFacade.fetchVideoStatistics(channel.getYoutubeApiKey(),
                                                                                                              youtubeIds)
                                                                                        .stream()
                                                                                        .collect(Collectors.toMap(YoutubeVideoStatistics::youtubeId,
                                                                                                                  Function.identity()));

            channelVideos.stream()
                         .map(video -> toVideoStatistics(video, statisticsByYoutubeId.get(video.getYoutubeId())))
                         .forEach(results::add);
        }

        return results;
    }

    private VideoStatisticsResponse toVideoStatistics(Video video, YoutubeVideoStatistics statistics) {
        if (statistics == null) {
            return new VideoStatisticsResponse(video.getId(),
                                               video.getYoutubeId(),
                                               video.getTitle(),
                                               video.getThumbnail(),
                                               0L,
                                               0L,
                                               0L,
                                               video.getPublishedAt());
        }

        return new VideoStatisticsResponse(video.getId(),
                                           video.getYoutubeId(),
                                           statistics.title(),
                                           statistics.thumbnailUrl(),
                                           statistics.viewCount(),
                                           statistics.likeCount(),
                                           statistics.commentCount(),
                                           video.getPublishedAt());
    }
}
