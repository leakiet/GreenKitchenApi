package com.greenkitchen.portal.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.EmailRequest;
import com.greenkitchen.portal.dtos.GoogleLoginRequest;
import com.greenkitchen.portal.dtos.LoginRequest;
import com.greenkitchen.portal.dtos.LoginResponse;
import com.greenkitchen.portal.dtos.PhoneLoginRequest;
import com.greenkitchen.portal.dtos.RegisterRequest;
import com.greenkitchen.portal.dtos.RegisterResponse;
import com.greenkitchen.portal.dtos.ResetPasswordRequest;
import com.greenkitchen.portal.dtos.VerifyRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.security.MyUserDetails;
import com.greenkitchen.portal.security.JwtUtils;
import com.greenkitchen.portal.security.MyUserDetailService;
import com.greenkitchen.portal.services.CustomerService;
import com.greenkitchen.portal.services.EmployeeService;
import com.greenkitchen.portal.services.GoogleAuthService;
import com.greenkitchen.portal.services.FirebaseAuthService;

import jakarta.validation.Valid;

import com.greenkitchen.portal.entities.Employee;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/apis/v1/auth")
public class AuthController {
  @Autowired
  private CustomerService customerService;

  @Autowired
  private EmployeeService employeeService;

  @Autowired
  private GoogleAuthService googleAuthService;

  @Autowired
  private FirebaseAuthService firebaseAuthService;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private MyUserDetailService userDetailService;

  @Autowired
  private ModelMapper mapper;

  @Autowired
  private AuthenticationManager authenticationManager;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> loginCustomer(@RequestBody LoginRequest request, HttpServletResponse httpResponse) {
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Username or password is incorrect");
    }
    Customer customer = customerService.checkLogin(request.getEmail(), request.getPassword());
    LoginResponse response = mapper.map(customer, LoginResponse.class);
    response.setRole("USER");
    response.setToken(jwtUtils.generateJwtToken(authentication));
    response.setRefreshToken(jwtUtils.generateRefreshToken(authentication)); // Thêm refresh token
    response.setTokenType("Bearer");

    // Lưu access token vào cookie
    Cookie accessTokenCookie = new Cookie("access_token", response.getToken());
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14 days
    httpResponse.addCookie(accessTokenCookie);

    // Lưu refresh token vào cookie
    Cookie refreshTokenCookie = new Cookie("refresh_token", response.getRefreshToken());
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days (lâu hơn access token)
    httpResponse.addCookie(refreshTokenCookie);

