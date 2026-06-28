package dev.vepo.engage.shared.youtube;

import java.util.List;

import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.SearchResult;

public record VideoSearchPage(List<SearchResult> items, String nextPageToken, boolean lastPage) {}
