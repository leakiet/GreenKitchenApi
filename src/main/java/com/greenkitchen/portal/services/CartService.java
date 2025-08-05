package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CartItemRequest;
import com.greenkitchen.portal.dtos.CartItemResponse;
import com.greenkitchen.portal.dtos.CartRequest;
import com.greenkitchen.portal.dtos.CartResponse;

public interface CartService {
    CartResponse getCartByCustomerId(Long customerId);

    CartResponse createOrUpdateCart(CartRequest request);

    CartItemResponse addItemToCart(Long customerId, CartItemRequest itemRequest);

    CartItemResponse updateCartItem(Long itemId, CartItemRequest itemRequest);

    void removeItemFromCart(Long customerId, Long itemId);

    void clearCart(Long customerId);

    CartResponse syncCart(Long customerId, CartRequest cartRequest);

    CartItemResponse increaseItemQuantity(Long customerId, Long itemId);

    CartItemResponse decreaseItemQuantity(Long customerId, Long itemId);
}
