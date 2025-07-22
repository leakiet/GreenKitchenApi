package com.greenkitchen.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.SavedFoodMix;

@Repository
public interface SavedFoodMixRepository extends JpaRepository<SavedFoodMix, Long> {
    List<SavedFoodMix> findAllByCustomerId(Long customerId);
}
