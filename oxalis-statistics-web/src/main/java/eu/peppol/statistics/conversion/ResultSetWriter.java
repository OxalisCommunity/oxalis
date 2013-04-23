package eu.peppol.statistics.conversion;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;

/**
 * @author steinar
 *         Date: 23.04.13
 *         Time: 23:36
 */
public class ResultSetWriter implements StreamingOutput {

    public void write(ResultSet rs) {
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {

    }
}
