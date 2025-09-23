package com.greenkitchen.portal.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.WeekMealDayRequest;
import com.greenkitchen.portal.dtos.WeekMealDayResponse;
import com.greenkitchen.portal.dtos.WeekMealDayUpdateRequest;
import com.greenkitchen.portal.dtos.WeekMealRequest;
import com.greenkitchen.portal.dtos.WeekMealResponse;
import com.greenkitchen.portal.entities.MenuMeal;
import com.greenkitchen.portal.entities.WeekMeal;
import com.greenkitchen.portal.entities.WeekMealDay;
import com.greenkitchen.portal.enums.MenuMealType;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.repositories.WeekMealDayRepository;
import com.greenkitchen.portal.repositories.WeekMealRepository;
import com.greenkitchen.portal.services.WeekMealService;

@Service
public class WeekMealServiceImpl implements WeekMealService {
  @Autowired
  private WeekMealRepository weekMealRepository;

  @Autowired
  private MenuMealRepository menuMealRepository;

  @Autowired
  private WeekMealDayRepository weekMealDayRepository; // Thêm nếu chưa có

  @Override
  public WeekMealDayResponse getWeekMealDayById(Long weekMealId, Long dayId) {
    WeekMealDay day = weekMealDayRepository.findById(dayId)
        .orElseThrow(() -> new RuntimeException("WeekMealDay not found"));

    if (!day.getWeekMeal().getId().equals(weekMealId)) {
      throw new RuntimeException("Day does not belong to the specified WeekMeal");
    }

    // Map to response manually
    WeekMealDayResponse response = new WeekMealDayResponse();
    response.setId(day.getId());
    response.setDay(day.getDay());
    response.setDate(day.getDate());
    response.setType(day.getWeekMeal().getType().name()); // Convert enum thành String

    // Set meal1
    if (day.getMeal1() != null) {
      MenuMealResponse meal1Response = new MenuMealResponse();
      meal1Response.setId(day.getMeal1().getId());
      meal1Response.setTitle(day.getMeal1().getTitle());
      meal1Response.setDescription(day.getMeal1().getDescription());
      meal1Response.setImage(day.getMeal1().getImage());
      meal1Response.setPrice(day.getMeal1().getPrice());
      meal1Response.setSlug(day.getMeal1().getSlug());
      meal1Response.setStock(day.getMeal1().getStock());
      meal1Response.setSoldCount(day.getMeal1().getSoldCount());
      meal1Response.setType(day.getMeal1().getType());
      meal1Response.setMenuIngredients(day.getMeal1().getMenuIngredients());
      if (day.getMeal1().getNutrition() != null) {
        meal1Response.setCalories(day.getMeal1().getNutrition().getCalories());
        meal1Response.setProtein(day.getMeal1().getNutrition().getProtein());
        meal1Response.setCarbs(day.getMeal1().getNutrition().getCarbs());
        meal1Response.setFat(day.getMeal1().getNutrition().getFat());
      }
      response.setMeal1(meal1Response);
    }

    // Set meal2
    if (day.getMeal2() != null) {
      MenuMealResponse meal2Response = new MenuMealResponse();
      meal2Response.setId(day.getMeal2().getId());
      meal2Response.setTitle(day.getMeal2().getTitle());
      meal2Response.setDescription(day.getMeal2().getDescription());
      meal2Response.setImage(day.getMeal2().getImage());
      meal2Response.setPrice(day.getMeal2().getPrice());
      meal2Response.setSlug(day.getMeal2().getSlug());
      meal2Response.setStock(day.getMeal2().getStock());
      meal2Response.setSoldCount(day.getMeal2().getSoldCount());
      meal2Response.setType(day.getMeal2().getType());
      meal2Response.setMenuIngredients(day.getMeal2().getMenuIngredients());
      if (day.getMeal2().getNutrition() != null) {
        meal2Response.setCalories(day.getMeal2().getNutrition().getCalories());
        meal2Response.setProtein(day.getMeal2().getNutrition().getProtein());
        meal2Response.setCarbs(day.getMeal2().getNutrition().getCarbs());
        meal2Response.setFat(day.getMeal2().getNutrition().getFat());
      }
      response.setMeal2(meal2Response);
    }

    // Set meal3
    if (day.getMeal3() != null) {
      MenuMealResponse meal3Response = new MenuMealResponse();
      meal3Response.setId(day.getMeal3().getId());
      meal3Response.setTitle(day.getMeal3().getTitle());
      meal3Response.setDescription(day.getMeal3().getDescription());
      meal3Response.setImage(day.getMeal3().getImage());
      meal3Response.setPrice(day.getMeal3().getPrice());
      meal3Response.setSlug(day.getMeal3().getSlug());
      meal3Response.setStock(day.getMeal3().getStock());
      meal3Response.setSoldCount(day.getMeal3().getSoldCount());
      meal3Response.setType(day.getMeal3().getType());
      meal3Response.setMenuIngredients(day.getMeal3().getMenuIngredients());
      if (day.getMeal3().getNutrition() != null) {
        meal3Response.setCalories(day.getMeal3().getNutrition().getCalories());
        meal3Response.setProtein(day.getMeal3().getNutrition().getProtein());
        meal3Response.setCarbs(day.getMeal3().getNutrition().getCarbs());
        meal3Response.setFat(day.getMeal3().getNutrition().getFat());
      }
      response.setMeal3(meal3Response);
    }

    return response;
  }

