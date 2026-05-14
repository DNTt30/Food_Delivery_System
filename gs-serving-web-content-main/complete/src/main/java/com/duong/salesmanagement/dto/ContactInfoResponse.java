package com.duong.salesmanagement.dto;

import java.util.List;
public class ContactInfoResponse {
 
     private Long orderId;
     private String orderStatus;
 
     /**
      * true khi status = COMPLETED hoặc CANCELLED.
      * Frontend dùng cờ này để:
      * - Disable ô nhập tin nhắn
      * - Ẩn nút gọi điện
      * - Hiển thị số điện thoại đã mask
      */
     private boolean isCompletedOrCancelled;
 
     /**
      * Danh sách những người có thể liên lạc trong đơn này.
      * Mỗi phần tử là 1 ContactDto.
      */
     private List<ContactDto> contacts;
 
     public ContactInfoResponse() {}
 
     public ContactInfoResponse(Long orderId, String orderStatus,
                                boolean isCompletedOrCancelled, List<ContactDto> contacts) {
         this.orderId = orderId;
         this.orderStatus = orderStatus;
         this.isCompletedOrCancelled = isCompletedOrCancelled;
         this.contacts = contacts;
     }
 
     // ---- Getters & Setters ----
     public Long getOrderId() { return orderId; }
     public void setOrderId(Long orderId) { this.orderId = orderId; }
 
     public String getOrderStatus() { return orderStatus; }
     public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
 
     public boolean isCompletedOrCancelled() { return isCompletedOrCancelled; }
     public void setCompletedOrCancelled(boolean completedOrCancelled) { isCompletedOrCancelled = completedOrCancelled; }
 
     public List<ContactDto> getContacts() { return contacts; }
     public void setContacts(List<ContactDto> contacts) { this.contacts = contacts; }
 
     // ────────────────────────────────────────────
     // Nested DTO: một người có thể liên lạc
     // ────────────────────────────────────────────
     public static class ContactDto {
 
         private String userId;
         private String displayName;
 
         /**
          * Role: CUSTOMER | RESTAURANT | DRIVER
          * Frontend dùng để xác định icon, màu bubble chat.
          */
         private String role;
 
         /**
          * Số điện thoại – đã được mask nếu đơn COMPLETED/CANCELLED
          * VD: 098****123
          * null nếu không có số hoặc không được phép xem
          */
         private String phone;
 
         /** Avatar URL nếu có */
         private String avatarUrl;
 
         public ContactDto() {}
 
         public ContactDto(String userId, String displayName, String role, String phone, String avatarUrl) {
             this.userId = userId;
             this.displayName = displayName;
             this.role = role;
             this.phone = phone;
             this.avatarUrl = avatarUrl;
         }
 
         public String getUserId() { return userId; }
         public void setUserId(String userId) { this.userId = userId; }
 
         public String getDisplayName() { return displayName; }
         public void setDisplayName(String displayName) { this.displayName = displayName; }
 
         public String getRole() { return role; }
         public void setRole(String role) { this.role = role; }
 
         public String getPhone() { return phone; }
         public void setPhone(String phone) { this.phone = phone; }
 
         public String getAvatarUrl() { return avatarUrl; }
         public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
     }
 }
