# Recent Changes Log

This document tracks recent modifications in the codebase. Every time a feature, service, or model is changed, this file must be updated to keep AI and developers in sync and prevent code regression.

---

## Change Entry Template

Use this format when logging new changes:

```markdown
### YYYY-MM-DD: [Brief Title of Change]
*   **Description**: Detailed description of what was changed and why.
*   **Changed Files**:
    *   `[Path/To/File.java]`
*   **Affected Modules**: [Auth | Profile | Order | Tracking | Chat | Notification]
*   **API Impact**: [REST API changes, routes, payloads, parameters]
*   **Database Schema Updates**: [Yes/No, DDL details]
*   **Retesting Checklist**:
    - [ ] Step 1
    - [ ] Step 2
```

---

## Historic Code Changes Log

### 2026-06-14: Voucher Usage Limits & Split Scope (Food/Shipping)
*   **Description**: Implemented voucher usage limits (Global Usage Limit & Per-User Limit) to prevent abuse. Split the voucher scope so that customers can apply a Food Voucher and a Shipping Voucher simultaneously on the same order. Fixed related UI bugs causing `shippingDiscountAmount` display errors.
*   **Changed Files**: `Voucher.java`, `FoodOrder.java`, `FoodOrderRepository.java`, `CustomerApiController.java`, `OrderService.java`, `cart.html`, `promotions.html`.
*   **Affected Modules**: `order`, `admin`
*   **API Impact**: `PlaceOrderRequest` now accepts `foodVoucherCode` and `shippingVoucherCode`. `VoucherDTO` includes limit parameters.
*   **Database Schema Updates**: Yes. Added `max_global_usage`, `current_global_usage`, `max_usage_per_user` to `vouchers`. Added `food_voucher_code`, `food_discount_amount`, `shipping_voucher_code`, `shipping_discount_amount` to `food_orders`.

### 2026-06-02: Driver Multiple Orders Fix & UI Polish
*   **Description**: Implemented a constraint to prevent drivers from accepting multiple orders simultaneously. Polished the Driver UI by introducing a history accordion view and removing native browser confirmation alerts for a smoother experience.
*   **Changed Files**: Driver controllers and templates.
*   **Affected Modules**: `order`, `tracking`
*   **API Impact**: Driver accept API now enforces 1 active order per driver constraint.
*   **Database Schema Updates**: None.

### 2026-06-02: Admin Notification Broadcast & History
*   **Description**: Overhauled the notification system for Administrators. Admins can now view a comprehensive Notification History and broadcast messages to all users or specific roles (Customers, Drivers, Partners).
*   **Changed Files**: Admin controllers, notification models, and admin templates.
*   **Affected Modules**: `notification`
*   **API Impact**: Added Admin broadcasting endpoints.
*   **Database Schema Updates**: None.

### 2026-06-02: Server-Side Pagination for Orders
*   **Description**: Implemented Server-Side Pagination for the orders list to handle large datasets efficiently. Also fixed a SpEL exception in JS and corrected API syntax.
*   **Changed Files**: Order controllers, repositories, and order list templates.
*   **Affected Modules**: `order`
*   **API Impact**: GET endpoints for orders now support page/size pagination query params.
*   **Database Schema Updates**: None.

### 2026-06-02: Admin Panel Overhaul & Chart.js Integration
*   **Description**: Completely redesigned the Admin panel. Added an interactive line chart for revenue/order statistics, a recent orders table, sidebar links, and a new promotions scope view.
*   **Changed Files**: Admin dashboard templates, admin controllers, CSS/JS assets.
*   **Affected Modules**: `order`
*   **API Impact**: None.
*   **Database Schema Updates**: None.

### 2026-06-02: Geocoding Search & Map Rendering Fix in Delivery Modal
*   **Description**: Fixed Leaflet map rendering bugs in modals and added a geocoding search input directly within the delivery address modal for easier coordinate selection.
*   **Changed Files**: Modal fragments, map JS scripts.
*   **Affected Modules**: `order`, `profile`
*   **API Impact**: None.
*   **Database Schema Updates**: None.

### 2026-05-22: Resolved Driver Delivering Screen Blank Rendering Issue
*   **Description**: Fixed a server-side Thymeleaf 3 template processing crash on the driver's delivery view. The expression parser incorrectly treated a 2D JavaScript array literal `[[...]]` as an inline Thymeleaf expression. Padding the nested brackets with a space (`[ [...] ]`) prevents Thymeleaf from parsing them, restoring full map and order details rendering.
*   **Changed Files**:
    *   [delivering.html](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/resources/templates/driver/delivering.html)
