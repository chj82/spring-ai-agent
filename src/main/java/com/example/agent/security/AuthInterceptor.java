package com.example.agent.security;

import com.example.agent.common.config.AppSecurityProperties;
import com.example.agent.common.context.LoginUser;
import com.example.agent.common.context.UserContext;
import com.example.agent.common.exception.BizException;
import com.example.agent.model.entity.UserSessionEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final CookieService cookieService;
    private final UserSessionService userSessionService;
    private final AppSecurityProperties properties;

    public AuthInterceptor(
        CookieService cookieService,
        UserSessionService userSessionService,
        AppSecurityProperties properties
    ) {
        this.cookieService = cookieService;
        this.userSessionService = userSessionService;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (isPublicPath(uri)) {
            return true;
        }

        boolean adminPath = uri.startsWith("/api/admin/");
        String cookieName = adminPath ? properties.getAdminCookieName() : properties.getAppCookieName();
        String token = cookieService.readCookie(request.getCookies(), cookieName);
        if (token == null || token.isBlank()) {
            throw new BizException(401, "未登录");
        }

        UserSessionEntity session = userSessionService.requireValidToken(token);
        LoginUser loginUser = userSessionService.loadLoginUser(session);
        UserContext.set(loginUser);

        boolean shouldRenew = userSessionService.shouldRenew(session);
        userSessionService.touchSession(token, shouldRenew);
        if (shouldRenew) {
            if (adminPath) {
                cookieService.writeAdminToken(response, token);
            } else {
                cookieService.writeAppToken(response, token);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isPublicPath(String uri) {
        return "/api/admin/auth/login".equals(uri)
            || "/api/app/auth/login".equals(uri)
            || "/api/public/auth/login-public-key".equals(uri)
            || "/error".equals(uri);
    }
}
