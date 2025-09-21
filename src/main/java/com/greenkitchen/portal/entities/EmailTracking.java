package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tracking_id", unique = true, nullable = false)
    private String trackingId;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "email_type")
    private String emailType; // CART_ABANDONMENT, BROADCAST, etc.
    
    @Column(name = "original_url")
    private String originalUrl;
    
    @Column(name = "link_type")
    private String linkType; // CART, CHECKOUT, UNSUBSCRIBE, PREFERENCES, etc.
    
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
