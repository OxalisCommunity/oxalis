package eu.peppol.statistics.conversion;

/**
 * @author steinar
 *         Date: 22.04.13
 *         Time: 13:41
 */
public class ConversionErrorException extends Throwable {

    private final Class destinationClass;
    private final TypeConversionRequest conversionRequest;

    public ConversionErrorException(Class destinationClass, TypeConversionRequest conversionRequest, Throwable cause) {
        super("Can not convert argument '" + conversionRequest.getLabel() + "' with value of " + conversionRequest.getStringValue() + " into " + destinationClass.getName() + "; " + cause.getMessage(), cause);

        this.destinationClass = destinationClass;
        this.conversionRequest = conversionRequest;
    }

    public Class getDestinationClass() {
        return destinationClass;
    }

    public TypeConversionRequest getConversionRequest() {
        return conversionRequest;
    }
}
