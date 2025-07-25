package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.MenuMealReview;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.repositories.MenuMealReviewRepository;
import com.greenkitchen.portal.services.MenuMealReviewService;

@Service
public class MenuMealReviewServiceImpl implements MenuMealReviewService {

    @Autowired
    private MenuMealReviewRepository menuMealReviewRepository;
    
    @Autowired
    private MenuMealRepository menuMealRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public MenuMealReview getMenuMealReviewById(Long id) {
        return menuMealReviewRepository.findById(id).orElse(null);
    }

    @Override
    public MenuMealReview createMenuMealReview(MenuMealReviewRequest dto) {
        MenuMeal menuMeal = menuMealRepository.findById(dto.getMenuMealId())
            .orElseThrow(() -> new RuntimeException("MenuMeal not found with id: " + dto.getMenuMealId()));
        
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + dto.getCustomerId()));
        
        if (menuMealReviewRepository.existsByMenuMealIdAndCustomerId(dto.getMenuMealId(), dto.getCustomerId())) {
            throw new RuntimeException("Customer has already reviewed this menu meal");
        }
        
        MenuMealReview review = new MenuMealReview();
        review.setMenuMeal(menuMeal);
        review.setCustomer(customer);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        
        return menuMealReviewRepository.save(review);
    }

    @Override
    public MenuMealReview updateMenuMealReview(Long id, MenuMealReviewRequest dto) {
        MenuMealReview existingReview = menuMealReviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("MenuMealReview not found with id: " + id));
        
        existingReview.setRating(dto.getRating());
        existingReview.setComment(dto.getComment());
        
        return menuMealReviewRepository.save(existingReview);
    }

    @Override
    public void deleteMenuMealReview(Long id) {
        try {
            MenuMealReview review = menuMealReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuMealReview not found with id: " + id));
            
            menuMealReviewRepository.delete(review);
        } catch (RuntimeException e) {
            System.err.println("Error deleting MenuMealReview: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error occurred while deleting MenuMealReview: " + e.getMessage());
            throw new RuntimeException("Failed to delete MenuMealReview with id: " + id, e);
        }
    }

    @Override
    public List<MenuMealReview> getAllReviewsByMenuMealId(Long menuMealId) {
        return menuMealReviewRepository.findByMenuMealId(menuMealId);
    }

    @Override
    public List<MenuMealReview> getAllReviewsByCustomerId(Long customerId) {
        return menuMealReviewRepository.findByCustomerId(customerId);
    }

}
