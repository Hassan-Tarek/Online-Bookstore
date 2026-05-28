package com.bookstore.config;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final AppProperties appProperties;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", appProperties.integration().cloudinary().cloudName());
        config.put("api_key", appProperties.integration().cloudinary().apiKey());
        config.put("api_secret", appProperties.integration().cloudinary().apiSecret());
        return new Cloudinary(config);
    }
}
