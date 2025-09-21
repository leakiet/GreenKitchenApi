package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.dtos.CartItemRequest;
import com.greenkitchen.portal.dtos.CartItemResponse;
import com.greenkitchen.portal.dtos.CartRequest;
import com.greenkitchen.portal.dtos.CartResponse;
import com.greenkitchen.portal.dtos.CustomMealDetailResponse;
import com.greenkitchen.portal.dtos.CustomMealResponse;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.entities.Cart;
import com.greenkitchen.portal.entities.CartItem;
import com.greenkitchen.portal.entities.CustomMeal;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.NutritionInfo;
import com.greenkitchen.portal.enums.OrderItemType;
import com.greenkitchen.portal.repositories.CartItemRepository;
import com.greenkitchen.portal.repositories.CartRepository;
import com.greenkitchen.portal.repositories.CustomMealRepository;
import com.greenkitchen.portal.repositories.IngredientRepository;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.services.CartService;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomMealRepository customMealRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuMealRepository menuMealRepository;

    @Autowired
    private MenuMealServiceImpl menuMealServiceImpl;

    @Autowired
    private CustomMealServiceImpl customMealServiceImpl;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartResponse getCartByCustomerId(Long customerId) {
        // Thay đổi từ findByCustomerIdAndStatus thành
        // findByCustomerIdAndStatusWithActiveItems
        Cart cart = cartRepository.findByCustomerIdAndStatusWithActiveItems(customerId)
                .orElse(createNewCart(customerId));
        return toCartResponse(cart);
    }

    @Override
    public CartResponse createOrUpdateCart(CartRequest request) {
        // Thay đổi từ findByCustomerIdAndStatus thành
        // findByCustomerIdAndStatusWithActiveItems
        Cart cart = cartRepository.findByCustomerIdAndStatusWithActiveItems(request.getCustomerId())
                .orElse(createNewCart(request.getCustomerId()));

        if (cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }

        if (request.getCartItems() != null) {
            List<CartItem> cartItems = request.getCartItems().stream()
                    .map(itemRequest -> createCartItem(itemRequest, cart))
                    .collect(Collectors.toList());
            cart.getCartItems().addAll(cartItems);
        }

        cart.setTotalAmount(calculateTotalAmount(cart));
        Cart saved = cartRepository.save(cart);
        return toCartResponse(saved);
    }

    @Override
    public CartItemResponse addItemToCart(Long customerId, CartItemRequest itemRequest) {
        Cart cart = cartRepository.findByCustomerIdAndStatusWithActiveItems(customerId)
                .orElse(createNewCart(customerId));

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> {
                    if (Boolean.TRUE.equals(itemRequest.getIsCustom())) {
                        return Boolean.TRUE.equals(item.getIsCustom()) &&
                                item.getCustomMeal() != null &&
                                item.getCustomMeal().getId().equals(itemRequest.getCustomMealId());
                    } else {
                        return Boolean.FALSE.equals(item.getIsCustom()) &&
                                item.getMenuMeal() != null &&
                                item.getMenuMeal().getId().equals(itemRequest.getMenuMealId());
                    }
                })
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + itemRequest.getQuantity());
            existingItem.setTotalPrice(existingItem.getUnitPrice() * existingItem.getQuantity());
        } else {
            CartItem cartItem = createCartItem(itemRequest, cart);
            cart.getCartItems().add(cartItem);
        }

        cart.setTotalAmount(calculateTotalAmount(cart));
        Cart saved = cartRepository.save(cart);

        CartItem resultItem = (existingItem != null) ? existingItem
                : saved.getCartItems().get(saved.getCartItems().size() - 1);
        return toItemResponse(resultItem);
    }

    @Override
    public CartItemResponse updateCartItem(Long itemId, CartItemRequest itemRequest) {
        CartItem existingItem = cartItemRepository.findByIdAndNotDeleted(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        updateCartItemFromRequest(existingItem, itemRequest);
        CartItem saved = cartItemRepository.save(existingItem);

        // Update cart total
        Cart cart = saved.getCart();
        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);

        return toItemResponse(saved);
    }

    @Override
    public void removeItemFromCart(Long customerId, Long itemId) {
        CartItem cartItem = cartItemRepository.findByIdAndNotDeleted(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        Cart cart = cartItem.getCart();
        if (cart == null || !cart.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Unauthorized: Item does not belong to this customer");
        }

        cart.getCartItems().remove(cartItem);

        cartItemRepository.delete(cartItem);

        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long customerId) {
        // Thay đổi từ findByCustomerIdAndStatus thành
        // findByCustomerIdAndStatusWithActiveItems
        cartRepository.findByCustomerIdAndStatusWithActiveItems(customerId)
                .ifPresent(cart -> {
                    // Soft delete tất cả items
                    cart.getCartItems().forEach(item -> item.setIsDeleted(true));
                    cart.setTotalAmount(0.0);
                    cartRepository.save(cart);
                });
    }

    @Override
    public CartResponse syncCart(Long customerId, CartRequest cartRequest) {
        // This method handles syncing FE Redux cart with BE database
        return createOrUpdateCart(cartRequest);
    }

    private Cart createNewCart(Long customerId) {
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        cart.setTotalAmount(0.0);
        return cart;
    }

    private CartItem createCartItem(CartItemRequest request, Cart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setIsCustom(request.getIsCustom());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(request.getUnitPrice());
        cartItem.setTotalPrice(request.getUnitPrice() * request.getQuantity());
        cartItem.setTitle(request.getTitle());
        cartItem.setDescription(request.getDescription());
        cartItem.setImage(request.getImage());

        if (request.getItemType() != null) {
            cartItem.setItemType(OrderItemType.valueOf(request.getItemType()));
        }

        // Set nutrition info
        if (request.getCalories() != null || request.getProtein() != null ||
                request.getCarbs() != null || request.getFat() != null) {
            NutritionInfo nutrition = new NutritionInfo();
            nutrition.setCalories(request.getCalories());
            nutrition.setProtein(request.getProtein());
            nutrition.setCarbs(request.getCarbs());
            nutrition.setFat(request.getFat());
            cartItem.setNutrition(nutrition);
        }

        // Set menu meal or custom meal reference
        if (Boolean.FALSE.equals(request.getIsCustom()) && request.getMenuMealId() != null) {
            menuMealRepository.findById(request.getMenuMealId())
                    .ifPresent(cartItem::setMenuMeal);
        } else if (Boolean.TRUE.equals(request.getIsCustom()) && request.getCustomMealId() != null) {
            customMealRepository.findById(request.getCustomMealId())
                    .ifPresent(cartItem::setCustomMeal);
        }

        return cartItem;
    }

    private void updateCartItemFromRequest(CartItem cartItem, CartItemRequest request) {
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(request.getUnitPrice());
        cartItem.setTotalPrice(request.getUnitPrice() * request.getQuantity());
        // Update nutrition info
        if (cartItem.getNutrition() == null) {
            cartItem.setNutrition(new NutritionInfo());
        }
        cartItem.getNutrition().setCalories(request.getCalories());
        cartItem.getNutrition().setProtein(request.getProtein());
        cartItem.getNutrition().setCarbs(request.getCarbs());
        cartItem.getNutrition().setFat(request.getFat());
    }

    // Có thể bỏ filter trong calculateTotalAmount và toCartResponse vì repository
    // đã filter sẵn
    private Double calculateTotalAmount(Cart cart) {
        return cart.getCartItems() != null ? cart.getCartItems().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum() : 0.0;
    }

    private CartResponse toCartResponse(Cart cart) {
        CartResponse response = new CartResponse();

        // Gán thủ công các field từ Cart
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomerId());
        response.setTotalAmount(cart.getTotalAmount());

        if (cart.getCartItems() != null) {
            List<CartItemResponse> itemResponses = cart.getCartItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList());
            response.setCartItems(itemResponses);

            // Calculate totals for FE convenience
            response.setTotalItems(cart.getCartItems().size());
            response.setTotalQuantity(cart.getCartItems().stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum());
        } else {
            response.setCartItems(null);
            response.setTotalItems(0);
            response.setTotalQuantity(0);
        }

        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();

        // Gán thủ công các field từ CartItem
        response.setId(item.getId());
        response.setCartId(item.getCart() != null ? item.getCart().getId() : null);
        response.setIsCustom(item.getIsCustom());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());
        response.setTitle(item.getTitle());
        response.setImage(item.getImage());
        response.setDescription(item.getDescription());
        response.setItemType(item.getItemType());

        // Gán nutrition từ CartItem (nếu có)
        if (item.getNutrition() != null) {
            response.setCalories(item.getNutrition().getCalories());
            response.setProtein(item.getNutrition().getProtein());
            response.setCarbs(item.getNutrition().getCarbs());
            response.setFat(item.getNutrition().getFat());
        } else {
            response.setCalories(null);
            response.setProtein(null);
            response.setCarbs(null);
            response.setFat(null);
        }

        // Gán menuMeal nếu có
        if (item.getMenuMeal() != null) {
            MenuMealResponse menuMealResponse = menuMealServiceImpl.toResponse(item.getMenuMeal());
            response.setMenuMeal(menuMealResponse);
            // Ưu tiên dinh dưỡng từ CartItem, nếu không có thì lấy từ menuMeal
            if (response.getCalories() == null && menuMealResponse.getCalories() != null)
                response.setCalories(menuMealResponse.getCalories());
            if (response.getProtein() == null && menuMealResponse.getProtein() != null)
                response.setProtein(menuMealResponse.getProtein());
            if (response.getCarbs() == null && menuMealResponse.getCarbs() != null)
                response.setCarbs(menuMealResponse.getCarbs());
            if (response.getFat() == null && menuMealResponse.getFat() != null)
                response.setFat(menuMealResponse.getFat());
        } else {
            response.setMenuMeal(null);
        }

        // Gán customMeal nếu có
        if (item.getCustomMeal() != null) {
            CustomMealResponse customMealResponse = customMealServiceImpl.toResponse(item.getCustomMeal());
            response.setCustomMeal(customMealResponse);
            // Ưu tiên dinh dưỡng từ CartItem, nếu không có thì lấy từ customMeal
            if (response.getCalories() == null && customMealResponse.getCalories() != null)
                response.setCalories(customMealResponse.getCalories());
            if (response.getProtein() == null && customMealResponse.getProtein() != null)
                response.setProtein(customMealResponse.getProtein());
            if (response.getCarbs() == null && customMealResponse.getCarb() != null)
                response.setCarbs(customMealResponse.getCarb());
            if (response.getFat() == null && customMealResponse.getFat() != null)
                response.setFat(customMealResponse.getFat());
        } else {
            response.setCustomMeal(null);
        }

        // Gán weekMeal (nếu có, nhưng trong code hiện tại không set, nên để null)
        response.setWeekMeal(null);

        return response;
    }

    @Override
    public CartItemResponse increaseItemQuantity(Long customerId, Long itemId) {
        CartItem existingItem = cartItemRepository.findByIdAndNotDeleted(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        // Validate customer ownership
        Cart cart = existingItem.getCart();
        if (cart == null || !cart.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Unauthorized: Item does not belong to this customer");
        }

        existingItem.setQuantity(existingItem.getQuantity() + 1);
        existingItem.setTotalPrice(existingItem.getUnitPrice() * existingItem.getQuantity());

        CartItem saved = cartItemRepository.save(existingItem);

        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);

        return toItemResponse(saved);
    }

    @Override
    public CartItemResponse decreaseItemQuantity(Long customerId, Long itemId) {
        CartItem existingItem = cartItemRepository.findByIdAndNotDeleted(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        // Validate customer ownership
        Cart cart = existingItem.getCart();
        if (cart == null || !cart.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Unauthorized: Item does not belong to this customer");
        }

        if (existingItem.getQuantity() <= 1) {
            throw new RuntimeException("Cannot decrease quantity below 1");
        }

        existingItem.setQuantity(existingItem.getQuantity() - 1);
        existingItem.setTotalPrice(existingItem.getUnitPrice() * existingItem.getQuantity());

        CartItem saved = cartItemRepository.save(existingItem);

        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);

        return toItemResponse(saved);
    }
}
