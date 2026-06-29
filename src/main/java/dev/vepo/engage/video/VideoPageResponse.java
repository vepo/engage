package dev.vepo.engage.video;

import java.util.List;

public record VideoPageResponse(List<VideoResponse> items, long total, int page, int pageSize) {}
