package com.bookstore.service.commerce;

import com.bookstore.dto.commerce.request.OrderCreateRequest;
import com.bookstore.dto.commerce.response.OrderResponse;
import com.bookstore.dto.notification.request.NotificationCreateRequest;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.commerce.Cart;
import com.bookstore.entity.commerce.CartItem;
import com.bookstore.entity.commerce.Order;
import com.bookstore.entity.commerce.OrderItem;
import com.bookstore.entity.commerce.Payment;
import com.bookstore.entity.commerce.Promotion;
import com.bookstore.entity.user.User;
import com.bookstore.enums.OrderStatus;
import com.bookstore.enums.PaymentStatus;
import com.bookstore.exception.AccessDeniedException;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.commerce.OrderMapper;
import com.bookstore.repository.commerce.CartRepository;
import com.bookstore.repository.commerce.OrderRepository;
import com.bookstore.repository.commerce.PromotionRepository;
import com.bookstore.service.catalog.InventoryService;
import com.bookstore.service.notification.NotificationService;
import com.bookstore.util.PriceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final PromotionRepository promotionRepository;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final CartService cartService;
    private final PriceUtils priceUtils;
    private final NotificationService notificationService;

    public Page<OrderResponse> getAllOrders(OrderStatus status, BigDecimal minPrice,
                                            BigDecimal maxPrice, Pageable pageable) {
        return orderRepository.findAllByStatusAndMinPriceAndMaxPrice(status, minPrice, maxPrice, pageable)
                .map(orderMapper::toResponse);
    }

    public Page<OrderResponse> getMyOrders(User user, OrderStatus status, BigDecimal minPrice,
                                           BigDecimal maxPrice, Pageable pageable) {
        return orderRepository.findAllByUserIdAndStatusAndMinPriceAndMaxPrice(user.getId(), status, minPrice, maxPrice, pageable)
                .map(orderMapper::toResponse);
    }

    public OrderResponse getOrderById(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id + " + orderId + " not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only view your own orders");
        }
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse checkout(OrderCreateRequest request, User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Promotion promotion = promotionRepository.findByCode(request.promoCode())
                .orElse(null);

        PriceUtils.PriceSummary summary =
                priceUtils.calculatePriceSummary(cart, request.shippingMethod(), promotion);

        Order order = Order.builder()
                .shippingAddress(request.shippingAddress())
                .billingAddress(request.billingAddress())
                .subtotal(summary.subtotal())
                .tax(summary.tax())
                .shippingFee(summary.shippingFee())
                .discountAmount(summary.discount())
                .totalPrice(summary.total())
                .shippingMethod(request.shippingMethod())
                .user(user)
                .promotion(promotion)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Book book = cartItem.getBook();
            if (book.getInventory().getAvailableStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Not enough stock for book: " + book.getTitle());
            }

            OrderItem orderItem = OrderItem.builder()
                    .originalUnitPrice(book.getPrice())
                    .finalUnitPrice(priceUtils.calculateItemFinalPrice(cartItem))
                    .quantity(cartItem.getQuantity())
                    .order(order)
                    .book(book)
                    .build();
            orderItems.add(orderItem);

            inventoryService.reserveStock(book.getInventory().getId(), cartItem.getQuantity());
        }
        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        // Process payment
        Payment payment = paymentService.createPayment(order);
        order.setPayment(payment);
        order = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(user);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id, User user) {
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + id + " not found"));

        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            throw new BadRequestException("Order is already cancelled");
        }

        if (order.getStatus().equals(OrderStatus.PAID)) {
            paymentService.refundPayment(order.getPayment().getPaymentIntentId(),
                    order.getPayment().getAmount(), order.getPayment().getCurrency());
        }

        order.getOrderItems().forEach(orderItem ->
                inventoryService.restoreStock(orderItem.getBook().getInventory().getId(), orderItem.getQuantity()));
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public void processAbandonedOrdersCleanup(LocalDateTime cutoff) {
        List<Order> abandonedOrders =
                orderRepository.findAllByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);

        for (Order order : abandonedOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            order.getPayment().setStatus(PaymentStatus.FAILED);
            order.getOrderItems().forEach(item ->
                    inventoryService.restoreStock(item.getBook().getInventory().getId(), item.getQuantity()));

            orderRepository.save(order);

            notificationService.createNotification(
                    new NotificationCreateRequest(
                            "Order Reservation Expired",
                            String.format("Your pending order #%s was automatically cancelled because payment wasn't completed within the 30-minute window.", order.getId())
                    ), order.getUser()
            );
        }
    }
}
