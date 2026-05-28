package com.bookstore.mapper.user;

import com.bookstore.dto.user.request.AddressCreateRequest;
import com.bookstore.dto.user.request.AddressUpdateRequest;
import com.bookstore.dto.user.response.AddressResponse;
import com.bookstore.entity.user.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    AddressResponse toResponse(Address address);

    Address toEntity(AddressCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AddressUpdateRequest request, @MappingTarget Address address);
}
