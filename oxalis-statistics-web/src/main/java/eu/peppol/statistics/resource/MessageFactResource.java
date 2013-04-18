package eu.peppol.statistics.resource;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import eu.peppol.statistics.StatisticsRepository;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * @author steinar
 *         Date: 16.04.13
 *         Time: 09:59
 */
@Path("/messagefact")
@RequestScoped
public class MessageFactResource {

    private StatisticsRepository statisticsRepository;


    @Inject
    public MessageFactResource(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    // TODO: error handling of parameters
    // TODO: Json result
    // TODO: Inject a DataSource
    @GET
    @Path("/")
    public Response filteredFacts( @QueryParam("start") String start, @QueryParam("end") String end, @QueryParam("granularity") String s3) {

//        statisticsRepository.getDataSource();

        // Inspects the

        return Response.ok("Everything is fine - thanks for all the fish").build();
    }


}
