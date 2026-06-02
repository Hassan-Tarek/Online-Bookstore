package com.bookstore.service.commerce;

import com.bookstore.entity.commerce.Promotion;
import com.bookstore.enums.PromotionScope;
import com.bookstore.enums.PromotionType;
import com.bookstore.exception.BadRequestException;
import com.bookstore.mapper.commerce.PromotionMapper;
import com.bookstore.repository.catalog.BookRepository;
import com.bookstore.repository.commerce.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion activeGlobalPromotion;

    @BeforeEach
    void setUp() {
        activeGlobalPromotion = Promotion.builder()
                .id(UUID.randomUUID())
                .title("Test Promo")
                .code("SAVE10")
                .type(PromotionType.PERCENTAGE)
                .scope(PromotionScope.GLOBAL)
                .value(new BigDecimal("10"))
                .usageLimit(100)
                .usedCount(0)
                .minCheckoutAmount(new BigDecimal("50.00"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .active(true)
                .build();
    }

    @Test
    void validateOrderPromotion_acceptsValidPromotion() {
        assertThatCode(() ->
                promotionService.validateOrderPromotion(activeGlobalPromotion, new BigDecimal("100.00")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateOrderPromotion_rejectsInactivePromotion() {
        activeGlobalPromotion.setActive(false);

        assertThatThrownBy(() ->
                promotionService.validateOrderPromotion(activeGlobalPromotion, new BigDecimal("100.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void validateOrderPromotion_rejectsExpiredPromotion() {
        activeGlobalPromotion.setEndDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() ->
                promotionService.validateOrderPromotion(activeGlobalPromotion, new BigDecimal("100.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateOrderPromotion_rejectsBookScopedPromotion() {
        activeGlobalPromotion.setScope(PromotionScope.BOOK);

        assertThatThrownBy(() ->
                promotionService.validateOrderPromotion(activeGlobalPromotion, new BigDecimal("100.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("global order promotion");
    }

    @Test
    void validateOrderPromotion_rejectsSubtotalBelowMinimum() {
        assertThatThrownBy(() ->
                promotionService.validateOrderPromotion(activeGlobalPromotion, new BigDecimal("25.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum checkout amount");
    }

    @Test
    void validateOrderPromotion_rejectsUsageLimitExceeded() {
        activeGlobalPromotion.setUsageLimit(5);
        activeGlobalPromotion.setUsedCount(5);

        assertThatThrownBy(() ->
                promotionService.validateOrderPromotion(activeGlobalPromotion, new BigDecimal("100.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("usage limit");
    }
}
