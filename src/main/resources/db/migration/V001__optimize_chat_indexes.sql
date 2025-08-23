-- Optimize indexes cho chat system để giảm deadlock và tăng performance

-- Index cho chat_messages table
-- Index cho conversation_id (quan trọng nhất vì hầu hết query theo conversation)
CREATE INDEX IF NOT EXISTS idx_chat_messages_conversation_id 
ON chat_messages(conversation_id);

-- Index composite cho conversation_id + timestamp (cho sắp xếp tin nhắn)
CREATE INDEX IF NOT EXISTS idx_chat_messages_conv_timestamp 
ON chat_messages(conversation_id, timestamp DESC);

-- Index cho status (để query PENDING messages nhanh)
CREATE INDEX IF NOT EXISTS idx_chat_messages_status 
ON chat_messages(status);

-- Index composite cho conversation_id + status
CREATE INDEX IF NOT EXISTS idx_chat_messages_conv_status 
ON chat_messages(conversation_id, status);

-- Index cho sender_type (để phân biệt CUSTOMER, AI, EMP)
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_type 
ON chat_messages(sender_type);

-- Index cho customer_id (để query tin nhắn của customer)
CREATE INDEX IF NOT EXISTS idx_chat_messages_customer_id 
ON chat_messages(customer_id);

-- Index cho employee_id (để query tin nhắn của employee)
CREATE INDEX IF NOT EXISTS idx_chat_messages_employee_id 
ON chat_messages(employee_id);

-- Index cho is_read (để đếm unread messages)
CREATE INDEX IF NOT EXISTS idx_chat_messages_is_read 
ON chat_messages(is_read);

-- Index cho conversations table
-- Index cho customer_id
CREATE INDEX IF NOT EXISTS idx_conversations_customer_id 
ON conversations(customer_id);

-- Index cho employee_id
CREATE INDEX IF NOT EXISTS idx_conversations_employee_id 
ON conversations(employee_id);

-- Index cho status (để query conversations theo trạng thái)
CREATE INDEX IF NOT EXISTS idx_conversations_status 
ON conversations(status);

-- Index composite cho status + start_time (để sắp xếp conversations)
CREATE INDEX IF NOT EXISTS idx_conversations_status_time 
ON conversations(status, start_time DESC);

-- Index cho start_time (để sắp xếp theo thời gian)
CREATE INDEX IF NOT EXISTS idx_conversations_start_time 
ON conversations(start_time DESC);

-- Thêm cột version cho optimistic locking chỉ cho 2 bảng chat (không áp dụng cho tất cả entities)
ALTER TABLE chat_messages 
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

ALTER TABLE conversations 
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Index cho version columns (optimistic locking)
-- Chỉ áp dụng cho 2 bảng chat để tránh conflict với entities khác
CREATE INDEX IF NOT EXISTS idx_chat_messages_version 
ON chat_messages(version);

CREATE INDEX IF NOT EXISTS idx_conversations_version 
ON conversations(version);
