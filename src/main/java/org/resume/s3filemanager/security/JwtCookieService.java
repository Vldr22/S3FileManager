package org.resume.s3filemanager.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.resume.s3filemanager.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class JwtCookieService {

    private final JwtTokenService jwtTokenService;
    private final SecurityProperties securityProperties;

    public void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = createCookie(token, jwtTokenService.getExpirationSeconds());
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = createCookie("", 0);
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> SecurityConstants.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private ResponseCookie createCookie(String value, long maxAge) {
        return ResponseCookie.from(SecurityConstants.COOKIE_NAME, value)
                .httpOnly(true)
                .secure(securityProperties.isCookieSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }
}
