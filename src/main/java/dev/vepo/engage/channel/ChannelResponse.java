package dev.vepo.engage.channel;

import java.time.Instant;

import dev.vepo.engage.model.Channel;

public record ChannelResponse(Long id,
                              String youtubeId,
                              boolean connected,
                              boolean apiKeyConfigured,
                              String nextPageToken,
                              Instant createdAt,
                              Instant updatedAt,
                              Instant syncAt) {
    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(channel.getId(),
                                   channel.getYoutubeId(),
                                   channel.isConnected(),
                                   channel.getYoutubeApiKey() != null && !channel.getYoutubeApiKey().isBlank(),
                                   channel.getNextPageToken(),
                                   channel.getCreatedAt(),
                                   channel.getUpdatedAt(),
                                   channel.getSyncAt());
    }
}