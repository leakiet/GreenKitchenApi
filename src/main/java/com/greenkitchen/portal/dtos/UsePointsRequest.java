package com.greenkitchen.portal.dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsePointsRequest {
  private Long customerId;
  private BigDecimal pointsToUse;
  private String description;
}
