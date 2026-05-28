package com.bookstore.repository.catalog;

import com.bookstore.entity.catalog.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthorRepository extends
        JpaRepository<Author, UUID> {

    @Query(value = "SELECT DISTINCT a FROM Author a JOIN a.followers f WHERE f.id = :userId",
            countQuery = "SELECT COUNT(DISTINCT a.id) FROM Author a JOIN a.followers f WHERE f.id = :userId")
    Page<Author> findFollowedAuthorsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Query(value = "INSERT INTO author_followers (author_id, user_id) VALUES (:authorId, :userId) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    int follow(@Param("authorId") UUID authorId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Author a SET a.followersCount = a.followersCount + 1 WHERE a.id = :authorId")
    void incrementFollowersCount(@Param("authorId") UUID authorId);

    @Modifying
    @Query(value = "DELETE FROM author_followers WHERE author_id = :authorId AND user_id = :userId",
            nativeQuery = true)
    int unfollow(@Param("authorId") UUID authorId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Author a SET a.followersCount = a.followersCount - 1 WHERE a.id = :authorId AND a.followersCount > 0")
    void decrementFollowersCount(@Param("authorId") UUID authorId);
}
