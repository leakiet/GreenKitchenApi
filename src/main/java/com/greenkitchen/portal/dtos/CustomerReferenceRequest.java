package com.greenkitchen.portal.dtos;

import com.greenkitchen.portal.enums.VegetarianType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.greenkitchen.portal.entities.FavoriteProtein;
import com.greenkitchen.portal.entities.FavoriteCarb;
import com.greenkitchen.portal.entities.FavoriteVegetable;
import com.greenkitchen.portal.entities.CustomerAllergy;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerReferenceRequest {
    
    private Long customerId;
    private VegetarianType vegetarianType;
    private Boolean canEatEggs;
    private Boolean canEatDairy;
    private String note;
    private List<FavoriteProtein> favoriteProteins;
    private List<FavoriteCarb> favoriteCarbs;
    private List<FavoriteVegetable> favoriteVegetables;
    private List<CustomerAllergy> allergies;
}
