package com.bookstore.mapper.commerce;

import com.bookstore.dto.commerce.request.ShipmentCreateRequest;
import com.bookstore.dto.commerce.response.ShipmentResponse;
import com.bookstore.entity.commerce.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShipmentMapper {

    @Mapping(target = "orderId", source = "order.id")
    ShipmentResponse toResponse(Shipment shipment);

    Shipment toEntity(ShipmentCreateRequest request);
}
