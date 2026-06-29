package dev.vepo.engage.comments.wordcloud;

import java.util.List;

import dev.vepo.engage.channel.ChannelRepository;
import dev.vepo.engage.comments.CommentRepository;
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
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/channels/{channelId}/comments/word-cloud")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ChannelCommentWordCloudEndpoint {

    private final CommentRepository commentRepository;
    private final CommentWordCloudService commentWordCloudService;
    private final ChannelRepository channelRepository;

    @Inject
    public ChannelCommentWordCloudEndpoint(CommentRepository commentRepository,
                                           CommentWordCloudService commentWordCloudService,
                                           ChannelRepository channelRepository) {
        this.commentRepository = commentRepository;
        this.commentWordCloudService = commentWordCloudService;
        this.channelRepository = channelRepository;
    }

    @GET
    public Response wordCloudByChannel(@PathParam("channelId") Long channelId) {
        if (channelRepository.findById(channelId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<WordCloudEntry> wordCloud = commentWordCloudService.buildFromComments(commentRepository.findByChannelId(channelId));
        return Response.ok(wordCloud).build();
    }
}
