package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.OtpRecords;
import com.greenkitchen.portal.dtos.CustomerResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
import com.greenkitchen.portal.dtos.UpdateAvatarResponse;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.OtpRecordsRepository;
import com.greenkitchen.portal.utils.ImageUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.greenkitchen.portal.services.EmailService;
import com.greenkitchen.portal.services.GoogleAuthService;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CustomerServiceImpl implements CustomerService {

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private OtpRecordsRepository otpRecordsRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private GoogleAuthService googleAuthService;

  @Autowired
  private ImageUtils imageUtils;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public Customer findByEmail(String email) {
    return customerRepository.findByEmail(email);
  }

  @Override
  public Customer findOrCreateCustomerByPhone(String phoneNumber) {
    Customer existingCustomer = customerRepository.findByPhone(phoneNumber);

    if (existingCustomer != null) {
      return existingCustomer;
    }

    // Create new customer if not exists
    Customer newCustomer = new Customer();
    newCustomer.setPhone(phoneNumber);

    String generatedEmail = phoneNumber.replaceAll("[^0-9]", "") + "@greenkitchen.temp";
    newCustomer.setEmail(generatedEmail);
    newCustomer.setFirstName("User");
    newCustomer.setLastName(phoneNumber);
    newCustomer.setIsPhoneLogin(true);
    newCustomer.setIsActive(true);
    newCustomer.setIsEmailLogin(false);

    return customerRepository.save(newCustomer);
  }

  @Override
  public List<Customer> listAll() {
    return customerRepository.findAll();
  }


  @Override
  public PagedResponse<CustomerResponse> listFilteredPaged(String q, int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
    
    Page<Customer> customerPage = customerRepository.findFilteredPaged(q, pageable);

    PagedResponse<CustomerResponse> response = new PagedResponse<>();
    response.setItems(customerPage.getContent().stream()
        .map(this::toCustomerResponse)
        .collect(Collectors.toList()));
    response.setTotal(customerPage.getTotalElements());
    response.setPage(page);
    response.setSize(size);

    return response;
  }

  private CustomerResponse toCustomerResponse(Customer customer) {
    CustomerResponse response = new CustomerResponse();
    response.setId(customer.getId());
    response.setFirstName(customer.getFirstName());
    response.setLastName(customer.getLastName());
    response.setFullName(customer.getFullName());
    response.setAvatar(customer.getAvatar());
    response.setEmail(customer.getEmail());
    response.setBirthDate(customer.getBirthDate());
    response.setGender(customer.getGender());
    response.setPhone(customer.getPhone());
    response.setIsActive(customer.getIsActive());
    response.setIsPhoneLogin(customer.getIsPhoneLogin());
    response.setIsEmailLogin(customer.getIsEmailLogin());
    response.setOauthProvider(customer.getOauthProvider());
    response.setIsOauthUser(customer.getIsOauthUser());
    response.setCreatedAt(customer.getCreatedAt());
    response.setUpdatedAt(customer.getUpdatedAt());

    return response;
  }

  @Override
  public Customer save(Customer customer) {
    String hashedPassword = encoder.encode(customer.getPassword());
    customer.setPassword(hashedPassword);
    return customerRepository.save(customer);
  }

  @Override
  public Customer findById(Long id) {
    return customerRepository.findById(id).orElse(null);
  }

  @Override
  public Customer update(Customer customer) {
    Customer existingCustomer = findByEmail(customer.getEmail());
    if (existingCustomer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + customer.getEmail());
    }
    existingCustomer.setFirstName(customer.getFirstName());
    existingCustomer.setLastName(customer.getLastName());
    existingCustomer.setBirthDate(customer.getBirthDate());
    existingCustomer.setGender(customer.getGender());
    existingCustomer.setPhone(customer.getPhone());

    return customerRepository.save(existingCustomer);
  }

  @Override
  public void deleteById(Long id) {
    customerRepository.deleteById(id);
  }

  @Override
  public Customer checkLogin(String email, String password) {
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null || !encoder.matches(password, customer.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }
    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    return customer;
  }

  @Override
  public Customer registerCustomer(Customer customer) {
    Customer existingCustomer = customerRepository.findByEmail(customer.getEmail());
    if (existingCustomer != null) {
      throw new IllegalArgumentException("Email already registered: " + customer.getEmail());
    }
    customer.setPassword(encoder.encode(customer.getPassword()));
    String verifyToken = UUID.randomUUID().toString();
    customer.setVerifyToken(verifyToken);
    customer.setVerifyTokenExpireAt(LocalDateTime.now().plusMinutes(1));
    Customer savedCustomer = customerRepository.save(customer);
    // Send verification email
    emailService.sendVerificationEmail(
        customer.getEmail(),
        verifyToken);
    return savedCustomer;
  }

  @Override
  public Customer verifyEmail(String email, String token) {
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    if (customer.getIsActive()) {
      throw new IllegalArgumentException("Email already verified");
    }
    if (!customer.getVerifyToken().equals(token)) {
      throw new IllegalArgumentException("Invalid verification token");
    }
    if (customer.getVerifyTokenExpireAt() == null || LocalDateTime.now().isAfter(customer.getVerifyTokenExpireAt())) {
      throw new IllegalArgumentException("Verification token has expired");
    }
    customer.setIsActive(true);
    customer.setVerifyToken(null);
    customer.setVerifyTokenExpireAt(null);
    customerRepository.save(customer);
    return customer;
  }

  @Override
  public Customer resendVerifyEmail(String email) {
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    String verifyToken = UUID.randomUUID().toString();
    customer.setVerifyToken(verifyToken);
    customer.setVerifyTokenExpireAt(LocalDateTime.now().plusMinutes(1));
    customerRepository.save(customer);
    emailService.sendVerificationEmail(
        customer.getEmail(),
        verifyToken);
    return customer;
  }

  @Override
  public void sendOtpCode(String email) {

    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }
    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Please verify your email first before requesting OTP");
    }
    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    // Check if there's already a recent OTP request within 5 minutes
    LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
    OtpRecords recentOtpRecord = otpRecordsRepository.findRecentOtpByEmailAndTime(email, fiveMinutesAgo);

    if (recentOtpRecord != null) {
      throw new IllegalArgumentException("You have requested OTP too frequently. Please try again later.");
    }

    // Generate 6-digit random OTP
    String otpCode = generateRandomOtp();
    // Set expiration time (5 minutes from now)
    LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
    // Create and save OTP record
    OtpRecords otpRecord = new OtpRecords();
    otpRecord.setEmail(email);
    otpRecord.setOtpCode(otpCode);
    otpRecord.setExpiredAt(expiredAt);
    otpRecordsRepository.save(otpRecord);

    // Send OTP via email
    emailService.sendOtpEmail(email, otpCode);
  }

  @Override
  public boolean verifyOtpCode(String email, String otpCode) {

    OtpRecords otpRecord = otpRecordsRepository.findByEmailAndOtpCode(email, otpCode);

    if (otpRecord == null)
      throw new IllegalArgumentException("Invalid OTP code");
    if (otpRecord.isExpired())
      throw new IllegalArgumentException("OTP code has expired");
    if (otpRecord.getIsUsed())
      throw new IllegalArgumentException("OTP code has already been used");
    // Mark OTP as used
    otpRecord.setIsUsed(true);
    otpRecordsRepository.save(otpRecord);

    return true;
  }

  @Override
  public void resetPassword(String email, String newPassword) {
    // Find customer
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }

    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email first.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    // Update password
    String hashedPassword = encoder.encode(newPassword);
    customer.setPassword(hashedPassword);
    customerRepository.save(customer);

    // Mark all other OTPs as used for security
    otpRecordsRepository.markAllOtpAsUsedByEmail(email);
  }

  @Override
  public void changePassword(String email, String oldPassword, String newPassword) {
    // Find customer
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }

    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email first.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    // Verify old password
    if (!encoder.matches(oldPassword, customer.getPassword())) {
      throw new IllegalArgumentException("Current password is incorrect");
    }

    // Update password
    String hashedNewPassword = encoder.encode(newPassword);
    customer.setPassword(hashedNewPassword);
    customer.setPasswordUpdatedAt(LocalDateTime.now());
    customerRepository.save(customer);
  }

  @Override
  public void unlinkGoogle(String email) {
    // Find customer
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }

    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email first.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    // Check if account is linked with Google
    if (!customer.getIsOauthUser() || !"google".equals(customer.getOauthProvider())) {
      throw new IllegalArgumentException("Account is not linked with Google");
    }

    // Unlink Google account
    customer.setOauthProvider(null);
    customer.setOauthProviderId(null);
    customer.setIsOauthUser(false);

    customerRepository.save(customer);
  }

  @Override
  public void linkGoogle(String email, String idToken) {
    // Find customer
    Customer customer = customerRepository.findByEmail(email);
    if (customer == null) {
      throw new IllegalArgumentException("Customer not found with email: " + email);
    }

    if (!customer.getIsActive()) {
      throw new IllegalArgumentException("Account is not active. Please verify your email first.");
    }

    if (customer.getIsDeleted()) {
      throw new IllegalArgumentException("Account is deleted. Please contact support.");
    }

    // Check if account is already linked with Google
    if (customer.getIsOauthUser() && "google".equals(customer.getOauthProvider())) {
      throw new IllegalArgumentException("Account is already linked with Google");
    }

    try {
      // Verify Google token and get Google user info
      Customer googleUser = googleAuthService.authenticateGoogleUser(idToken);

      // Link Google account to existing customer
      customer.setOauthProvider("google");
      customer.setOauthProviderId(googleUser.getOauthProviderId());
      customer.setIsOauthUser(true);

      customerRepository.save(customer);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to link Google account: " + e.getMessage());
    }
  }

  @Override
  public UpdateAvatarResponse updateAvatar(String email, MultipartFile file) {
    try {
      // Kiểm tra file có tồn tại không
      if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("Image file is required");
      }

      // Tìm customer theo email
      Customer customer = findByEmail(email);
      if (customer == null) {
        throw new IllegalArgumentException("Customer not found with email: " + email);
      }

      // Xóa ảnh cũ nếu có
      if (customer.getAvatar() != null && !customer.getAvatar().isEmpty()) {
        imageUtils.deleteImage(customer.getAvatar());
      }

      // Upload ảnh mới
      String imageUrl = imageUtils.uploadImage(file);

      // Cập nhật avatar
      customer.setAvatar(imageUrl);
      customerRepository.save(customer);

      // Trả về response
      return new UpdateAvatarResponse(email, imageUrl);

    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to update avatar: " + e.getMessage());
    }
  }

  private String generateRandomOtp() {
    Random random = new Random();
    int otp = 100000 + random.nextInt(900000); // Generate 6-digit OTP
    return String.valueOf(otp);
  }

}