  @Override
  public WeekMealDay updateWeekMealDay(Long weekMealId, Long dayId, WeekMealDayUpdateRequest request) {
    WeekMealDay day = weekMealDayRepository.findById(dayId)
        .orElseThrow(() -> new RuntimeException("WeekMealDay not found"));

    if (!day.getWeekMeal().getId().equals(weekMealId)) {
      throw new RuntimeException("Day does not belong to the specified WeekMeal");
    }

    if (request.getMeal1Id() != null) {
      MenuMeal meal1 = menuMealRepository.findById(request.getMeal1Id())
          .orElseThrow(() -> new RuntimeException("Meal1 not found"));
      day.setMeal1(meal1);
    }
    if (request.getMeal2Id() != null) {
      MenuMeal meal2 = menuMealRepository.findById(request.getMeal2Id())
          .orElseThrow(() -> new RuntimeException("Meal2 not found"));
      day.setMeal2(meal2);
    }
    if (request.getMeal3Id() != null) {
      MenuMeal meal3 = menuMealRepository.findById(request.getMeal3Id())
          .orElseThrow(() -> new RuntimeException("Meal3 not found"));
      day.setMeal3(meal3);
    }

    return weekMealDayRepository.save(day);
  }

  @Override
  public WeekMealResponse getWeekMealByTypeAndDate(String type, String date) {
    var typeEnum = MenuMealType.valueOf(type.toUpperCase());

    LocalDate inputDate = LocalDate.parse(date);
    LocalDate weekStart = inputDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    var weekMealOpt = weekMealRepository.findByTypeAndWeekStart(typeEnum, weekStart);
    if (weekMealOpt.isEmpty())
      return null;

    WeekMeal weekMeal = weekMealOpt.get();

    // Map WeekMeal to WeekMealResponse manually
    WeekMealResponse response = new WeekMealResponse();
    response.setId(weekMeal.getId());
    response.setType(weekMeal.getType().name());
    response.setWeekStart(weekMeal.getWeekStart());
    response.setWeekEnd(weekMeal.getWeekEnd());

    var dayResponses = new ArrayList<WeekMealDayResponse>();
    for (WeekMealDay day : weekMeal.getDays()) {
      var dayRes = new WeekMealDayResponse();
      dayRes.setId(day.getId());
      dayRes.setDay(day.getDay());
      dayRes.setDate(day.getDate());
      dayRes.setType(weekMeal.getType().name());

      // Set meal1 manually
      if (day.getMeal1() != null) {
        MenuMealResponse meal1Response = new MenuMealResponse();
        meal1Response.setId(day.getMeal1().getId());
        meal1Response.setTitle(day.getMeal1().getTitle());
        meal1Response.setDescription(day.getMeal1().getDescription());
        meal1Response.setImage(day.getMeal1().getImage());
        meal1Response.setPrice(day.getMeal1().getPrice());
        meal1Response.setSlug(day.getMeal1().getSlug());
        meal1Response.setStock(day.getMeal1().getStock());
        meal1Response.setSoldCount(day.getMeal1().getSoldCount());
        meal1Response.setType(day.getMeal1().getType());
        meal1Response.setMenuIngredients(day.getMeal1().getMenuIngredients());
        if (day.getMeal1().getNutrition() != null) {
          meal1Response.setCalories(day.getMeal1().getNutrition().getCalories());
          meal1Response.setProtein(day.getMeal1().getNutrition().getProtein());
          meal1Response.setCarbs(day.getMeal1().getNutrition().getCarbs());
          meal1Response.setFat(day.getMeal1().getNutrition().getFat());
        }
        dayRes.setMeal1(meal1Response);
      }

      // Set meal2 manually
      if (day.getMeal2() != null) {
        MenuMealResponse meal2Response = new MenuMealResponse();
        meal2Response.setId(day.getMeal2().getId());
        meal2Response.setTitle(day.getMeal2().getTitle());
        meal2Response.setDescription(day.getMeal2().getDescription());
        meal2Response.setImage(day.getMeal2().getImage());
        meal2Response.setPrice(day.getMeal2().getPrice());
        meal2Response.setSlug(day.getMeal2().getSlug());
        meal2Response.setStock(day.getMeal2().getStock());
        meal2Response.setSoldCount(day.getMeal2().getSoldCount());
        meal2Response.setType(day.getMeal2().getType());
        meal2Response.setMenuIngredients(day.getMeal2().getMenuIngredients());
        if (day.getMeal2().getNutrition() != null) {
          meal2Response.setCalories(day.getMeal2().getNutrition().getCalories());
          meal2Response.setProtein(day.getMeal2().getNutrition().getProtein());
          meal2Response.setCarbs(day.getMeal2().getNutrition().getCarbs());
          meal2Response.setFat(day.getMeal2().getNutrition().getFat());
        }
        dayRes.setMeal2(meal2Response);
      }

      // Set meal3 manually
      if (day.getMeal3() != null) {
        MenuMealResponse meal3Response = new MenuMealResponse();
        meal3Response.setId(day.getMeal3().getId());
        meal3Response.setTitle(day.getMeal3().getTitle());
        meal3Response.setDescription(day.getMeal3().getDescription());
        meal3Response.setImage(day.getMeal3().getImage());
        meal3Response.setPrice(day.getMeal3().getPrice());
        meal3Response.setSlug(day.getMeal3().getSlug());
        meal3Response.setStock(day.getMeal3().getStock());
        meal3Response.setSoldCount(day.getMeal3().getSoldCount());
        meal3Response.setType(day.getMeal3().getType());
        meal3Response.setMenuIngredients(day.getMeal3().getMenuIngredients());
        if (day.getMeal3().getNutrition() != null) {
          meal3Response.setCalories(day.getMeal3().getNutrition().getCalories());
          meal3Response.setProtein(day.getMeal3().getNutrition().getProtein());
          meal3Response.setCarbs(day.getMeal3().getNutrition().getCarbs());
          meal3Response.setFat(day.getMeal3().getNutrition().getFat());
        }
        dayRes.setMeal3(meal3Response);
      }

      dayResponses.add(dayRes);
    }
    response.setDays(dayResponses);

    return response;
  }

