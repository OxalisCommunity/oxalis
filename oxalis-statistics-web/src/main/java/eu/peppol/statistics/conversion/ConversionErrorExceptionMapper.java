package eu.peppol.statistics.conversion;

import com.google.inject.Singleton;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 13:39
 */
@Provider
@Singleton
public class ConversionErrorExceptionMapper implements ExceptionMapper<ConversionErrorException> {

    @Override
    public Response toResponse(final ConversionErrorException e) {
        TypeConversionRequest conversionRequest = e.getConversionRequest();
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid value \"" + conversionRequest.getStringValue() + "\" for argument '" + conversionRequest.getLabel() + "' ; " + e.getCause().getMessage()).build();
    }
}
