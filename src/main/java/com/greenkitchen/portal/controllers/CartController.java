package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenkitchen.portal.dtos.CartItemRequest;
import com.greenkitchen.portal.dtos.CartItemResponse;
import com.greenkitchen.portal.dtos.CartRequest;
import com.greenkitchen.portal.dtos.CartResponse;
import com.greenkitchen.portal.services.CartService;

@RestController
@RequestMapping("/apis/v1/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Get cart by customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CartResponse> getCartByCustomer(@PathVariable("customerId") Long customerId) {
        try {
            CartResponse cart = cartService.getCartByCustomerId(customerId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving cart for customer: " + customerId);
        }
    }

    /**
     * Sync cart from FE Redux to BE Database
     */
    @PostMapping("/sync")
    public ResponseEntity<CartResponse> syncCart(@RequestBody CartRequest request) {
        try {
            if (request.getCustomerId() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            CartResponse cart = cartService.syncCart(request.getCustomerId(), request);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            // Log chi tiết lỗi để debug
            System.err.println("Error syncing cart: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error syncing cart: " + e.getMessage(), e);
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/customer/items/{customerId}")
    public ResponseEntity<CartItemResponse> addItemToCart(
            @PathVariable("customerId") Long customerId,
            @RequestBody CartItemRequest itemRequest) {
        try {
            if (itemRequest.getQuantity() == null || itemRequest.getUnitPrice() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            CartItemResponse item = cartService.addItemToCart(customerId, itemRequest);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            throw new RuntimeException("Error adding item to cart");
        }
    }

    /**
     * Update cart item
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable("itemId") Long itemId,
            @RequestBody CartItemRequest itemRequest) {
        try {
            if (itemRequest.getQuantity() == null || itemRequest.getUnitPrice() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            CartItemResponse item = cartService.updateCartItem(itemId, itemRequest);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            throw new RuntimeException("Error updating cart item");
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/customer/{customerId}/items/{itemId}")
    public ResponseEntity<String> removeItemFromCart(
            @PathVariable("customerId") Long customerId,
            @PathVariable("itemId") Long itemId) {
        try {
            cartService.removeItemFromCart(customerId, itemId);
            return ResponseEntity.ok("Item removed successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error removing item from cart");
        }
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<String> clearCart(@PathVariable("customerId") Long customerId) {
        try {
            cartService.clearCart(customerId);
            return ResponseEntity.ok("Cart cleared successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error clearing cart");
        }
    }

    /**
     * Create or update entire cart
     */
    @PostMapping
    public ResponseEntity<CartResponse> createOrUpdateCart(@RequestBody CartRequest request) {
        try {
            if (request.getCustomerId() == null) {
                return ResponseEntity.badRequest().body(null);
            }
            CartResponse cart = cartService.createOrUpdateCart(request);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            throw new RuntimeException("Error creating/updating cart");
        }
    }

    @PostMapping("/customer/{customerId}/items/{itemId}/increase")
    public ResponseEntity<CartItemResponse> increaseItemQuantity(
            @PathVariable("customerId") Long customerId,
            @PathVariable("itemId") Long itemId) {
        try {
            CartItemResponse item = cartService.increaseItemQuantity(customerId, itemId);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            throw new RuntimeException("Error increasing item quantity: " + e.getMessage());
        }
    }

    /**
     * Decrease item quantity
     */
    @PostMapping("/customer/{customerId}/items/{itemId}/decrease")
    public ResponseEntity<CartItemResponse> decreaseItemQuantity(
            @PathVariable("customerId") Long customerId,
            @PathVariable("itemId") Long itemId) {
        try {
            CartItemResponse item = cartService.decreaseItemQuantity(customerId, itemId);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            throw new RuntimeException("Error decreasing item quantity: " + e.getMessage());
        }
    }
}
