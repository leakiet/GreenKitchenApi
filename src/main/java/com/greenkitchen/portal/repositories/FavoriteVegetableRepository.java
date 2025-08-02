package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.FavoriteVegetable;
import java.util.List;

@Repository
public interface FavoriteVegetableRepository extends JpaRepository<FavoriteVegetable, Long> {
    List<FavoriteVegetable> findByCustomerReferenceId(Long customerReferenceId);
    void deleteByCustomerReferenceId(Long customerReferenceId);
}
