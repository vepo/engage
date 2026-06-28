package dev.vepo.engage.shared.youtube;

import java.util.List;

import com.google.api.services.youtube.model.CommentThread;

public record CommentThreadPage(List<CommentThread> items, String nextPageToken, boolean lastPage) {}
