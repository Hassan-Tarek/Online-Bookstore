package com.bookstore.repository.catalog;

import com.bookstore.entity.catalog.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends
        JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    @Query(value = """
        SELECT b
        FROM Book AS b LEFT JOIN b.reviews AS r
        GROUP BY b.id
        ORDER BY
            COALESCE(AVG(r.rating), 0) DESC,
            COUNT(DISTINCT r.id)
    """,
    countQuery = """
        SELECT COUNT(b.id)
        FROM Book b
    """)
    Page<Book> findTopRated(Pageable pageable);

    Page<Book> findAllByOrderByPublicationDateDesc(Pageable pageable);

    @EntityGraph(attributePaths = { "series", "categories", "authors" })
    Optional<Book> findWithSeriesAndCategoriesAndAuthorsById(UUID id);

    @Query("SELECT b FROM Book AS b JOIN b.authors AS a WHERE a.id = :authorId")
    Page<Book> findAllByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);

    @Query("SELECT b FROM Book AS b JOIN b.categories AS c WHERE c.id = :categoryId")
    Page<Book> findAllByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    Page<Book> findAllBySeriesId(UUID seriesId, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE Book b
        SET b.averageRating = b.averageRating + (:newRating - b.averageRating) / (b.ratingCount + 1),
        b.ratingCount = b.ratingCount + 1
        WHERE b.id = :bookId
    """)
    void incrementRating(@Param("bookId") UUID bookId, @Param("newRating") int newRating);

    @Modifying
    @Query("""
        UPDATE Book b
        SET b.averageRating = CASE
            WHEN (b.ratingCount - 1) <= 0 THEN 0.0
            ELSE ((b.averageRating * b.ratingCount) - :oldRating) / (b.ratingCount - 1)
        END,
        b.ratingCount = b.ratingCount - 1
        WHERE b.id = :bookId
    """)
    void decrementRating(@Param("bookId") UUID bookId, @Param("oldRating") int oldRating);

    @Modifying
    @Query("UPDATE Book b SET b.averageRating = (b.averageRating - :oldRating + :newRating) / b.ratingCount")
    void updateRating(@Param("bookId") UUID bookId, @Param("oldRating") int oldRating, @Param("newRating") int newRating);
}
