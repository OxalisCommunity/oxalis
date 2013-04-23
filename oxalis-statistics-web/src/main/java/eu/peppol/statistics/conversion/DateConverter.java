package eu.peppol.statistics.conversion;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 13:20
 */
public class DateConverter implements StringConverter<Date> {

    static Date convert(String label, String dateAsString) {

        DateTime dateTime = new DateTime(dateAsString);
        return dateTime.toDate();
    }

    public Date convert(TypeConversionRequest dateConversionRequest) throws ConversionErrorException {
        try {
            return convert(dateConversionRequest.getLabel(), dateConversionRequest.getStringValue());
        } catch (Exception e) {
            throw new ConversionErrorException(Date.class, dateConversionRequest, e);
        }
    }
}
