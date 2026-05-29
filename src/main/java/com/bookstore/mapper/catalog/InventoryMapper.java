package com.bookstore.mapper.catalog;

import com.bookstore.dto.catalog.request.InventoryCreateRequest;
import com.bookstore.dto.catalog.response.InventoryResponse;
import com.bookstore.entity.catalog.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    @Mapping(target = "bookId", source = "book.id")
    InventoryResponse toResponse(Inventory inventory);

    Inventory toEntity(InventoryCreateRequest request);
}
