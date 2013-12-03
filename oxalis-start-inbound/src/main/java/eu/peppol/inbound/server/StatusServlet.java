package eu.peppol.inbound.server;

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
 * Created with IntelliJ IDEA.
 * User: ebe
 * Date: 03.12.13
 * Time: 19:11
 * To change this template use File | Settings | File Templates.
 */
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
        writer.println("certificate.expired: " + ourCertificate.getNotAfter().after(new Date()));

    }
}
