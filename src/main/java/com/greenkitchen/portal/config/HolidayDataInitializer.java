package com.greenkitchen.portal.config;

import com.greenkitchen.portal.entities.Holiday;
import com.greenkitchen.portal.repositories.HolidayRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class HolidayDataInitializer {

    @Bean
    CommandLineRunner initHolidays(HolidayRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            // NOTE: Lunar new year (Tết) varies yearly; here we prefill for a few years.
            // Future years should be added via admin UI or batch import.
            Holiday tet2025 = new Holiday();
            tet2025.setName("Tết Nguyên Đán 2025");
            tet2025.setCountry("VN");
            tet2025.setDate(LocalDate.of(2025, 1, 29)); // 2025-01-29 (approx start)
            tet2025.setLunar(true);
            tet2025.setRecurrenceType(Holiday.RecurrenceType.NONE);
            tet2025.setDescription("Lunar New Year (start)");

            Holiday blackFriday2025 = new Holiday();
            blackFriday2025.setName("Black Friday 2025");
            blackFriday2025.setCountry("VN");
            blackFriday2025.setDate(LocalDate.of(2025, 11, 28));
            blackFriday2025.setLunar(false);
            blackFriday2025.setRecurrenceType(Holiday.RecurrenceType.NONE);
            blackFriday2025.setDescription("Black Friday");

            Holiday christmas = new Holiday();
            christmas.setName("Christmas");
            christmas.setCountry("VN");
            christmas.setDate(LocalDate.of(LocalDate.now().getYear(), 12, 25));
            christmas.setLunar(false);
            christmas.setRecurrenceType(Holiday.RecurrenceType.YEARLY_GREGORIAN);
            christmas.setDescription("Christmas Day");

            Holiday nationalDay = new Holiday();
            nationalDay.setName("Vietnam National Day");
            nationalDay.setCountry("VN");
            nationalDay.setDate(LocalDate.of(LocalDate.now().getYear(), 9, 2));
            nationalDay.setLunar(false);
            nationalDay.setRecurrenceType(Holiday.RecurrenceType.YEARLY_GREGORIAN);
            nationalDay.setDescription("Quốc khánh 2/9");

            repo.saveAll(List.of(tet2025, blackFriday2025, christmas, nationalDay));
        };
    }
}


