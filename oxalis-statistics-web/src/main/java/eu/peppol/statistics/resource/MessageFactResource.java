package eu.peppol.statistics.resource;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.core.header.ContentDisposition;
import eu.peppol.statistics.ResultSetWriter;
import eu.peppol.statistics.AggregatedStatistics;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.conversion.ConversionErrorException;
import eu.peppol.statistics.conversion.DateConverter;
import eu.peppol.statistics.conversion.StatisticsGranularityConverter;
import eu.peppol.statistics.conversion.TypeConversionRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

/**
 * @author steinar
 *         Date: 16.04.13
 *         Time: 09:59
 */
@Path("/messagefact")
@RequestScoped
public class MessageFactResource {

    private StatisticsRepository statisticsRepository;
    private final DateConverter dateConverter;
    private final StatisticsGranularityConverter statisticsGranularityConverter;


    @Inject
    public MessageFactResource(StatisticsRepository statisticsRepository, DateConverter dateConverter, StatisticsGranularityConverter statisticsGranularityConverter) {
        this.statisticsRepository = statisticsRepository;
        this.dateConverter = dateConverter;
        this.statisticsGranularityConverter = statisticsGranularityConverter;
    }

    /**
     * Performs a select in the repository and returns a CSV formatted list of data
     * @param start date (inclusive) from which to retrieve data
     * @param end date (inclusive) until which data should be retrieved
     * @param granularity granularity of Year, Month, Day or Hour.
     * @return HTTP response with Content-Disposition
     * @throws ConversionErrorException if the input parameters can not be converted.
     */
    @GET
    @Path("/")
    @Produces("text/csv")
    public Response filteredFacts(@QueryParam("start") String start, @QueryParam("end") String end, @QueryParam("granularity") String granularity) throws ConversionErrorException {

        // Parses the input parameters
        Date startDate = dateConverter.convert(new TypeConversionRequest("start", start));
        Date endDate = dateConverter.convert(new TypeConversionRequest("end", end));
        StatisticsGranularity statisticsGranularity = statisticsGranularityConverter.convert(new TypeConversionRequest("granularity", granularity));

        return createResponse(startDate, endDate, statisticsGranularity);
    }


    private Response createResponse(Date startDate, Date endDate, StatisticsGranularity statisticsGranularity) {
        // Establishes an object which will act as a bridge between the JAX-RS StreamingOutput, the CSVWriter and the SQL ResultSet
        ResultSetToCsvStreamer resultSetToCsvStreamer = new ResultSetToCsvStreamer(statisticsRepository, startDate, endDate, statisticsGranularity);
        // Indicates how the HTTP client should dispose of the results
        ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName("oxalis.csv").creationDate(new Date()).build();

        // This will call our bridge object and perform all the magic
        return Response.ok(resultSetToCsvStreamer).header("Content-Disposition", contentDisposition).build();
    }


    /**
     * Magic bridge object which will be invoked by JAX-RS runtime in order to stream the results back to the client.
     * Upon construction, all required parameters are stored internally in order to be used once the runtime
     * calls our #write(OutputStream) method, which in turn will invoke our repository, which will call back
     * to the #writeAll method.
     */
    static class ResultSetToCsvStreamer implements StreamingOutput, ResultSetWriter {

        private final StatisticsRepository repository;
        private final Date startDate;
        private final Date endDate;
        private final StatisticsGranularity statisticsGranularity;
        private OutputStream outputStream;

        ResultSetToCsvStreamer(StatisticsRepository repository, Date startDate, Date endDate, StatisticsGranularity statisticsGranularity) {

            this.repository = repository;
            this.startDate = startDate;
            this.endDate = endDate;
            this.statisticsGranularity = statisticsGranularity;
        }

        /** Entry method, which will cause the writeAll method (below) to be invoked */
        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            this.outputStream = outputStream;
            repository.selectAggregatedStatistics(this, startDate, endDate, statisticsGranularity);
        }

        /**
         * Call back method invoked by our repository to write the entire ResultSet into the output stream being returned to
         * the client.
         * @param resultSet the JDBC results from the SQL Query
         */
        @Override
        public void writeAll(ResultSet resultSet) {
            // Establishes our CSVWriter
            CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new OutputStreamWriter(outputStream,Charset.forName("UTF-8"))));
            try {
                // Dumps the ResultSet
                csvWriter.writeAll(resultSet, true);
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to fetch and write contents to CSV file " + e.getMessage(), e);
            } catch (IOException e) {
                throw new IllegalStateException("While writing CSV file into stream: " + e.getMessage(), e);
            } finally {
                try {
                    // For gods sake, remember to close the output stream
                    csvWriter.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close outputstream " + e.getMessage(), e);
                }
            }
        }
    }

}
