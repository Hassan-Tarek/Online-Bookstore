package com.bookstore.mapper.catalog;

import com.bookstore.dto.catalog.request.CategoryCreateRequest;
import com.bookstore.dto.catalog.request.CategoryUpdateRequest;
import com.bookstore.dto.catalog.response.CategoryResponse;
import com.bookstore.entity.catalog.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    Category toEntity(CategoryCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(CategoryUpdateRequest request, @MappingTarget Category category);
}
