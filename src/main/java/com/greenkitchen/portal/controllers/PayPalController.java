package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.CreatePayPalOrderRequest;
import com.greenkitchen.portal.utils.CurrencyService;
import com.greenkitchen.portal.dtos.CapturePayPalOrderRequest;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.Base64;

@RestController
@RequestMapping("/apis/v1/paypal")
public class PayPalController {

    @Autowired
    private APIContext apiContext;
    
    @Autowired
    private CurrencyService currencyService;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${app.paypal.client-id}")
    private String paypalClientId;
    
    @Value("${app.paypal.client-secret}")
    private String paypalClientSecret;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreatePayPalOrderRequest request) {
        try {
            // Xử lý chuyển đổi tiền tệ nếu cần
            String currency = request.getCurrency();
            String amount = request.getAmount();
            
            // Nếu currency là VND, chuyển đổi sang USD cho PayPal
            if ("VND".equalsIgnoreCase(currency)) {
                BigDecimal vndAmount = new BigDecimal(amount);
                BigDecimal usdAmount = currencyService.convertVndToUsd(vndAmount);
                amount = usdAmount.toString();
                currency = "USD";
                
                System.out.println("Chuyển đổi: " + currencyService.formatVnd(vndAmount) + 
                    " -> " + currencyService.formatUsd(usdAmount));
            }
            
            // Create payment amount
            Amount paymentAmount = new Amount();
            paymentAmount.setCurrency(currency);
            paymentAmount.setTotal(amount);

            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setDescription(request.getDescription());
            transaction.setAmount(paymentAmount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            // Create payer
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            // Create payment
            Payment payment = new Payment();
            payment.setIntent("sale");
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            // Set redirect URLs
            RedirectUrls redirectUrls = new RedirectUrls();
            // Use first URL from comma-separated list
            String baseUrl = frontendUrl.split(",")[0];
            redirectUrls.setCancelUrl(baseUrl + "/checkout?cancel=true");
            redirectUrls.setReturnUrl(baseUrl + "/checkout?success=true");
            payment.setRedirectUrls(redirectUrls);

            // Create payment
            Payment createdPayment = payment.create(apiContext);

            // Extract approval URL
            String approvalUrl = null;
            for (Links link : createdPayment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    approvalUrl = link.getHref();
                    break;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", createdPayment.getId());
            result.put("status", createdPayment.getState());
            result.put("approval_url", approvalUrl);
            
            return ResponseEntity.ok(result);
            
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating PayPal payment: " + e.getMessage());
        }
    }

    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(@RequestBody CapturePayPalOrderRequest request) {
        try {            
            // Sử dụng PayPal Orders API v2 để capture
            String captureUrl = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + request.getOrderID() + "/capture";
            
            // Get access token
            String accessToken = getPayPalAccessToken();
            if (accessToken == null) {
                throw new RuntimeException("Could not get PayPal access token");
            }
            
            // Create HTTP request to capture order
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("PayPal-Request-Id", UUID.randomUUID().toString());
            
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(captureUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ PayPal capture successful: " + response.getBody());
                System.out.println("✅ Payment record was already created in createOrder - no need to create again");

                Map<String, Object> result = new HashMap<>();
                result.put("id", request.getOrderID());
                result.put("status", "COMPLETED");
                result.put("orderId", request.getOrderId());
                result.put("message", "Payment captured successfully");
                result.put("paymentDetails", response.getBody());
                
                return ResponseEntity.ok(result);
            } else {
                throw new RuntimeException("PayPal capture failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.err.println("PayPal capture error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "PayPal capture failed");
            errorResult.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
    
    private String getPayPalAccessToken() {
        try {
            String tokenUrl = "https://api-m.sandbox.paypal.com/v1/oauth2/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            if (paypalClientId == null || paypalClientSecret == null) {
                System.err.println("PayPal credentials not found in Spring properties");
                return null;
            }
            
            String auth = Base64.getEncoder().encodeToString((paypalClientId + ":" + paypalClientSecret).getBytes());
            headers.set("Authorization", "Basic " + auth);
            
            String body = "grant_type=client_credentials";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, entity, String.class);

            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse JSON response to get access token
                String responseBody = response.getBody();
                // Simple JSON parsing for access_token
                if (responseBody.contains("access_token")) {
                    String[] parts = responseBody.split("\"access_token\":\"");
                    if (parts.length > 1) {
                        String token = parts[1].split("\"")[0];
                        System.out.println("PayPal access token obtained successfully");
                        return token;
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error getting PayPal access token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/exchange-rate")
    public ResponseEntity<?> getExchangeRate() {
        try {
            BigDecimal rate = currencyService.getUsdToVndRate();
            Map<String, Object> result = new HashMap<>();
            result.put("usdToVnd", rate);
            result.put("message", "Tỷ giá USD sang VND");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting exchange rate: " + e.getMessage());
        }
    }
}
