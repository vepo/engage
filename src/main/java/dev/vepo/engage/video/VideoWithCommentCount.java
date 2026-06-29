package dev.vepo.engage.video;

import dev.vepo.engage.model.Video;

public record VideoWithCommentCount(Video video, long commentCount) {}
