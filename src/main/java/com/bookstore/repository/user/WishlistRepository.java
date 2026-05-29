package com.bookstore.repository.user;

import com.bookstore.entity.user.Wishlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends
        JpaRepository<Wishlist, UUID> {

    @EntityGraph(attributePaths = { "wishlistItems", "wishlistItems.book" })
    Optional<Wishlist> findByUserId(UUID userId);
}
