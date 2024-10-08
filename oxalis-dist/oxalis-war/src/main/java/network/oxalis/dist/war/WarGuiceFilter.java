package network.oxalis.dist.war;

import com.google.inject.servlet.GuiceFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
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
