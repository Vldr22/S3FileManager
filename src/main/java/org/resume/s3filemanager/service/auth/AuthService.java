package org.resume.s3filemanager.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.SecurityErrorMessages;
import org.resume.s3filemanager.dto.AuthRequest;
import org.resume.s3filemanager.dto.LoginResponse;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.security.JwtWhitelistService;
import org.resume.s3filemanager.security.JwtCookieService;
import org.resume.s3filemanager.security.JwtTokenService;
import org.resume.s3filemanager.security.MySecurityUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtWhitelistService jwtWhitelistService;
    private final JwtCookieService jwtCookieService;
    private final UserService userService;

    public LoginResponse login(AuthRequest request, HttpServletResponse response) {
        User user = verifyCredentials(request.getUsername(), request.getPassword());

        String token = jwtTokenService.generateToken(user.getUsername(), user.getRole());

        jwtWhitelistService.saveToken(
                user.getUsername(),
                token,
                jwtTokenService.getExpirationSeconds()
        );

        jwtCookieService.setAuthCookie(response, token);

        log.info("User logged in successfully: {}", user.getUsername());
        return new LoginResponse(token, user.getUsername(), user.getRole().getAuthority());
    }

    public void register(AuthRequest request) {
        userService.createUser(request.getUsername(), request.getPassword());
        log.info("User registered successfully: {}", request.getUsername());
    }

    public void logout(HttpServletResponse response) {
        String username = MySecurityUtils.getCurrentUsername();

        jwtWhitelistService.deleteToken(username);
        jwtCookieService.clearAuthCookie(response);

        log.info("User logged out successfully: {}", username);
    }

    private User verifyCredentials(String username, String password) {
        User user = userService.findByUsername(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException(SecurityErrorMessages.INVALID_CREDENTIALS);
        }
        return user;
    }

    private UserRole getUserRole(User user) {
        return user.getRole();
    }
}
