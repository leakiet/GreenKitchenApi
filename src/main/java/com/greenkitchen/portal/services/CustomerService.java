package com.greenkitchen.portal.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.CustomerResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.dtos.UpdateAvatarResponse;
import com.greenkitchen.portal.entities.Customer;

public interface CustomerService {
  Customer findByEmail(String email);
  Customer findOrCreateCustomerByPhone(String phoneNumber);
  List<Customer> listAll();
  List<Customer> findActiveCustomers();
  PagedResponse<CustomerResponse> listFilteredPaged(String q, int page, int size);
  Customer update(Customer customer);
  Customer save(Customer customer);
  Customer findById(Long id);
  void deleteById(Long id);
  Customer registerCustomer(Customer customer);
  Customer verifyEmail(String email, String verifyToken);
  Customer resendVerifyEmail(String email);
  Customer checkLogin(String email, String password);
  void sendOtpCode(String email);
  boolean verifyOtpCode(String email, String otpCode);
  void resetPassword(String email, String newPassword);
  void changePassword(String email, String oldPassword, String newPassword);
  void unlinkGoogle(String email);
  void linkGoogle(String email, String idToken);
  UpdateAvatarResponse updateAvatar(String email, MultipartFile file);
}
