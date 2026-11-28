package com.uco.eoi.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Puts the bank's browser-hardening headers on every response: the strict Content Security
 * Policy (no inline script, no inline style, nothing from another site), frame denial and
 * no caching of the officer's pages. Same header set as UcoRealtyStaff.
 *
 * The H2 console of the laptop profile needs inline script and frames, so it is skipped.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP =
            "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
                    + "font-src 'self' data:; connect-src 'self'; object-src 'none'; "
                    + "base-uri 'self'; form-action 'self'; frame-ancestors 'none'";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return pathOf(request).startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
        if (request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if (!pathOf(request).startsWith("/assets/")) {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
        }
        chain.doFilter(request, response);
    }

    static String pathOf(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
