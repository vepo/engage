package dev.vepo.engage.statistics;

import java.time.Instant;
import java.util.List;

public record PlatformStatisticsResponse(List<ChannelStatisticsResponse> channels,
                                         List<VideoStatisticsResponse> videos,
                                         Instant fetchedAt) {}
