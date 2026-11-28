package com.uco.eoi.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sends a visitor who has no signed-in officer to the login screen.
 *
 * A plain HandlerInterceptor, not Spring Security: the bank asked for the
 * session-based approach and no extra dependency.
 *
 * The check is on when the property uco.sso.auth-required is true. It is off on
 * the h2 profile so a developer can work without signing in.
 */
@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final boolean authRequired;

    public AuthConfig(@Value("${uco.sso.auth-required:true}") boolean authRequired) {
        this.authRequired = authRequired;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!authRequired) {
            return;
        }
        registry.addInterceptor(new StaffInterceptor())
                .addPathPatterns("/dashboard", "/dashboard/**", "/reports/**");
    }

    /** Redirects to the login screen when the session holds no officer. */
    static class StaffInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(AuthController.STAFF) != null) {
                return true;
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
    }
}
