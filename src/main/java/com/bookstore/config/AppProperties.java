package com.bookstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Settings settings,
        Frontend frontend,
        Ttl ttl,
        Security security,
        Integration integration
) {
    public record Settings(String admin, BigDecimal tax, ShippingFees shippingFees, String currency, Duration returnPeriod) { }
    public record ShippingFees(BigDecimal standard, BigDecimal express, BigDecimal overnight, BigDecimal free) { }

    public record Frontend(String url, String verificationPath, String passwordResetPath) { }
    
    public record Ttl(Auth auth, Cache cache) { }
    public record Auth(Duration access, Duration refresh, Duration verification, Duration passwordReset) { }
    public record Cache(Duration catalog) { }

    public record Security(Cors cors, Jwt jwt) { }
    public record Cors(List<String> origins, List<String> methods, List<String> headers) { }
    public record Jwt(String secretKey, Duration accessTokenTtl, Duration refreshTokenTtl,
                      RefreshTokenCookie refreshTokenCookie) { }
    public record RefreshTokenCookie(String name, String domain, Boolean secure, String path,
                                     String sameSite, Duration maxAge) { }

    public record Integration(Cloudinary cloudinary, Stripe stripe) { }
    public record Cloudinary(String cloudName, String apiKey, String apiSecret, String folder) { }
    public record Stripe(String secretKey, String webhookSecret) { }
}
