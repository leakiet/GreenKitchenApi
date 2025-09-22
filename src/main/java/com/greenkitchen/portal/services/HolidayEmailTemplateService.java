package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.EmailTemplate;
import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.entities.Holiday;

public interface HolidayEmailTemplateService {
    
    /**
     * Generate email template based on holiday type
     */
    EmailTemplate generateTemplate(Holiday holiday);
    
    /**
     * Generate template for specific holiday type
     */
    EmailTemplate generateTemplate(Holiday holiday, String templateType);
    
    /**
     * Generate email template based on holiday DTO
     */
    EmailTemplate generateTemplate(HolidayDto holidayDto);
    
    /**
     * Generate template for specific holiday type from DTO
     */
    EmailTemplate generateTemplate(HolidayDto holidayDto, String templateType);
    
    /**
     * Get available template types
     */
    String[] getAvailableTemplateTypes();
}
