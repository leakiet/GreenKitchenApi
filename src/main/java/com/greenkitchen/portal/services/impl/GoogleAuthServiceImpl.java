package com.greenkitchen.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.services.GoogleAuthService;
import com.greenkitchen.portal.dtos.GoogleLoginMobileRequest;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    @Value("${google.client.id:}")
    private String googleClientId;

    @Autowired
    private CustomerRepository customerRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Customer authenticateGoogleUser(String idToken) {
        try {
            // Verify token với Google
            JsonNode tokenInfo = getGoogleTokenInfo(idToken);
            if (tokenInfo == null || !tokenInfo.has("email")) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            String email = tokenInfo.get("email").asText();
            String firstName = tokenInfo.has("given_name") ? tokenInfo.get("given_name").asText() : "";
            String lastName = tokenInfo.has("family_name") ? tokenInfo.get("family_name").asText() : "";
            String googleId = tokenInfo.get("sub").asText();

            // Tìm user existing hoặc tạo mới
            Customer customer = customerRepository.findByEmail(email);
            
            if (customer == null) {
                // Tạo customer mới cho Google user
                customer = new Customer();
                customer.setEmail(email);
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                customer.setOauthProvider("google");
                customer.setOauthProviderId(googleId);
                customer.setIsOauthUser(true);
                customer.setIsActive(true); // OAuth users auto active
                customer.setIsEmailLogin(true);
                customerRepository.save(customer);
            } else {
                // Update OAuth info cho existing user (nếu chưa có)
                if (customer.getOauthProvider() == null) {
                    customer.setOauthProvider("google");
                    customer.setOauthProviderId(googleId);
                    customer.setIsOauthUser(true);
                    customer.setIsEmailLogin(true);
                    customerRepository.save(customer);
                }
            }

            return customer;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to authenticate Google user: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyGoogleToken(String idToken) {
        try {
            JsonNode tokenInfo = getGoogleTokenInfo(idToken);
            return tokenInfo != null && tokenInfo.has("email");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Customer authenticateGoogleUserWithUserInfo(GoogleLoginMobileRequest request) {
        try {
            // Extract user information từ request
            String email = request.getEmail();
            String googleId = request.getId();
            String firstName = request.getGivenName();
            String lastName = request.getFamilyName();
            String picture = request.getPicture();

            // Validate required fields
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (googleId == null || googleId.trim().isEmpty()) {
                throw new IllegalArgumentException("Google ID is required");
            }
            // Tìm user existing hoặc tạo mới
            Customer customer = customerRepository.findByEmail(email);

            if (customer == null) {
                // Tạo customer mới cho Google user
                customer = new Customer();
                customer.setEmail(email);

                // Set name từ firstName + lastName hoặc từ name field
                if (firstName != null && !firstName.trim().isEmpty()) {
                    customer.setFirstName(firstName);
                }

                if (lastName != null && !lastName.trim().isEmpty()) {
                    customer.setLastName(lastName);
                }

                customer.setAvatar(picture);
                customer.setOauthProvider("google");
                customer.setOauthProviderId(googleId);
                customer.setIsOauthUser(true);
                customer.setIsActive(true); // OAuth users auto active
                customer.setIsEmailLogin(true);
                customer = customerRepository.save(customer);
                System.out.println("Created new Google user: " + email);
            } else {
                // Update OAuth info cho existing user (nếu chưa có)
                if (customer.getOauthProvider() == null) {
                    customer.setOauthProvider("google");
                    customer.setOauthProviderId(googleId);
                    customer.setIsOauthUser(true);
                    customer = customerRepository.save(customer);
                    System.out.println("Updated existing user with Google OAuth info: " + email);
                } else {
                    System.out.println("Found existing Google user: " + email);
                }
            }

            return customer;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to authenticate Google user with userInfo: " + e.getMessage());
        }
    }

    private JsonNode getGoogleTokenInfo(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode tokenInfo = objectMapper.readTree(response.getBody());
                
                // Verify audience (client ID)
                if (googleClientId != null && !googleClientId.isEmpty()) {
                    String aud = tokenInfo.get("aud").asText();
                    if (!googleClientId.equals(aud)) {
                        return null; // Invalid audience
                    }
                }
                
                return tokenInfo;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
