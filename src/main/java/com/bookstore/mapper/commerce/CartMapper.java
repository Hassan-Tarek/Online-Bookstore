package com.bookstore.mapper.commerce;

import com.bookstore.dto.commerce.response.CartItemResponse;
import com.bookstore.dto.commerce.response.CartResponse;
import com.bookstore.entity.commerce.Cart;
import com.bookstore.entity.commerce.CartItem;
import com.bookstore.mapper.catalog.BookMapper;
import com.bookstore.util.PriceUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = { PriceUtils.class, BookMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalPrice", source = "cart", qualifiedByName = "rectifySubtotal")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "originalUnitPrice", source = "book.price")
    @Mapping(target = "finalUnitPrice", source = "cartItem", qualifiedByName = "rectifyItemFinalPrice")
    CartItemResponse toItemResponse(CartItem cartItem);
}
