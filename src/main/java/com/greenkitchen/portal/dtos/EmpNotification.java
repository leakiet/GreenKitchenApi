// EmpNotification.java
package com.greenkitchen.portal.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpNotification {
    private Long conversationId;
    private Long employeeId;   // id nhân viên được ghép
    private Long customerId;   // id khách (null khi chuyển về AI)
}
