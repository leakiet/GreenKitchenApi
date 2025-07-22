package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.CustomerHealthInfoRequest;
import com.greenkitchen.portal.dtos.CustomerHealthInfoResponse;

public interface CustomerHealthInfoService {
    CustomerHealthInfoResponse getByCustomerId(Long customerId);
    CustomerHealthInfoResponse create(CustomerHealthInfoRequest request);
    CustomerHealthInfoResponse update(Long customerId, CustomerHealthInfoRequest request);
}
