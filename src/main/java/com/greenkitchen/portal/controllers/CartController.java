package com.greenkitchen.portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getCartByCustomer(@PathVariable("customerId") Long customerId) {
        try {
            CartResponse cart = cartService.getCartByCustomerId(customerId);
            if (cart == null) {
                return ResponseEntity.status(404).body("Cart not found for customer: " + customerId);
            }
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Sync cart from FE Redux to BE Database
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncCart(@RequestBody CartRequest request) {
        try {
            if (request.getCustomerId() == null) {
                return ResponseEntity.badRequest().body("CustomerId is required");
            }
            CartResponse cart = cartService.syncCart(request.getCustomerId(), request);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Add item to cart
     */
    @PostMapping("/customer/items/{customerId}")
    public ResponseEntity<?> addItemToCart(
            @PathVariable("customerId") Long customerId,
            @RequestBody CartItemRequest itemRequest) {
        try {
            if (itemRequest.getQuantity() == null || itemRequest.getUnitPrice() == null) {
                return ResponseEntity.badRequest().body("Quantity and UnitPrice are required");
            }
            CartItemResponse item = cartService.addItemToCart(customerId, itemRequest);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Update cart item
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable("itemId") Long itemId,
            @RequestBody CartItemRequest itemRequest) {
        try {
            if (itemRequest.getQuantity() == null || itemRequest.getUnitPrice() == null) {
                return ResponseEntity.badRequest().body("Quantity and UnitPrice are required");
            }
            CartItemResponse item = cartService.updateCartItem(itemId, itemRequest);
            if (item == null) {
                return ResponseEntity.status(404).body("Cart item not found with id: " + itemId);
            }
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/customer/{customerId}/items/{itemId}")
    public ResponseEntity<?> removeItemFromCart(
            @PathVariable("customerId") Long customerId,
            @PathVariable("itemId") Long itemId) {
        try {
            cartService.removeItemFromCart(customerId, itemId);
            return ResponseEntity.ok("Item removed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<?> clearCart(@PathVariable("customerId") Long customerId) {
        try {
            cartService.clearCart(customerId);
            return ResponseEntity.ok("Cart cleared successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Create or update entire cart
     */
    @PostMapping
    public ResponseEntity<?> createOrUpdateCart(@RequestBody CartRequest request) {
        try {
            if (request.getCustomerId() == null) {
                return ResponseEntity.badRequest().body("CustomerId is required");
            }
            CartResponse cart = cartService.createOrUpdateCart(request);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/customer/{customerId}/items/{itemId}/increase")
    public ResponseEntity<?> increaseItemQuantity(
            @PathVariable("customerId") Long customerId,
            @PathVariable("itemId") Long itemId) {
        try {
            CartItemResponse item = cartService.increaseItemQuantity(customerId, itemId);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Decrease item quantity
     */
    @PostMapping("/customer/{customerId}/items/{itemId}/decrease")
    public ResponseEntity<?> decreaseItemQuantity(
            @PathVariable("customerId") Long customerId,
            @PathVariable("itemId") Long itemId) {
        try {
            CartItemResponse item = cartService.decreaseItemQuantity(customerId, itemId);
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
}
