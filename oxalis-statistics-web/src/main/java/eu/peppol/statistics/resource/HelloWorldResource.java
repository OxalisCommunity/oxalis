package eu.peppol.statistics.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author steinar
 *         Date: 15.04.13
 *         Time: 16:27
 */
@Path("/hello")
public class HelloWorldResource {

    @GET
    @Produces("text/plain")
    public String getClicheMessage() {
        return "Hello world!";
    }
}
