package dev.vepo.engage.channel;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.model.Channel;
import dev.vepo.engage.shared.youtube.YoutubeApiFacade;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ChannelService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    private final ChannelRepository channelRepository;
    private final YoutubeApiFacade youtubeApiFacade;

    @Inject
    public ChannelService(ChannelRepository channelRepository, YoutubeApiFacade youtubeApiFacade) {
        this.channelRepository = channelRepository;
        this.youtubeApiFacade = youtubeApiFacade;
    }

    public List<ChannelResponse> listChannels() {
        return channelRepository.findAll()
                                .stream()
                                .map(ChannelResponse::from)
                                .toList();
    }

    public ChannelResponse findById(Long id) {
        return channelRepository.findById(id)
                                .map(ChannelResponse::from)
                                .orElseThrow(() -> new WebApplicationException("Channel not found with id: %d".formatted(id),
                                                                               Response.Status.NOT_FOUND));
    }

    @Transactional
    public ChannelResponse registerChannel(CreateChannelRequest request) {
        logger.info("Registering channel with youtubeId: {}", request.youtubeId());

        if (channelRepository.existsByYoutubeId(request.youtubeId())) {
            throw new WebApplicationException("Channel with YouTube ID %s already exists".formatted(request.youtubeId()),
                                              Response.Status.CONFLICT);
        }

        validateConnectionRequest(request.connected(), request.youtubeApiKey());

        var channel = new Channel();
        channel.setYoutubeId(request.youtubeId());
        channel.setYoutubeApiKey(normalizeApiKey(request.youtubeApiKey()));
        channel.setConnected(request.connected());
        channel.setSyncAt(Instant.now());

        if (channel.isReadyForSync()) {
            validateYoutubeConnection(channel.getYoutubeApiKey(), channel.getYoutubeId());
        }

        return ChannelResponse.from(channelRepository.save(channel));
    }

    @Transactional
    public ChannelResponse updateChannel(Long id, UpdateChannelRequest request) {
        logger.info("Updating channel with id: {}", id);

        var channel = channelRepository.findById(id)
                                       .orElseThrow(() -> new WebApplicationException("Channel not found with id: %d".formatted(id),
                                                                                      Response.Status.NOT_FOUND));

        if (request.youtubeId() != null) {
            if (!request.youtubeId().equals(channel.getYoutubeId()) && channelRepository.existsByYoutubeId(request.youtubeId())) {
                throw new WebApplicationException("Channel with YouTube ID %s already exists".formatted(request.youtubeId()),
                                                  Response.Status.CONFLICT);
            }
            if (!request.youtubeId().equals(channel.getYoutubeId())) {
                channel.setNextPageToken(null);
                channel.setUploadsPlaylistId(null);
            }
            channel.setYoutubeId(request.youtubeId());
        }

        var connected = request.connected() != null ? request.connected() : channel.isConnected();
        var apiKey = request.youtubeApiKey() != null ? normalizeApiKey(request.youtubeApiKey()) : channel.getYoutubeApiKey();
        validateConnectionRequest(connected, apiKey);

        if (request.youtubeApiKey() != null) {
            channel.setYoutubeApiKey(apiKey);
        }
        if (request.connected() != null) {
            channel.setConnected(request.connected());
        }

        if (channel.isReadyForSync()) {
            validateYoutubeConnection(apiKey, channel.getYoutubeId());
        }

        return ChannelResponse.from(channelRepository.merge(channel));
    }

    @Transactional
    public void deleteChannel(Long id) {
        logger.info("Deleting channel with id: {}", id);

        var channel = channelRepository.findById(id)
                                       .orElseThrow(() -> new WebApplicationException("Channel not found with id: %d".formatted(id),
                                                                                      Response.Status.NOT_FOUND));
        channelRepository.delete(channel);
    }

    private void validateConnectionRequest(boolean connected, String apiKey) {
        if (connected && (apiKey == null || apiKey.isBlank())) {
            throw new WebApplicationException("YouTube API key is required when channel is connected",
                                              Response.Status.BAD_REQUEST);
        }
    }

    private String normalizeApiKey(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        var trimmed = apiKey.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateYoutubeConnection(String apiKey, String youtubeChannelId) {
        try {
            youtubeApiFacade.validateChannelExists(apiKey, youtubeChannelId);
        } catch (IllegalStateException ex) {
            throw new WebApplicationException(ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }
}
