package com.greenkitchen.portal.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.MenuMealResponse;
import com.greenkitchen.portal.dtos.WeekMealDayRequest;
import com.greenkitchen.portal.dtos.WeekMealDayResponse;
import com.greenkitchen.portal.dtos.WeekMealRequest;
import com.greenkitchen.portal.dtos.WeekMealResponse;
import com.greenkitchen.portal.entities.WeekMeal;
import com.greenkitchen.portal.entities.WeekMealDay;
import com.greenkitchen.portal.enums.MenuMealType;
import com.greenkitchen.portal.repositories.MenuMealRepository;
import com.greenkitchen.portal.repositories.WeekMealRepository;
import com.greenkitchen.portal.services.WeekMealService;

@Service
public class WeekMealServiceImpl implements WeekMealService {
  @Autowired
  private WeekMealRepository weekMealRepository;

  @Autowired
  private MenuMealRepository menuMealRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public WeekMealResponse getWeekMealByTypeAndDate(String type, String date) {
    var typeEnum = MenuMealType.valueOf(type.toUpperCase());

    LocalDate inputDate = LocalDate.parse(date);
    LocalDate weekStart = inputDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    var weekMealOpt = weekMealRepository.findByTypeAndWeekStart(typeEnum, weekStart);
    if (weekMealOpt.isEmpty())
      return null;

    WeekMeal weekMeal = weekMealOpt.get();
    WeekMealResponse response = modelMapper.map(weekMeal, WeekMealResponse.class);

    var dayResponses = new ArrayList<WeekMealDayResponse>();
    for (WeekMealDay day : weekMeal.getDays()) {
      var dayRes = new WeekMealDayResponse();
      dayRes.setDay(day.getDay());
      dayRes.setDate(day.getDate());
      dayRes.setMeal1(modelMapper.map(day.getMeal1(), MenuMealResponse.class));
      if (day.getMeal1().getNutrition() != null) {
        dayRes.getMeal1().setCalories(day.getMeal1().getNutrition().getCalories());
        dayRes.getMeal1().setProtein(day.getMeal1().getNutrition().getProtein());
        dayRes.getMeal1().setCarbs(day.getMeal1().getNutrition().getCarbs());
        dayRes.getMeal1().setFat(day.getMeal1().getNutrition().getFat());
      }
      dayRes.setMeal2(modelMapper.map(day.getMeal2(), MenuMealResponse.class));
      if (day.getMeal2().getNutrition() != null) {
        dayRes.getMeal2().setCalories(day.getMeal2().getNutrition().getCalories());
        dayRes.getMeal2().setProtein(day.getMeal2().getNutrition().getProtein());
        dayRes.getMeal2().setCarbs(day.getMeal2().getNutrition().getCarbs());
        dayRes.getMeal2().setFat(day.getMeal2().getNutrition().getFat());
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

    WeekMeal weekMeal = modelMapper.map(request, WeekMeal.class);

    List<WeekMealDay> days = new ArrayList<>();
    for (WeekMealDayRequest d : request.getDays()) {
      WeekMealDay day = modelMapper.map(d, WeekMealDay.class);
      day.setMeal1(menuMealRepository.findById(d.getMeal1()).orElseThrow());
      day.setMeal2(menuMealRepository.findById(d.getMeal2()).orElseThrow());
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
    WeekMealResponse response = modelMapper.map(weekMeal, WeekMealResponse.class);

    var dayResponses = new ArrayList<WeekMealDayResponse>();
    for (WeekMealDay day : weekMeal.getDays()) {
      var dayRes = new WeekMealDayResponse();
      dayRes.setDay(day.getDay());
      dayRes.setDate(day.getDate());
      dayRes.setMeal1(modelMapper.map(day.getMeal1(), MenuMealResponse.class));
      if (day.getMeal1().getNutrition() != null) {
        dayRes.getMeal1().setCalories(day.getMeal1().getNutrition().getCalories());
        dayRes.getMeal1().setProtein(day.getMeal1().getNutrition().getProtein());
        dayRes.getMeal1().setCarbs(day.getMeal1().getNutrition().getCarbs());
        dayRes.getMeal1().setFat(day.getMeal1().getNutrition().getFat());
      }
      dayRes.setMeal2(modelMapper.map(day.getMeal2(), MenuMealResponse.class));
      if (day.getMeal2().getNutrition() != null) {
        dayRes.getMeal2().setCalories(day.getMeal2().getNutrition().getCalories());
        dayRes.getMeal2().setProtein(day.getMeal2().getNutrition().getProtein());
        dayRes.getMeal2().setCarbs(day.getMeal2().getNutrition().getCarbs());
        dayRes.getMeal2().setFat(day.getMeal2().getNutrition().getFat());
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
    boolean isSame =
        weekMeal.getType().name().equalsIgnoreCase(request.getType()) &&
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
                !dbDay.getMeal2().getId().equals(reqDay.getMeal2())) {
                isSame = false;
                break;
            }
        }
    } else {
        isSame = false;
    }

    if (isSame) {
        // Không update, trả về dữ liệu hiện tại
        return modelMapper.map(weekMeal, WeekMealResponse.class);
    }

    weekMeal.setType(MenuMealType.valueOf(request.getType().toUpperCase()));
    weekMeal.setWeekStart(request.getWeekStart());
    weekMeal.setWeekEnd(request.getWeekEnd());

    List<Long> allMealIds = new ArrayList<>();
    for (WeekMealDayRequest d : request.getDays()) {
      allMealIds.add(d.getMeal1());
      allMealIds.add(d.getMeal2());
    }
    List<WeekMealDay> existingDays = weekMealRepository.findAll().stream()
        .flatMap(wm -> wm.getDays().stream())
        .filter(day -> day.getWeekMeal().getId() != request.getId())
        .collect(Collectors.toList());
    for (Long mealId : allMealIds) {
      if (existingDays.stream().anyMatch(day -> (day.getMeal1() != null && day.getMeal1().getId().equals(mealId)) ||
          (day.getMeal2() != null && day.getMeal2().getId().equals(mealId)))) {
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
      day.setWeekMeal(weekMeal);
      updatedDays.add(day);
    }

    weekMeal.getDays().clear();
    weekMeal.getDays().addAll(updatedDays);

    WeekMeal savedWeekMeal = weekMealRepository.save(weekMeal);
    // Trả về DTO bằng modelMapper
    return modelMapper.map(savedWeekMeal, WeekMealResponse.class);
  }
}
