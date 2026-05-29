package com.bookstore.mapper.catalog;

import com.bookstore.dto.catalog.request.ReviewCreateRequest;
import com.bookstore.dto.catalog.request.ReviewUpdateRequest;
import com.bookstore.dto.catalog.response.ReviewResponse;
import com.bookstore.entity.catalog.Review;
import com.bookstore.mapper.user.UserMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = { UserMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewResponse toResponse(Review review);

    Review toEntity(ReviewCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ReviewUpdateRequest request, @MappingTarget Review review);
}
