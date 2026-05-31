package com.bookstore.service.catalog;

import com.bookstore.dto.catalog.request.BookCreateRequest;
import com.bookstore.dto.catalog.request.BookSearchCriteria;
import com.bookstore.dto.catalog.request.BookUpdateRequest;
import com.bookstore.dto.catalog.response.BookResponse;
import com.bookstore.dto.catalog.response.BookSummaryResponse;
import com.bookstore.entity.catalog.Author;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.catalog.Category;
import com.bookstore.entity.catalog.Series;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.BookMapper;
import com.bookstore.repository.catalog.AuthorRepository;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.catalog.CategoryRepository;
import com.bookstore.repository.catalog.SeriesRepository;
import com.bookstore.repository.catalog.specification.BookSpecification;
import com.bookstore.service.storage.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final SeriesRepository seriesRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        Specification<Book> specification = BookSpecification.buildSearch(criteria);
        return bookRepository.findAll(specification, pageable)
                .map(bookMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getTopRatedBooks(Pageable pageable) {
        return bookRepository.findTopRated(pageable)
                .map(bookMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> getNewReleases(Pageable pageable) {
        return bookRepository.findAllByOrderByPublicationDateDesc(pageable)
                .map(bookMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#id")
    public BookResponse getBookById(UUID id) {
        Book book = bookRepository.findWithSeriesAndCategoriesAndAuthorsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " not found"));
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponse createBook(BookCreateRequest request, MultipartFile coverImage) {
        Book book = bookMapper.toEntity(request);
        if (coverImage != null && !coverImage.isEmpty()) {
            Map<String, String> result = cloudinaryService.uploadImage(coverImage);
            book.setCoverImageUrl(result.get("secure_url"));
            book.setCoverImagePublicId(result.get("public_id"));
        }

        if (request.seriesId() != null) {
            Series series = seriesRepository.findById(request.seriesId())
                    .orElseThrow(() -> new ResourceNotFoundException("Series with id " + request.seriesId() + " not found"));
            book.setSeries(series);
        }
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.categoryIds());
            if (categories.size() != request.categoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found");
            }
            book.setCategories(new HashSet<>(categories));
        }
        if (request.authorIds() != null && !request.authorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(request.authorIds());
            if (authors.size() != request.authorIds().size()) {
                throw new ResourceNotFoundException("One or more authors not found");
            }
            book.setAuthors(new HashSet<>(authors));
        }
        book = bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Transactional
    @CachePut(value = "books", key = "#id")
    public BookResponse updateBook(UUID id, BookUpdateRequest request) {
        Book book = bookRepository.findWithSeriesAndCategoriesAndAuthorsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " not found"));
        bookMapper.updateEntity(request, book);
        book = bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Transactional
    @CachePut(value = "books", key = "#id")
    public BookResponse updateBookCoverImage(UUID id, MultipartFile coverImage) {
        Book book = bookRepository.findWithSeriesAndCategoriesAndAuthorsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " not found"));
        if (book.getCoverImageUrl() != null) {
            cloudinaryService.deleteImage(book.getCoverImagePublicId());
        }

        Map<String, String> result = cloudinaryService.uploadImage(coverImage);
        book.setCoverImageUrl(result.get("secure_url"));
        book.setCoverImagePublicId(result.get("public_id"));
        book = bookRepository.save(book);
        return bookMapper.toResponse(book);
    }

    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void deleteBookCoverImage(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " not found"));
        if (book.getCoverImagePublicId() != null) {
            cloudinaryService.deleteImage(book.getCoverImagePublicId());
        }
        book.setCoverImageUrl(null);
        book.setCoverImagePublicId(null);
        bookRepository.save(book);
    }

    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id " + id + " not found"));
        if (book.getCoverImagePublicId() != null) {
            cloudinaryService.deleteImage(book.getCoverImagePublicId());
        }
        bookRepository.delete(book);
    }
}
