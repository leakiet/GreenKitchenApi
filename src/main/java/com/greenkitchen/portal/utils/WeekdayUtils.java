package com.greenkitchen.portal.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekdayUtils {
  public static String toVietnameseDay(LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    switch (dayOfWeek) {
      case MONDAY:
        return "T2";
      case TUESDAY:
        return "T3";
      case WEDNESDAY:
        return "T4";
      case THURSDAY:
        return "T5";
      case FRIDAY:
        return "T6";
      case SATURDAY:
        return "T7";
      case SUNDAY:
        return "CN";
      default:
        return "";
    }
  }
}
