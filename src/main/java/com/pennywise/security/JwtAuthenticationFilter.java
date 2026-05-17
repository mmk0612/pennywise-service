package com.pennywise.security;

import com.pennywise.auth.JwtService;
import com.pennywise.common.exception.InvalidTokenException;
import com.pennywise.common.exception.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    jwtService.validateToken(token);
                    Long userId = jwtService.getUserIdFromToken(token);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    String.valueOf(userId), null, null);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (TokenExpiredException e) {
                    log.debug("Token expired in request: {}", e.getMessage());
                    // Continue without auth, let controller handle it
                } catch (InvalidTokenException e) {
                    log.debug("Invalid token in request: {}", e.getMessage());
                    // Continue without auth, let controller handle it
                }
            }
        } catch (Exception e) {
            log.debug("Could not set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
