package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealRequest;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.services.MenuMealService;

@Service
public class MenuMealServiceImpl implements MenuMealService {

    @Autowired
    private MenuMealRepository menuMealRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public MenuMeal createMenuMeal(MenuMealRequest dto) {
        MenuMeal menuMeal = new MenuMeal();
        modelMapper.map(dto, menuMeal);
        return menuMealRepository.save(menuMeal);
    }

    @Override
    public MenuMeal getMenuMealById(Long id) {
        return menuMealRepository.findById(id).orElse(null);
    }

    @Override
    public MenuMeal getMenuMealBySlug(String slug) {
        return menuMealRepository.findBySlugActive(slug);
    }

    @Override
    public List<MenuMeal> getAllMenuMeals() {
        return menuMealRepository.findAllActive();
    }

    @Override
    public MenuMeal updateMenuMeal(Long id, MenuMealRequest dto) {
        MenuMeal existingMenuMeal = menuMealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuMeal not found with id: " + id));

        modelMapper.map(dto, existingMenuMeal);
        existingMenuMeal.setId(id);

        return menuMealRepository.save(existingMenuMeal);
    }

    @Override
    public void deleteMenuMeal(Long id) {
        try {
            MenuMeal menuMeal = menuMealRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("MenuMeal not found with id: " + id));

            if (menuMeal.getIsDeleted()) {
                throw new RuntimeException("MenuMeal with id: " + id + " is already deleted");
            }

            menuMeal.setIsDeleted(true);
            menuMealRepository.save(menuMeal);

        } catch (RuntimeException e) {
            System.err.println("Error deleting MenuMeal: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred while deleting MenuMeal: " + e.getMessage());
            throw new RuntimeException("Failed to delete MenuMeal with id: " + id, e);
        }
    }

    @Override
    public boolean existsBySlug(String slug) {
        return menuMealRepository.existsBySlug(slug);
    }

}
