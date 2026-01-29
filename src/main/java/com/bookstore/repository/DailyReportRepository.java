package com.bookstore.repository;

import com.bookstore.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DailyReportRepository extends
        JpaRepository<DailyReport, UUID> {
}
