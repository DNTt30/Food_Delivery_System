package com.duong.salesmanagement.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {
 
     private Long id;
     private Long orderId;
 
     private String senderId;
     private String senderName;
     private String senderRole;      // CUSTOMER | RESTAURANT | DRIVER
 
     private String receiverId;
     private String receiverName;
     private String receiverRole;
 
     private String content;
     private LocalDateTime createdAt;
 
     public ChatMessageResponse() {}
 
     // ---- Getters & Setters ----
     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
 
     public Long getOrderId() { return orderId; }
     public void setOrderId(Long orderId) { this.orderId = orderId; }
 
     public String getSenderId() { return senderId; }
     public void setSenderId(String senderId) { this.senderId = senderId; }
 
     public String getSenderName() { return senderName; }
     public void setSenderName(String senderName) { this.senderName = senderName; }
 
     public String getSenderRole() { return senderRole; }
     public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
 
     public String getReceiverId() { return receiverId; }
     public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
 
     public String getReceiverName() { return receiverName; }
     public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
 
     public String getReceiverRole() { return receiverRole; }
     public void setReceiverRole(String receiverRole) { this.receiverRole = receiverRole; }
 
     public String getContent() { return content; }
     public void setContent(String content) { this.content = content; }
 
     public LocalDateTime getCreatedAt() { return createdAt; }
     public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
 
     // ---- Builder Pattern ----
     public static Builder builder() { return new Builder(); }
 
     public static class Builder {
         private final ChatMessageResponse obj = new ChatMessageResponse();
 
         public Builder id(Long id)                          { obj.id = id; return this; }
         public Builder orderId(Long orderId)                { obj.orderId = orderId; return this; }
         public Builder senderId(String senderId)            { obj.senderId = senderId; return this; }
         public Builder senderName(String senderName)        { obj.senderName = senderName; return this; }
         public Builder senderRole(String senderRole)        { obj.senderRole = senderRole; return this; }
         public Builder receiverId(String receiverId)        { obj.receiverId = receiverId; return this; }
         public Builder receiverName(String receiverName)    { obj.receiverName = receiverName; return this; }
         public Builder receiverRole(String receiverRole)    { obj.receiverRole = receiverRole; return this; }
         public Builder content(String content)              { obj.content = content; return this; }
         public Builder createdAt(LocalDateTime createdAt)   { obj.createdAt = createdAt; return this; }
         public ChatMessageResponse build()                  { return obj; }
     }
 }
