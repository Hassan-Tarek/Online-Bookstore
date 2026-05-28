package com.bookstore.service.storage;

import com.bookstore.config.AppProperties;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final AppProperties appProperties;

    public Map<String, String> uploadImage(MultipartFile file) {
        try {
            log.info("Uploading image to Cloudinary");
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", appProperties.integration().cloudinary().folder(),
                            "resource_type", "image"
                    ));
            Map<String, String> result = new HashMap<>();
            result.put("secure_url", uploadResult.get("secure_url").toString());
            result.put("public_id", uploadResult.get("public_id").toString());
            return result;
        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public void deleteImage(String imagePublicId) {
        try {
            log.info("Deleting image {}", imagePublicId);
            cloudinary.uploader().destroy(
                    imagePublicId,
                    ObjectUtils.asMap(
                            "resource_type", "image"
                    ));
        } catch (IOException e) {
            log.error("Failed to delete image: {}", e.getMessage());
            throw new RuntimeException("Failed to delete image", e);
        }
    }
}
