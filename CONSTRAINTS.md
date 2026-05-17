# Coding Constraints & Architecture Guidelines

This file enforces architectural boundaries, coding rules, naming conventions, and anti-pattern warnings. **All active code changes, modifications, or extensions must strictly adhere to these rules.**

---

## 1. Architectural Boundaries (Layers Isolation)

To maintain long-term scalability, strict separation between application layers is mandatory:

```
┌─────────────────────────────────────────────────────────┐
│                    Controller Layer                     │
└───────────────────────────┬─────────────────────────────┘
                            ▼ (REST DTOs / HTML Models)
┌─────────────────────────────────────────────────────────┐
│                      Service Layer                      │
└───────────────────────────┬─────────────────────────────┘
                            ▼ (JPA Transaction Guarded)
┌─────────────────────────────────────────────────────────┐
│                    Repository Layer                     │
└─────────────────────────────────────────────────────────┘
```

### Layer Constraints:
*   **Controllers** must never access the database/repositories directly. They must delegate all operations to the service layer.
*   **Database Entities** (JPA entities annotated with `@Entity`) must never leak directly to API JSON outputs or Thymeleaf models when updating. Always map them to custom DTOs (`*DTO`, `*Response`) at the Service or Controller layer to avoid security disclosures or serialization circular references.
*   **Services** must manage transaction boundaries. Use `@Transactional` for database writes and `@Transactional(readOnly = true)` for query-only methods to optimize performance.
*   **Cross-Module references** must always be executed through the module's target service rather than querying other databases directly.

---

## 2. Coding & Design Conventions

*   **No Lombok**:
    *   Lombok is strictly **forbidden** in this codebase.
    *   All JPA and DTO classes must declare standard, clean explicit getter and setter methods, constructors, and builders.
*   **User Keys**:
    *   The `User` entity primary key is a UUID string: `private String id = java.util.UUID.randomUUID().toString();`
    *   Never treat user ID as an integer, sequential number, or Long. Any method accepting or comparing user IDs must type them as `String`.
*   **Database Keys**:
    *   Other models (e.g. `FoodOrder`, `OrderItem`, `MenuItem`, `Voucher`, `Review`) must use standard auto-incremented database-generated primary keys of type `Long`.
*   **Database Query Optimization**:
    *   Use `foodOrderRepository.getReferenceById(orderId)` instead of `findById()` when you only need a JPA Proxy object for relational reference (e.g. creating a sub-record, as seen in `LocationTrackingService`). This prevents a redundant database `SELECT` query.

---

## 3. Strict Naming Standards

| Component Type | Naming Standard | Example |
| :--- | :--- | :--- |
| **JPA Entities** | CamelCase, singular noun | `FoodOrder`, `CustomerProfile`, `Voucher` |
| **DTO Requests/Responses** | CamelCase + Role + Request/Response/DTO | `ChatMessageRequest`, `TrackingResponseDTO` |
| **Spring Repositories** | CamelCase + ModelName + Repository | `FoodOrderRepository`, `UserRepository` |
| **Spring Controllers** | REST APIs: `*ApiController` \| Views: `*ViewController` | `DriverApiController`, `CustomerViewController` |
| **Spring Services** | CamelCase + Domain + Service | `LocationTrackingService`, `ChatService` |
| **Database Tables** | lowercase, snake_case, plural nouns | `users`, `food_orders`, `chat_messages` |
| **Variables / Methods** | camelCase | `estimateETA()`, `maskIf()`, `shippingFee` |

---

## 4. Forbidden Actions

> [!CAUTION]
> **Violating any of these forbidden actions will immediately cause system crashes or severe security gaps:**
>
> 1. **Do NOT use Lombok** annotations (`@Data`, `@Getter`, `@AllArgsConstructor`).
> 2. **Do NOT perform manual phone number masking** in controllers. You must use `PhoneMaskUtil.mask()` or `PhoneMaskUtil.maskIf()`.
> 3. **Do NOT write `@Transactional` on query methods** without the `readOnly = true` flag. It causes unnecessary write locks.
> 4. **Do NOT leak User password hashes** or OTP codes into DTOs or log files.
> 5. **Do NOT bypass hasPermissionToTrackOrder checks** when exposing order tracking coordinate streams.
> 6. **Do NOT initialize REST template headers without user-agent** in external calls. Nominatim OSM will black-list the server.

---

## 5. Duplicate Logic Avoidance (Must Reuse!)

The following common utilities must be reused instead of recreated:

### 1. Phone Masking
*   **Do NOT** manually substring or replace character arrays.
*   **REUSE**: `PhoneMaskUtil.mask(phone)` or `PhoneMaskUtil.maskIf(phone, shouldMask)`.

### 2. Distance Calculation
*   **Do NOT** implement custom math calculations for distance.
*   **REUSE**: `ShippingCalculationService.calculateDistance(lat1, lon1, lat2, lon2)` (uses Haversine algorithm).

### 3. Shipping Fees & ETA
*   **Do NOT** hardcode delivery pricing tiers or arrival calculations.
*   **REUSE**: `ShippingCalculationService.calculateShippingFee(distance)` and `ShippingCalculationService.estimateETA(distance)`.

### 4. Coordinate Geocoding
*   **Do NOT** invoke manual HTTP queries to OpenStreetMap or Google Maps.
*   **REUSE**: `GeocodingService.getCoordinates(address)`.

### 5. Chat History & Logic
*   **Do NOT** query `ChatMessageRepository` directly from tracking or order controllers.
*   **REUSE**: `ChatService.sendMessage()` or `ChatService.getOrderMessages()`.

### 6. Order Tracking Access checks
*   **Do NOT** write manual comparison logic checks in controllers.
*   **REUSE**: `orderService.hasPermissionToTrackOrder(orderId, user)` (checks role-based rights for customers, drivers, and partners).
