package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.FavoriteCarb;
import java.util.List;

@Repository
public interface FavoriteCarbRepository extends JpaRepository<FavoriteCarb, Long> {
    List<FavoriteCarb> findByCustomerReferenceId(Long customerReferenceId);
    void deleteByCustomerReferenceId(Long customerReferenceId);
}
