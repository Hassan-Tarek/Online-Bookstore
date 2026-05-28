package com.bookstore.mapper.user;

import com.bookstore.dto.auth.request.RegisterRequest;
import com.bookstore.dto.user.response.UserResponse;
import com.bookstore.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    User toEntity(RegisterRequest request);
}
