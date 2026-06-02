package com.bookstore.service.commerce;

import com.bookstore.dto.commerce.request.CartItemCreateRequest;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.catalog.Inventory;
import com.bookstore.entity.commerce.Cart;
import com.bookstore.entity.commerce.CartItem;
import com.bookstore.entity.user.User;
import com.bookstore.enums.InventoryStatus;
import com.bookstore.exception.ConflictException;
import com.bookstore.mapper.commerce.CartMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.commerce.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    @Test
    void addCartItem_throwsWhenBookAlreadyInCart() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .password("Password@1234")
                .emailVerified(true)
                .build();
        Inventory inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .availableStock(5)
                .reservedStock(0)
                .status(InventoryStatus.IN_STOCK)
                .build();
        Book book = Book.builder()
                .id(UUID.randomUUID())
                .title("Book")
                .price(new BigDecimal("15.00"))
                .inventory(inventory)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .quantity(3)
                .book(book)
                .build();
        Cart cart = Cart.builder()
                .user(user)
                .cartItems(List.of(cartItem))
                .build();
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        CartItemCreateRequest request = new CartItemCreateRequest(book.getId(), 3);

        assertThatThrownBy(() -> cartService.addCartItem(request, user))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists in cart");
    }
}
