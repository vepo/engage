package dev.vepo.engage.shared.youtube;

import java.time.Instant;

public record YoutubeVideoSnippet(String youtubeId,
                                  String title,
                                  String description,
                                  String thumbnailUrl,
                                  Instant publishedAt) {}
