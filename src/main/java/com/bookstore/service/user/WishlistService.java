package com.bookstore.service.user;

import com.bookstore.dto.user.request.WishlistItemRequest;
import com.bookstore.dto.user.response.WishlistResponse;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.user.User;
import com.bookstore.entity.user.Wishlist;
import com.bookstore.entity.user.WishlistItem;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.user.WishlistMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.user.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final BookRepository bookRepository;

    @Transactional
    public WishlistResponse getMyWishlist(User user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        return wishlistMapper.toResponse(wishlist);
    }

    @Transactional
    public WishlistResponse addWishlistItem(WishlistItemRequest request, User user) {
        Wishlist wishlist = getOrCreateWishlist(user);

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + request.bookId() + " not found"));

        boolean alreadyExists = wishlist.getWishlistItems()
                .stream()
                .anyMatch(item -> item.getBook().getId().equals(book.getId()));
        if (alreadyExists) {
            throw new ConflictException("Item already exists in the wishlist");
        }

        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlist(wishlist)
                .book(book)
                .build();
        wishlist.getWishlistItems().add(wishlistItem);
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlist = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(wishlist);
    }

    @Transactional
    public void deleteWishlistItem(UUID itemId, User user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        boolean removed = wishlist.getWishlistItems()
                .removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Wishlist item not found");
        }
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);
    }

    @Transactional
    public void clearWishlist(User user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        wishlist.getWishlistItems().clear();
        wishlist.setUpdatedAt(LocalDateTime.now());
        wishlistRepository.save(wishlist);
    }

    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .user(user)
                            .build();
                    return wishlistRepository.save(wishlist);
                });
    }
}
