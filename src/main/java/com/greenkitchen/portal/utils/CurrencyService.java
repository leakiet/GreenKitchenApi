package com.greenkitchen.portal.utils;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CurrencyService {
    
    private static final String EXCHANGE_API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    private static final BigDecimal DEFAULT_USD_TO_VND_RATE = new BigDecimal("24000");
    
    /**
     * Chuyển đổi từ VND sang USD
     * @param vndAmount số tiền VND
     * @return số tiền USD
     */
    public BigDecimal convertVndToUsd(BigDecimal vndAmount) {
        try {
            BigDecimal exchangeRate = getUsdToVndRate();
            return vndAmount.divide(exchangeRate, 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            // Sử dụng tỷ giá mặc định nếu không lấy được tỷ giá thực
            return vndAmount.divide(DEFAULT_USD_TO_VND_RATE, 2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Lấy tỷ giá USD sang VND từ API
     * @return tỷ giá USD sang VND
     */
    @SuppressWarnings("unchecked")
    public BigDecimal getUsdToVndRate() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(EXCHANGE_API_URL, Map.class);
            
            if (response != null && response.containsKey("rates")) {
                Map<String, Object> rates = (Map<String, Object>) response.get("rates");
                if (rates.containsKey("VND")) {
                    Number vndRate = (Number) rates.get("VND");
                    return new BigDecimal(vndRate.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể lấy tỷ giá từ API: " + e.getMessage());
        }
        
        return DEFAULT_USD_TO_VND_RATE;
    }
    
    /**
     * Format tiền VND
     * @param amount số tiền
     * @return chuỗi định dạng VND
     */
    public String formatVnd(BigDecimal amount) {
        return String.format("%,.0f₫", amount);
    }
    
    /**
     * Format tiền USD
     * @param amount số tiền
     * @return chuỗi định dạng USD
     */
    public String formatUsd(BigDecimal amount) {
        return String.format("$%.2f", amount);
    }
}
