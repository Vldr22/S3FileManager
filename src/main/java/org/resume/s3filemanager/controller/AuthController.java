package org.resume.s3filemanager.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.constant.SuccessMessages;
import org.resume.s3filemanager.dto.AuthRequest;
import org.resume.s3filemanager.dto.CommonResponse;
import org.resume.s3filemanager.dto.LoginResponse;
import org.resume.s3filemanager.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, response);
        return CommonResponse.success(loginResponse);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<String> register(@Valid @RequestBody AuthRequest request) {
        authService.register(request);
        return CommonResponse.success(SuccessMessages.REGISTRATION_SUCCESS);
    }

    @PostMapping("/logout")
    public CommonResponse<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return CommonResponse.success(SuccessMessages.LOGOUT_SUCCESS);
    }
}
