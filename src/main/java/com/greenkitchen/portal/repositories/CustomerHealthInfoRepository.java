package com.greenkitchen.portal.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenkitchen.portal.entities.CustomerHealthInfo;

@Repository
public interface CustomerHealthInfoRepository extends JpaRepository<CustomerHealthInfo, Long> {
    Optional<CustomerHealthInfo> findByCustomerId(Long customerId);
}
