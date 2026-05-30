package com.bookstore.controller.auth;

import com.bookstore.config.AppProperties;
import com.bookstore.dto.auth.request.LoginRequest;
import com.bookstore.dto.auth.request.PasswordForgotRequest;
import com.bookstore.dto.auth.request.RegisterRequest;
import com.bookstore.dto.auth.request.PasswordResetRequest;
import com.bookstore.dto.auth.response.AuthenticationResponse;
import com.bookstore.dto.user.response.UserResponse;
import com.bookstore.service.auth.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/auth",
        produces = "application/json")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AppProperties appProperties;

    @PostMapping(path = "/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        var response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/verification/resend")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> resendVerification(
            @Email @RequestParam(name = "email") String email) {
        authenticationService.resendVerification(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/verify")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> verify(
            @RequestParam String token) {
        authenticationService.verify(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        var authResponse = authenticationService.login(request);
        setRefreshTokenCookie(authResponse.refreshToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping(path = "/password/forgot")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody PasswordForgotRequest request) {
        authenticationService.requestPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/password/reset")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> resetPassword(
            @RequestParam(name = "token") String token,
            @Valid @RequestBody PasswordResetRequest request) {
        authenticationService.resetPassword(token, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/refresh")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthenticationResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshTokenCookie(request);
        var authResponse = authenticationService.refreshToken(refreshToken);
        setRefreshTokenCookie(authResponse.refreshToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping(path = "/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshTokenCookie(request);
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (refreshToken != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            authenticationService.logout(refreshToken, authHeader.substring(7));
        }

        // Delete the cookie from the browser
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> cookie.getName().equals(
                        appProperties.security().jwt().refreshTokenCookie().name()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void setRefreshTokenCookie(
            String refreshToken,
            HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(appProperties.security().jwt().refreshTokenCookie().name(), refreshToken)
                .httpOnly(true) // Protect against XSS attacks
                .domain(appProperties.security().jwt().refreshTokenCookie().domain())
                .secure(appProperties.security().jwt().refreshTokenCookie().secure())
                .path(appProperties.security().jwt().refreshTokenCookie().path())
                .sameSite(appProperties.security().jwt().refreshTokenCookie().sameSite())
                .maxAge(appProperties.security().jwt().refreshTokenCookie().maxAge())
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(
            HttpServletResponse response) {
        ResponseCookie deletedCookie = ResponseCookie
                .from(appProperties.security().jwt().refreshTokenCookie().name(), "")
                .httpOnly(true)
                .domain(appProperties.security().jwt().refreshTokenCookie().domain())
                .secure(appProperties.security().jwt().refreshTokenCookie().secure())
                .path(appProperties.security().jwt().refreshTokenCookie().path())
                .sameSite(appProperties.security().jwt().refreshTokenCookie().sameSite())
                .maxAge(Duration.ZERO)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, deletedCookie.toString());
    }
}
