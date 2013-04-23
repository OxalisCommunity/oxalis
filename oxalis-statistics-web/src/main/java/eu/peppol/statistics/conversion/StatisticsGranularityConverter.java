package eu.peppol.statistics.conversion;

import eu.peppol.statistics.StatisticsGranularity;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 14:35
 */
public class StatisticsGranularityConverter implements StringConverter<StatisticsGranularity>{
    @Override
    public StatisticsGranularity convert(TypeConversionRequest typeConversionRequest) throws ConversionErrorException {

        try {
            StatisticsGranularity statisticsGranularity = StatisticsGranularity.valueForAbbreviation(typeConversionRequest.getStringValue());
            return statisticsGranularity;
        } catch (Exception e) {
            throw new ConversionErrorException(StatisticsGranularity.class, typeConversionRequest, e);
        }
    }
}
