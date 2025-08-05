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
import com.greenkitchen.portal.entities.Cart;
import com.greenkitchen.portal.entities.CartItem;
import com.greenkitchen.portal.entities.CustomMeal;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.NutritionInfo;
import com.greenkitchen.portal.enums.CartStatus;
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
    private ModelMapper modelMapper;

    @Override
    public CartResponse getCartByCustomerId(Long customerId) {
        // Thay đổi từ findByCustomerIdAndStatus thành findByCustomerIdAndStatusWithActiveItems
        Cart cart = cartRepository.findByCustomerIdAndStatusWithActiveItems(customerId, CartStatus.ACTIVE)
                .orElse(createNewCart(customerId));
        return toCartResponse(cart);
    }

    @Override
    public CartResponse createOrUpdateCart(CartRequest request) {
        // Thay đổi từ findByCustomerIdAndStatus thành findByCustomerIdAndStatusWithActiveItems
        Cart cart = cartRepository.findByCustomerIdAndStatusWithActiveItems(request.getCustomerId(), CartStatus.ACTIVE)
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
        // Thay đổi từ findByCustomerIdAndStatus thành findByCustomerIdAndStatusWithActiveItems
        Cart cart = cartRepository.findByCustomerIdAndStatusWithActiveItems(customerId, CartStatus.ACTIVE)
                .orElse(createNewCart(customerId));

        CartItem cartItem = createCartItem(itemRequest, cart);
        cart.getCartItems().add(cartItem);
        cart.setTotalAmount(calculateTotalAmount(cart));

        Cart saved = cartRepository.save(cart);
        CartItem savedItem = saved.getCartItems().get(saved.getCartItems().size() - 1);

        return toItemResponse(savedItem);
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

        // Soft delete - chỉ đánh dấu isDeleted = true
        cartItem.setIsDeleted(true);
        cartItemRepository.save(cartItem);

        // Update cart total (chỉ tính items không bị xóa)
        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long customerId) {
        // Thay đổi từ findByCustomerIdAndStatus thành findByCustomerIdAndStatusWithActiveItems
        cartRepository.findByCustomerIdAndStatusWithActiveItems(customerId, CartStatus.ACTIVE)
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
        cart.setStatus(CartStatus.ACTIVE);
        cart.setTotalAmount(0.0);
        return cart;
    }

    private CartItem createCartItem(CartItemRequest request, Cart cart) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setIsCustom(request.getIsCustom());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setBasePrice(request.getBasePrice());
        cartItem.setTotalPrice(request.getBasePrice() * request.getQuantity());
        cartItem.setTitle(request.getTitle());
        cartItem.setDescription(request.getDescription());

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
        cartItem.setBasePrice(request.getBasePrice());
        cartItem.setTotalPrice(request.getBasePrice() * request.getQuantity());
        cartItem.setTitle(request.getTitle());
        cartItem.setDescription(request.getDescription());

        // Update nutrition info
        if (cartItem.getNutrition() == null) {
            cartItem.setNutrition(new NutritionInfo());
        }
        cartItem.getNutrition().setCalories(request.getCalories());
        cartItem.getNutrition().setProtein(request.getProtein());
        cartItem.getNutrition().setCarbs(request.getCarbs());
        cartItem.getNutrition().setFat(request.getFat());
    }

    // Có thể bỏ filter trong calculateTotalAmount và toCartResponse vì repository đã filter sẵn
    private Double calculateTotalAmount(Cart cart) {
        return cart.getCartItems() != null ?
                cart.getCartItems().stream()
                        .mapToDouble(CartItem::getTotalPrice)
                        .sum() : 0.0;
    }

    private CartResponse toCartResponse(Cart cart) {
        CartResponse response = modelMapper.map(cart, CartResponse.class);

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
        }

        return response;
    }

    private CartItemResponse toItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();

        response.setId(item.getId());
        response.setCartId(item.getCart().getId());
        response.setIsCustom(item.getIsCustom());
        response.setQuantity(item.getQuantity());
        response.setBasePrice(item.getBasePrice());
        response.setTotalPrice(item.getTotalPrice());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());

        // Set nutrition info
        if (item.getNutrition() != null) {
            response.setCalories(item.getNutrition().getCalories());
            response.setProtein(item.getNutrition().getProtein());
            response.setCarbs(item.getNutrition().getCarbs());
            response.setFat(item.getNutrition().getFat());
        }

        if (item.getIsCustom() != null && item.getIsCustom()) {
            // Custom meal item
            if (item.getCustomMeal() != null) {
                CustomMeal customMeal = item.getCustomMeal();
                response.setCustomMealId(customMeal.getId());
                response.setCustomMealName(customMeal.getName());

                // Map custom meal details
                if (customMeal.getDetails() != null) {
                    List<CustomMealDetailResponse> detailResponses = customMeal.getDetails().stream()
                            .map(d -> {
                                CustomMealDetailResponse detailResponse = new CustomMealDetailResponse();
                                detailResponse.setQuantity(d.getQuantity());

                                ingredientRepository.findById(d.getIngredientId())
                                        .ifPresent(ingredient -> {
                                            detailResponse.setId(ingredient.getId());
                                            detailResponse.setTitle(ingredient.getTitle());
                                            detailResponse.setType(ingredient.getType());
                                            detailResponse.setDescription(ingredient.getDescription());
                                            detailResponse.setImage(ingredient.getImage());

                                            if (ingredient.getNutrition() != null) {
                                                detailResponse.setCalories(ingredient.getNutrition().getCalories());
                                                detailResponse.setProtein(ingredient.getNutrition().getProtein());
                                                detailResponse.setCarbs(ingredient.getNutrition().getCarbs());
                                                detailResponse.setFat(ingredient.getNutrition().getFat());
                                            }
                                        });
                                return detailResponse;
                            })
                            .collect(Collectors.toList());
                    response.setDetails(detailResponses);
                }
            }
        } else {
            // Menu meal item
            if (item.getMenuMeal() != null) {
                MenuMeal menuMeal = item.getMenuMeal();
                response.setMenuMealId(menuMeal.getId());
                response.setMenuMealTitle(menuMeal.getTitle());
                response.setMenuMealDescription(menuMeal.getDescription());
                response.setMenuMealImage(menuMeal.getImage());
                response.setMenuMealPrice(menuMeal.getPrice());
                response.setMenuMealSlug(menuMeal.getSlug());
                response.setMenuMealType(menuMeal.getType() != null ? menuMeal.getType().toString() : null);
            }
        }

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
        existingItem.setTotalPrice(existingItem.getBasePrice() * existingItem.getQuantity());

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
        existingItem.setTotalPrice(existingItem.getBasePrice() * existingItem.getQuantity());

        CartItem saved = cartItemRepository.save(existingItem);

        cart.setTotalAmount(calculateTotalAmount(cart));
        cartRepository.save(cart);

        return toItemResponse(saved);
    }
}
