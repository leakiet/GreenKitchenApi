package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.StoreLocation;

@Repository
public interface StoreLocationRepository extends JpaRepository<StoreLocation, Long> {
    boolean existsByName(String name);
}


