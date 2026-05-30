package com.bookstore.service.commerce;

import com.bookstore.dto.commerce.request.CartItemCreateRequest;
import com.bookstore.dto.commerce.request.CartItemUpdateRequest;
import com.bookstore.dto.commerce.response.CartResponse;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.commerce.Cart;
import com.bookstore.entity.commerce.CartItem;
import com.bookstore.entity.user.User;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.commerce.CartMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.commerce.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse getCartByUser(User user) {
        Cart cart = getOrCreateCart(user);
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addCartItem(CartItemCreateRequest request, User user) {
        Cart cart = getOrCreateCart(user);
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + request.bookId() + " not found"));

        boolean alreadyExists = cart.getCartItems()
                .stream()
                .anyMatch(item -> item.getBook().getId().equals(book.getId()));
        if (alreadyExists) {
            throw new ConflictException("Book already exists in cart");
        }

        CartItem cartItem = CartItem.builder()
                .quantity(request.quantity())
                .cart(cart)
                .book(book)
                .build();
        cart.getCartItems().add(cartItem);
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(UUID itemId, CartItemUpdateRequest request, User user) {
        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartItem.setQuantity(request.quantity());
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public void deleteCartItem(UUID itemId, User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> Cart.builder().user(user).build());
        cart.getCartItems().removeIf(cartItem -> cartItem.getId().equals(itemId));
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(cart);
                });
    }
}
