package dev.vepo.engage.video.list;

import dev.vepo.engage.shared.security.RequiredRoles;
import dev.vepo.engage.video.VideoPageResponse;
import dev.vepo.engage.video.VideoRepository;
import dev.vepo.engage.video.VideoResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("videos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ListVideoEndpoint {

    private static final int MAX_PAGE_SIZE = 100;

    private final VideoRepository videoRepository;

    @Inject
    public ListVideoEndpoint(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @GET
    public VideoPageResponse list(@QueryParam("page") @DefaultValue("0") int page,
                                  @QueryParam("size") @DefaultValue("20") int size,
                                  @QueryParam("q") String search) {
        var pageIndex = Math.max(0, page);
        var pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        var total = videoRepository.count(search);
        var items = videoRepository.findPage(pageIndex, pageSize, search)
                                   .stream()
                                   .map(row -> VideoResponse.from(row.video(), row.commentCount()))
                                   .toList();
        return new VideoPageResponse(items, total, pageIndex, pageSize);
    }
}
