package com.bookstore.dto.auth.request;

import jakarta.validation.constraints.Email;

public record PasswordForgotRequest(

        @Email(message = "Email must be a valid email address")
        String email
) { }
