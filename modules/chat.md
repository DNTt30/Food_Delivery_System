# Module: Order-Based Chat System (chat)

## 1. Purpose
The `chat` module implements secure, order-scoped real-time messaging. It enables communication between customers, drivers, and restaurants during active delivery cycles. It enforces a strict visibility matrix, automatically locks conversations upon completion, and masks phone numbers to protect user privacy.

---

## 2. Public API & STOMP Channels

### A. HTTP REST Endpoints
*   `GET /api/chat/contacts/{orderId}` ([ChatApiController.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/controller/ChatApiController.java)):
    *   Retrieves allowed contacts based on the user's role and order status (applies phone masking).
*   `GET /api/chat/history/{orderId}`: Retrieves order message history.
*   `POST /api/chat/send`: REST fallback endpoint to send chat messages.

### B. Real-time STOMP Channels
*   `SEND /app/chat.send`: STOMP mapping to publish chat messages via WebSocket.
*   `SUBSCRIBE /topic/order.{orderId}`: Target broadcast channel for real-time messages.

---

## 3. Contact Visibility Matrix & Lifecycle

To prevent spam and protect privacy, participants can only contact each other during specific order phases:

| Buyer Role | Allowed Contacts | Active States | Phone Privacy |
| :--- | :--- | :--- | :--- |
| **CUSTOMER** | Restaurant | `PENDING`, `PREPARING`, `DELIVERING` | Excluded (`null`) |
| **CUSTOMER** | Driver | `PREPARING`, `DELIVERING` | Masked (`098****321`) |
| **RESTAURANT** | Customer | `PENDING`, `PREPARING`, `DELIVERING` | Masked (`098****321`) |
| **RESTAURANT** | Driver | `PREPARING`, `DELIVERING` | Masked (`098****321`) |
| **DRIVER** | Customer | `PREPARING`, `DELIVERING` | Masked (`098****321`) |
| **DRIVER** | Restaurant | `PREPARING`, `DELIVERING` | Excluded (`null`) |

*   **Conversation Locking**: The chat room is automatically set to **Read-Only** once the order transitions to `COMPLETED` or `CANCELLED`. Attempts to send messages will trigger a `ChatLockedException`.
*   **Phone Masking Rules**: When conversations are locked, active phone contacts are masked using `PhoneMaskUtil.maskIf(phone, true)`. This keeps the first 3 and last 3 digits, replacing the middle with `****`.

---

## 4. Reusable Logic & Security Assets

1.  **[PhoneMaskUtil.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/util/PhoneMaskUtil.java)**: Utility used to mask phone numbers based on order states.
2.  **[ContactService.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/service/ContactService.java)**: Core service implementing the contact visibility matrix.
3.  **[ChatService.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/service/ChatService.java)**: Implements validation checks (checks if order exists, asserts if users are participants, enforces lock rules, and validates content length).

---

## 5. Dependencies

*   **Order Module**: Accesses order details and role permissions via `OrderService`.
*   **Notification Module**: Automatically triggers database alerts via `NotificationService` for incoming messages (`NEW_MESSAGE` type).
*   **WebSocket Interceptors**: Relies on `WebSocketAuthInterceptor` to extract JWT authentication tokens during connection setups.

---

## 6. Known Bugs & Security Vulnerabilities

> [!WARNING]
> ### 🚨 Critical Security Vulnerability: Missing STOMP Subscription Authorization Check
> *   In [WebSocketAuthInterceptor.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/security/WebSocketAuthInterceptor.java), subscription validation is only enforced for destinations starting with `/topic/tracking/`.
> *   The WebSocket interceptor **does not validate** subscriptions to the chat channel `/topic/order.{orderId}`.
> *   Any authenticated user can subscribe to `/topic/order.{orderId}` and eavesdrop on all real-time chat messages for that order.
> *   *Proposed Fix*: Update `validateSubscription()` to intercept and authorize subscriptions starting with `/topic/order.`:
>     ```java
>     if (destination.startsWith("/topic/order.")) {
>         String orderIdStr = destination.substring("/topic/order.".length());
>         Long orderId = Long.parseLong(orderIdStr);
>         if (!orderService.hasPermissionToTrackOrder(orderId, user)) {
>             throw new AccessDeniedException("Unauthorized chat subscription");
>         }
>     }
>     ```

---

## 7. Future Risks

*   **Chat DB Bloat**: Storing every message directly in the main relational database can cause disk space shortages as the user base grows.
    *   *Mitigation*: Implement message retention schedules or transition historical chats to cheaper storage (NoSQL/S3) after a set period.

---

## 8. Related Components & Templates

*   `templates/fragments/chat_widget.html`: Dynamic Thymeleaf chat window interface.
*   `static/js/websocket-manager.js`: Establishes WebSocket client connections, subscribes to channels, and maps messages to the UI.
