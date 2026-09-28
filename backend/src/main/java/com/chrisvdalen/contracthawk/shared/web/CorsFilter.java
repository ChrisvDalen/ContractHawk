package com.chrisvdalen.contracthawk.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strict same-site CORS policy for the Angular frontend.
 *
 * <p>Only exact matches against {@code contracthawk.cors.allowed-origins}
 * receive allow headers; wildcard origins are deliberately not supported.
 * In the default compose deployment the API is same-origin (nginx proxies
 * {@code /api}), so this filter mainly serves direct API access from local
 * development.
 */
@Component
public class CorsFilter extends OncePerRequestFilter {

    private static final long MAX_AGE_SECONDS = 3600;
    private static final String ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS";

    private final Set<String> allowedOrigins;

    public CorsFilter(@Value("${contracthawk.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null || !allowedOrigins.contains(origin)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(MAX_AGE_SECONDS));
        String requestedHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        if (requestedHeaders != null) {
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestedHeaders);
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