*   **Affected Modules**: `tracking`, `order` (Driver Delivery View)
*   **API Impact**: None.
*   **Database Schema Updates**: None.
*   **Retesting Checklist**:
    - [x] Login as a Driver, accept/deliver an order, and navigate to `/driver/delivering`.
    - [x] Verify the page loads successfully (not blank) and the Leaflet map initializes correctly with route line, restaurant, and customer markers.


### 2026-05-15: Modernized Partner Profiles & Map Location Pickers
*   **Description**: Decoupled restaurant profiles from shared profile forms by establishing a standalone, role-specific partner workspace. Integrated a premium location coordinates selection map with search query autocomplete capabilities.
*   **Changed Files**:
    *   [RestaurantProfile.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/model/RestaurantProfile.java)
    *   [RestaurantApiController.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/controller/RestaurantApiController.java)
    *   `templates/restaurant/profile.html` (Added standalone view)
    *   `templates/layouts/dashboard_layout.html` (Upgraded sidebar wrappers)
    *   `static/js/map-tracking.js` (Enhanced with leaflet markers, map address picker & geolocation support)
    *   `static/css/map-premium.css` (Premium Coral Red UI overrides)
*   **Affected Modules**: `profile`, `order` (Geolocated estimation system)
*   **API Impact**:
    *   `GET /restaurant/profile` returns dedicated restaurant editing workspace.
    *   `PUT /api/restaurant/profile` updates latitude, longitude, address and banner.
*   **Database Schema Updates**: None (populated existing columns).
*   **Retesting Checklist**:
    - [x] Login as a Restaurant user and verify navigation leads to `/restaurant/profile`.
    - [x] Trigger the address selection map, drag marker or type query, verify that `latitude` and `longitude` fields are auto-filled.
    - [x] Click "Cập nhật", verify database is updated, and the layout looks clean.

---

### 2026-05-14: Redesigned Customer Profile Page & Global Layouts
*   **Description**: Modernized the customer UI with unified layouts, premium "Coral Red" aesthetics, and a cohesive double-column responsive profile layout. Resolved navigation duplication.
*   **Changed Files**:
    *   `templates/common/profile.html` (Reconstructed layout, personal info column, credentials update column)
    *   `templates/layouts/customer_layout.html` (Refactored layout base)
    *   `templates/fragments/navbar_customer.html` (Decoupled navigation and added dynamic notifications indicator)
    *   `static/css/style.css` (Added premium HSL gradients, glassmorphism card classes, micro-animations)
*   **Affected Modules**: `profile`, `notification`
*   **API Impact**: None (REST payload formats stayed consistent).
*   **Database Schema Updates**: None.
*   **Retesting Checklist**:
    - [x] Access customer profile view and verify full responsiveness on mobile screens.
    - [x] Confirm navbar fragment is correctly declared under `customer_layout.html` and not declared ad-hoc on separate customer views.
    - [x] Test card hover styles, ensure animations execute smoothly.

---

### 2026-05-13: Transitioned Identities to String-based UUIDs
*   **Description**: Migrated the core `User` identifier structure from sequential `Long` integers to a secure, decentralized `String` representation containing UUIDs to improve multi-tenant privacy. Resolves compilation errors on services depending on users.
*   **Changed Files**:
    *   [User.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/model/User.java)
    *   [ChatMessage.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/model/ChatMessage.java)
    *   [ChatApiController.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/controller/ChatApiController.java)
    *   [WebSocketChatController.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/controller/WebSocketChatController.java)
    *   [ChatService.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/service/ChatService.java)
    *   [ContactService.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/service/ContactService.java)
    *   [SecurityConfig.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/security/SecurityConfig.java)
*   **Affected Modules**: All modules (cross-cutting identity changes)
*   **API Impact**:
    *   User details JSON payloads now return a 36-character UUID string in the `id` field.
    *   Chat sender and receiver IDs transitioned to UUID Strings.
*   **Database Schema Updates**: Yes. `users.id` schema altered to `VARCHAR(36)`. All matching foreign key mappings (`sender_id`, `receiver_id` in `chat_messages` table, `user_id` in profile tables) updated to `VARCHAR(36)`.
*   **Retesting Checklist**:
    - [x] Perform full database migration and check table schemas via Aiven database CLI.
    - [x] Run registration and login sequence to verify token creation with UUID claims.
    - [x] Start an order and run the chat sequence to ensure that messages can be saved with UUID key relationships.
