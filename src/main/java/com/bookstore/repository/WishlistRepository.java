package com.bookstore.repository;

import com.bookstore.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WishlistRepository
        extends JpaRepository<Wishlist, UUID> {
}
