package com.greenkitchen.portal.entities;

import java.time.LocalDateTime;

import com.greenkitchen.portal.enums.MessageStatus;
import com.greenkitchen.portal.enums.SenderType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.PrePersist;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessage extends AbstractEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8192353433099522644L;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;           // null = guest

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;   // null = guest
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String  senderName;
    private Boolean isFromAI;

    @Enumerated(EnumType.STRING)
    private SenderType senderType;

    @Lob private String content;
    private LocalDateTime timestamp;
    // Trạng thái đã đọc hay chưa
    private Boolean isRead = false;
    @Enumerated(EnumType.STRING)
    
    private MessageStatus status = MessageStatus.SENT;

    
    @Lob
    @Column(name = "menu_json", columnDefinition = "TEXT")
    private String menuJson;

    @Version
    @JsonIgnore
    private Long version = 0L;

    @PrePersist
    protected void onPersist() {
        if (this.version == null) {
            this.version = 0L;
        }
    }


}