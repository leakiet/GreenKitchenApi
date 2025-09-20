package com.greenkitchen.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealReviewRequest;
import com.greenkitchen.portal.dtos.MenuMealReviewResponse;
import com.greenkitchen.portal.dtos.PagedResponse;
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
    public MenuMealReviewResponse createMenuMealReview(MenuMealReviewRequest dto) {
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
        
        MenuMealReview savedReview = menuMealReviewRepository.save(review);
        return toResponse(savedReview);
    }

    @Override
    public MenuMealReviewResponse updateMenuMealReview(Long id, MenuMealReviewRequest dto) {
        MenuMealReview existingReview = menuMealReviewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("MenuMealReview not found with id: " + id));
        
        existingReview.setRating(dto.getRating());
        existingReview.setComment(dto.getComment());
        
        MenuMealReview savedReview = menuMealReviewRepository.save(existingReview);
        return toResponse(savedReview);
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
    public List<MenuMealReviewResponse> getAllReviewsByMenuMealId(Long menuMealId) {
        List<MenuMealReview> reviews = menuMealReviewRepository.findByMenuMealIdWithCustomerAndMenuMeal(menuMealId);
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<MenuMealReviewResponse> getAllReviewsByCustomerId(Long customerId) {
        List<MenuMealReview> reviews = menuMealReviewRepository.findByCustomerIdWithCustomerAndMenuMeal(customerId);
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    public PagedResponse<MenuMealReviewResponse> listFilteredPaged(int page, int size, String status, String q) {
        Pageable pageable = PageRequest.of(page, size);
        // Sửa call để xóa fromDate và toDate
        Page<MenuMealReview> reviewsPage = menuMealReviewRepository.findAllFiltered(pageable, status, q);
        List<MenuMealReviewResponse> items = reviewsPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        
        PagedResponse<MenuMealReviewResponse> response = new PagedResponse<>();
        response.setItems(items);
        response.setTotal(reviewsPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        return response;
    }

    @Override
    public List<MenuMealReviewResponse> listAll() {
        List<MenuMealReview> reviews = menuMealReviewRepository.findAll();
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Override
    public PagedResponse<MenuMealReviewResponse> getPagedReviewsByMenuMealId(Long menuMealId, int page, int size, String status, String q) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MenuMealReview> reviewsPage = menuMealReviewRepository.findFilteredByMenuMealId(menuMealId, pageable, status, q);
        List<MenuMealReviewResponse> items = reviewsPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        
        PagedResponse<MenuMealReviewResponse> response = new PagedResponse<>();
        response.setItems(items);
        response.setTotal(reviewsPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        return response;
    }
    
    // Helper method to convert entity to response DTO
    private MenuMealReviewResponse toResponse(MenuMealReview review) {
        MenuMealReviewResponse response = new MenuMealReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        
        if (review.getMenuMeal() != null) {
            response.setMenuMealId(review.getMenuMeal().getId());
            response.setMenuMealTitle(review.getMenuMeal().getTitle());
        }
        
        if (review.getCustomer() != null) {
            response.setCustomerId(review.getCustomer().getId());
            response.setCustomerName(review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName());
        }
        
        return response;
    }

}
