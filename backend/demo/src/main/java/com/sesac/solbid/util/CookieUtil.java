package com.sesac.solbid.util;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class CookieUtil {
    @Value("${app.auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${app.auth.cookie.path:/}")
    private String cookiePath;

    @Value("${app.auth.cookie.domain:}")
    private String cookieDomain;

    @PostConstruct
    void validate() {
        if ("None".equalsIgnoreCase(sameSite) && !cookieSecure) {
            log.warn("SameSite=None은 Secure=true가 필요합니다. 현재 secure=false로 설정되어 브라우저에서 쿠키가 차단될 수 있습니다.");
        }
    }

    public void addTokenCookies(HttpServletResponse response,
                                String accessToken, long accessTokenExpiresInSeconds,
                                String refreshToken, long refreshTokenExpiresInSeconds) {
        addCookie(response, "accessToken", accessToken, accessTokenExpiresInSeconds);
        addCookie(response, "refreshToken", refreshToken, refreshTokenExpiresInSeconds);
    }

    public void clearTokenCookies(HttpServletResponse response) {
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");
    }

    public void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(Duration.ofSeconds(Math.max(0, maxAgeSeconds)));

        if (sameSite != null && !sameSite.isBlank()) {
            builder.sameSite(sameSite);
        }
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        ResponseCookie cookie = builder.build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(Duration.ZERO);

        if (sameSite != null && !sameSite.isBlank()) {
            builder.sameSite(sameSite);
        }
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        ResponseCookie cookie = builder.build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
