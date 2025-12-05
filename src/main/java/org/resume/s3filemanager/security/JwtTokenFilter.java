package org.resume.s3filemanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.SecurityErrorMessages;
import org.resume.s3filemanager.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final JwtWhitelistService jwtWhitelistService;
    private final JwtCookieService jwtCookieService;
    private final ObjectMapper objectMapper;

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/home"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return Arrays.stream(PUBLIC_PATHS)
                .anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtCookieService.extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtTokenService.extractSubject(token);

            if (!jwtWhitelistService.isValid(username, token)) {
                sendError(response, request.getRequestURI(), SecurityErrorMessages.TOKEN_INVALID);
                log.warn("Token validation failed for user: {}", username);
                return;
            }

            String role = jwtTokenService.extractRole(token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication successful - User: {}, Role: {}", username, role);

        } catch (ExpiredJwtException e) {
            sendError(response, request.getRequestURI(), SecurityErrorMessages.TOKEN_EXPIRED);
            log.warn("Token expired for: {}", request.getRequestURI());
            return;
        } catch (Exception e) {
            sendError(response, request.getRequestURI(), SecurityErrorMessages.TOKEN_INVALID);
            log.error("Invalid token for: {}. Error: {}", request.getRequestURI(), e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String path, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("path", path);

        CommonResponse<?> errorResponse = CommonResponse.error(problemDetail);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