    return ResponseEntity.ok(response);

  }

  @DeleteMapping("/logout")
  public ResponseEntity<String> logoutCustomer(HttpServletResponse httpResponse) {
    // Xóa cookie access_token
    Cookie accessTokenCookie = new Cookie("access_token", null);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(0);
    httpResponse.addCookie(accessTokenCookie);

    // Xóa cookie refresh_token
    Cookie refreshTokenCookie = new Cookie("refresh_token", null);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(0);
    httpResponse.addCookie(refreshTokenCookie);

    return ResponseEntity.ok("Logout successful");
  }

  @PostMapping("/employee/login")
  public ResponseEntity<LoginResponse> employeeLogin(@RequestBody LoginRequest request) {
    try {
      Authentication authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      Employee employee = employeeService.findByEmail(request.getEmail());
      if (employee == null || !authentication.isAuthenticated()) {
        throw new IllegalArgumentException("Username or password is incorrect");
      }
      LoginResponse response = mapper.map(employee, LoginResponse.class);
      response.setToken(jwtUtils.generateJwtToken(authentication));
      response.setRefreshToken(jwtUtils.generateRefreshToken(authentication)); // Thêm refresh token cho employee
      response.setTokenType("Bearer");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Username or password is incorrect");
    }
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> registerCustomer(@Valid @RequestBody RegisterRequest registerRequest) {
    Customer customer = mapper.map(registerRequest, Customer.class);

    Customer registeredCustomer = customerService.registerCustomer(customer);

    RegisterResponse response = new RegisterResponse();
    response.setEmail(registeredCustomer.getEmail());
    response.setFirstName(registeredCustomer.getFirstName());
    response.setLastName(registeredCustomer.getLastName());
    response.setMessage("Register successfully");

    return ResponseEntity.ok(response);
  }

  @PutMapping("/verify")
  public ResponseEntity<String> verifyAccount(@RequestBody VerifyRequest request) {

    customerService.verifyEmail(request.getEmail(), request.getToken());

    return ResponseEntity.ok("Email verified successfully");
  }

  @PutMapping("/resendVerifyEmail")
  public ResponseEntity<String> resendVerifyEmail(@RequestBody EmailRequest request) {
    customerService.resendVerifyEmail(request.getEmail());
    return ResponseEntity.ok("Verification email resent!");
  }

  @PostMapping("/sendOtpCode")
  public ResponseEntity<String> sendOtpCode(@RequestBody EmailRequest request) {
    customerService.sendOtpCode(request.getEmail());
    return ResponseEntity.ok("OTP code sent successfully");
  }

  @PostMapping("/verifyOtpCode")
  public ResponseEntity<String> verifyOtpCode(@RequestBody VerifyRequest request) {
    boolean isValid = customerService.verifyOtpCode(request.getEmail(), request.getToken());
    if (!isValid) {
      throw new IllegalArgumentException("Invalid or expired OTP code");
    }
    return ResponseEntity.ok("OTP verified successfully");
  }
  

  @PostMapping("/resetPassword")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {  

    customerService.resetPassword(request.getEmail(), request.getPassword());
    
    return ResponseEntity.ok("Password reset successfully. Please login with your new password.");
  }

  @PostMapping("/google-login")
  public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request, HttpServletResponse httpResponse) {
    try {
      // Authenticate user với Google
      Customer customer = googleAuthService.authenticateGoogleUser(request.getIdToken());
      
      // Tạo MyUserDetails object cho customer
      MyUserDetails userDetails = new MyUserDetails(customer);
      
      // Generate JWT token
      Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
      String jwt = jwtUtils.generateJwtToken(authentication);
      String refreshToken = jwtUtils.generateRefreshToken(authentication);
      
      // Return response giống như login thường
      LoginResponse response = mapper.map(customer, LoginResponse.class);
      response.setRole("USER");
      response.setToken(jwt);
      response.setRefreshToken(refreshToken);
      response.setTokenType("Bearer");

      // Lưu access token vào cookie
      Cookie accessTokenCookie = new Cookie("access_token", response.getToken());
      accessTokenCookie.setHttpOnly(true);
      accessTokenCookie.setPath("/");
      accessTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14 days
      httpResponse.addCookie(accessTokenCookie);

      // Lưu refresh token vào cookie
      Cookie refreshTokenCookie = new Cookie("refresh_token", response.getRefreshToken());
      refreshTokenCookie.setHttpOnly(true);
      refreshTokenCookie.setPath("/");
      refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days (lâu hơn access token)
      httpResponse.addCookie(refreshTokenCookie);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Google login failed: " + e.getMessage());
    }
  }

  @PostMapping("/phone-login")
  public ResponseEntity<LoginResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request, HttpServletResponse httpResponse) {
    try {
      // Verify Firebase ID token
      if (!firebaseAuthService.verifyIdToken(request.getFirebaseIdToken())) {
        throw new IllegalArgumentException("Invalid Firebase ID token");
      }
      
      // Extract phone number from token or use request phone number
      String phoneNumber = firebaseAuthService.extractPhoneNumber(request.getFirebaseIdToken());
      if (phoneNumber == null) {
        phoneNumber = request.getPhoneNumber();
      }
      
      // Validate phone number exists
      if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
        throw new IllegalArgumentException("Phone number is required");
      }
      
      // Use MyUserDetailService to handle phone authentication
      MyUserDetails userDetails = (MyUserDetails) userDetailService.loadUserByPhoneNumber(phoneNumber);
      Customer customer = userDetails.getCustomer();
      
      // Generate JWT tokens
      Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
      String jwt = jwtUtils.generateJwtToken(authentication);
      String refreshToken = jwtUtils.generateRefreshToken(authentication);

      // Create response
      LoginResponse response = mapper.map(customer, LoginResponse.class);
      response.setRole("USER");
      response.setToken(jwt);
      response.setRefreshToken(refreshToken);
      response.setTokenType("Bearer");

      // Save access token to cookie
      Cookie accessTokenCookie = new Cookie("access_token", response.getToken());
      accessTokenCookie.setHttpOnly(true);
      accessTokenCookie.setPath("/");
      accessTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14 days
      httpResponse.addCookie(accessTokenCookie);

      // Save refresh token to cookie
      Cookie refreshTokenCookie = new Cookie("refresh_token", response.getRefreshToken());
      refreshTokenCookie.setHttpOnly(true);
      refreshTokenCookie.setPath("/");
      refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
      httpResponse.addCookie(refreshTokenCookie);
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      throw new IllegalArgumentException("Phone login failed: " + e.getMessage());
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse httpResponse) {
    try {
      // Lấy refresh token từ cookie
      String refreshToken = null;
      if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
          if ("refresh_token".equals(cookie.getName())) {
            refreshToken = cookie.getValue();
            break;
          }
        }
      }
      
      if (refreshToken == null || refreshToken.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      
      // Validate refresh token (expired)
      if (!jwtUtils.validateRefreshToken(refreshToken)) {
        return ResponseEntity.status(HttpStatus.GONE).build();
      }
      
      // Lấy username từ refresh token
      String username = jwtUtils.getUserNameFromRefreshToken(refreshToken);
      
      // Load user details
      MyUserDetails userDetails = (MyUserDetails) userDetailService.loadUserByUsername(username);
      
      // Tạo authentication object
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      
      // Generate new tokens
      String newAccessToken = jwtUtils.generateJwtToken(authentication);
      // Refresh token giữ nguyên, không tạo mới
      
      // Tạo response
      LoginResponse response = new LoginResponse();
      if (userDetails.getEmployee() != null) {
        response = mapper.map(userDetails.getEmployee(), LoginResponse.class);
      } else if (userDetails.getCustomer() != null) {
        response = mapper.map(userDetails.getCustomer(), LoginResponse.class);
        response.setRole("USER");
      }
      
      response.setToken(newAccessToken);
      response.setRefreshToken(refreshToken); // Giữ nguyên refresh token cũ
      response.setTokenType("Bearer");
      
      // Chỉ cập nhật access token cookie, refresh token giữ nguyên
      Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
      accessTokenCookie.setHttpOnly(true);
      accessTokenCookie.setPath("/");
      accessTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 14 days
      httpResponse.addCookie(accessTokenCookie);
      // Không cập nhật refresh token cookie
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      return ResponseEntity.status(418).build();
    }
  }

}
