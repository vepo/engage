package dev.vepo.engage.shared.youtube;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.json.GenericJson;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.CommentThreadReplies;
import com.google.api.services.youtube.model.CommentThreadSnippet;
import com.google.api.services.youtube.model.PageInfo;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Google API client deserializes JSON reflectively. Quarkus native image
 * requires explicit registration or calls fail with {@code GoogleJsonError} /
 * {@code GenericJson} instantiation errors even when the HTTP response is
 * valid.
 */
@RegisterForReflection(targets = { GenericJson.class, GoogleJsonError.class, GoogleJsonError.ErrorInfo.class, GoogleJsonError.Details.class, GoogleJsonError.ParameterViolations.class, GoogleJsonErrorContainer.class, ChannelListResponse.class, Channel.class, ChannelSnippet.class, ChannelStatistics.class, ThumbnailDetails.class, Thumbnail.class, PageInfo.class, SearchListResponse.class, SearchResult.class, SearchResultSnippet.class, CommentThreadListResponse.class, CommentThread.class, CommentThreadSnippet.class, CommentThreadReplies.class, Comment.class, CommentSnippet.class, VideoListResponse.class, Video.class, VideoSnippet.class, VideoStatistics.class
}, registerFullHierarchy = true)
public final class GoogleApiReflectionConfig {
    private GoogleApiReflectionConfig() {}
}
