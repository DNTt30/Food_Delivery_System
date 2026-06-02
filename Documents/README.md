# 📁 Tài liệu Dự án – Hệ thống Quản lý Đặt đồ ăn Trực tuyến

> **Nhóm:** TEAM 12  
> **Môn học:** Phân tích & Thiết kế Phần mềm  
> **GitHub:** [Food_Delivery_System](https://github.com/DNTt30/Food_Delivery_System)

---

## 📊 Tiến độ Dự án

| Tuần | Mục tiêu | Trạng thái |
| :--- | :--- | :--- |
| 1–2 | Phân tích yêu cầu (SRS), Use Case Diagram | ✅ |
| 3 | Class Diagram, Code Skeleton, OTP Email | ✅ |
| 5 | Chat Real-time (WebSocket), UUID Migration | ✅ |
| 6 | Memory System, Bảo mật & Technical Debt | ✅ |
| 7 | Cập nhật README & Tài liệu | ✅ |

---

## ✅ Tuần 3 – Thiết kế Lớp & Code Skeleton

**Hoạt động:** 
- Trích xuất **16 lớp entity** từ use case (User, CustomerProfile, RestaurantProfile, DriverProfile, MenuItem, FoodOrder, OrderItem, Payment, Review, Voucher, etc.)
- Xác định **13+ quan hệ** giữa lớp với multiplicity đầy đủ
- Vẽ **Class Diagram** chi tiết (ClassDiagram_Team12_Tuan3.drawio)
- Tạo **Spring Boot Skeleton** với cấu trúc 3-tầng (Controller-Service-Repository)
- Triển khai **OTP Email xác thực** qua Gmail SMTP + giao diện hiện đại

**Sản phẩm:** ✅ Class Diagram | ✅ Code Skeleton | ✅ AuthService + OTP

---

## ✅ Tuần 5 – Hoàn thiện & UUID Migration

**Hoạt động:**
- **Chat Real-time:** WebSocket/STOMP thay Polling, widget pop-up, tự động khóa theo trạng thái đơn
- **Restaurant Dashboard:** Kanban Board, phân trang server-side, bộ lọc nhanh
- **Driver Mobile-First:** Bottom navigation, luồng xử lý đơn trực quan
- **UUID Migration:** Chuyển User ID từ `Long` → `UUID` (String) tăng bảo mật

**Sản phẩm:** ✅ WebSocket Chat | ✅ Kanban UI | ✅ Mobile Driver | ✅ UUID DB

---

## ✅ Tuần 6 – Memory System & Security Hardening

**Hoạt động:**
- **AI Memory System:** 
  - [PROJECT_STATE.md](../PROJECT_STATE.md) - Bản đồ kiến trúc, tech stack, module
  - [CONSTRAINTS.md](../CONSTRAINTS.md) - Quy chuẩn viết mã, phân tách 3 tầng
  - [project-manifest.json](../project-manifest.json) - API routes, DB schemas, WebSocket
  - [RECENT_CHANGES.md](../RECENT_CHANGES.md) - Nhật ký thay đổi
  - `modules/` - Tài liệu chuyên sâu 6 module (auth, profile, order, tracking, chat, notification)

- **Phân tích Bảo mật:**
  - Phát hiện lỗ hổng WebSocket auth (subscription `/topic/order.{orderId}`)
  - Rà soát rủi ro API Nominatim (thiếu User-Agent header)
  - Xác định technical debt (OrderContactService dư thừa)

**Sản phẩm:** ✅ Memory System | ✅ Docs Module | ✅ Security Analysis

---

## ✅ Tuần 7 – Sửa lỗi Geocoding, Đồng bộ Tọa độ & Cập nhật Tài liệu

**Hoạt động:**
- **Khắc phục lỗi định vị (Nominatim API):** Bổ sung header `User-Agent` (`FoodDeliveryApp/1.0`) cho RestTemplate của `GeocodingService` để giải quyết triệt để lỗi 403 Forbidden chặn IP từ OpenStreetMap.
- **Tích hợp Map Picker tự động lưu tọa độ:** Nâng cấp bản đồ chọn vị trí (Leaflet Map) trên trang cá nhân của Khách hàng và Đối tác Nhà hàng, hỗ trợ tự động đồng bộ và lưu tọa độ `latitude`/`longitude` trực tiếp vào Database khi người dùng xác nhận vị trí trên bản đồ.
- **Cơ chế Snapshots & Fallback thông minh:** Cập nhật `OrderService` khi tạo đơn: tự động geocode địa chỉ giao hàng và địa chỉ quán, chỉ fallback về tọa độ profile nếu địa chỉ trùng khớp, bổ sung cơ chế tự sửa lỗi (self-healing) tọa độ cho các đơn hàng cũ thiếu dữ liệu tracking.
- **Thanh trạng thái tiến trình giao hàng (Tracking Progress UI Bar):** Thêm thanh tiến trình trực quan động trên trang theo dõi đơn hàng của Khách hàng (`tracking.html`), đồng bộ với các trạng thái đơn hàng thời gian thực.
- **Cập nhật Tài liệu Toàn diện:** 
  - Cập nhật [README.md root](../README.md) với đầy đủ API Endpoints, hướng dẫn cài đặt, kiến trúc 3-tầng và stack công nghệ.
  - Cập nhật tài liệu chuyên biệt từng module trong thư mục `modules/` để phản ánh đúng các thay đổi kỹ thuật của dự án.

**Sản phẩm:** ✅ Bản vá lỗi Nominatim API 403 | ✅ Map Picker Auto-Save | ✅ Tracking Progress Bar | ✅ README Root & Module Docs

---

## 📚 Tài liệu Tham khảo

| Tài liệu | Link | Mục đích |
| :--- | :--- | :--- |
| **Architecture** | [PROJECT_STATE.md](../PROJECT_STATE.md) | Bản đồ hệ thống & tech stack |
| **Constraints** | [CONSTRAINTS.md](../CONSTRAINTS.md) | Quy chuẩn, cấm tuyệt đối |
| **Recent Changes** | [RECENT_CHANGES.md](../RECENT_CHANGES.md) | Nhật ký thay đổi code |
| **Manifest** | [project-manifest.json](../project-manifest.json) | API routes, DB schemas |
| **Module: Auth** | [modules/auth.md](../modules/auth.md) | JWT, OTP, Security |
| **Module: Profile** | [modules/profile.md](../modules/profile.md) | 3 vai trò customer/restaurant/driver |
| **Module: Order** | [modules/order.md](../modules/order.md) | Quản lý đơn, vận chuyển |
| **Module: Tracking** | [modules/tracking.md](../modules/tracking.md) | GPS Real-time |
| **Module: Chat** | [modules/chat.md](../modules/chat.md) | WebSocket STOMP |
| **Module: Notification** | [modules/notification.md](../modules/notification.md) | Thông báo hệ thống |

---

## 🚀 Công nghệ Stack

| Lớp | Công nghệ | Phiên bản |
| :--- | :--- | :--- |
| **Backend** | Spring Boot | 3.3.0 |
| **Language** | Java | 17 |
| **Database** | MySQL (Aiven Cloud) | 8.0 |
| **Authentication** | Spring Security + JJWT | Latest |
| **Real-time** | WebSocket + STOMP | Latest |
| **Email** | JavaMailSender (Gmail SMTP) | Latest |
| **Frontend** | Thymeleaf + Bootstrap 5 + Chart.js | Latest |

---

## ⭐ Đặc điểm Nổi bật

✅ **100% Use Case hoàn thiện** (20/20)  
✅ **Chat Real-time** WebSocket STOMP  
✅ **UUID Security** thay Long ID  
✅ **Kanban Dashboard** quản lý đơn  
✅ **Mobile-first UI** tối ưu di động  
✅ **OTP Email** xác thực người dùng  
✅ **Live Tracking GPS** tức thời  
✅ **Phân quyền 4 vai trò** (Customer, Restaurant, Driver, Admin)

---

*Cập nhật lần cuối: 02/06/2026*

### 🔍 Hoạt động đã thực hiện

#### 1. Trích xuất lớp từ kịch bản (Noun Extraction)
Đọc lại tất cả kịch bản use case, gạch chân danh từ, phân loại và xác định **16 lớp đối tượng** chính:

| Lớp | Stereotype | Mô tả |
| :--- | :--- | :--- |
| `User` | `«abstract»` | Lớp cơ sở cho tất cả người dùng |
| `CustomerProfile` | `«entity»` | Hồ sơ khách hàng |
| `RestaurantProfile` | `«entity»` | Hồ sơ nhà hàng |
| `DriverProfile` | `«entity»` | Hồ sơ tài xế giao hàng |
| `Category` | `«entity»` | Danh mục món ăn |
| `MenuItem` | `«entity»` | Món ăn trong thực đơn |
| `FoodOrder` | `«entity»` | Đơn hàng (trung tâm hệ thống) |
| `OrderItem` | `«entity»` | Dòng item snapshot giá |
| `Payment` | `«entity»` | Giao dịch thanh toán |
| `Review` | `«entity»` | Đánh giá sau giao hàng |
| `Voucher` | `«entity»` | Mã khuyến mãi |
| `Cart` | `«entity»` | Giỏ hàng tạm thời |
| `CartItem` | `«entity»` | Dòng item giỏ hàng |
| `Address` | `«entity»` | Địa chỉ giao hàng |
| `Notification` | `«entity»` | Thông báo hệ thống |

**Loại bỏ** (không phải entity): Mật khẩu/Token → thuộc tính của `User`; Cổng thanh toán → `«interface»`.

---

#### 2. Xác định thuộc tính và phương thức mỗi lớp

Với mỗi lớp đã xác định:
- **Kiểu dữ liệu đầy đủ**: `Long`, `String`, `BigDecimal`, `LocalDateTime`, `Boolean`...
- **Phạm vi truy cập**: `private` cho thuộc tính, `public` cho phương thức
- **Stereotype**: `«entity»`, `«abstract»`, `«enumeration»`

Ví dụ `FoodOrder`:
```java
@Entity
@Table(name = "food_orders")
public class FoodOrder {
    private Long id;
    private CustomerProfile customer;   // @ManyToOne
    private RestaurantProfile restaurant; // @ManyToOne
    private DriverProfile driver;        // @ManyToOne
    private OrderStatus status;          // @Enumerated
    private Double totalAmount;
    private LocalDateTime orderTime;

    + updateStatus(s: OrderStatus): void
    + cancel(reason: String): void
    + isDelivered(): boolean
}
```

---

#### 3. Xác định tất cả quan hệ giữa các lớp

| Lớp A | Lớp B | Loại quan hệ | Multiplicity |
| :--- | :--- | :--- | :--- |
| `User` | `CustomerProfile`, `RestaurantProfile`, `DriverProfile` | Composition | `1 : 0..1` |
| `RestaurantProfile` | `MenuItem` | Composition | `1 : 0..*` |
| `Category` | `MenuItem` | Association | `1 : 0..*` |
| `FoodOrder` | `CustomerProfile` | Association `@ManyToOne` | `0..* : 1` |
| `FoodOrder` | `RestaurantProfile` | Association `@ManyToOne` | `0..* : 1` |
| `FoodOrder` | `DriverProfile` | Association `@ManyToOne` | `0..* : 1` |
| `FoodOrder` | `OrderItem` | Composition `@OneToMany` | `1 : 1..*` |
| `OrderItem` | `MenuItem` | Association `@ManyToOne` | `0..* : 1` |
| `FoodOrder` | `Payment` | Composition | `1 : 0..1` |
| `FoodOrder` | `Review` | Composition | `1 : 0..1` |
| `FoodOrder` | `Voucher` | Association | `0..* : 0..1` |
| `User` | `UserRole` | Dependency `«uses»` | `1 : 1` |
| `FoodOrder` | `OrderStatus` | Dependency `«uses»` | `1 : 1` |

**Tổng:** 13+ quan hệ với đầy đủ multiplicity.

---

#### 4. Vẽ Class Diagram (Draw.io)

- **File:** `ClassDiagram_Team12_Tuan3.drawio`
- **Nội dung:** 16 lớp, 5 enum, đầy đủ quan hệ, chú thích màu sắc theo tầng
- **Phân màu:**
  - 🔵 Xanh dương: Lớp User và Order
  - 🟢 Xanh lá: CustomerProfile
  - 🟠 Cam: Restaurant, Menu
  - 🟣 Tím: DriverProfile/Shipper
  - 🟡 Vàng: Enum, Payment, Voucher

---

#### 5. Tạo khung mã nguồn (Code Skeleton)

**Cấu trúc thư mục dự án** (Spring Boot Maven):

```
gs-serving-web-content-main/complete/
├── pom.xml                              ← Spring Boot 3.3.0, Java 17
└── src/
    ├── main/
    │   ├── java/com/duong/salesmanagement/
    │   │   ├── model/                   ← JPA Entities (16 lớp)
    │   │   │   ├── User.java
    │   │   │   ├── CustomerProfile.java
    │   │   │   ├── RestaurantProfile.java
    │   │   │   ├── DriverProfile.java
    │   │   │   ├── Category.java
    │   │   │   ├── MenuItem.java
    │   │   │   ├── FoodOrder.java
    │   │   │   ├── OrderItem.java
    │   │   │   ├── Payment.java
    │   │   │   ├── Review.java
    │   │   │   └── Voucher.java
    │   │   ├── repository/              ← Spring Data JPA
    │   │   ├── service/                 ← Business Logic
    │   │   ├── controller/              ← REST API
    │   │   ├── config/                  ← Security, JWT
    │   │   └── enums/                   ← Role, OrderStatus...
    │   └── resources/
    │       └── application.properties   ← DB + JWT config
    └── test/
```

**Công nghệ:** Spring Boot 3.3.0 · Java 17 · MySQL · Spring Security · JWT · Maven

---

#### 6. Triển khai Hệ thống Đăng ký & Xác thực Email (OTP)

Xây dựng luồng xác thực người dùng hoàn chỉnh qua Email để đảm bảo tính an toàn:
- **Tích hợp Spring Mail**: Sử dụng Gmail SMTP để gửi mã OTP.
- **Cơ chế OTP**: Sinh mã 6 chữ số ngẫu nhiên, lưu trữ vào DB kèm thời gian hết hạn (15 phút).
- **Luồng Đăng ký & Quên mật khẩu**: 
    - Đăng ký 👉 Gửi OTP 👉 Xác minh 👉 Kích hoạt tài khoản (`ENABLED = true`).
    - Quên mật khẩu 👉 Gửi OTP 👉 Nhập mã & Mật khẩu mới 👉 Reset thành công.
- **Giao diện hiện đại**: Thiết kế trang `register.html` và `forgot-password.html` sử dụng phong cách **Glassmorphism**, responsive và interactive.

---

### 📦 Sản phẩm tuần 3

| # | Sản phẩm | File/Link | Status |
| :--- | :--- | :--- | :--- |
| 1 | Biểu đồ Lớp (Class Diagram) | `ClassDiagram_Team12_Tuan3.drawio` | ✅ |
| 2 | Tài liệu thiết kế lớp (Word) | `ClassDiagram_Tuan3_Team12.docx` | ✅ |
| 3 | Khung mã nguồn (Skeleton) | `src/.../model/*.java` | ✅ |
| 4 | Hệ thống xác thực OTP/Mail | `EmailService.java`, `AuthService.java` | ✅ |
| 5 | Giao diện Đăng ký & Reset PW | `register.html`, `forgot-password.html` | ✅ |
| 6 | README hướng dẫn chạy | `README.md` (root) | ✅ |

---

## ✅ Tuần 5 – Hoàn thiện, Chat Real-time & UUID Migration

**Mục tiêu:** Hoàn thiện 100% chức năng, chuyển đổi hệ thống Chat sang WebSocket và di chuyển định danh người dùng sang UUID để tăng cường bảo mật.

### 🔍 Hoạt động đã thực hiện

#### 1. Hệ thống Chat Real-time (WebSocket STOMP Architecture)
- **Nâng cấp công nghệ:** Thay thế hoàn toàn cơ chế Polling cũ bằng WebSocket/STOMP, giúp tin nhắn được truyền tải tức thời.
- **Tính năng nổi bật:** 
    - Widget chat pop-up phong cách ShopeeFood.
    - Tự động khóa/mở chat dựa trên trạng thái đơn hàng (Pending/Preparing/Delivering).
    - Phân quyền chặt chẽ: Chỉ người tham gia đơn hàng mới có thể nhắn tin.
    - Masking số điện thoại khi đơn hàng đã hoàn thành để bảo mật thông tin.

#### 2. Hiện đại hóa Giao diện & Trải nghiệm Người dùng (UI/UX)
- **Restaurant Dashboard 2.0:** 
    - Triển khai **Kanban Board** quản lý đơn hàng theo cột (Mới nhận, Đang nấu, Hoàn thành).
    - Tối ưu hóa hiệu năng với phân trang Server-side và bộ lọc nhanh.
- **Driver Mobile-First:** 
    - Giao diện tối ưu cho thiết bị di động với Bottom Navigation.
    - Luồng xử lý đơn hàng trực quan: Nhận đơn → Đến nhà hàng → Lấy hàng → Giao hàng.
- **Landing Page Redesign:** 
    - Sử dụng phong cách **Glassmorphism**, hiệu ứng animations (fade-up, floating).
    - Tự động điều hướng người dùng (Routing logic) dựa trên vai trò sau khi đăng nhập.

#### 3. Bảo mật & Di chuyển Dữ liệu (UUID Migration)
- **UUID Primary Key:** Chuyển đổi toàn bộ `User ID` từ kiểu `Long` sang `UUID` để ngăn chặn việc dò quét dữ liệu và hỗ trợ mở rộng hệ thống.
- **Security Hardening:** 
    - Hoàn thiện hệ thống xác thực OTP qua Gmail.
    - Rà soát Null-safety và tối ưu hóa các khối xử lý ngoại lệ trong `Notification` và `Chat` API.

### 📦 Sản phẩm giai đoạn hoàn thiện

| # | Sản phẩm | Trạng thái |
| :--- | :--- | :--- |
| 1 | Hệ thống Chat Real-time (WebSocket) | ✅ |
| 2 | Quản lý Đơn hàng Kanban (Restaurant) | ✅ |
| 3 | Giao diện Mobile-First (Driver) | ✅ |
| 4 | Database UUID Migration | ✅ |
| 5 | Hệ thống Thông báo & OTP | ✅ |
| 6 | Landing Page Modern UI | ✅ |

---

## ✅ Tuần 6 – Hệ thống Bộ nhớ Architect & An toàn Thông tin (AI Memory System & Security Hardening)

**Mục tiêu:** Thiết lập hệ thống tài liệu thông minh ("memory system") hỗ trợ lập trình AI, rà soát lỗ hổng bảo mật thời gian thực và dọn dẹp kỹ thuật (technical debt).

### 🔍 Hoạt động đã thực hiện

#### 1. Xây dựng Hệ thống Bộ nhớ AI (AI Memory System)
*   **[PROJECT_STATE.md](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/PROJECT_STATE.md)**: Bản đồ kiến trúc, tech stack (Spring Boot 3.3.0, Java 17, MySQL), luồng dữ liệu nghiệp vụ (Order, Live Tracking, WebSocket Chat) và tài nguyên tái sử dụng.
*   **[CONSTRAINTS.md](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/CONSTRAINTS.md)**: Quy chuẩn viết mã nghiêm ngặt, ranh giới phân tách 3 tầng (Controller-Service-Repository), cấm tuyệt đối sử dụng thư viện Lombok, quy định định danh khóa chính User UUID dạng String, và chống trùng lặp code.
*   **[project-manifest.json](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/project-manifest.json)**: File cấu trúc máy (JSON) lưu trữ toàn bộ API routes, database schemas, WebSocket channels, và đồ thị phụ thuộc giữa các module.
*   **[RECENT_CHANGES.md](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/RECENT_CHANGES.md)**: Nhật ký cập nhật lịch sử thay đổi mã nguồn.
*   **Thư mục [modules/](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/modules/)**: Bộ tài liệu chuyên sâu cho 6 module nghiệp vụ lõi (`auth`, `profile`, `order`, `tracking`, `chat`, `notification`).

#### 2. Phân tích Lỗ hổng Bảo mật & Nợ Kỹ thuật
*   **Phát hiện lỗ hổng rò rỉ STOMP:** Phát hiện lỗ hổng nghiêm trọng thiếu xác thực subscription đối với kênh chat `/topic/order.{orderId}` tại `WebSocketAuthInterceptor`. Đề xuất giải pháp kiểm tra token và quyền tham gia đơn hàng.
*   **Rà soát rủi ro bên thứ ba:** Phát hiện nguy cơ bị OpenStreetMap chặn IP do thiếu header `User-Agent` khi gọi API Nominatim trong `GeocodingService`.
*   **Định vị mã nguồn cũ:** Xác định lớp adapter dư thừa `OrderContactService` để đưa vào danh sách loại bỏ trong kỳ bảo trì tiếp theo.

### 📦 Sản phẩm tuần 6

| # | Sản phẩm | Trạng thái |
| :--- | :--- | :--- |
| 1 | Bộ nhớ AI Memory System (`*.md`, `*.json`) | ✅ |
| 2 | Bộ tài liệu chuyên biệt từng module (`modules/`) | ✅ |
| 3 | Báo cáo phân tích bảo mật & technical debt | ✅ |

---

## 📌 Các tuần trước

### Tuần 1–2
- Phân tích yêu cầu, viết SRS
- Xây dựng Use Case Diagram và các kịch bản
- Tạo repository Git, cấu trúc ban đầu

### Tuần 3 – Thiết kế Lớp và Tạo cơ sở Code
- Xây dựng Class Diagram (16 entity chính).
- Tạo khung mã nguồn (Skeleton) Spring Boot.
- Triển khai hệ thống đăng ký & xác thực OTP Email.

### Tuần 5 – Hoàn thiện, Chat Real-time & UUID Migration
- Hệ thống Chat Real-time (WebSocket STOMP).
- Quản lý Đơn hàng Kanban (Restaurant).
- Giao diện Mobile-First (Driver).
- Database UUID Migration.

---

*Cập nhật lần cuối: 02/06/2026*

