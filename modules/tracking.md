# Module: Live GPS Tracking (tracking)

## 1. Purpose
The `tracking` module handles live driver location tracking. It processes GPS coordinate updates from active drivers, maps order statuses to tracking phases, broadcasts coordinate payloads to the customer via WebSocket, and asynchronously logs coordinate histories.

---

## 2. Public API & STOMP Channels

### A. HTTP REST Endpoints
*   `POST /api/driver/location` ([DriverLocationController.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/controller/DriverLocationController.java)):
    *   *Payload*: `LocationUpdateDTO` (orderId, latitude, longitude).
    *   *Access*: Authenticated drivers assigned to the specific order.
    *   *Action*: Triggers STOMP coordinate broadcast, and asynchronously saves coordinates.
*   `GET /api/customer/orders/{orderId}/live-location` ([CustomerTrackingController.java](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/gs-serving-web-content-main/complete/src/main/java/com/duong/salesmanagement/controller/CustomerTrackingController.java)):
    *   *Payload*: None.
    *   *Access*: Customer who placed the order, assigned Driver, Restaurant partner, or Admin.
    *   *Action*: Resolves latest coordinates from the database for initial client map rendering.

### B. Real-time STOMP Broker Channel
*   `SUBSCRIBE /topic/tracking/{orderId}`: Topic where coordinate broadcasts are published. Supported by subscription filters in `WebSocketAuthInterceptor` to prevent unauthorized eavesdropping.

---

## 3. Real-time Tracking Architecture

```
[Driver Client App]
   │ (Sends GPS coordinate payload via HTTP POST)
   ▼
[DriverLocationController.updateLocation]
 ├── 1. Asserts active driver ownership
 ├── 2. Determines TrackingPhase (e.g. GOING_TO_RESTAURANT, DELIVERING)
 ├── 3. Broadcasts GPS coordinates to "/topic/tracking/{orderId}" via SimpMessagingTemplate
 └── 4. Calls LocationTrackingService.saveTrackingHistory (Async @Async worker)
                                        │
                                        ▼
                   [Database (order_tracking_locations table)]
```

---

## 4. Reusable Logic & Helper Methods

*   **Status-to-Phase Mapping**: `DriverLocationController.determinePhase()` maps active `OrderStatus` to matching `TrackingPhase`:
    *   `PENDING` ──> `GOING_TO_RESTAURANT`
    *   `PREPARING` ──> `WAITING_AT_RESTAURANT`
    *   `DELIVERING` ──> `DELIVERING`
    *   `COMPLETED` ──> `ARRIVED`
*   **Asynchronous Database Logger**: `LocationTrackingService.saveTrackingHistory()` carries `@Async` annotation. This delegates database writes to a background worker pool, preventing slow SQL writes from delaying the client's live WebSocket coordinate streams.
*   **Hibernate Proxy Performance Optimization**: Uses `foodOrderRepository.getReferenceById(orderId)` instead of `findById()` when saving location histories, avoiding redundant SELECT queries.

---

## 5. Dependencies

*   **Order Module**: Accesses order details and role permissions via `OrderService`.
*   **Spring Task Executor**: Relies on thread pools configured in `AsyncConfig` to execute asynchronous GPS database writes.
*   **Leaflet & OpenStreetMap**: Integrated on customer views to render real-time paths, pins, and map indicators.

---

## 6. Known Bugs & Code Limitations

*   **Missing Frequency Throttling**: The server does not perform frequency throttling on incoming coordinates. If a driver's client app floods coordinates (e.g., several times per second), it can overwhelm the database transaction pool.
    *   *Refactor Proposal*: Enforce coordinate rate limits in `DriverLocationController` (e.g., minimum 5-second intervals).

---

## 7. Future Risks

*   **High DB Scale Noise**: Logging every single coordinate directly to the relational database `order_tracking_locations` table will cause extreme database bloating in production.
    *   *Mitigation*: Implement cache storage (e.g. Redis) for live, real-time coordinate states, and only write consolidated path histories to MySQL when the order is closed.

---

## 8. Related Components & Templates

*   `templates/customer/tracking.html`: Leaflet map view tracking driver movements.
*   `static/js/map-tracking.js` & `tracking-service.js`: Renders active maps, marker coordinates, pins (Customer, Restaurant, Driver), and dynamic direction paths.
