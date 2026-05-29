package com.bookstore.service.catalog;

import com.bookstore.dto.catalog.request.SeriesCreateRequest;
import com.bookstore.dto.catalog.request.SeriesUpdateRequest;
import com.bookstore.dto.catalog.response.BookSummaryResponse;
import com.bookstore.dto.catalog.response.SeriesResponse;
import com.bookstore.entity.catalog.Series;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.BookMapper;
import com.bookstore.mapper.catalog.SeriesMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.catalog.SeriesRepository;
import com.bookstore.service.storage.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final BookRepository bookRepository;
    private final SeriesMapper seriesMapper;
    private final BookMapper bookMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<SeriesResponse> getAllSeries(Pageable pageable) {
        return seriesRepository.findAll(pageable)
                .map(seriesMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SeriesResponse getSeriesById(UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " not found"));
        return seriesMapper.toResponse(series);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getSeriesBooks(UUID id, Pageable pageable) {
        if (!seriesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Series with id " + id + " not found");
        }
        return bookRepository.findAllBySeriesId(id, pageable)
                .map(bookMapper::toSummaryResponse);
    }

    @Transactional
    public SeriesResponse createSeries(SeriesCreateRequest request, MultipartFile coverImage) {
        Series series = seriesMapper.toEntity(request);
        if (coverImage != null && !coverImage.isEmpty()) {
            Map<String, String> result = cloudinaryService.uploadImage(coverImage);
            series.setCoverImageUrl(result.get("secure_url"));
            series.setCoverImagePublicId(result.get("public_id"));
        }
        series = seriesRepository.save(series);
        return seriesMapper.toResponse(series);
    }

    @Transactional
    public SeriesResponse updateSeries(UUID id, SeriesUpdateRequest request) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " not found"));
        seriesMapper.updateEntity(request, series);
        series = seriesRepository.save(series);
        return seriesMapper.toResponse(series);
    }

    @Transactional
    public SeriesResponse updateSeriesCoverImage(UUID id, MultipartFile coverImage) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " not found"));
        // Delete old image if exists
        if (series.getCoverImageUrl() != null) {
            cloudinaryService.deleteImage(series.getCoverImagePublicId());
        }

        Map<String, String> result = cloudinaryService.uploadImage(coverImage);
        series.setCoverImageUrl(result.get("secure_url"));
        series.setCoverImagePublicId(result.get("public_id"));
        series = seriesRepository.save(series);
        return seriesMapper.toResponse(series);
    }

    @Transactional
    public void deleteSeriesCoverImage(UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " not found"));
        if (series.getCoverImagePublicId() != null) {
            cloudinaryService.deleteImage(series.getCoverImagePublicId());
        }
        series.setCoverImageUrl(null);
        series.setCoverImagePublicId(null);
        seriesRepository.save(series);
    }

    @Transactional
    public void deleteSeries(UUID id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " not found"));
        if (series.getCoverImagePublicId() != null) {
            cloudinaryService.deleteImage(series.getCoverImagePublicId());
        }
        seriesRepository.delete(series);
    }
}
