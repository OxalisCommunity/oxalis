package network.oxalis.commons.mode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
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
        Span span = tracer.buildSpan("Validate certificate").start();
        perform(service, certificate, span);
    }

    public void validate(Service service, X509Certificate certificate, Span root) throws PeppolSecurityException {
        Span span = tracer.buildSpan("Validate certificate").asChildOf(root).start();
        perform(service, certificate, span);
    }

    private void perform(Service service, X509Certificate certificate, Span span) throws PeppolSecurityException {
        DefaultSpanManager.getInstance().activate(span);

        try {
            span.setTag("subject", certificate.getSubjectX500Principal().toString());
            span.setTag("issuer", certificate.getIssuerX500Principal().toString());

            this.certificateValidator.validate(service, certificate);
        } finally {
            span.finish();
        }
    }
}
