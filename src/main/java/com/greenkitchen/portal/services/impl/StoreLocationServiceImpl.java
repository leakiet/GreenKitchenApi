package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenkitchen.portal.entities.StoreLocation;
import com.greenkitchen.portal.repositories.StoreLocationRepository;
import com.greenkitchen.portal.services.StoreLocationService;

@Service
@Transactional
public class StoreLocationServiceImpl implements StoreLocationService {

    @Autowired
    private StoreLocationRepository repository;

    @Override
    public StoreLocation create(StoreLocation storeLocation) {
        return repository.save(storeLocation);
    }

    @Override
    public StoreLocation update(Long id, StoreLocation storeLocation) {
        StoreLocation existing = repository.findById(id).orElseThrow();
        existing.setName(storeLocation.getName());
        existing.setAddress(storeLocation.getAddress());
        existing.setLatitude(storeLocation.getLatitude());
        existing.setLongitude(storeLocation.getLongitude());
        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public StoreLocation findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<StoreLocation> findAll() {
        return repository.findAll();
    }
}


