package dev.vepo.engage.comments.wordcloud;

import java.util.List;

import dev.vepo.engage.comments.CommentRepository;
import dev.vepo.engage.shared.security.RequiredRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/comments/word-cloud")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class PlatformCommentWordCloudEndpoint {

    private final CommentRepository commentRepository;
    private final CommentWordCloudService commentWordCloudService;

    @Inject
    public PlatformCommentWordCloudEndpoint(CommentRepository commentRepository,
                                            CommentWordCloudService commentWordCloudService) {
        this.commentRepository = commentRepository;
        this.commentWordCloudService = commentWordCloudService;
    }

    @GET
    public Response wordCloudForAllVideos() {
        List<WordCloudEntry> wordCloud = commentWordCloudService.buildFromComments(commentRepository.findAll());
        return Response.ok(wordCloud).build();
    }
}
