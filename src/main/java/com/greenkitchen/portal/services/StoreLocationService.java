package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.entities.StoreLocation;

public interface StoreLocationService {
    StoreLocation create(StoreLocation storeLocation);
    StoreLocation update(Long id, StoreLocation storeLocation);
    void delete(Long id);
    StoreLocation findById(Long id);
    List<StoreLocation> findAll();
}