  @Override
  public WeekMeal createWeekMeal(WeekMealRequest request) {
    MenuMealType typeEnum = MenuMealType.valueOf(request.getType().toUpperCase());
    LocalDate weekStart = request.getWeekStart();

    // Kiểm tra đã tồn tại WeekMeal cho tuần này chưa
    if (weekMealRepository.findByTypeAndWeekStart(typeEnum, weekStart).isPresent()) {
      throw new IllegalArgumentException(
          "WeekMeal with type " + typeEnum + " for week starting " + weekStart + " already exists.");
    }

    // Map WeekMealRequest to WeekMeal manually
    WeekMeal weekMeal = new WeekMeal();
    weekMeal.setType(typeEnum);
    weekMeal.setWeekStart(request.getWeekStart());
    weekMeal.setWeekEnd(request.getWeekEnd());

    List<WeekMealDay> days = new ArrayList<>();
    for (WeekMealDayRequest d : request.getDays()) {
      // Map WeekMealDayRequest to WeekMealDay manually
      WeekMealDay day = new WeekMealDay();
      day.setDay(d.getDay());
      day.setDate(d.getDate());
      day.setMeal1(menuMealRepository.findById(d.getMeal1()).orElseThrow());
      day.setMeal2(menuMealRepository.findById(d.getMeal2()).orElseThrow());
      day.setMeal3(menuMealRepository.findById(d.getMeal3()).orElseThrow());
      day.setWeekMeal(weekMeal);
      days.add(day);
    }
    weekMeal.setDays(days);
    return weekMealRepository.save(weekMeal);
  }

