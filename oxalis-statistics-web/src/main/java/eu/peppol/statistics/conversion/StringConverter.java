package eu.peppol.statistics.conversion;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 13:57
 */
public interface StringConverter<T> {

    T convert(TypeConversionRequest typeConversionRequest) throws ConversionErrorException;
}
