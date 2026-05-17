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
