package dev.vepo.engage.channel.find;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.channel.ChannelResponse;
import dev.vepo.engage.channel.ChannelService;
import dev.vepo.engage.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/channels/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class FindChannelByIdEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(FindChannelByIdEndpoint.class);

    private final ChannelService channelService;

    @Inject
    public FindChannelByIdEndpoint(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GET
    @Path("")
    public ChannelResponse getChannel(@PathParam("id") Long id) {
        logger.info("Getting channel with id: {}", id);
        return channelService.findById(id);
    }
}
