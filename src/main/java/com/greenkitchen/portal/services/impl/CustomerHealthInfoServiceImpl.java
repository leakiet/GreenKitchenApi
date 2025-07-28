package com.greenkitchen.portal.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.CustomerHealthInfoRequest;
import com.greenkitchen.portal.dtos.CustomerHealthInfoResponse;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.entities.CustomerHealthInfo;
import com.greenkitchen.portal.repositories.CustomerHealthInfoRepository;
import com.greenkitchen.portal.repositories.CustomerRepository;
import com.greenkitchen.portal.services.CustomerHealthInfoService;
import com.greenkitchen.portal.services.CustomerService;

@Service
public class CustomerHealthInfoServiceImpl implements CustomerHealthInfoService {

  @Autowired
  private CustomerHealthInfoRepository customerHealthInfoRepository;

  @Autowired
  private CustomerService customerService;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public CustomerHealthInfoResponse getByCustomerId(Long customerId) {
    CustomerHealthInfo healthInfo = customerHealthInfoRepository.findByCustomerId(customerId)
        .orElse(null);

    if (healthInfo == null) {
      return null;
    }

    return convertToResponse(healthInfo);
  }

  @Override
  public CustomerHealthInfoResponse create(CustomerHealthInfoRequest request) {
    CustomerHealthInfo healthInfo = new CustomerHealthInfo();
    BeanUtils.copyProperties(request, healthInfo, "customerId");

    Customer customer = customerService.findById(request.getCustomerId());
    if (customer == null) {
      throw new RuntimeException("Customer not found");
    }
    healthInfo.setCustomer(customer);

    CustomerHealthInfo saved = customerHealthInfoRepository.save(healthInfo);
    return convertToResponse(saved);
  }

  @Override
  public CustomerHealthInfoResponse update(Long customerId, CustomerHealthInfoRequest request) {
    CustomerHealthInfo existing = customerHealthInfoRepository.findByCustomerId(customerId)
        .orElseThrow(() -> new RuntimeException("Health info not found"));

    BeanUtils.copyProperties(request, existing, "customerId");
    existing.setCustomer(existing.getCustomer());

    CustomerHealthInfo updated = customerHealthInfoRepository.save(existing);
    return convertToResponse(updated);
  }

  private CustomerHealthInfoResponse convertToResponse(CustomerHealthInfo healthInfo) {
    CustomerHealthInfoResponse response = new CustomerHealthInfoResponse();
    BeanUtils.copyProperties(healthInfo, response);
    response.setCustomerId(healthInfo.getCustomer().getId());
    return response;
  }
}
