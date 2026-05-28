package com.bookstore.mapper.user;

import com.bookstore.dto.auth.request.RegisterRequest;
import com.bookstore.dto.user.request.UserUpdateRequest;
import com.bookstore.dto.user.response.UserResponse;
import com.bookstore.dto.user.response.UserSummaryResponse;
import com.bookstore.entity.user.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);

    User toEntity(RegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
}
