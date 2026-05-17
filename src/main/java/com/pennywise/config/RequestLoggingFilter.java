package com.pennywise.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    jakarta.servlet.FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {
        try {
            String path = request.getRequestURI();
            String ct = request.getContentType();
            boolean isMultipart = request instanceof MultipartHttpServletRequest;
            log.info("Incoming request {} Content-Type='{}' isMultipart={}", path, ct, isMultipart);
        } catch (Exception e) {
            log.debug("Failed to log request info: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
