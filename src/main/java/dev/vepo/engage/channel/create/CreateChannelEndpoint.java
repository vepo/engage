package dev.vepo.engage.channel.create;

import java.net.URI;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.channel.ChannelResponse;
import dev.vepo.engage.channel.CreateChannelRequest;
import dev.vepo.engage.model.Channel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreateChannelEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(CreateChannelEndpoint.class);

    private final ChannelRepository channelRepository;
    private final UriInfo uriInfo;

    @Inject
    public CreateChannelEndpoint(ChannelRepository channelRepository, @Context UriInfo uriInfo) {
        this.channelRepository = channelRepository;
        this.uriInfo = uriInfo;
    }

    @POST
    @Transactional
    public Response createChannel(@Valid CreateChannelRequest request) {
        logger.info("Creating new channel with youtubeId: {}", request.youtubeId());

        // Check if channel already exists
        if (channelRepository.existsByYoutubeId(request.youtubeId())) {
            throw new WebApplicationException("Channel with YouTube ID %s already exists".formatted(request.youtubeId()), Response.Status.CONFLICT);
        }

        // Validate YouTube channel exists (optional - would require YouTube API call)
        // This could be implemented with a validation service

        var channel = new Channel();
        channel.setYoutubeId(request.youtubeId());
        channel.setSyncAt(Instant.now());
        Channel savedChannel = channelRepository.save(channel);

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(savedChannel.getId().toString())
                              .build();

        return Response.created(location)
                       .entity(ChannelResponse.from(savedChannel))
                       .build();
    }
}
