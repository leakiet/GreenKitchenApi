package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.FavoriteProtein;
import java.util.List;

@Repository
public interface FavoriteProteinRepository extends JpaRepository<FavoriteProtein, Long> {
    List<FavoriteProtein> findByCustomerReferenceId(Long customerReferenceId);
    void deleteByCustomerReferenceId(Long customerReferenceId);
}
