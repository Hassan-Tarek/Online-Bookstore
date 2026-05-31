package com.bookstore.controller.commerce;

import com.bookstore.dto.commerce.response.PaymentResponse;
import com.bookstore.enums.PaymentStatus;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.commerce.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/payments",
        produces = "application/json")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        var responses = paymentService.getAllPayments(status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(
            @RequestParam(required = false) PaymentStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = paymentService.getMyPayments(userDetails.getUser(), status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = paymentService.getPaymentById(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/webhook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.noContent().build();
    }
}
