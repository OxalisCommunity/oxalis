package eu.peppol.inbound.server;

import com.google.inject.Singleton;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().println("<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "        <title>Oxalis PEPPOL server</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Welcome to the Oxalis PEPPOL server</h1>\n" +
                "        <p>The protocols for this Access Point are :</p>\n" +
                "        <ul>\n" +
                "            <li>The AS2 endpoint can be found <a href=\"as2\">here</a>.</li>\n" +
                "        </ul>\n" +
                "        <p>Some status information can be found at <a href=\"status\">status</a>.</p>\n" +
                "    </body>\n" +
                "</html>\n");
    }
}
