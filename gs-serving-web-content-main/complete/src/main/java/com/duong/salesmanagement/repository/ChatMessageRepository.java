package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByOrderIdOrderByCreatedAtAsc(Long orderId);

}
