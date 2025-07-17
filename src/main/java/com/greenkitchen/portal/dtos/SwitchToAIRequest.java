// src/main/java/com/greenkitchen/portal/dtos/SwitchToAIRequest.java
package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwitchToAIRequest {
    private Long conversationId;
}
