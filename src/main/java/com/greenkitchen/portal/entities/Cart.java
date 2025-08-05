package com.greenkitchen.portal.entities;

import java.util.ArrayList;
import java.util.List;

import com.greenkitchen.portal.enums.CartStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart extends AbstractEntity {
  private static final long serialVersionUID = 1L;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CartItem> cartItems = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  private CartStatus status = CartStatus.ACTIVE; // ACTIVE, CHECKED_OUT, ABANDONED

  private Double totalAmount;
}
