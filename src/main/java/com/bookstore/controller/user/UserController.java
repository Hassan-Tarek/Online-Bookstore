package com.bookstore.controller.user;

import com.bookstore.dto.user.request.ChangePasswordRequest;
import com.bookstore.dto.user.request.UserUpdateRequest;
import com.bookstore.dto.user.response.UserResponse;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users",
        produces = "application/json")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            Pageable pageable) {
        var responses = userService.getAllUsers(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id) {
        var response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = userService.getMyProfile(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> promoteUser(
            @PathVariable UUID id) {
        var response = userService.promoteUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/demote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> demoteUser(
            @PathVariable UUID id) {
        var response = userService.demoteUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> disableUser(
            @PathVariable UUID id) {
        var response = userService.disableUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> enableUser(
            @PathVariable UUID id) {
        var response = userService.enableUser(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = userService.updateMyProfile(request, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/me/profile-image",
            consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUserProfileImage(
            @RequestParam MultipartFile profileImage,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = userService.updateUserProfileImage(profileImage, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.changePassword(request, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUserProfileImage(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteMyProfile(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
