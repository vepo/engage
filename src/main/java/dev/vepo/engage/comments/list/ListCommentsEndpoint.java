package dev.vepo.engage.comments.list;

import dev.vepo.engage.comments.CommentRepository;
import dev.vepo.engage.comments.CommentResponse;
import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.shared.security.RequiredRoles;
import dev.vepo.engage.video.VideoRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ListCommentsEndpoint {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final ChannelRepository channelRepository;

    @Inject
    public ListCommentsEndpoint(CommentRepository commentRepository,
                                VideoRepository videoRepository,
                                ChannelRepository channelRepository) {
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
        this.channelRepository = channelRepository;
    }

    @GET
    @Path("videos/{videoId}/comments")
    public Response listByVideo(@PathParam("videoId") Long videoId) {
        return videoRepository.findById(videoId)
                              .map(video -> Response.ok(commentRepository.findByVideo(video)
                                                                         .stream()
                                                                         .map(CommentResponse::from)
                                                                         .toList())
                                                    .build())
                              .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("channels/{channelId}/comments")
    public Response listByChannel(@PathParam("channelId") Long channelId) {
        return channelRepository.findById(channelId)
                                .map(channel -> Response.ok(commentRepository.findByChannelId(channelId)
                                                                             .stream()
                                                                             .map(CommentResponse::from)
                                                                             .toList())
                                                        .build())
                                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
