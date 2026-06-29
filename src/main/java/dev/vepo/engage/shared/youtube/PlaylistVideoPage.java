package dev.vepo.engage.shared.youtube;

import java.util.List;

public record PlaylistVideoPage(List<YoutubeVideoSnippet> items, String nextPageToken, boolean lastPage) {}
