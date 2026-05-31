package com.bookstore.controller.management;

import com.bookstore.dto.management.response.BookReportResponse;
import com.bookstore.dto.management.response.SalesReportResponse;
import com.bookstore.dto.management.response.UserReportResponse;
import com.bookstore.enums.ReportFormat;
import com.bookstore.service.management.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping(path = "/sales", produces = "application/json")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        var response = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/top-sellers", produces = "application/json")
    public ResponseEntity<List<BookReportResponse>> getTopSellersReport(
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int limit) {
        var responses = reportService.getTopSellersReport(limit);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/top-customers", produces = "application/json")
    public ResponseEntity<List<UserReportResponse>> getTopCustomersReport(
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int limit) {
        var responses = reportService.getTopCustomersReport(limit);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/export/sales")
    public ResponseEntity<byte[]> exportSalesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam ReportFormat format) {
        byte[] response = reportService.exportSalesReport(startDate, endDate, format);
        return createExportResponse(response, "sales-report", format);
    }

    @GetMapping(path = "/export/top-sellers")
    public ResponseEntity<byte[]> exportTopSellersReport(
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int limit,
            @RequestParam ReportFormat format) {
        byte[] response = reportService.exportTopSellersReport(limit, format);
        return createExportResponse(response, "top-sellers-report", format);
    }

    @GetMapping(path = "/export/top-customers")
    public ResponseEntity<byte[]> exportTopCustomersReport(
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int limit,
            @RequestParam ReportFormat format) {
        byte[] response = reportService.exportTopCustomersReport(limit, format);
        return createExportResponse(response, "top-customers-report", format);
    }

    private ResponseEntity<byte[]> createExportResponse(byte[] response, String filename, ReportFormat format) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename + "." + format.name().toLowerCase())
                .contentType(resolveMediaType(format))
                .contentLength(response.length)
                .body(response);
    }

    private MediaType resolveMediaType(ReportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case XML -> MediaType.APPLICATION_XML;
            case HTML -> MediaType.TEXT_HTML;
        };
    }
}
