package com.bookstore.repository.catalog.specification;

import com.bookstore.dto.catalog.request.BookSearchCriteria;
import com.bookstore.entity.catalog.Author;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.catalog.Category;
import com.bookstore.entity.catalog.Review;
import com.bookstore.entity.catalog.Series;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookSpecification {

    public static Specification<Book> buildSearch(BookSearchCriteria criteria) {
        return Specification.allOf(
                BookSpecification.hasTitle(criteria.title()),
                BookSpecification.hasSeries(criteria.series()),
                BookSpecification.hasCategory(criteria.category()),
                BookSpecification.hasAuthor(criteria.author()),
                BookSpecification.hasLanguage(criteria.language()),
                BookSpecification.hasMinPrice(criteria.minPrice()),
                BookSpecification.hasMaxPrice(criteria.maxPrice()),
                BookSpecification.hasMinRating(criteria.minRating()),
                BookSpecification.hasMaxRating(criteria.maxRating()),
                BookSpecification.hasPublicationDate(criteria.publicationDate())
        );
    }

    private static Specification<Book> hasTitle(String title) {
        return (root, query, cb) -> title == null ? null :
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private static Specification<Book> hasSeries(String series) {
        return (root, query, cb) -> {
            if (series == null || series.isEmpty()) {
                return null;
            }

            Join<Book, Series> seriesJoin = root.join("series");
            query.distinct(true);
            return cb.like(cb.lower(seriesJoin.get("title")), "%" + series.toLowerCase() + "%");
        };
    }

    private static Specification<Book> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.isEmpty()) {
                return null;
            }

            Join<Book, Category> categoryJoin = root.join("categories");
            query.distinct(true);
            return cb.like(cb.lower(categoryJoin.get("name")), "%" + category.toLowerCase() + "%");
        };
    }

    private static Specification<Book> hasAuthor(String author) {
        return (root, query, cb) -> {
            if (author == null || author.isEmpty()) {
                return null;
            }

            Join<Book, Author> authorJoin = root.join("authors");
            query.distinct(true);
            return cb.like(cb.lower(authorJoin.get("name")), "%" + author.toLowerCase() + "%");
        };
    }

    private static Specification<Book> hasLanguage(String language) {
        return (root, query, cb) -> language == null ? null :
                cb.like(cb.lower(root.get("language")), "%" + language.toLowerCase() + "%");
    }

    private static Specification<Book> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null :
                cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<Book> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null :
                cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private static Specification<Book> hasMinRating(Integer minRating) {
        return (root, query, cb) -> {
            if (minRating == null) {
                return null;
            }

            Join<Book, Review> reviews = root.join("reviews");
            query.distinct(true);
            return cb.greaterThanOrEqualTo(reviews.get("rating"), minRating);
        };
    }

    private static Specification<Book> hasMaxRating(Integer maxRating) {
        return (root, query, cb) -> {
            if (maxRating == null) {
                return null;
            }

            Join<Book, Review> reviews = root.join("reviews");
            query.distinct(true);
            return cb.lessThanOrEqualTo(reviews.get("rating"), maxRating);
        };
    }

    private static Specification<Book> hasPublicationDate(LocalDate publicationDate) {
        return (root, query, cb) -> publicationDate == null ? null :
                cb.greaterThanOrEqualTo(root.get("publicationDate"), publicationDate);
    }
}
