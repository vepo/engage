package dev.vepo.engage.video;

import java.time.Instant;

import dev.vepo.engage.model.Video;

public record VideoResponse(long id,
                            String youtubeId,
                            String title,
                            String description,
                            String thumbnail,
                            Instant publishedAt,
                            long commentCount) {
    public static VideoResponse from(Video video) {
        return from(video, 0L);
    }

    public static VideoResponse from(Video video, long commentCount) {
        return new VideoResponse(video.getId(),
                                 video.getYoutubeId(),
                                 video.getTitle(),
                                 video.getDescription(),
                                 video.getThumbnail(),
                                 video.getPublishedAt(),
                                 commentCount);
    }
}
