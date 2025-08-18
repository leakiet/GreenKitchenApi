package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CustomerReferenceRequest;
import com.greenkitchen.portal.entities.CustomerReference;
import java.util.List;

public interface CustomerReferenceService {
    List<CustomerReference> getCustomerReferencesByCustomerId(Long customerId);
    CustomerReference createCustomerReference(CustomerReferenceRequest request);
    CustomerReference updateCustomerReference(CustomerReferenceRequest request);
}
