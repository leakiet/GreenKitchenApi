package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrendingMenusResponse {
  private List<MenuItem> menus;

  @Getter
  @Setter
  public static class MenuItem {
    private String name;
    private int orderCount;
    private double price;
    private String imageUrl;

  }
}