package com.bookstore.repository.catalog;

import com.bookstore.entity.catalog.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeriesRepository extends
        JpaRepository<Series, UUID> {
}
