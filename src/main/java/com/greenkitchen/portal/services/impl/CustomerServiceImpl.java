package com.greenkitchen.portal.services.impl;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.OtpRecords;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.OtpRecordsRepository;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.greenkitchen.portal.services.EmailService;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class CustomerServiceImpl implements CustomerService {

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private OtpRecordsRepository otpRecordsRepository;

  @Autowired
  private EmailService emailService;

  private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Override
  public Customer findByEmail(String email) {
    return customerRepository.findByEmail(email);
  }

  @Override
  public List<Customer> listAll() {
    return customerRepository.findAll();
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
    Customer existingCustomer = findById(customer.getId());
    if (existingCustomer == null) {
      throw new IllegalArgumentException("Customer not found with id: " + customer.getId());
    }
    existingCustomer.setFirstName(customer.getFirstName());
    existingCustomer.setLastName(customer.getLastName());
    existingCustomer.setEmail(customer.getEmail());
    existingCustomer.setBirthDate(customer.getBirthDate());
    existingCustomer.setGender(customer.getGender());
    existingCustomer.setPhone(customer.getPhone());
    existingCustomer.setAddress(customer.getAddress());

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
        verifyToken
      );
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
    if(customer.getIsDeleted()) {
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

    if (otpRecord == null) throw new IllegalArgumentException("Invalid OTP code");
    if (otpRecord.isExpired()) throw new IllegalArgumentException("OTP code has expired");
    if (otpRecord.getIsUsed()) throw new IllegalArgumentException("OTP code has already been used");
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

  private String generateRandomOtp() {
    Random random = new Random();
    int otp = 100000 + random.nextInt(900000); // Generate 6-digit OTP
    return String.valueOf(otp);
  }

}
