package dev.vepo.engage.video.list;

import java.util.List;

import dev.vepo.engage.video.VideoRepository;
import dev.vepo.engage.video.VideoResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("videos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListVideoEndpoint {

    private final VideoRepository videoRepository;

    @Inject
    public ListVideoEndpoint(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @GET
    public List<VideoResponse> list() {
        // Check for existing role with same name
        return this.videoRepository.findAll()
                                   .stream()
                                   .map(VideoResponse::from)
                                   .toList();
    }
}
