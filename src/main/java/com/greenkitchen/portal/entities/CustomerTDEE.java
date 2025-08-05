package com.greenkitchen.portal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import com.greenkitchen.portal.enums.Gender;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.greenkitchen.portal.enums.ActivityLevel_v2;
import com.greenkitchen.portal.enums.Goal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_tdees")
public class CustomerTDEE {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  @JsonBackReference
  private Customer customer;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private Gender gender;

  @Column(name = "age")
  private Integer age;

  @Column(name = "height")
  private Double height;

  @Column(name = "weight")
  private Double weight;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_level")
  private ActivityLevel_v2 activityLevel;

  @Column(name = "bmr")
  private Double bmr;

  @Column(name = "tdee")
  private Double tdee;

  @Enumerated(EnumType.STRING)
  @Column(name = "goal")
  private Goal goal;

  @Column(name = "body_fat_percentage")
  private Double bodyFatPercentage;

  @Column(name = "calculation_date")
  private LocalDateTime calculationDate;

  @PrePersist
  protected void onSave() {
    this.calculationDate = LocalDateTime.now();
  }
}
