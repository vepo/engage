package dev.vepo.engage.video;

import java.time.Instant;

import dev.vepo.engage.model.Video;

public record VideoResponse(long id, String youtubeId, String title, String description, String thumbnail, Instant publishedAt) {
    public static VideoResponse from(Video video) {
        return new VideoResponse(video.getId(),
                                 video.getYoutubeId(),
                                 video.getTitle(),
                                 video.getDescription(),
                                 video.getThumbnail(),
                                 video.getPublishedAt());
    }
}
