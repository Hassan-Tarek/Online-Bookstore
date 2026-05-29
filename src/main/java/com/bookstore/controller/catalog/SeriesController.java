package com.bookstore.controller.catalog;

import com.bookstore.dto.catalog.request.SeriesCreateRequest;
import com.bookstore.dto.catalog.request.SeriesUpdateRequest;
import com.bookstore.dto.catalog.response.SeriesResponse;
import com.bookstore.service.catalog.SeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/series",
        produces = "application/json")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<SeriesResponse>> getAllSeries(
            Pageable pageable) {
        var responses = seriesService.getAllSeries(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<SeriesResponse> getSeries(
            @PathVariable UUID id) {
        var response = seriesService.getSeriesById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeriesResponse> createSeries(
            @RequestPart(value = "data") @Valid SeriesCreateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile coverImage) {
        var response = seriesService.createSeries(request, coverImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeriesResponse> updateSeries(
            @PathVariable UUID id,
            @Valid @RequestBody SeriesUpdateRequest request) {
        var response = seriesService.updateSeries(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{id}/cover-image",
            consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeriesResponse> updateSeriesCoverImage(
            @PathVariable UUID id,
            @RequestParam MultipartFile coverImage) {
        var response = seriesService.updateSeriesCoverImage(id, coverImage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/{id}/cover-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeriesCoverImage(
            @PathVariable UUID id) {
        seriesService.deleteSeriesCoverImage(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeries(
            @PathVariable UUID id) {
        seriesService.deleteSeries(id);
        return ResponseEntity.noContent().build();
    }
}
