package network.oxalis.dist.war;

import com.google.inject.servlet.GuiceFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @author erlend
 */
@WebFilter("/*")
public class WarGuiceFilter extends GuiceFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        super.doFilter(servletRequest, servletResponse, filterChain);
    }
}
