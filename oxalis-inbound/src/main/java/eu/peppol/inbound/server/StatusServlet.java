package eu.peppol.inbound.server;

import com.google.inject.Singleton;
import eu.peppol.security.KeystoreManager;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisVersion;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Servlet returning diagnostic information to ease operation, support and debugging.
 * Since this servlet is public accessible, it should NOT contain any sensitive
 * information about it's runtime environment.
 * @author ebe
 * @author thore
 */
@Singleton
public class StatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        GlobalConfiguration globalConfiguration = GlobalConfiguration.getInstance();
        X509Certificate ourCertificate = KeystoreManager.getInstance().getOurCertificate();

        resp.setContentType("text/plain");

        PrintWriter writer = resp.getWriter();
        writer.println("version.oxalis: " + OxalisVersion.getVersion());
        writer.println("version.java: " + System.getProperty("java.version"));
        writer.println(GlobalConfiguration.PropertyDef.OPERATION_MODE.getPropertyName() + ": " + globalConfiguration.getModeOfOperation());
        writer.println(GlobalConfiguration.PropertyDef.PKI_VERSION.getPropertyName() + ": " + globalConfiguration.getPkiVersion());
        writer.println(GlobalConfiguration.PropertyDef.SML_HOSTNAME.getPropertyName() + ": " + globalConfiguration.getSmlHostname());
        writer.println("certificate.subject: " + ourCertificate.getSubjectX500Principal().getName());
        writer.println("certificate.issuer: " + ourCertificate.getIssuerX500Principal().getName());
        writer.println("certificate.expired: " + ourCertificate.getNotAfter().before(new Date()));
        writer.println("build.id: " + OxalisVersion.getBuildId());
        writer.println("build.tstamp: " + OxalisVersion.getBuildTimeStamp());

        // TODO add flag to indicate if OXALIS_HOME is specified or if default is used
        // TODO add info about Metro version installed

    }
}
