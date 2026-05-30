package com.bookstore.mapper.commerce;

import com.bookstore.dto.commerce.request.PromotionCreateRequest;
import com.bookstore.dto.commerce.request.PromotionUpdateRequest;
import com.bookstore.dto.commerce.response.PromotionResponse;
import com.bookstore.entity.commerce.Promotion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {

    PromotionResponse toResponse(Promotion promotion);

    Promotion toEntity(PromotionCreateRequest promotionCreateRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PromotionUpdateRequest promotionUpdateRequest, @MappingTarget Promotion promotion);
}
