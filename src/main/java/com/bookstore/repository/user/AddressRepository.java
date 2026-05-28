package com.bookstore.repository.user;

import com.bookstore.entity.user.Address;
import com.bookstore.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends
        JpaRepository<Address, UUID> {

    Page<Address> findAllByUser(User user, Pageable pageable);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    Optional<Address> findFirstByUser(User user);

    boolean existsByUser(User user);

    @Modifying
    @Query("UPDATE Address AS a SET a.isDefault = FALSE WHERE a.user.id = :userId AND a.isDefault = TRUE")
    void unsetCurrentDefault(@Param("userId") UUID userId);
}