  @Override
  public WeekMealResponse getWeekMealById(Long id) {
    var weekMealOpt = weekMealRepository.findById(id);
    if (weekMealOpt.isEmpty())
      return null;

    WeekMeal weekMeal = weekMealOpt.get();

    // Map WeekMeal to WeekMealResponse manually
    WeekMealResponse response = new WeekMealResponse();
    response.setId(weekMeal.getId());
    response.setType(weekMeal.getType().name());
    response.setWeekStart(weekMeal.getWeekStart());
    response.setWeekEnd(weekMeal.getWeekEnd());

    var dayResponses = new ArrayList<WeekMealDayResponse>();
    for (WeekMealDay day : weekMeal.getDays()) {
      var dayRes = new WeekMealDayResponse();
      dayRes.setId(day.getId());
      dayRes.setDay(day.getDay());
      dayRes.setDate(day.getDate());
      dayRes.setType(weekMeal.getType().name());

      // Set meal1 manually
      if (day.getMeal1() != null) {
        MenuMealResponse meal1Response = new MenuMealResponse();
        meal1Response.setId(day.getMeal1().getId());
        meal1Response.setTitle(day.getMeal1().getTitle());
        meal1Response.setDescription(day.getMeal1().getDescription());
        meal1Response.setImage(day.getMeal1().getImage());
        meal1Response.setPrice(day.getMeal1().getPrice());
        meal1Response.setSlug(day.getMeal1().getSlug());
        meal1Response.setStock(day.getMeal1().getStock());
        meal1Response.setSoldCount(day.getMeal1().getSoldCount());
        meal1Response.setType(day.getMeal1().getType());
        meal1Response.setMenuIngredients(day.getMeal1().getMenuIngredients());
        if (day.getMeal1().getNutrition() != null) {
          meal1Response.setCalories(day.getMeal1().getNutrition().getCalories());
          meal1Response.setProtein(day.getMeal1().getNutrition().getProtein());
          meal1Response.setCarbs(day.getMeal1().getNutrition().getCarbs());
          meal1Response.setFat(day.getMeal1().getNutrition().getFat());
        }
        dayRes.setMeal1(meal1Response);
      }

      // Set meal2 manually
      if (day.getMeal2() != null) {
        MenuMealResponse meal2Response = new MenuMealResponse();
        meal2Response.setId(day.getMeal2().getId());
        meal2Response.setTitle(day.getMeal2().getTitle());
        meal2Response.setDescription(day.getMeal2().getDescription());
        meal2Response.setImage(day.getMeal2().getImage());
        meal2Response.setPrice(day.getMeal2().getPrice());
        meal2Response.setSlug(day.getMeal2().getSlug());
        meal2Response.setStock(day.getMeal2().getStock());
        meal2Response.setSoldCount(day.getMeal2().getSoldCount());
        meal2Response.setType(day.getMeal2().getType());
        meal2Response.setMenuIngredients(day.getMeal2().getMenuIngredients());
        if (day.getMeal2().getNutrition() != null) {
          meal2Response.setCalories(day.getMeal2().getNutrition().getCalories());
          meal2Response.setProtein(day.getMeal2().getNutrition().getProtein());
          meal2Response.setCarbs(day.getMeal2().getNutrition().getCarbs());
          meal2Response.setFat(day.getMeal2().getNutrition().getFat());
        }
        dayRes.setMeal2(meal2Response);
      }

      // Set meal3 manually
      if (day.getMeal3() != null) {
        MenuMealResponse meal3Response = new MenuMealResponse();
        meal3Response.setId(day.getMeal3().getId());
        meal3Response.setTitle(day.getMeal3().getTitle());
        meal3Response.setDescription(day.getMeal3().getDescription());
        meal3Response.setImage(day.getMeal3().getImage());
        meal3Response.setPrice(day.getMeal3().getPrice());
        meal3Response.setSlug(day.getMeal3().getSlug());
        meal3Response.setStock(day.getMeal3().getStock());
        meal3Response.setSoldCount(day.getMeal3().getSoldCount());
        meal3Response.setType(day.getMeal3().getType());
        meal3Response.setMenuIngredients(day.getMeal3().getMenuIngredients());
        if (day.getMeal3().getNutrition() != null) {
          meal3Response.setCalories(day.getMeal3().getNutrition().getCalories());
          meal3Response.setProtein(day.getMeal3().getNutrition().getProtein());
          meal3Response.setCarbs(day.getMeal3().getNutrition().getCarbs());
          meal3Response.setFat(day.getMeal3().getNutrition().getFat());
        }
        dayRes.setMeal3(meal3Response);
      }

      dayResponses.add(dayRes);
    }
    response.setDays(dayResponses);

    return response;
  }

