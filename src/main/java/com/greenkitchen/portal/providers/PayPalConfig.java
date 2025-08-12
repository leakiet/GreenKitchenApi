package com.greenkitchen.portal.providers;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PayPalConfig {

    @Value("${app.paypal.client-id}")
    private String clientId;

    @Value("${app.paypal.client-secret}")
    private String clientSecret;

    @Value("${app.paypal.mode}")
    private String mode; // sandbox or live
    
    @Bean
    public Map<String, String> paypalSdkConfig() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("mode", mode);       
        return configMap;
    }
    
    @Bean
    public APIContext apiContext() {
        // Sử dụng constructor mới không bị deprecated
        APIContext context = new APIContext(clientId, clientSecret, mode);
        context.setConfigurationMap(paypalSdkConfig());
        return context;
    }
}
