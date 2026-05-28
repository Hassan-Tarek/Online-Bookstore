package com.bookstore.dto.auth.request;

import com.bookstore.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @ValidPassword(message = "Password must be 8+ chars and contain uppercase, lowercase, digits, symbols")
        String newPassword
) { }
