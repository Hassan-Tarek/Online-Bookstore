package com.bookstore.mapper.catalog;

import com.bookstore.dto.catalog.request.AuthorCreateRequest;
import com.bookstore.dto.catalog.request.AuthorUpdateRequest;
import com.bookstore.dto.catalog.response.AuthorResponse;
import com.bookstore.entity.catalog.Author;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {

    AuthorResponse toResponse(Author author);

    Author toEntity(AuthorCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AuthorUpdateRequest request, @MappingTarget Author author);
}
