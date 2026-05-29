package com.bookstore.mapper.catalog;

import com.bookstore.dto.catalog.request.BookCreateRequest;
import com.bookstore.dto.catalog.request.BookUpdateRequest;
import com.bookstore.dto.catalog.response.BookResponse;
import com.bookstore.dto.catalog.response.BookSummaryResponse;
import com.bookstore.entity.catalog.Book;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    @Mapping(target = "availableStock", source = "inventory.availableStock")
    BookResponse toResponse(Book book);

    @Mapping(target = "stockStatus", source = "inventory.status")
    BookSummaryResponse toSummaryResponse(Book book);

    Book toEntity(BookCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BookUpdateRequest request, @MappingTarget Book book);
}
