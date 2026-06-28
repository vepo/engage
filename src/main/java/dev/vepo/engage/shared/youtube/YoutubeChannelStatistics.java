package dev.vepo.engage.shared.youtube;

public record YoutubeChannelStatistics(String title,
                                       String thumbnailUrl,
                                       long subscriberCount,
                                       long viewCount,
                                       long videoCount,
                                       boolean hiddenSubscriberCount) {}
