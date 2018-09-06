package no.difi.oxalis.ext.testbed.v1;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.settings.Settings;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author erlend
 */
@Singleton
@Slf4j
public class TestbedFilter extends HttpFilter {

    @Inject
    private Settings<TestbedConf> settings;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req.getHeader("Authorization") == null || !req.getHeader("Authorization").trim().equals(settings.getString(TestbedConf.PASSWORD))) {
            res.setStatus(401);
            res.getWriter().write("No access.");
            return;
        }

        chain.doFilter(req, res);
    }
}
