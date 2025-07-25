package com.greenkitchen.portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.greenkitchen.portal.dtos.MenuMealRequest;
import com.greenkitchen.portal.entities.MenuMeal;
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

  @GetMapping
  public ResponseEntity<List<MenuMeal>> getAllMenuMeals() {
    List<MenuMeal> menuMeals = menuMealService.getAllMenuMeals();
    return ResponseEntity.ok(menuMeals);
  }

  @PostMapping
  public ResponseEntity<MenuMeal> createMenuMeal(@ModelAttribute MenuMealRequest request,
      @RequestParam("imageFile") MultipartFile file) {
    String baseSlug = SlugUtils.toSlug(request.getTitle());
    String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug,
        slug -> menuMealService.existsBySlug(slug));
    request.setSlug(uniqueSlug);

    if (file != null && !file.isEmpty()) {
      String imageUrl = imageUtils.uploadImage(file);
      request.setImage(imageUrl);
    }

    MenuMeal menuMeal = menuMealService.createMenuMeal(request);
    return ResponseEntity.ok(menuMeal);
  }
  

  @PutMapping("/{id}")
  public ResponseEntity<MenuMeal> updateMenuMeal(@PathVariable("id") Long id, @ModelAttribute MenuMealRequest request,
      @RequestParam("imageFile") MultipartFile file) {

    MenuMeal existingMenuMeal = menuMealService.getMenuMealById(id);
    if (existingMenuMeal == null) {
      return ResponseEntity.notFound().build();
    }

    if (!existingMenuMeal.getTitle().equals(request.getTitle())) {
      String baseSlug = SlugUtils.toSlug(request.getTitle());
      String uniqueSlug = SlugUtils.generateUniqueSlug(baseSlug,
          slug -> !slug.equals(existingMenuMeal.getSlug()) && menuMealService.existsBySlug(slug));
      request.setSlug(uniqueSlug);
    } else {
      request.setSlug(existingMenuMeal.getSlug());
    }

    if (file != null && !file.isEmpty()) {
      String imageUrl = imageUtils.uploadImage(file);
      request.setImage(imageUrl);
    }

    MenuMeal menuMeal = menuMealService.updateMenuMeal(id, request);
    return ResponseEntity.ok(menuMeal);
  }

  @GetMapping("/{id}")
  public ResponseEntity<MenuMeal> getMenuMealById(@PathVariable("id") Long id) {
    MenuMeal menuMeal = menuMealService.getMenuMealById(id);
    if (menuMeal == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(menuMeal);
  }

  @GetMapping("/slug/{slug}")
  public ResponseEntity<MenuMeal> getMenuMealBySlug(@PathVariable("slug") String slug) {
    MenuMeal menuMeal = menuMealService.getMenuMealBySlug(slug);
    if (menuMeal == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(menuMeal);
  }

  @GetMapping("/delete/{id}")
  public ResponseEntity<String> deleteMenuMeal(@PathVariable("id") Long id) {
    try {
      menuMealService.deleteMenuMeal(id);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error deleting MenuMeal: " + e.getMessage());
    }
    return ResponseEntity.ok("MenuMeal deleted successfully");
  }
}