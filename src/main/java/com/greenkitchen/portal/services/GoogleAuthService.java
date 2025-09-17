package com.greenkitchen.portal.services;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.dtos.GoogleLoginMobileRequest;

public interface GoogleAuthService {
    Customer authenticateGoogleUser(String idToken);
    boolean verifyGoogleToken(String idToken);
    Customer authenticateGoogleUserWithUserInfo(GoogleLoginMobileRequest request);
}
