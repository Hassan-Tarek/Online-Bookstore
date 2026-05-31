package com.bookstore.service.commerce;

import com.bookstore.config.AppProperties;
import com.bookstore.dto.commerce.response.PaymentResponse;
import com.bookstore.dto.notification.request.NotificationCreateRequest;
import com.bookstore.entity.commerce.Order;
import com.bookstore.entity.commerce.Payment;
import com.bookstore.entity.user.User;
import com.bookstore.enums.OrderStatus;
import com.bookstore.enums.PaymentStatus;
import com.bookstore.exception.AccessDeniedException;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.commerce.PaymentMapper;
import com.bookstore.repository.commerce.PaymentRepository;
import com.bookstore.service.catalog.InventoryService;
import com.bookstore.service.notification.NotificationService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AppProperties appProperties;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findAllByStatus(status, pageable)
                .map(paymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(User user, PaymentStatus status, Pageable pageable) {
        return paymentRepository.findAllByUserIdAndStatus(user.getId(), status, pageable)
                .map(paymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id, User user) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + id + " not found"));
        if (!payment.getOrder().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to view this payment.");
        }
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public Payment createPayment(Order order) {
        try {
            // Idempotency check
            if (paymentRepository.existsByOrderId(order.getId())) {
                throw new BadRequestException("Payment already exists for this order.");
            }

            // Create Stripe Intent with Idempotency Key
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("order_" + order.getId())
                    .build();

            long amountInCents = order.getTotalPrice().multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(appProperties.settings().currency())
                    .setDescription("Order: " + order.getId())
                    .putMetadata("orderId", order.getId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params, options);

            Payment payment = Payment.builder()
                    .paymentIntentId(intent.getId())
                    .clientSecret(intent.getClientSecret())
                    .amount(order.getTotalPrice())
                    .currency(appProperties.settings().currency())
                    .build();
            payment.setOrder(order);
            payment = paymentRepository.save(payment);
            return payment;
        } catch (StripeException e) {
            notificationService.createNotification(
                    new NotificationCreateRequest(
                            "Payment Authorization Failed",
                            String.format("We couldn't process your payment details for order #%s. Please try checking out again.", order.getId())
                    ), order.getUser()
            );
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void refundPayment(String paymentIntentId, BigDecimal amount, String currency) {
        try {
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .build();
            Refund.create(params);
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + paymentIntentId + " not found"));
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature,
                    appProperties.integration().stripe().webhookSecret());
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject()
                    .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found"));
            String paymentIntentId = intent.getId();
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + paymentIntentId + " not found"));
            Order order = payment.getOrder();
            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    payment.setStatus(PaymentStatus.PAID);
                    order.setStatus(OrderStatus.PAID);

                    notificationService.createNotification(
                            new NotificationCreateRequest(
                                    "Order Placed Successfully",
                                    String.format("Thank you for your purchase! Your order #%s has been received and confirmed.", order.getId())
                            ), order.getUser()
                    );
                }
                case "payment_intent.failed" -> {
                    payment.setStatus(PaymentStatus.FAILED);
                    order.setStatus(OrderStatus.CANCELLED);
                    order.getOrderItems().forEach(item ->
                            inventoryService.restoreStock(item.getBook().getId(), item.getQuantity())
                    );

                    notificationService.createNotification(
                            new NotificationCreateRequest(
                                    "Payment Authorization Failed",
                                    String.format("We couldn't process your payment details for order #%s. Transaction has been aborted and items have been returned to active stock. Please try checking out again.", order.getId())
                            ), order.getUser()
                    );
                }
                case "charge.refunded" -> {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    payment.getOrder().setStatus(OrderStatus.CANCELLED);
                    order.getOrderItems().forEach(item ->
                            inventoryService.restoreStock(item.getBook().getId(), item.getQuantity())
                    );

                    notificationService.createNotification(
                            new NotificationCreateRequest(
                                    "Order Cancelled Successfully",
                                    String.format("Order #%s has been officially cancelled. If your account was already charged, a refund has been initiated back to your original payment method.", order.getId())
                            ), order.getUser()
                    );
                }
            }
            paymentRepository.save(payment);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid signature: " + e.getMessage());
        }
    }
}
