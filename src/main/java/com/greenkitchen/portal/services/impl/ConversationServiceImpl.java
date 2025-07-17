package com.greenkitchen.portal.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import com.greenkitchen.portal.repositories.ConversationRepository;
import com.greenkitchen.portal.services.ConversationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
  private final ConversationRepository repo;
  @Override public List<Conversation> findByCustomer(Customer c) {
    return repo.findByCustomer(c);
  }
  @Override public Conversation findByIdAndOwner(Long id, Customer c) {
    Conversation conv = repo.findById(id).orElseThrow();
    if (!conv.getCustomer().getId().equals(c.getId())) throw new SecurityException();
    return conv;
  }
}

