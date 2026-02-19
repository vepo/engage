package dev.vepo.engage.channel.find;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.channel.ChannelResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/channels/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FindChannelByIdEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(FindChannelByIdEndpoint.class);

    private final ChannelRepository channelRepository;

    @Inject
    public FindChannelByIdEndpoint(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @GET
    @Path("")
    public ChannelResponse getChannel(@PathParam("id") Long id) {
        logger.info("Getting channel with id: {}", id);
        return channelRepository.findById(id)
                                .map(ChannelResponse::from)
                                .orElseThrow(() -> new NotFoundException("Channel not found with id: %d".formatted( id)));
    }
}
