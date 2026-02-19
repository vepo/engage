package dev.vepo.engage.comments.list;

import dev.vepo.engage.comments.CommentRepository;
import dev.vepo.engage.comments.CommentResponse;
import dev.vepo.engage.video.VideoRepository;
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
@Path("videos/{videoId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListCommentsEndpoint {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;

    @Inject
    public ListCommentsEndpoint(CommentRepository commentRepository, VideoRepository videoRepository) {
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
    }

    @GET
    public Response list(@PathParam("videoId") Long videoId) {
        return videoRepository.findById(videoId)
                              .map(video -> Response.ok(commentRepository.findByVideo(video)
                                                                         .stream()
                                                                         .map(CommentResponse::from)
                                                                         .toList())
                                                    .build())
                              .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}