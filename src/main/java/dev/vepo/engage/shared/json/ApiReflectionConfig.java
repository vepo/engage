package dev.vepo.engage.shared.json;

import dev.vepo.engage.channel.ChannelResponse;
import dev.vepo.engage.channel.CreateChannelRequest;
import dev.vepo.engage.channel.UpdateChannelRequest;
import dev.vepo.engage.comments.CommentResponse;
import dev.vepo.engage.shared.notification.PublishNotificationItemRequest;
import dev.vepo.engage.shared.notification.PublishNotificationRequest;
import dev.vepo.engage.statistics.ChannelStatisticsResponse;
import dev.vepo.engage.statistics.PlatformStatisticsResponse;
import dev.vepo.engage.statistics.VideoStatisticsResponse;
import dev.vepo.engage.video.VideoPageResponse;
import dev.vepo.engage.video.VideoResponse;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * REST JSON records must be registered for GraalVM native image builds;
 * otherwise Jackson cannot discover record accessors at runtime and API
 * responses fail with {@code InvalidDefinitionException}.
 */
@RegisterForReflection(targets = { ChannelResponse.class, ChannelStatisticsResponse.class, CommentResponse.class, CreateChannelRequest.class, PlatformStatisticsResponse.class, PublishNotificationItemRequest.class, PublishNotificationRequest.class, UpdateChannelRequest.class, VideoPageResponse.class, VideoResponse.class, VideoStatisticsResponse.class
})
public final class ApiReflectionConfig {
    private ApiReflectionConfig() {}
}
