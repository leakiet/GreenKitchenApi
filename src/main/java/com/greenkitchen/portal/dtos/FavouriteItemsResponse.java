package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavouriteItemsResponse {
  private List<FavouriteItem> items;

  // Getter & Setter
  @Getter
  @Setter
  public static class FavouriteItem {
    private String name;
    private double price;
    private String description;
    private String imageUrl;

    // Getter & Setter
  }
}
