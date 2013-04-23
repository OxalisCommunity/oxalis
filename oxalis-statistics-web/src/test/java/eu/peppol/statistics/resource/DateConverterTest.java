package eu.peppol.statistics.resource;

import eu.peppol.statistics.conversion.ConversionErrorException;
import eu.peppol.statistics.conversion.DateConverter;
import eu.peppol.statistics.conversion.TypeConversionRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 13:22
 */
public class DateConverterTest {

    DateConverter dateConverter;

    @BeforeTest
    public void createDateConverter() {
        dateConverter = new DateConverter();
    }
    @Test
    public void convertValidDate() throws Exception, ConversionErrorException {

        Date date = dateConverter.convert(new TypeConversionRequest("start", "2013-01-01T12"));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 12, "Invalid conversion, expected hour=12");
        assertEquals(cal.get(Calendar.MINUTE), 0, "Invalid conversion, expected hour = 00");
    }

    @Test(expectedExceptions = ConversionErrorException.class)
    public void convertInvalidDate() throws ConversionErrorException {
        Date date = dateConverter.convert(new TypeConversionRequest("end", "2013-01-T"));
    }

    @Test(expectedExceptions = ConversionErrorException.class)
    public void convertInvalidHour() throws ConversionErrorException {
        Date date = dateConverter.convert(new TypeConversionRequest("xxx", "2013-01-12T25"));
    }
}
