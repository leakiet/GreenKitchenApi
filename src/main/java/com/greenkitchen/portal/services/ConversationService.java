package com.greenkitchen.portal.services;

import java.util.List;

import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;

public interface ConversationService {
	  List<Conversation> findByCustomer(Customer customer);
	  Conversation findByIdAndOwner(Long id, Customer customer);
	}