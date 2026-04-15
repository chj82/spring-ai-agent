package com.example.agent.security;

import com.example.agent.common.config.AppSecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieService {

    private final AppSecurityProperties properties;

    public CookieService(AppSecurityProperties properties) {
        this.properties = properties;
    }

    public void writeAdminToken(HttpServletResponse response, String token) {
        writeCookie(response, properties.getAdminCookieName(), token, properties.getTokenValiditySeconds());
    }

    public void writeAppToken(HttpServletResponse response, String token) {
        writeCookie(response, properties.getAppCookieName(), token, properties.getTokenValiditySeconds());
    }

    public void clearAdminToken(HttpServletResponse response) {
        writeCookie(response, properties.getAdminCookieName(), "", 0);
    }

    public void clearAppToken(HttpServletResponse response) {
        writeCookie(response, properties.getAppCookieName(), "", 0);
    }

    public String readCookie(Cookie[] cookies, String cookieName) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(properties.isCookieSecure())
            .sameSite(properties.getCookieSameSite())
            .path("/")
            .maxAge(maxAgeSeconds);
        if (properties.getCookieDomain() != null && !properties.getCookieDomain().isBlank()) {
            builder.domain(properties.getCookieDomain());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
