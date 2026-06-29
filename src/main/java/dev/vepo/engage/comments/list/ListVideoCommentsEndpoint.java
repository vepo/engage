package dev.vepo.engage.comments.list;

import dev.vepo.engage.comments.CommentRepository;
import dev.vepo.engage.comments.CommentResponse;
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
@Path("/videos/{videoId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ListVideoCommentsEndpoint {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;

    @Inject
    public ListVideoCommentsEndpoint(CommentRepository commentRepository, VideoRepository videoRepository) {
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
    }

    @GET
    public Response listByVideo(@PathParam("videoId") Long videoId) {
        if (videoRepository.findById(videoId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(commentRepository.findByVideoId(videoId)
                                            .stream()
                                            .map(CommentResponse::from)
                                            .toList())
                       .build();
    }
}
