package network.oxalis.commons.util;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;

public final class OpenTelemetryUtils {

    private static final String SERVICE_NAME = "oxalis";
    public static final String SERVICE_NAME_ATTRIBUTE_KEY = "service.name";

    private OpenTelemetryUtils() {
    }

    private static OpenTelemetry openTelemetry;

    public static OpenTelemetry initOpenTelemetry(SpanProcessor spanProcessor) {
        if (openTelemetry == null) {
            Resource serviceNameResource =
                    Resource.create(Attributes.of(InternalAttributeKeyImpl.create(SERVICE_NAME_ATTRIBUTE_KEY, AttributeType.STRING), SERVICE_NAME));

            SdkTracerProvider tracerProvider =
                    SdkTracerProvider.builder()
                            .addSpanProcessor(spanProcessor)
                            .setResource(Resource.getDefault().merge(serviceNameResource))
                            .build();
            openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();

            // add a shutdown hook to shut down the SDK
            Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
        }

        return openTelemetry;
    }

}
