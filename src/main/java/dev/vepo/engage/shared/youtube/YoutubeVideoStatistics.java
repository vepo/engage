package dev.vepo.engage.shared.youtube;

public record YoutubeVideoStatistics(String youtubeId,
                                     String title,
                                     String thumbnailUrl,
                                     long viewCount,
                                     long likeCount,
                                     long commentCount) {}
