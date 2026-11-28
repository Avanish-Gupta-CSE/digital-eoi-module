package com.uco.eoi.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** The CSRF token check and the browser-hardening headers. */
class SecurityFiltersTest {

    private final CsrfFilter csrf = new CsrfFilter();

    @Test
    @DisplayName("a page view gets a token in the session")
    void get_putsATokenInTheSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dashboard");
        csrf.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(request.getSession().getAttribute(CsrfFilter.TOKEN)).isNotNull();
    }

    @Test
    @DisplayName("a POST without the token is refused and never reaches the controller")
    void post_withoutToken_isRefused() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/1/approve");
        request.getSession().setAttribute(CsrfFilter.TOKEN, "right-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        csrf.doFilter(request, response, chain);

        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=expired");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("a POST with a wrong token is refused")
    void post_withWrongToken_isRefused() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/1/approve");
        request.getSession().setAttribute(CsrfFilter.TOKEN, "right-token");
        request.setParameter(CsrfFilter.TOKEN, "wrong-token");
        MockFilterChain chain = new MockFilterChain();

        csrf.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("a POST that carries the session's token goes through")
    void post_withTheRightToken_goesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/reports/1/approve");
        HttpSession session = request.getSession();
        session.setAttribute(CsrfFilter.TOKEN, "right-token");
        request.setParameter(CsrfFilter.TOKEN, "right-token");
        MockFilterChain chain = new MockFilterChain();

        csrf.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("every page carries the strict CSP; the H2 console is left alone")
    void headers_areSetOnPages_butNotOnTheH2Console() throws Exception {
        SecurityHeadersFilter headers = new SecurityHeadersFilter();

        MockHttpServletResponse page = new MockHttpServletResponse();
        headers.doFilter(new MockHttpServletRequest("GET", "/dashboard"), page, new MockFilterChain());
        assertThat(page.getHeader("Content-Security-Policy"))
                .contains("script-src 'self'").contains("frame-ancestors 'none'");
        assertThat(page.getHeader("Cache-Control")).isEqualTo("no-store");

        MockHttpServletResponse console = new MockHttpServletResponse();
        headers.doFilter(new MockHttpServletRequest("GET", "/h2-console/login"), console,
                new MockFilterChain());
        assertThat(console.getHeader("Content-Security-Policy")).isNull();
    }
}
