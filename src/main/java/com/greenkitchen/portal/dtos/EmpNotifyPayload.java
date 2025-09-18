package com.greenkitchen.portal.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpNotifyPayload {
    private Long conversationId;
    private String status;
    private String triggeredBy;
    private LocalDateTime timestamp;
}




