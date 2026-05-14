package com.duong.salesmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatMessageRequest {
 
     @NotNull(message = "orderId is required")
     private Long orderId;
 
     @NotNull(message = "receiverId is required")
     private String receiverId;
 
     @NotBlank(message = "content must not be blank")
     @Size(min = 1, max = 1000, message = "content must be between 1 and 1000 characters")
     private String content;
 
     public ChatMessageRequest() {}
 
     public ChatMessageRequest(Long orderId, String receiverId, String content) {
         this.orderId = orderId;
         this.receiverId = receiverId;
         this.content = content;
     }
 
     public Long getOrderId() { return orderId; }
     public void setOrderId(Long orderId) { this.orderId = orderId; }
 
     public String getReceiverId() { return receiverId; }
     public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
 
     public String getContent() { return content; }
     public void setContent(String content) { this.content = content; }
 }
