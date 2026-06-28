package dev.vepo.engage.statistics;

import java.time.Instant;

public record VideoStatisticsResponse(long id,
                                      String youtubeId,
                                      String title,
                                      String thumbnailUrl,
                                      long viewCount,
                                      long likeCount,
                                      long commentCount,
                                      Instant publishedAt) {}
