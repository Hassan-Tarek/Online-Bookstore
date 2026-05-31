package com.bookstore.util;

import com.bookstore.config.AppProperties;
import com.bookstore.entity.catalog.Book;
import com.bookstore.entity.commerce.Cart;
import com.bookstore.entity.commerce.CartItem;
import com.bookstore.entity.commerce.Promotion;
import com.bookstore.enums.ShippingMethod;
import com.bookstore.repository.commerce.PromotionRepository;
import com.bookstore.service.commerce.PromotionService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class PriceUtils {

    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;
    private final AppProperties appProperties;

    @Named("rectifySubtotal")
    public BigDecimal calculateSubtotal(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            Book book = item.getBook();
            Promotion promotion = promotionRepository.findActiveByBookId(book.getId())
                    .orElse(null);
            BigDecimal discount = calculateDiscountValue(promotion, book.getPrice());
            BigDecimal itemPriceSum = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal itemDiscountSum = discount.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemPriceSum).subtract(itemDiscountSum);
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(BigDecimal subtotal, BigDecimal shippingFee, BigDecimal orderDiscount) {
        BigDecimal taxableAmount = subtotal.add(shippingFee).subtract(orderDiscount);
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxableAmount = BigDecimal.ZERO;
        }
        BigDecimal tax = appProperties.settings().tax();
        BigDecimal taxRate = tax.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return taxableAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateShippingFee(ShippingMethod shippingMethod) {
        var shippingFees = appProperties.settings().shippingFees();
        return switch (shippingMethod) {
            case STANDARD -> shippingFees.standard();
            case EXPRESS -> shippingFees.express();
            case OVERNIGHT -> shippingFees.overnight();
            case FREE -> shippingFees.free();
        };
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal, Promotion promotion) {
        promotionService.validateOrderPromotion(promotion, subtotal);
        return calculateDiscountValue(promotion, subtotal);
    }

    public PriceSummary calculatePriceSummary(Cart cart, ShippingMethod shippingMethod, Promotion promotion) {
        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal shippingFee = calculateShippingFee(shippingMethod);
        BigDecimal discount = calculateDiscount(subtotal, promotion);
        BigDecimal tax = calculateTax(subtotal, shippingFee, discount);
        BigDecimal total = subtotal.add(shippingFee).subtract(discount).add(tax)
                .setScale(2, RoundingMode.HALF_UP);
        return new PriceSummary(subtotal, tax, shippingFee, discount, total);
    }

    @Named("rectifyItemFinalPrice")
    public BigDecimal calculateItemFinalPrice(CartItem cartItem) {
        Book book = cartItem.getBook();
        Promotion promotion = promotionRepository.findActiveByBookId(book.getId())
                .orElse(null);
        BigDecimal discount = calculateDiscountValue(promotion, book.getPrice());
        return book.getPrice().subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDiscountValue(Promotion promotion, BigDecimal base) {
        if (promotion == null)
            return BigDecimal.ZERO;
        return switch (promotion.getType()) {
            case FIXED -> promotion.getValue();
            case PERCENTAGE -> base.multiply(promotion.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        };
    }

    public record PriceSummary(
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal shippingFee,
            BigDecimal discount,
            BigDecimal total
    ) { }
}
