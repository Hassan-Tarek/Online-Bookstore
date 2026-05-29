package com.bookstore.mapper.user;

import com.bookstore.dto.user.response.WishlistItemResponse;
import com.bookstore.dto.user.response.WishlistResponse;
import com.bookstore.entity.user.Wishlist;
import com.bookstore.entity.user.WishlistItem;
import com.bookstore.mapper.catalog.BookMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = { BookMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WishlistMapper {

    @Mapping(target = "items", source = "wishlistItems")
    WishlistResponse toResponse(Wishlist wishlist);

    WishlistItemResponse toItemResponse(WishlistItem wishlistItem);
}
