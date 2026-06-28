package dev.vepo.engage.statistics.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.vepo.engage.shared.security.RequiredRoles;
import dev.vepo.engage.statistics.PlatformStatisticsResponse;
import dev.vepo.engage.statistics.StatisticsService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("/statistics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RequiredRoles.ENGAGE_ADMIN)
public class ListStatisticsEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ListStatisticsEndpoint.class);

    private final StatisticsService statisticsService;

    @Inject
    public ListStatisticsEndpoint(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GET
    public PlatformStatisticsResponse listPlatformStatistics() {
        logger.info("Loading YouTube platform statistics");
        return statisticsService.loadPlatformStatistics();
    }
}
