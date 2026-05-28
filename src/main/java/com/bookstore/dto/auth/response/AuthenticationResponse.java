package com.bookstore.dto.auth.response;

import com.bookstore.dto.user.response.UserResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        String tokenType,
        UserResponse userResponse
) { }
