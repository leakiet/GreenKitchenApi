package com.greenkitchen.portal.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 5)
    private String country;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean lunar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Column(length = 512)
    private String description;

    public enum RecurrenceType {
        NONE,
        YEARLY_GREGORIAN
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isLunar() { return lunar; }
    public void setLunar(boolean lunar) { this.lunar = lunar; }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}


