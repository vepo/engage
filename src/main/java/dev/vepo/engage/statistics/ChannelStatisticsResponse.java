package dev.vepo.engage.statistics;

import java.time.Instant;

public record ChannelStatisticsResponse(long id,
                                        String youtubeId,
                                        String title,
                                        String thumbnailUrl,
                                        long subscriberCount,
                                        long viewCount,
                                        long videoCount,
                                        boolean hiddenSubscriberCount,
                                        Instant lastSyncAt) {}
