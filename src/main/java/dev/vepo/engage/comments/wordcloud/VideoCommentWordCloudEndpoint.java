package dev.vepo.engage.comments.wordcloud;

import java.util.List;

import dev.vepo.engage.comments.CommentRepository;
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
@Path("/videos/{videoId}/comments/word-cloud")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class VideoCommentWordCloudEndpoint {

    private final CommentRepository commentRepository;
    private final CommentWordCloudService commentWordCloudService;
    private final VideoRepository videoRepository;

    @Inject
    public VideoCommentWordCloudEndpoint(CommentRepository commentRepository,
                                         CommentWordCloudService commentWordCloudService,
                                         VideoRepository videoRepository) {
        this.commentRepository = commentRepository;
        this.commentWordCloudService = commentWordCloudService;
        this.videoRepository = videoRepository;
    }

    @GET
    public Response wordCloudByVideo(@PathParam("videoId") Long videoId) {
        if (videoRepository.findById(videoId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<WordCloudEntry> wordCloud = commentWordCloudService.buildFromComments(commentRepository.findByVideoId(videoId));
        return Response.ok(wordCloud).build();
    }
}
