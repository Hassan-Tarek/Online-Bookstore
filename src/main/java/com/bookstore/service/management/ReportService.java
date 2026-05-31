package com.bookstore.service.management;

import com.bookstore.dto.management.response.BookReportResponse;
import com.bookstore.dto.management.response.SalesReportResponse;
import com.bookstore.dto.management.response.UserReportResponse;
import com.bookstore.enums.ReportFormat;
import com.bookstore.repository.commerce.OrderRepository;
import com.bookstore.repository.commerce.projection.OrderStatsProjection;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;

    public SalesReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        OrderStatsProjection stats = orderRepository.findOrderStats(startDateTime, endDateTime);
        long itemsCount = orderRepository.findTotalItemsByStartDateAndEndDate(startDateTime, endDateTime);
        return new SalesReportResponse(
                startDateTime.toLocalDate(),
                endDateTime.toLocalDate(),
                stats.getTotalOrders(),
                itemsCount,
                stats.getTotalRevenue(),
                stats.getAverageOrderValue()
        );
    }

    public List<BookReportResponse> getTopSellersReport(int limit) {
        return orderRepository.findTopSellers(PageRequest.of(0, limit))
                .stream()
                .map(projection -> new BookReportResponse(
                        projection.getIsbn(),
                        projection.getTitle(),
                        projection.getTotalQuantitySold(),
                        projection.getTotalRevenue()
                ))
                .toList();
    }

    public List<UserReportResponse> getTopCustomersReport(int limit) {
        return orderRepository.findTopCustomers(PageRequest.of(0, limit))
                .stream()
                .map(projection -> new UserReportResponse(
                        projection.getFirstName(),
                        projection.getLastName(),
                        projection.getEmail(),
                        projection.getTotalOrders(),
                        projection.getTotalSpent()
                ))
                .toList();
    }

    public byte[] exportSalesReport(LocalDate startDate, LocalDate endDate, ReportFormat format) {
        try {
            SalesReportResponse response = getSalesReport(startDate, endDate);

            String templateName = "reports/sales-report.jrxml";
            InputStream reportStream = new ClassPathResource(templateName).getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("startDate", response.getStartDate());
            parameters.put("endDate", response.getEndDate());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(List.of(response));
            return exportReport(jasperReport, parameters, dataSource, format);
        } catch (IOException | JRException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] exportTopSellersReport(int limit, ReportFormat format) {
        try {
            List<BookReportResponse> responses = getTopSellersReport(limit);
            String templateName = "reports/top-sellers-report.jrxml";
            InputStream reportStream = new ClassPathResource(templateName).getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(responses);
            return exportReport(jasperReport, Map.of(), dataSource, format);
        } catch (JRException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] exportTopCustomersReport(int limit, ReportFormat format) {
        try {
            List<UserReportResponse> responses = getTopCustomersReport(limit);
            String templateName = "reports/top-customers-report.jrxml";
            InputStream reportStream = new ClassPathResource(templateName).getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(responses);
            return exportReport(jasperReport, Map.of(), dataSource, format);
        } catch (JRException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] exportReport(JasperReport jasperReport, Map<String, Object> parameters,
                                JRBeanCollectionDataSource dataSource, ReportFormat format) {
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            return switch (format) {
                case PDF -> JasperExportManager.exportReportToPdf(jasperPrint);
                case XML -> JasperExportManager.exportReportToXml(jasperPrint).getBytes(StandardCharsets.UTF_8);
                case HTML -> {
                    HtmlExporter exporter = new HtmlExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
                    exporter.exportReport();
                    yield outputStream.toByteArray();
                }
            };
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }
}
