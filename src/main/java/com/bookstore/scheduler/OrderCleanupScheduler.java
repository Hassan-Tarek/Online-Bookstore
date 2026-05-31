package com.bookstore.scheduler;

import com.bookstore.service.commerce.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {

    private final OrderService orderService;

    @Scheduled(cron = "0 */15 * * * *")
    public void cleanupAbandonedOrders() {
        log.info("Starting cron job: Abandoned orders cleanup");
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        orderService.processAbandonedOrdersCleanup(cutoff);
        log.info("Successfully cancelled abandoned orders and restored relevant book inventory items.");
    }
}
