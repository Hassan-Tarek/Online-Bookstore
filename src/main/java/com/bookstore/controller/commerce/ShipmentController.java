package com.bookstore.controller.commerce;

import com.bookstore.dto.commerce.request.ShipmentCreateRequest;
import com.bookstore.dto.commerce.response.ShipmentResponse;
import com.bookstore.enums.ShipmentStatus;
import com.bookstore.security.user.CustomUserDetails;
import com.bookstore.service.commerce.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/shipments",
        produces = "application/json")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ShipmentResponse>> getAllShipments(
            @RequestParam(required = false) ShipmentStatus status,
            Pageable pageable) {
        var responses = shipmentService.getAllShipments(status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ShipmentResponse>> getMyShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        var responses = shipmentService.getMyShipments(userDetails.getUser(), status, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = shipmentService.getShipmentById(id, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> createShipment(
            @RequestBody ShipmentCreateRequest request) {
        var response = shipmentService.createShipment(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(path = "/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @PathVariable UUID id,
            @RequestParam ShipmentStatus status) {
        var response = shipmentService.updateShipmentStatus(id, status);
        return ResponseEntity.ok(response);
    }
}
