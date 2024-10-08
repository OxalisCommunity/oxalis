package network.oxalis.commons.mode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import network.oxalis.vefa.peppol.common.code.Service;
import network.oxalis.vefa.peppol.security.api.CertificateValidator;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;

import java.security.cert.X509Certificate;

/**
 * @author erlend
 */
@Singleton
public class OxalisCertificateValidator implements CertificateValidator {

    private CertificateValidator certificateValidator;

    private Tracer tracer;

    @Inject
    public OxalisCertificateValidator(CertificateValidator certificateValidator, Tracer tracer) {
        this.certificateValidator = certificateValidator;
        this.tracer = tracer;
    }

    @Override
    public void validate(Service service, X509Certificate certificate) throws PeppolSecurityException {
        Span span = tracer.spanBuilder("Validate certificate").startSpan();
        try {
            span.setAttribute("subject", certificate.getSubjectX500Principal().toString());
            span.setAttribute("issuer", certificate.getIssuerX500Principal().toString());

            this.certificateValidator.validate(service, certificate);
        } finally {
            span.end();
        }
    }

}
