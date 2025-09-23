package com.greenkitchen.portal.dtos;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AITopicsResponse {
    private List<String> topics;
    private String status; // success | error
    private String message;
}


