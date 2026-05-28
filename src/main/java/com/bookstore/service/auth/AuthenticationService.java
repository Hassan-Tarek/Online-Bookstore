package com.bookstore.service.auth;

import com.bookstore.dto.auth.request.LoginRequest;
import com.bookstore.dto.auth.request.PasswordForgotRequest;
import com.bookstore.dto.auth.request.PasswordResetRequest;
import com.bookstore.dto.auth.request.RegisterRequest;
import com.bookstore.dto.auth.response.AuthenticationResponse;
import com.bookstore.dto.notification.request.NotificationCreateRequest;
import com.bookstore.dto.user.response.UserResponse;
import com.bookstore.entity.user.User;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.mapper.user.UserMapper;
import com.bookstore.repository.user.UserRepository;
import com.bookstore.security.jwt.JwtService;
import com.bookstore.security.token.TokenGenerator;
import com.bookstore.security.token.TokenStore;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.notification.EmailService;
import com.bookstore.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenStore tokenStore;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User already exists with email: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        // Save verification token
        String token = TokenGenerator.generateRandomToken();
        tokenStore.saveVerificationToken(user.getEmail(), token);

        // Send verification email
        emailService.sendVerificationEmail(user.getFirstName(), user.getEmail(), token);

        return userMapper.toResponse(user);
    }

    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getEmailVerified()) {
            throw new ConflictException("User already verified");
        }

        // Delete old token and generate new one
        String token = tokenStore.getVerificationToken(email);
        if (token != null) {
            tokenStore.deleteVerificationToken(token);
        }
        token = TokenGenerator.generateRandomToken();
        tokenStore.saveVerificationToken(email, token);

        // Send verification email
        emailService.sendVerificationEmail(user.getFirstName(), user.getEmail(), token);
    }

    @Transactional
    public void verify(String token) {
        String email = tokenStore.getVerificationEmail(token);
        if (email == null) {
            throw new BadRequestException("Invalid token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (user.getEmailVerified()) {
            throw new ConflictException("User already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        tokenStore.deleteVerificationToken(token);
        notificationService.createNotification(
                new NotificationCreateRequest(
                        "Welcome to Online Bookstore!",
                        "Your email has been verified. You can now log in and start exploring books."
                ), user
        );
    }

    public AuthenticationResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = Objects.requireNonNull(userDetails).getUser();

        if (!user.getEmailVerified()) {
            throw new ConflictException("User is not verified");
        }

        return generateAuthenticationResponse(user);
    }

    public void requestPasswordReset(PasswordForgotRequest request) {
        // Delete old token and generate new one
        String token = tokenStore.getPasswordResetToken(request.email());
        if (token != null) {
            tokenStore.deletePasswordResetToken(token);
        }
        token = TokenGenerator.generateRandomToken();
        tokenStore.savePasswordResetToken(request.email(), token);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getFirstName(), user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, PasswordResetRequest request) {
        String email = tokenStore.getPasswordResetEmail(token);
        if (email == null) {
            throw new BadRequestException("Token is invalid");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        tokenStore.deletePasswordResetToken(token);
        notificationService.createNotification(
                new NotificationCreateRequest(
                        "Password changed",
                        "Your password was successfully updated. If this wasn’t you, please contact support immediately."
                ), user
        );
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        String jti = jwtService.extractJti(refreshToken);
        if (jti == null || jwtService.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token is invalid or expired");
        }

        // Ensure this token matches the one we stored
        String storedToken = tokenStore.getRefreshToken(jti);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token has been revoked or rotated");
        }

        // Generate new tokens and Update Redis
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        AuthenticationResponse response = generateAuthenticationResponse(user);

        // Revoke old token
        tokenStore.deleteRefreshToken(jti);

        return response;
    }

    public void logout(String refreshToken, String accessToken) {
        String refreshTokenJti = jwtService.extractJti(refreshToken);
        String email = jwtService.extractUsername(refreshToken);
        if (refreshTokenJti == null || email == null || jwtService.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token is invalid or expired");
        }

        // Ensure this refresh token is the active one we stored
        String storedRefreshToken = tokenStore.getRefreshToken(refreshTokenJti);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token has been revoked or rotated");
        }

        // Revoke refresh token
        tokenStore.deleteRefreshToken(refreshTokenJti);

        // Blacklist access token
        String accessTokenJti = jwtService.extractJti(accessToken);
        if (accessTokenJti != null && !jwtService.isTokenExpired(accessToken)) {
            tokenStore.blacklistToken(accessTokenJti, accessToken);
        }

        SecurityContextHolder.clearContext();
    }

    private AuthenticationResponse generateAuthenticationResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        UserResponse userResponse = userMapper.toResponse(user);

        // Save Refresh Token
        tokenStore.saveRefreshToken(jwtService.extractJti(refreshToken), refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userResponse(userResponse)
                .build();
    }
}
