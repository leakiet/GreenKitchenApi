package com.greenkitchen.portal.services.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.greenkitchen.portal.dtos.CartScanResponse;
import com.greenkitchen.portal.entities.Cart;
import com.greenkitchen.portal.entities.CartItem;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.repositories.CartRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.services.CartEmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CartEmailServiceImpl implements CartEmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private com.greenkitchen.portal.repositories.CartEmailLogRepository cartEmailLogRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendCartAbandonmentEmail(Cart cart, String customerEmail, String customerName) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (cart == null || customerEmail == null || customerName == null) {
                log.error("Dữ liệu không hợp lệ để gửi email: cart={}, email={}, name={}", 
                    cart != null, customerEmail != null, customerName != null);
                return;
            }
            
            // Kiểm tra cart có hợp lệ không
            if (!isCartValid(cart)) {
                log.error("Cart không hợp lệ để gửi email cho customer: {}", customerName);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject("🛒 Bạn quên gì đó trong giỏ hàng - Green Kitchen");
            
            // Chuẩn bị dữ liệu cho template với validation
            Context context = new Context();
            context.setVariable("customerName", customerName.trim());
            context.setVariable("cartItems", prepareCartItemsData(cart.getCartItems()));
            context.setVariable("totalAmount", formatPrice(cart.getTotalAmount()));
            context.setVariable("frontendUrl", frontendUrl != null ? frontendUrl : "http://localhost:3000");
            context.setVariable("unsubscribeUrl", (frontendUrl != null ? frontendUrl : "http://localhost:3000") + "/unsubscribe");
            context.setVariable("preferencesUrl", (frontendUrl != null ? frontendUrl : "http://localhost:3000") + "/email-preferences");
            
            // Log dữ liệu template để debug
            log.info("Template variables: customerName={}, cartItems={}, totalAmount={}", 
                customerName, cart.getCartItems().size(), formatPrice(cart.getTotalAmount()));
            
            // Log thông tin cart trước khi gửi
            log.info("📧 Chuẩn bị gửi email cho customer: '{}' (email: {})", customerName, customerEmail);
            log.info("🛒 Cart có {} items, tổng tiền: {}", 
                cart.getCartItems().size(), formatPrice(cart.getTotalAmount()));
            
            // Render template HTML
            String htmlContent = templateEngine.process("cart-abandonment-email", context);
            helper.setText(htmlContent, true);
            
            // Gửi email
            mailSender.send(message);
            
            // Lưu log email đã gửi
            com.greenkitchen.portal.entities.CartEmailLog emailLog = new com.greenkitchen.portal.entities.CartEmailLog(
                cart.getCustomerId(),
                cart.getCartItems().size(),
                cart.getTotalAmount(),
                "CART_ABANDONMENT"
            );
            cartEmailLogRepository.save(emailLog);
            
            log.info("✅ Đã gửi email cart abandonment thành công cho customer {} (email: {})", customerName, customerEmail);
            
        } catch (MessagingException e) {
            log.error("❌ Lỗi khi gửi email cart abandonment cho customer {}: {}", customerName, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Lỗi không mong muốn khi gửi email cho customer {}: {}", customerName, e.getMessage());
        }
    }

    @Override
    public void sendBulkCartAbandonmentEmails(CartScanResponse scanResponse) {
        log.info("Bắt đầu gửi bulk email cart abandonment cho {} customers", scanResponse.getNewCustomersFound());
        
        int emailsSent = 0;
        int emailsFailed = 0;
        
        for (CartScanResponse.CustomerCartInfo cartInfo : scanResponse.getCustomerCarts()) {
            try {
                // Lấy thông tin customer đầy đủ từ database
                Customer customer = customerRepository.findById(cartInfo.getCustomerId()).orElse(null);
                if (customer == null) {
                    log.warn("Không tìm thấy customer với ID: {}", cartInfo.getCustomerId());
                    emailsFailed++;
                    continue;
                }
                
                // Kiểm tra email và tên customer
                String customerEmail = customer.getEmail();
                String customerName = getCustomerFullName(customer);
                
                if (customerEmail == null || customerEmail.trim().isEmpty()) {
                    log.warn("Customer ID {} không có email hợp lệ", cartInfo.getCustomerId());
                    emailsFailed++;
                    continue;
                }
                
                if (customerName == null || customerName.trim().isEmpty()) {
                    log.warn("Customer ID {} không có tên hợp lệ", cartInfo.getCustomerId());
                    emailsFailed++;
                    continue;
                }
                
                // Lấy cart đầy đủ từ database
                Cart cart = getCartByCustomerId(cartInfo.getCustomerId());
                if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                    log.warn("Customer ID {} không có cart hoặc cart rỗng", cartInfo.getCustomerId());
                    emailsFailed++;
                    continue;
                }
                
                // Kiểm tra cart có items hợp lệ
                if (!isCartValid(cart)) {
                    log.warn("Cart của customer ID {} không hợp lệ", cartInfo.getCustomerId());
                    emailsFailed++;
                    continue;
                }
                
                // Gửi email với tên thật
                sendCartAbandonmentEmail(cart, customerEmail, customerName);
                emailsSent++;
                
                log.info("✅ Đã gửi email thành công cho customer '{}' (ID: {}) với {} items, tổng tiền: {}", 
                    customerName, cartInfo.getCustomerId(), cart.getCartItems().size(), formatPrice(cart.getTotalAmount()));
                
                // Delay nhỏ để tránh spam
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Lỗi khi gửi email cho customer ID {}: {}", cartInfo.getCustomerId(), e.getMessage());
                emailsFailed++;
            }
        }
        
        log.info("Hoàn thành gửi bulk email: {} thành công, {} thất bại", emailsSent, emailsFailed);
    }

    @Override
    public void sendTestCartEmail(Long customerId, String customerEmail) {
        try {
            // Lấy thông tin customer thật từ database
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                log.error("Không tìm thấy customer với ID: {}", customerId);
                return;
            }
            
            // Lấy tên thật của customer
            String customerName = getCustomerFullName(customer);
            if (customerName == null || customerName.trim().isEmpty()) {
                log.warn("Customer ID {} không có tên hợp lệ, sử dụng email", customerId);
                customerName = customer.getEmail() != null ? customer.getEmail().split("@")[0] : "Khách hàng";
            }
            
            // Lấy cart thật từ database
            Cart realCart = getCartByCustomerId(customerId);
            Cart cartToUse;
            
            if (realCart != null && isCartValid(realCart)) {
                // Sử dụng cart thật nếu có
                cartToUse = realCart;
                log.info("Sử dụng cart thật cho customer {} với {} items", customerName, realCart.getCartItems().size());
            } else {
                // Tạo cart test data nếu không có cart thật
                cartToUse = createTestCart(customerId);
                log.info("Sử dụng cart test data cho customer {}", customerName);
            }
            
            // Gửi email test với tên thật
            sendCartAbandonmentEmail(cartToUse, customerEmail, customerName);
            
            log.info("Đã gửi email test cart cho customer {} (ID: {}) với email: {}", customerName, customerId, customerEmail);
            
        } catch (Exception e) {
            log.error("Lỗi khi gửi email test cart: {}", e.getMessage());
        }
    }
    
    /**
     * Chuẩn bị dữ liệu cart items cho template
     */
    private List<Map<String, Object>> prepareCartItemsData(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            log.warn("Cart items rỗng hoặc null");
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> itemsData = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            if (item != null && isCartItemValid(item)) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("title", item.getTitle() != null ? item.getTitle().trim() : "Sản phẩm không tên");
                itemData.put("quantity", item.getQuantity() != null ? item.getQuantity() : 0);
                itemData.put("description", item.getDescription() != null ? item.getDescription().trim() : "Không có mô tả");
                itemData.put("totalPrice", formatPrice(item.getTotalPrice()));
                
                itemsData.add(itemData);
                log.debug("Đã chuẩn bị item: title={}, quantity={}, price={}", 
                    itemData.get("title"), itemData.get("quantity"), itemData.get("totalPrice"));
            }
        }
        
        log.info("Đã chuẩn bị {} cart items cho template", itemsData.size());
        return itemsData;
    }
    
    /**
     * Format giá tiền theo định dạng Việt Nam
     */
    private String formatPrice(Double price) {
        if (price == null) return "0 VNĐ";
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price).replace("₫", "VNĐ");
    }
    
    /**
     * Lấy cart theo customer ID từ database
     */
    private Cart getCartByCustomerId(Long customerId) {
        try {
            return cartRepository.findByCustomerIdWithActiveItems(customerId).orElse(null);
        } catch (Exception e) {
            log.error("Lỗi khi lấy cart cho customer ID {}: {}", customerId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo cart test data
     */
    private Cart createTestCart(Long customerId) {
        Cart testCart = new Cart();
        testCart.setCustomerId(customerId);
        testCart.setTotalAmount(150000.0);
        
        // Tạo test cart items
        CartItem testItem1 = new CartItem();
        testItem1.setTitle("Salad Gà Nướng");
        testItem1.setQuantity(2);
        testItem1.setDescription("Salad tươi với gà nướng thơm ngon");
        testItem1.setTotalPrice(80000.0);
        
        CartItem testItem2 = new CartItem();
        testItem2.setTitle("Smoothie Xanh");
        testItem2.setQuantity(1);
        testItem2.setDescription("Smoothie rau xanh bổ dưỡng");
        testItem2.setTotalPrice(70000.0);
        
        testCart.setCartItems(List.of(testItem1, testItem2));
        
        return testCart;
    }
    
    /**
     * Lấy tên đầy đủ của customer một cách an toàn
     */
    private String getCustomerFullName(Customer customer) {
        if (customer == null) return null;
        
        // Ưu tiên sử dụng fullName nếu có
        if (customer.getFullName() != null && !customer.getFullName().trim().isEmpty()) {
            return customer.getFullName().trim();
        }
        
        // Nếu không có fullName, ghép firstName và lastName
        String firstName = customer.getFirstName();
        String lastName = customer.getLastName();
        
        if (firstName != null && lastName != null) {
            return (firstName.trim() + " " + lastName.trim()).trim();
        } else if (firstName != null) {
            return firstName.trim();
        } else if (lastName != null) {
            return lastName.trim();
        }
        
        // Fallback về email nếu không có tên
        return customer.getEmail() != null ? customer.getEmail().split("@")[0] : "Khách hàng";
    }
    
    /**
     * Kiểm tra cart có hợp lệ không
     */
    private boolean isCartValid(Cart cart) {
        if (cart == null) return false;
        
        // Kiểm tra cart items
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return false;
        }
        
        // Kiểm tra từng item có đầy đủ thông tin
        for (CartItem item : cart.getCartItems()) {
            if (item == null) continue;
            
            // Kiểm tra các trường bắt buộc
            if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
                log.warn("Cart item không có title: {}", item.getId());
                return false;
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                log.warn("Cart item không có quantity hợp lệ: {}", item.getId());
                return false;
            }
            
            if (item.getTotalPrice() == null || item.getTotalPrice() <= 0) {
                log.warn("Cart item không có price hợp lệ: {}", item.getId());
                return false;
            }
        }
        
        // Kiểm tra total amount
        if (cart.getTotalAmount() == null || cart.getTotalAmount() <= 0) {
            log.warn("Cart không có total amount hợp lệ");
            return false;
        }
            
        return true;
    }
    
    /**
     * Kiểm tra cart item có hợp lệ không
     */
    private boolean isCartItemValid(CartItem item) {
        return item != null 
            && item.getTitle() != null && !item.getTitle().trim().isEmpty()
            && item.getQuantity() != null && item.getQuantity() > 0
            && item.getTotalPrice() != null && item.getTotalPrice() > 0;
    }
}