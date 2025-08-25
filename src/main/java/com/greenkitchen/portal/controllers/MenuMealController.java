package com.greenkitchen.portal.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.MenuMealRequest;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.enums.MenuIngredients;
import com.greenkitchen.portal.services.MenuMealService;
import com.greenkitchen.portal.utils.ImageUtils;
import com.greenkitchen.portal.utils.SlugUtils;

@RestController
@RequestMapping("/apis/v1/menu-meals")
public class MenuMealController {
  @Autowired
  private ImageUtils imageUtils;

  @Autowired
  private MenuMealService menuMealService;

  // @Autowired
  // public MenuMealController(ImageUtils ImageUtils) {
  // this.ImageUtils = ImageUtils;
  // }

  @GetMapping("/customers")
  public ResponseEntity<List<MenuMealResponse>> getAllMenuMeals() {
    List<MenuMealResponse> menuMeals = menuMealService.getAllMenuMeals();
    return ResponseEntity.ok(menuMeals);
  }

  @PostMapping("/customers")
  @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
  public ResponseEntity<?> createMenuMeal(@ModelAttribute MenuMealRequest request,
      @RequestParam("imageFile") MultipartFile file) {
    try {
      String baseSlug = SlugUtils.toSlug(request.getTitle());
      String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug,
          slug -> menuMealService.existsBySlug(slug));
      request.setSlug(uniqueSlug);

      if (file != null && !file.isEmpty()) {
        String imageUrl = imageUtils.uploadImage(file);
        request.setImage(imageUrl);
      }

      if (request.getMenuIngredientsString() != null && !request.getMenuIngredientsString().isEmpty()) {
        Set<MenuIngredients> allergenSet = Arrays.stream(request.getMenuIngredientsString().split(","))
            .map(String::trim)
            .map(String::toUpperCase)
            .map(MenuIngredients::valueOf)
            .collect(Collectors.toSet());
        request.setMenuIngredients(allergenSet);
      }

      MenuMeal menuMeal = menuMealService.createMenuMeal(request);
      return ResponseEntity.ok(menuMeal);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/customers/{id}/image")
  @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
  public ResponseEntity<?> updateMenuMealImage(
      @PathVariable("id") Long id,
      @RequestParam("imageFile") MultipartFile file) {
    try {
      MenuMealResponse existingMenuMeal = menuMealService.getMenuMealById(id);
      if (existingMenuMeal == null) {
        return ResponseEntity.status(404).body("MenuMeal not found");
      }

      // Xóa ảnh cũ nếu có
      if (existingMenuMeal.getImage() != null && !existingMenuMeal.getImage().isEmpty()) {
        imageUtils.deleteImage(existingMenuMeal.getImage());
      }

      // Upload ảnh mới
      String imageUrl = imageUtils.uploadImage(file);

      // Tạo request chỉ chứa ảnh mới
      MenuMealRequest request = new MenuMealRequest();
      request.setImage(imageUrl);

      // Cập nhật ảnh cho MenuMeal
      MenuMeal menuMeal = menuMealService.updateMenuMeal(id, request);
      return ResponseEntity.ok(menuMeal);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @PutMapping("/customers/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
  public ResponseEntity<?> updateMenuMeal(
      @PathVariable("id") Long id,
      @RequestBody MenuMealRequest request) {
    try {
      MenuMealResponse existingMenuMeal = menuMealService.getMenuMealById(id);
      if (existingMenuMeal == null) {
        return ResponseEntity.status(404).body("MenuMeal not found");
      }

      if (!existingMenuMeal.getTitle().equals(request.getTitle())) {
        String baseSlug = SlugUtils.toSlug(request.getTitle());
        String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug,
            slug -> !slug.equals(existingMenuMeal.getSlug()) && menuMealService.existsBySlug(slug));
        request.setSlug(uniqueSlug);
      } else {
        request.setSlug(existingMenuMeal.getSlug());
      }

      // Giữ nguyên ảnh cũ
      request.setImage(existingMenuMeal.getImage());

      MenuMeal menuMeal = menuMealService.updateMenuMeal(id, request);
      return ResponseEntity.ok(menuMeal);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }

  @GetMapping("/customers/{id}")
  public ResponseEntity<MenuMealResponse> getMenuMealById(@PathVariable("id") Long id) {
    MenuMealResponse menuMeal = menuMealService.getMenuMealById(id);
    if (menuMeal == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(menuMeal);
  }

  @GetMapping("/customers/slug/{slug}")
  public ResponseEntity<MenuMealResponse> getMenuMealBySlug(@PathVariable("slug") String slug) {
    MenuMealResponse menuMeal = menuMealService.getMenuMealBySlug(slug);
    if (menuMeal == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(menuMeal);
  }

  @DeleteMapping("/customers/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
  public ResponseEntity<?> deleteMenuMeal(@PathVariable("id") Long id) {
    try {
      menuMealService.deleteMenuMeal(id);
      return ResponseEntity.ok("MenuMeal deleted successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
    }
  }
}