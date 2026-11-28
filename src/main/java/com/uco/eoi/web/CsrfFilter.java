package com.uco.eoi.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cross-site request forgery protection without Spring Security.
 *
 * Every session gets one random token, kept in the session as "_csrf". Every form carries it
 * as a hidden field of the same name. A POST without the matching token is refused, so
 * another website cannot make a signed-in officer submit, approve or delete anything.
 *
 * A stale form (the session timed out) is sent to the sign-in screen instead of an error page.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CsrfFilter extends OncePerRequestFilter {

    /** The session attribute and the form field that carry the token. */
    public static final String TOKEN = "_csrf";

    private static final Logger log = LoggerFactory.getLogger(CsrfFilter.class);
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = SecurityHeadersFilter.pathOf(request);
        return path.startsWith("/assets/") || path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (SAFE_METHODS.contains(request.getMethod())) {
            HttpSession session = request.getSession(true);
            if (session.getAttribute(TOKEN) == null) {
                session.setAttribute(TOKEN, newToken());
            }
            chain.doFilter(request, response);
            return;
        }
        if (!tokenMatches(request)) {
            log.warn("[EOI SECURITY] {} {} refused: missing or wrong form token", request.getMethod(),
                    request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/login?error=expired");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean tokenMatches(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object expected = session == null ? null : session.getAttribute(TOKEN);
        String supplied = request.getParameter(TOKEN);
        if (expected == null || supplied == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.toString().getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
