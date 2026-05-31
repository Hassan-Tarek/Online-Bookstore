package com.bookstore.mapper.commerce;

import com.bookstore.dto.commerce.response.OrderItemResponse;
import com.bookstore.dto.commerce.response.OrderResponse;
import com.bookstore.entity.commerce.Order;
import com.bookstore.entity.commerce.OrderItem;
import com.bookstore.mapper.catalog.BookMapper;
import com.bookstore.util.PriceUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = { PriceUtils.class, BookMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "promoCode", source = "promotion.code")
    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem orderItem);
}
