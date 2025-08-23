package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;
import java.util.List;

import com.greenkitchen.portal.enums.ConversationStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "conversations")
public class Conversation extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;          // null = guest

    private String title;
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee; // nullable, chỉ set khi có Emp join

    @Enumerated(EnumType.STRING)
    private ConversationStatus status = ConversationStatus.AI;

    @OneToMany(mappedBy = "conversation",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<ChatMessage> messages;

    @Version
    @JsonIgnore
    private Long version;
}