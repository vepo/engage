package dev.vepo.engage.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ChannelResource {
    private static final Logger logger = LoggerFactory.getLogger(ChannelResource.class);

    private final ChannelService channelService;
    private final ChannelRepository channelRepository;

    @Inject
    public ChannelResource(ChannelService channelService, ChannelRepository channelRepository) {
        this.channelService = channelService;
        this.channelRepository = channelRepository;
    }

    @PUT
    @Path("/{id}")
    public Response updateChannel(@PathParam("id") Long id, @Valid UpdateChannelRequest request) {
        logger.info("Updating channel with id: {}", id);
        return Response.ok(channelService.updateChannel(id, request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteChannel(@PathParam("id") Long id) {
        logger.info("Deleting channel with id: {}", id);
        channelService.deleteChannel(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/youtube/{youtubeId}")
    public Response getChannelByYoutubeId(@PathParam("youtubeId") String youtubeId) {
        logger.info("Getting channel with youtubeId: {}", youtubeId);
        return channelRepository.findByYoutubeId(youtubeId)
                                .map(channel -> Response.ok(ChannelResponse.from(channel)).build())
                                .orElse(Response.status(Response.Status.NOT_FOUND)
                                                .entity("Channel not found with YouTube ID: %s".formatted(youtubeId))
                                                .build());
    }
}
