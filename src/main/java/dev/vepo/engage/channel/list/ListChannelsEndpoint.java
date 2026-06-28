package dev.vepo.engage.channel.list;

import java.util.List;

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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ListChannelsEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ListChannelsEndpoint.class);

    private final ChannelService channelService;

    @Inject
    public ListChannelsEndpoint(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GET
    public List<ChannelResponse> listAllChannels() {
        logger.info("Listing all channels");
        return channelService.listChannels();
    }
}
