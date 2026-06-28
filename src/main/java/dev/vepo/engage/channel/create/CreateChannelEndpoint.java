package dev.vepo.engage.channel.create;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.channel.ChannelResponse;
import dev.vepo.engage.channel.ChannelService;
import dev.vepo.engage.channel.CreateChannelRequest;
import dev.vepo.engage.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class CreateChannelEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(CreateChannelEndpoint.class);

    private final ChannelService channelService;
    private final UriInfo uriInfo;

    @Inject
    public CreateChannelEndpoint(ChannelService channelService, @Context UriInfo uriInfo) {
        this.channelService = channelService;
        this.uriInfo = uriInfo;
    }

    @POST
    public Response createChannel(@Valid CreateChannelRequest request) {
        logger.info("Creating new channel with youtubeId: {}", request.youtubeId());

        var savedChannel = channelService.registerChannel(request);

        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(savedChannel.id().toString())
                              .build();

        return Response.created(location).entity(savedChannel).build();
    }
}
