package com.greenkitchen.portal.providers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryProvider {

    @Value("${CLOUDINARY_CLOUD_NAME:}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY:}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET:}")
    private String apiSecret;

    @Bean
    public Cloudinary getCloudinary() {
        // If any of the required properties are missing, create a dummy Cloudinary instance
        if (cloudName == null || cloudName.trim().isEmpty() || 
            apiKey == null || apiKey.trim().isEmpty() || 
            apiSecret == null || apiSecret.trim().isEmpty()) {
            
            // Create a dummy configuration for development
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dummy-cloud");
            config.put("api_key", "dummy-key");
            config.put("api_secret", "dummy-secret");
            config.put("secure", true);
            return new Cloudinary(config);
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
