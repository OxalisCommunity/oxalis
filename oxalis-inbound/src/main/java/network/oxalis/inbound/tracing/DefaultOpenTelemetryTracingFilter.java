package network.oxalis.inbound.tracing;

import com.google.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.commons.tracing.SpanManager;
import network.oxalis.commons.util.ClosableSpan;

import java.io.IOException;

@Slf4j
public class DefaultOpenTelemetryTracingFilter implements OpenTelemetryTracingFilter {

    @Inject
    private SpanManager spanManager;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String rootSpanName = deriveSpanName(servletRequest);
        try (ClosableSpan ignore = spanManager.startClosableSpan(rootSpanName)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private String deriveSpanName(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            return String.format("%s - %s", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        } else {
            log.warn("unable to determine span name from {}, fall back to \"default\"", servletRequest.getClass());
            return "default";
        }
    }

}
