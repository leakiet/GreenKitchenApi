package com.greenkitchen.portal.dtos;

import org.checkerframework.checker.units.qual.A;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    private Long id;
    private String status;
}