  @Override
  public void deleteWeekMeal(Long id) {
    WeekMeal weekMeal = weekMealRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("WeekMeal not found with id: " + id));
    weekMeal.setIsDeleted(true);
    if (weekMeal.getDays() != null) {
      for (WeekMealDay day : weekMeal.getDays()) {
        day.setIsDeleted(true);
      }
    }
    weekMealRepository.save(weekMeal);
  }

  @Override
  public WeekMealResponse updateWeekMeal(WeekMealRequest request) {
    if (request.getId() == null) {
      throw new IllegalArgumentException("WeekMeal id is required for update");
    }

    WeekMeal weekMeal = weekMealRepository.findById(request.getId())
        .orElseThrow(() -> new IllegalArgumentException("WeekMeal not found with id: " + request.getId()));

    // So sánh dữ liệu cơ bản
    boolean isSame = weekMeal.getType().name().equalsIgnoreCase(request.getType()) &&
        weekMeal.getWeekStart().equals(request.getWeekStart()) &&
        weekMeal.getWeekEnd().equals(request.getWeekEnd());

    // So sánh days
    if (isSame && weekMeal.getDays().size() == request.getDays().size()) {
      for (int i = 0; i < weekMeal.getDays().size(); i++) {
        WeekMealDay dbDay = weekMeal.getDays().get(i);
        WeekMealDayRequest reqDay = request.getDays().get(i);
        if (!dbDay.getDay().equals(reqDay.getDay()) ||
            !dbDay.getDate().equals(reqDay.getDate()) ||
            !dbDay.getMeal1().getId().equals(reqDay.getMeal1()) ||
            !dbDay.getMeal2().getId().equals(reqDay.getMeal2()) ||
            !dbDay.getMeal3().getId().equals(reqDay.getMeal3())) {
          isSame = false;
          break;
        }
      }
    } else {
      isSame = false;
    }

    if (isSame) {
      // Không update, trả về dữ liệu hiện tại
      WeekMealResponse response = new WeekMealResponse();
      response.setId(weekMeal.getId());
      response.setType(weekMeal.getType().name());
      response.setWeekStart(weekMeal.getWeekStart());
      response.setWeekEnd(weekMeal.getWeekEnd());
      return response;
    }

    weekMeal.setType(MenuMealType.valueOf(request.getType().toUpperCase()));
    weekMeal.setWeekStart(request.getWeekStart());
    weekMeal.setWeekEnd(request.getWeekEnd());

    List<Long> allMealIds = new ArrayList<>();
    for (WeekMealDayRequest d : request.getDays()) {
      allMealIds.add(d.getMeal1());
      allMealIds.add(d.getMeal2());
      allMealIds.add(d.getMeal3());
    }
    List<WeekMealDay> existingDays = weekMealRepository.findAll().stream()
        .flatMap(wm -> wm.getDays().stream())
        .filter(day -> day.getWeekMeal().getId() != request.getId())
        .collect(Collectors.toList());
    for (Long mealId : allMealIds) {
      if (existingDays.stream().anyMatch(day -> (day.getMeal1() != null && day.getMeal1().getId().equals(mealId)) ||
          (day.getMeal2() != null && day.getMeal2().getId().equals(mealId)) ||
          (day.getMeal3() != null && day.getMeal3().getId().equals(mealId)))) {
        throw new IllegalArgumentException("MenuMeal with id " + mealId + " is already used in another WeekMealDay");
      }
    }

    Map<LocalDate, WeekMealDay> existingDaysMap = weekMeal.getDays().stream()
        .collect(Collectors.toMap(WeekMealDay::getDate, day -> day));
    List<WeekMealDay> updatedDays = new ArrayList<>();

    for (WeekMealDayRequest d : request.getDays()) {
      WeekMealDay day = existingDaysMap.getOrDefault(d.getDate(), new WeekMealDay());
      day.setDay(d.getDay());
      day.setDate(d.getDate());
      day.setMeal1(menuMealRepository.findById(d.getMeal1())
          .orElseThrow(() -> new IllegalArgumentException("MenuMeal not found with id: " + d.getMeal1())));
      day.setMeal2(menuMealRepository.findById(d.getMeal2())
          .orElseThrow(() -> new IllegalArgumentException("MenuMeal not found with id: " + d.getMeal2())));
      day.setMeal3(menuMealRepository.findById(d.getMeal3())
          .orElseThrow(() -> new IllegalArgumentException("MenuMeal not found with id: " + d.getMeal3())));
      day.setWeekMeal(weekMeal);
      updatedDays.add(day);
    }

    weekMeal.getDays().clear();
    weekMeal.getDays().addAll(updatedDays);

    WeekMeal savedWeekMeal = weekMealRepository.save(weekMeal);

    // Map WeekMeal to WeekMealResponse manually
    WeekMealResponse response = new WeekMealResponse();
    response.setId(savedWeekMeal.getId());
    response.setType(savedWeekMeal.getType().name());
    response.setWeekStart(savedWeekMeal.getWeekStart());
    response.setWeekEnd(savedWeekMeal.getWeekEnd());

    List<WeekMealDayResponse> dayResponses = new ArrayList<>();
    for (WeekMealDay day : savedWeekMeal.getDays()) {
      WeekMealDayResponse dayRes = new WeekMealDayResponse();
      dayRes.setId(day.getId());
      dayRes.setDay(day.getDay());
      dayRes.setDate(day.getDate());
      dayRes.setType(savedWeekMeal.getType().name());

      // Set meal1 manually
      if (day.getMeal1() != null) {
        MenuMealResponse meal1Response = new MenuMealResponse();
        meal1Response.setId(day.getMeal1().getId());
        meal1Response.setTitle(day.getMeal1().getTitle());
        meal1Response.setDescription(day.getMeal1().getDescription());
        meal1Response.setImage(day.getMeal1().getImage());
        meal1Response.setPrice(day.getMeal1().getPrice());
        meal1Response.setSlug(day.getMeal1().getSlug());
        meal1Response.setStock(day.getMeal1().getStock());
        meal1Response.setSoldCount(day.getMeal1().getSoldCount());
        meal1Response.setType(day.getMeal1().getType());
        meal1Response.setMenuIngredients(day.getMeal1().getMenuIngredients());
        if (day.getMeal1().getNutrition() != null) {
          meal1Response.setCalories(day.getMeal1().getNutrition().getCalories());
          meal1Response.setProtein(day.getMeal1().getNutrition().getProtein());
          meal1Response.setCarbs(day.getMeal1().getNutrition().getCarbs());
          meal1Response.setFat(day.getMeal1().getNutrition().getFat());
        }
        dayRes.setMeal1(meal1Response);
      }

      // Set meal2 manually
      if (day.getMeal2() != null) {
        MenuMealResponse meal2Response = new MenuMealResponse();
        meal2Response.setId(day.getMeal2().getId());
        meal2Response.setTitle(day.getMeal2().getTitle());
        meal2Response.setDescription(day.getMeal2().getDescription());
        meal2Response.setImage(day.getMeal2().getImage());
        meal2Response.setPrice(day.getMeal2().getPrice());
        meal2Response.setSlug(day.getMeal2().getSlug());
        meal2Response.setStock(day.getMeal2().getStock());
        meal2Response.setSoldCount(day.getMeal2().getSoldCount());
        meal2Response.setType(day.getMeal2().getType());
        meal2Response.setMenuIngredients(day.getMeal2().getMenuIngredients());
        if (day.getMeal2().getNutrition() != null) {
          meal2Response.setCalories(day.getMeal2().getNutrition().getCalories());
          meal2Response.setProtein(day.getMeal2().getNutrition().getProtein());
          meal2Response.setCarbs(day.getMeal2().getNutrition().getCarbs());
          meal2Response.setFat(day.getMeal2().getNutrition().getFat());
        }
        dayRes.setMeal2(meal2Response);
      }

      // Set meal3 manually
      if (day.getMeal3() != null) {
        MenuMealResponse meal3Response = new MenuMealResponse();
        meal3Response.setId(day.getMeal3().getId());
        meal3Response.setTitle(day.getMeal3().getTitle());
        meal3Response.setDescription(day.getMeal3().getDescription());
        meal3Response.setImage(day.getMeal3().getImage());
        meal3Response.setPrice(day.getMeal3().getPrice());
        meal3Response.setSlug(day.getMeal3().getSlug());
        meal3Response.setStock(day.getMeal3().getStock());
        meal3Response.setSoldCount(day.getMeal3().getSoldCount());
        meal3Response.setType(day.getMeal3().getType());
        meal3Response.setMenuIngredients(day.getMeal3().getMenuIngredients());
        if (day.getMeal3().getNutrition() != null) {
          meal3Response.setCalories(day.getMeal3().getNutrition().getCalories());
          meal3Response.setProtein(day.getMeal3().getNutrition().getProtein());
          meal3Response.setCarbs(day.getMeal3().getNutrition().getCarbs());
          meal3Response.setFat(day.getMeal3().getNutrition().getFat());
        }
        dayRes.setMeal3(meal3Response);
      }

      dayResponses.add(dayRes);
    }
    response.setDays(dayResponses);

    return response;
  }
}
