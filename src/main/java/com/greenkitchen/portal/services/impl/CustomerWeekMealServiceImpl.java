package com.greenkitchen.portal.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.CustomerWeekMealDayRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealDayResponse;
import com.greenkitchen.portal.dtos.CustomerWeekMealDayUpdateRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealRequest;
import com.greenkitchen.portal.dtos.CustomerWeekMealResponse;
import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerWeekMeal;
import com.greenkitchen.portal.entities.CustomerWeekMealDay;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.enums.MenuMealType;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.repositories.CustomerWeekMealRepository;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.services.CustomerWeekMealService;

@Service
public class CustomerWeekMealServiceImpl implements CustomerWeekMealService {

    @Autowired
    private CustomerWeekMealRepository customerWeekMealRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MenuMealRepository menuMealRepository;

    @Override
    public CustomerWeekMeal createCustomerWeekMeal(CustomerWeekMealRequest request) {
        MenuMealType typeEnum = MenuMealType.valueOf(request.getType().toUpperCase());
        LocalDate weekStart = request.getWeekStart();

        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + request.getCustomerId()));

        // Map CustomerWeekMealRequest to CustomerWeekMeal manually
        CustomerWeekMeal customerWeekMeal = new CustomerWeekMeal();
        customerWeekMeal.setType(typeEnum);
        customerWeekMeal.setWeekStart(request.getWeekStart());
        customerWeekMeal.setWeekEnd(request.getWeekEnd());
        customerWeekMeal.setCustomer(customer);

        List<CustomerWeekMealDay> days = new ArrayList<>();
        for (CustomerWeekMealDayRequest d : request.getDays()) {
            // Map CustomerWeekMealDayRequest to CustomerWeekMealDay manually
            CustomerWeekMealDay day = new CustomerWeekMealDay();
            day.setDay(d.getDay());
            day.setDate(d.getDate());

            if (d.getMeal1() != null) {
                day.setMeal1(menuMealRepository.findById(d.getMeal1()).orElseThrow());
            }
            if (d.getMeal2() != null) {
                day.setMeal2(menuMealRepository.findById(d.getMeal2()).orElseThrow());
            }
            if (d.getMeal3() != null) {
                day.setMeal3(menuMealRepository.findById(d.getMeal3()).orElseThrow());
            }

            day.setCustomerWeekMeal(customerWeekMeal);
            days.add(day);
        }
        customerWeekMeal.setDays(days);
        return customerWeekMealRepository.save(customerWeekMeal);
    }

    @Override
    public CustomerWeekMealResponse getCustomerWeekMealByCustomerIdAndTypeAndDate(Long customerId, String type, String date) {
        var typeEnum = MenuMealType.valueOf(type.toUpperCase());
        LocalDate inputDate = LocalDate.parse(date);
        LocalDate weekStart = inputDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        var customerWeekMealOpt = customerWeekMealRepository.findByCustomerIdAndTypeAndWeekStart(customerId, typeEnum, weekStart);
        if (customerWeekMealOpt.isEmpty())
            return null;

        CustomerWeekMeal customerWeekMeal = customerWeekMealOpt.get();
        return mapToCustomerWeekMealResponse(customerWeekMeal);
    }

    @Override
    public CustomerWeekMealResponse updateCustomerWeekMeal(CustomerWeekMealRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("CustomerWeekMeal id is required for update");
        }

        CustomerWeekMeal existingCustomerWeekMeal = customerWeekMealRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalArgumentException("CustomerWeekMeal not found with id: " + request.getId()));

        // Update basic fields
        existingCustomerWeekMeal.setWeekStart(request.getWeekStart());
        existingCustomerWeekMeal.setWeekEnd(request.getWeekEnd());
        existingCustomerWeekMeal.setType(MenuMealType.valueOf(request.getType().toUpperCase()));

        // Update days if provided
        if (request.getDays() != null && !request.getDays().isEmpty()) {
            // Remove existing days
            if (existingCustomerWeekMeal.getDays() != null) {
                existingCustomerWeekMeal.getDays().clear();
            }

            List<CustomerWeekMealDay> days = new ArrayList<>();
            for (CustomerWeekMealDayRequest d : request.getDays()) {
                CustomerWeekMealDay day = new CustomerWeekMealDay();
                day.setDay(d.getDay());
                day.setDate(d.getDate());

                if (d.getMeal1() != null) {
                    day.setMeal1(menuMealRepository.findById(d.getMeal1()).orElseThrow());
                }
                if (d.getMeal2() != null) {
                    day.setMeal2(menuMealRepository.findById(d.getMeal2()).orElseThrow());
                }
                if (d.getMeal3() != null) {
                    day.setMeal3(menuMealRepository.findById(d.getMeal3()).orElseThrow());
                }

                day.setCustomerWeekMeal(existingCustomerWeekMeal);
                days.add(day);
            }
            existingCustomerWeekMeal.setDays(days);
        }

        CustomerWeekMeal saved = customerWeekMealRepository.save(existingCustomerWeekMeal);
        return mapToCustomerWeekMealResponse(saved);
    }

    @Override
    public CustomerWeekMealResponse getCustomerWeekMealById(Long id) {
        var customerWeekMealOpt = customerWeekMealRepository.findById(id);
        if (customerWeekMealOpt.isEmpty())
            return null;

        CustomerWeekMeal customerWeekMeal = customerWeekMealOpt.get();
        return mapToCustomerWeekMealResponse(customerWeekMeal);
    }

    @Override
    public void deleteCustomerWeekMeal(Long id) {
        CustomerWeekMeal customerWeekMeal = customerWeekMealRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("CustomerWeekMeal not found with id: " + id));
        customerWeekMeal.setIsDeleted(true);
        if (customerWeekMeal.getDays() != null) {
            for (CustomerWeekMealDay day : customerWeekMeal.getDays()) {
                day.setIsDeleted(true);
            }
        }
        customerWeekMealRepository.save(customerWeekMeal);
    }

    @Override
    public CustomerWeekMealDay updateCustomerWeekMealDay(Long customerWeekMealId, Long dayId, CustomerWeekMealDayUpdateRequest request) {
        CustomerWeekMeal customerWeekMeal = customerWeekMealRepository.findById(customerWeekMealId)
            .orElseThrow(() -> new RuntimeException("CustomerWeekMeal not found"));

        CustomerWeekMealDay day = customerWeekMeal.getDays().stream()
            .filter(d -> d.getId().equals(dayId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Day not found"));

        if (request.getMeal1Id() != null) {
            MenuMeal meal1 = menuMealRepository.findById(request.getMeal1Id())
                .orElseThrow(() -> new RuntimeException("MenuMeal not found for meal1"));
            day.setMeal1(meal1);
        }

        if (request.getMeal2Id() != null) {
            MenuMeal meal2 = menuMealRepository.findById(request.getMeal2Id())
                .orElseThrow(() -> new RuntimeException("MenuMeal not found for meal2"));
            day.setMeal2(meal2);
        }

        if (request.getMeal3Id() != null) {
            MenuMeal meal3 = menuMealRepository.findById(request.getMeal3Id())
                .orElseThrow(() -> new RuntimeException("MenuMeal not found for meal3"));
            day.setMeal3(meal3);
        }

        return customerWeekMealRepository.save(customerWeekMeal).getDays().stream()
            .filter(d -> d.getId().equals(dayId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to update day"));
    }

    @Override
    public CustomerWeekMealDayResponse getCustomerWeekMealDayById(Long customerWeekMealId, Long dayId) {
        CustomerWeekMeal customerWeekMeal = customerWeekMealRepository.findById(customerWeekMealId)
            .orElseThrow(() -> new RuntimeException("CustomerWeekMeal not found"));

        CustomerWeekMealDay day = customerWeekMeal.getDays().stream()
            .filter(d -> d.getId().equals(dayId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Day not found"));

        return mapToCustomerWeekMealDayResponse(day);
    }

    @Override
    public List<CustomerWeekMealResponse> getCustomerWeekMealsByCustomerId(Long customerId) {
        List<CustomerWeekMeal> customerWeekMeals = customerWeekMealRepository.findByCustomerId(customerId);
        return customerWeekMeals.stream()
            .map(this::mapToCustomerWeekMealResponse)
            .toList();
    }

    @Override
    public List<CustomerWeekMealResponse> getCustomerWeekMealsByCustomerIdAndType(Long customerId, String type) {
        var typeEnum = MenuMealType.valueOf(type.toUpperCase());
        List<CustomerWeekMeal> customerWeekMeals = customerWeekMealRepository.findByCustomerIdAndType(customerId, typeEnum);
        return customerWeekMeals.stream()
            .map(this::mapToCustomerWeekMealResponse)
            .toList();
    }

    private CustomerWeekMealResponse mapToCustomerWeekMealResponse(CustomerWeekMeal customerWeekMeal) {
        CustomerWeekMealResponse response = new CustomerWeekMealResponse();
        response.setId(customerWeekMeal.getId());
        response.setCustomerId(customerWeekMeal.getCustomer().getId());
        response.setType(customerWeekMeal.getType().name());
        response.setWeekStart(customerWeekMeal.getWeekStart());
        response.setWeekEnd(customerWeekMeal.getWeekEnd());

        var dayResponses = new ArrayList<CustomerWeekMealDayResponse>();
        for (CustomerWeekMealDay day : customerWeekMeal.getDays()) {
            dayResponses.add(mapToCustomerWeekMealDayResponse(day));
        }
        response.setDays(dayResponses);

        return response;
    }

    private CustomerWeekMealDayResponse mapToCustomerWeekMealDayResponse(CustomerWeekMealDay day) {
        CustomerWeekMealDayResponse response = new CustomerWeekMealDayResponse();
        response.setId(day.getId());
        response.setDay(day.getDay());
        response.setDate(day.getDate());
        response.setType(day.getCustomerWeekMeal().getType().name());

        // Set meal1
        if (day.getMeal1() != null) {
            response.setMeal1(mapToMenuMealResponse(day.getMeal1()));
        }

        // Set meal2
        if (day.getMeal2() != null) {
            response.setMeal2(mapToMenuMealResponse(day.getMeal2()));
        }

        // Set meal3
        if (day.getMeal3() != null) {
            response.setMeal3(mapToMenuMealResponse(day.getMeal3()));
        }

        return response;
    }

    private MenuMealResponse mapToMenuMealResponse(MenuMeal menuMeal) {
        MenuMealResponse response = new MenuMealResponse();
        response.setId(menuMeal.getId());
        response.setTitle(menuMeal.getTitle());
        response.setDescription(menuMeal.getDescription());
        response.setImage(menuMeal.getImage());
        response.setPrice(menuMeal.getPrice());
        response.setSlug(menuMeal.getSlug());
        response.setStock(menuMeal.getStock());
        response.setSoldCount(menuMeal.getSoldCount());
        response.setType(menuMeal.getType());
        // Avoid accessing menuIngredients to prevent lazy loading issues
        response.setMenuIngredients(null);

        // Avoid accessing nutrition to prevent potential lazy loading issues
        if (menuMeal.getNutrition() != null) {
            response.setCalories(menuMeal.getNutrition().getCalories());
            response.setProtein(menuMeal.getNutrition().getProtein());
            response.setCarbs(menuMeal.getNutrition().getCarbs());
            response.setFat(menuMeal.getNutrition().getFat());
        }

        // Don't set reviews to avoid serialization issues
        response.setReviews(null);

        return response;
    }
}
