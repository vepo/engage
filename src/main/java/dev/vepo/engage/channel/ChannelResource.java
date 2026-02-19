package dev.vepo.engage.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.model.Channel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@ApplicationScoped
@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChannelResource {
    private static final Logger logger = LoggerFactory.getLogger(ChannelResource.class);

    private final ChannelRepository channelRepository;

    @Context
    UriInfo uriInfo;

    @Inject
    public ChannelResource(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateChannel(@PathParam("id") Long id, @Valid UpdateChannelRequest request) {
        logger.info("Updating channel with id: {}", id);

        return channelRepository.findById(id)
                                .map(channel -> {
                                    // Check if new youtubeId is already used by another channel
                                    if (request.youtubeId() != null &&
                                            !request.youtubeId().equals(channel.getYoutubeId()) &&
                                            channelRepository.existsByYoutubeId(request.youtubeId())) {
                                        return Response.status(Response.Status.CONFLICT)
                                                       .entity("Channel with YouTube ID " + request.youtubeId() + " already exists")
                                                       .build();
                                    }

                                    // Update fields if provided
                                    if (request.youtubeId() != null) {
                                        channel.setYoutubeId(request.youtubeId());
                                    }

                                    Channel updatedChannel = channelRepository.save(channel);
                                    return Response.ok(ChannelResponse.from(updatedChannel)).build();
                                })
                                .orElse(Response.status(Response.Status.NOT_FOUND)
                                                .entity("Channel not found with id: " + id)
                                                .build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteChannel(@PathParam("id") Long id) {
        logger.info("Deleting channel with id: {}", id);

        return channelRepository.findById(id)
                                .map(channel -> {
                                    // Check if channel has associated videos
                                    // This would require a video repository to check
                                    // For now, we'll just delete it
                                    channelRepository.delete(channel);
                                    return Response.noContent().build();
                                })
                                .orElse(Response.status(Response.Status.NOT_FOUND)
                                                .entity("Channel not found with id: " + id)
                                                .build());
    }

    @GET
    @Path("/youtube/{youtubeId}")
    public Response getChannelByYoutubeId(@PathParam("youtubeId") String youtubeId) {
        logger.info("Getting channel with youtubeId: {}", youtubeId);
        return channelRepository.findByYoutubeId(youtubeId)
                                .map(channel -> Response.ok(ChannelResponse.from(channel)).build())
                                .orElse(Response.status(Response.Status.NOT_FOUND)
                                                .entity("Channel not found with YouTube ID: " + youtubeId)
                                                .build());
    }
}