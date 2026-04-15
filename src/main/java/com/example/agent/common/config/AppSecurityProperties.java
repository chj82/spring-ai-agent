package com.example.agent.common.config;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private long tokenValiditySeconds;
    private long renewThresholdSeconds;
    private String adminCookieName;
    private String appCookieName;
    private boolean cookieSecure;
    private String cookieDomain;
    private String cookieSameSite;
    private long corsMaxAgeSeconds;
    private List<String> allowedOrigins = new ArrayList<>();

    public long getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(long tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    public long getRenewThresholdSeconds() {
        return renewThresholdSeconds;
    }

    public void setRenewThresholdSeconds(long renewThresholdSeconds) {
        this.renewThresholdSeconds = renewThresholdSeconds;
    }

    public String getAdminCookieName() {
        return adminCookieName;
    }

    public void setAdminCookieName(String adminCookieName) {
        this.adminCookieName = adminCookieName;
    }

    public String getAppCookieName() {
        return appCookieName;
    }

    public void setAppCookieName(String appCookieName) {
        this.appCookieName = appCookieName;
    }

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public String getCookieSameSite() {
        return cookieSameSite;
    }

    public void setCookieSameSite(String cookieSameSite) {
        this.cookieSameSite = cookieSameSite;
    }

    public long getCorsMaxAgeSeconds() {
        return corsMaxAgeSeconds;
    }

    public void setCorsMaxAgeSeconds(long corsMaxAgeSeconds) {
        this.corsMaxAgeSeconds = corsMaxAgeSeconds;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @PostConstruct
    public void validate() {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalStateException("app.security.allowed-origins 不能为空");
        }
        if (cookieSameSite == null || cookieSameSite.isBlank()) {
            cookieSameSite = "Lax";
        }
        if ("None".equalsIgnoreCase(cookieSameSite) && !cookieSecure) {
            throw new IllegalStateException("跨站 Cookie 使用 SameSite=None 时必须同时开启 cookie-secure=true");
        }
        if (corsMaxAgeSeconds <= 0) {
            corsMaxAgeSeconds = 3600;
        }
    }
}
