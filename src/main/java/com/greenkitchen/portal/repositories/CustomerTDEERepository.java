package com.greenkitchen.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.greenkitchen.portal.entities.CustomerTDEE;
import java.util.List;

@Repository
public interface CustomerTDEERepository extends JpaRepository<CustomerTDEE, Long> {
  List<CustomerTDEE> findByCustomerIdOrderByCalculationDateDesc(Long customerId);
}
