package network.oxalis.ext.testbed.v1;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author erlend
 */
@Singleton
@Slf4j
public class TestbedFilter extends HttpFilter {

    @Inject
    private TestbedSecurity security;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (req.getHeader("Authorization") == null) {
            noAccess(res);
            return;
        }

        List<String> parts = Stream.of(req.getHeader("Authorization").trim().split("\\s+", 2))
                .map(String::trim)
                .collect(Collectors.toList());

        if (parts.size() != 2 || !parts.get(0).equals("Digest")) {
            noAccess(res);
            return;
        }

        if (!parts.get(1).equals(security.getExpectedAuthorization())) {
            noAccess(res);
            return;
        }

        chain.doFilter(req, res);
    }

    private void noAccess(HttpServletResponse res) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("No access.");
    }
}
