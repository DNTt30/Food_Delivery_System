# 🍔 Food Delivery Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blueviolet.svg)](https://docs.spring.io/spring-framework/reference/web/websocket.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📌 Giới thiệu

Nền tảng giao đồ ăn toàn diện kết nối **Khách hàng**, **Nhà hàng**, **Tài xế** trên **Spring Boot 3.3 + Java 17**.

**✅ 100% Use Case | ⚡ WebSocket Chat | 🗺️ GPS Tracking | 🎯 4 Vai trò | 📊 Dashboard**

---

## 🚀 Chạy Nhanh

**Yêu cầu:** Java 17+, Maven 3.8+, MySQL 8.0+

```bash
# Clone
git clone https://github.com/DNTt30/Food_Delivery_System.git
cd Food_Delivery_System/gs-serving-web-content-main/complete

# Build & Run
./mvnw spring-boot:run
```

Truy cập: **http://localhost:8080**

**Cấu hình database:** [Xem chi tiết ↓](#cấu-hình-database)

---

## 📂 Cấu trúc Dự án

```
Food_Delivery_System/
├── gs-serving-web-content-main/complete/
│   ├── src/main/java/com/duong/salesmanagement/
│   │   ├── controller/     # REST APIs (Auth, Profile, Order, Chat...)
│   │   ├── service/        # Business Logic (Order, Chat, Auth...)
│   │   ├── repository/     # Spring Data JPA
│   │   ├── model/          # JPA Entities
│   │   ├── security/       # JWT, WebSocket Auth
│   │   ├── dto/            # Request/Response DTOs
│   │   ├── exception/      # Custom Exceptions
│   │   ├── config/         # WebSocket, Security Config
│   │   └── util/           # Utilities (Phone mask, etc)
│   ├── src/main/resources/
│   │   ├── templates/      # Thymeleaf HTML
│   │   └── static/         # CSS, JS, Images
│   └── pom.xml
├── modules/                # Module documentation (6 modules)
├── Documents/              # Design & technical docs
└── README.md
```

---

## ⚙️ Chức năng Chính (20/20 Use Case)

### 👤 Khách hàng (Customer)
- ✅ **Xác thực & Đăng ký:** OTP qua Email, Reset mật khẩu
- ✅ **Trải nghiệm Premium UI/UX:** Giao diện Glassmorphism, Dark Mode hiện đại, Micro-animations cực mượt.
- ✅ **Food TikTok Feed:** Khám phá món ăn qua video ngắn, lướt vuốt thả tim siêu thực tế.
- ✅ **Món Ăn Yêu Thích:** Thả tim video món ăn để lưu vào danh sách Yêu thích, mua sắm nhanh chóng.
- ✅ **Tìm kiếm & Đặt hàng:** Search thông minh, filter nhà hàng, Giỏ hàng Global Real-time (Hiệu ứng chấm đỏ nảy sinh động).
- ✅ **Quản lý Đơn hàng:** Xem lịch sử, hủy đơn (trạng thái cho phép)
- ✅ **Theo dõi Giao hàng:** Live GPS Tracking trực quan với bản đồ và thanh tiến trình động (Tracking Progress Bar)
- ✅ **Đánh giá & Feedback:** Gửi feedback sao sau khi nhận hàng
- ✅ **Chat Trực tiếp:** Nhắn tin với Nhà hàng (PENDING/PREPARING/DELIVERING) và Tài xế (PREPARING/DELIVERING)

### 🏪 Nhà hàng (Restaurant)
- ✅ **Quản lý Hồ sơ:** Cập nhật thông tin, bản đồ chọn vị trí (Leaflet Map Picker) tự động đồng bộ tọa độ, upload banner
- ✅ **Quản lý Thực đơn:** CRUD món ăn, upload hình ảnh chi tiết, set giá
- ✅ **Quản lý Đơn hàng:** Tiếp nhận, chế biến, bàn giao cho Tài xế
- ✅ **Dashboard:** Thống kê doanh thu trực quan (Chart.js), xem đơn theo ngày/tháng
- ✅ **Chat Trực tiếp:** Nhắn tin với Khách hàng và Tài xế theo từng giai đoạn đơn hàng

### 🛵 Tài xế (Driver)
- ✅ **Quản lý Hồ sơ:** Cập nhật thông tin cá nhân, chọn vị trí làm việc
- ✅ **Nhận Đơn hàng:** Xem danh sách đơn đang chờ, nhận đơn theo khu vực
- ✅ **Giao hàng:** Cập nhật trạng thái (PREPARING → DELIVERING → COMPLETED)
- ✅ **Tracking:** Gửi vị trí GPS Real-time cho Khách hàng
- ✅ **Chat Trực tiếp:** Nhắn tin với Khách hàng và Nhà hàng khi đang xử lý đơn

### 🛡️ Quản trị viên (Admin)
- ✅ **Quản lý Người dùng:** Khóa/mở tài khoản, duyệt đối tác nhà hàng mới
- ✅ **Hệ thống Khuyến mãi:** Tạo/sửa/xóa Voucher (% hoặc số tiền cố định)
- ✅ **Thống kê Hệ thống:** Xem tổng quan doanh thu, số đơn, người dùng

---

## 💬 Hệ thống Chat (WebSocket/STOMP)

Tính năng chat được triển khai theo kiến trúc **WebSocket / STOMP** cho phép truyền tải tin nhắn tức thời với Widget pop-up phong cách ShopeeFood.

### Quy tắc Chat
| Cặp chat | Trạng thái cho phép | Mô tả |
| :--- | :--- | :--- |
| Khách hàng ↔ Nhà hàng | PENDING, PREPARING, DELIVERING | Hỏi về menu, thời gian chuẩn bị |
| Khách hàng ↔ Tài xế | PREPARING, DELIVERING | Hỏi về vị trí, thời gian giao |
| Tài xế ↔ Nhà hàng | PREPARING, DELIVERING | Xác nhận đơn hàng, chi tiết giao |
| Tất cả | COMPLETED / CANCELLED | **Chỉ đọc**, số điện thoại bị mask |

### Tính năng Nổi bật
- ⚡ **Real-time:** WebSocket/STOMP tức thời (milliseconds)
- 🔒 **An toàn:** Tự động khóa chat khi đơn hoàn thành/hủy
- 📞 **Bảo mật:** Hiển thị số điện thoại, mask ở dạng `098****321` khi đơn đóng
- 🎨 **UI/UX:** Màu header thay đổi theo vai trò:
  - Nhà hàng: **Cam (#FF6B35)**
  - Tài xế: **Xanh lá (#4CAF50)**
  - Khách hàng: **Xanh dương (#2196F3)**
- 🔐 **Phân quyền:** Chỉ người thuộc đơn mới đọc/gửi được

---

## 🛠️ Stack Công nghệ

| Lớp | Công nghệ |
| :--- | :--- |
| **Backend** | Spring Boot 3.3.0 · Java 17 · Spring Data JPA |
| **Bảo mật** | Spring Security · JWT · OTP Email |
| **Database** | MySQL 8.0 (Aiven Cloud) · Hibernate |
| **Frontend** | Thymeleaf · Bootstrap 5 · Vanilla JS · Chart.js |
| **Real-time** | WebSocket/STOMP (Chat) · HTTP Polling (GPS) |
| **Email** | Gmail SMTP Server |

---

## 📡 API Endpoints

### Authentication API
```
POST   /api/auth/register         # Đăng ký tài khoản
POST   /api/auth/verify-otp       # Xác thực OTP
POST   /api/auth/login            # Đăng nhập
POST   /api/auth/forgot-password  # Quên mật khẩu
POST   /api/auth/reset-password   # Reset mật khẩu
```

### Profile API
```
GET    /api/profile               # Lấy hồ sơ người dùng
PUT    /api/profile               # Cập nhật hồ sơ
GET    /api/profile/all/:role     # Lấy danh sách theo vai trò
GET    /api/restaurant/profile    # Hồ sơ nhà hàng
PUT    /api/restaurant/profile    # Cập nhật hồ sơ nhà hàng
```

### Order API (Customer)
```
GET    /api/customer/restaurants  # Danh sách nhà hàng
GET    /api/customer/menu/:rid    # Thực đơn nhà hàng
POST   /api/customer/orders       # Tạo đơn hàng
GET    /api/customer/orders       # Lịch sử đơn hàng
GET    /api/customer/orders/:id   # Chi tiết đơn hàng
PUT    /api/customer/orders/:id/cancel # Hủy đơn hàng
```

### Order API (Restaurant)
```
GET    /api/restaurant/orders     # Đơn hàng của nhà hàng
PUT    /api/restaurant/orders/:id/status # Cập nhật trạng thái
POST   /api/restaurant/menu       # Thêm món ăn
PUT    /api/restaurant/menu/:id   # Cập nhật món ăn
DELETE /api/restaurant/menu/:id   # Xóa món ăn
```

### Order API (Driver)
```
GET    /api/driver/available-orders # Danh sách đơn chờ
POST   /api/driver/orders/:id/accept # Nhận đơn
PUT    /api/driver/orders/:id/status # Cập nhật trạng thái giao
```

### Chat API
```
GET    /api/chat/messages/:orderId       # Lấy tin nhắn của đơn
POST   /api/chat/messages                # Gửi tin nhắn
GET    /api/chat/contacts/:userId       # Danh sách liên hệ
WS     /ws/chat                          # WebSocket STOMP endpoint
```

### Tracking API
```
GET    /api/tracking/current/:orderId    # GPS hiện tại của Tài xế
POST   /api/tracking/update              # Cập nhật vị trí GPS
GET    /api/tracking/history/:orderId    # Lịch sử GPS
```

### Notification API
```
GET    /api/notifications                # Danh sách thông báo
PUT    /api/notifications/:id/read       # Đánh dấu đã đọc
PUT    /api/notifications/read-all       # Đánh dấu tất cả đã đọc
```

### Voucher API (Admin)
```
GET    /api/admin/vouchers               # Danh sách voucher
POST   /api/admin/vouchers               # Tạo voucher
PUT    /api/admin/vouchers/:id           # Cập nhật voucher
DELETE /api/admin/vouchers/:id           # Xóa voucher
```

---

## 🏗️ Kiến trúc Hệ thống

```
┌─────────────────────────────────────────────────────────┐
│  Client Layer (Browser)                                 │
│  ├─ Thymeleaf Templates (HTML)                         │
│  ├─ Vanilla JavaScript (Fetch, WebSocket)              │
│  └─ CSS3 + Bootstrap 5 (Responsive UI)                 │
└────────────────┬────────────────────────────────────────┘
                 │ HTTP/HTTPS + WebSocket
┌────────────────▼────────────────────────────────────────┐
│  Spring Security Layer                                  │
│  ├─ JwtAuthenticationFilter (Token validation)         │
│  ├─ SecurityConfig (Route guards)                      │
│  └─ STOMP Channel Interceptor (WebSocket auth)         │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│  Controller Layer (REST API)                            │
│  ├─ AuthController, ProfileApiController               │
│  ├─ CustomerApiController, RestaurantApiController     │
│  ├─ DriverApiController, WebSocketChatController       │
│  └─ Other feature controllers                          │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│  Service Layer (Business Logic)                         │
│  ├─ AuthService (Registration, JWT, OTP)              │
│  ├─ ProfileService (User profiles by role)            │
│  ├─ OrderService (Complete order lifecycle)           │
│  ├─ ChatService (Chat rules, locks, permissions)      │
│  ├─ LocationTrackingService (GPS logging)             │
│  ├─ ShippingCalculationService (Fee estimation)       │
│  └─ Other business services                           │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│  Repository Layer (Data Access)                         │
│  ├─ Spring Data JPA Repositories                       │
│  ├─ Custom @Query methods                              │
│  └─ Native SQL queries                                 │
└────────────────┬─────────────## 👨‍💻 Đội ngũ Phát triển

| Thành viên | MSSV | Vai trò |
| :--- | :--- | :--- |
| **Dương Ngọc Tú** | 22010052 | Fullstack Developer / Team Lead |
| **Đinh Thị Như Quỳnh** | 23010844 | Frontend Developer / Documentation |
| **Ngô Minh Quân** | 23017112 | Backend Developer / Database |

---

## 📚 Tài liệu & Liên kết

| Tài liệu | Nội dung |
| :--- | :--- |
| [Documents/README.md](Documents/README.md) | Nhật ký phát triển (Tuần 1-7) |
| [PROJECT_STATE.md](PROJECT_STATE.md) | Bản đồ kiến trúc & tech stack |
| [CONSTRAINTS.md](CONSTRAINTS.md) | Quy chuẩn viết mã |
| [RECENT_CHANGES.md](RECENT_CHANGES.md) | Nhật ký thay đổi code |
| [modules/](modules/) | Tài liệu 6 module chuyên sâu |
| [CHANGELOG.md](CHANGELOG.md) | Lịch sử phiên bản |

---

## 📞 Liên hệ & Support

- **GitHub Issues:** [Báo cáo bug](https://github.com/DNTt30/Food_Delivery_System/issues)
- **Giấy phép:** [MIT License](LICENSE)

---

*Cập nhật: 21/05/2026 | Dự án phát triển ứng dụng Web - Phân tích & Thiết kế Phần mềm*** Development Team
- **Email:** [contact@example.com]
- **Issues:** GitHub Issues
- **Documentation:** [Project Wiki]

---

## 📄 Giấy phép

Dự án này được cấp phép theo [MIT License](LICENSE)

---

## ✨ Cảm ơn

Cảm ơn tất cả những người đã góp phần vào dự án này!

| Thành phần | Công nghệ sử dụng |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.3.0, Spring Data JPA |
| **Bảo mật** | Spring Security, JWT (Stateless), OTP Email Verification |
| **Database** | MySQL 8 (Aiven Cloud), Hibernate (ddl-auto=update) |
| **Frontend** | Thymeleaf, Bootstrap 5.3, Vanilla JS, Chart.js |
| **Real-time** | WebSocket / STOMP (Chat), HTTP Polling (Tracking) |
| **Email** | Gmail SMTP Server |

---

## 🚀 Hướng dẫn Chạy Dự án

### 1. Yêu cầu Hệ thống
- **Java JDK 17** trở lên.
- **Maven 3.8+**.
- **MySQL** (Hoặc dùng database cloud đã cấu hình sẵn).

### 2. Clone & Cài đặt
```bash
git clone https://github.com/DNTt30/Food_Delivery_System.git
cd Food_Delivery_System/gs-serving-web-content-main/complete
```

### 3. Khởi chạy
Sử dụng Maven Wrapper:
```powershell
# Windows
.\mvnw spring-boot:run

# Linux/MacOS
./mvnw spring-boot:run
```

### 4. Truy cập
👉 [http://localhost:8080](http://localhost:8080)

---

## 📁 Cấu trúc Project chính

```text
src/main/java/com/duong/salesmanagement/
├── controller/     # API Endpoints & Web Controllers (Chat, Profile, Order...)
├── model/          # Entities (User, FoodOrder, ChatMessage, MenuItem...)
├── service/        # Business Logic (ChatService, OrderService...)
├── repository/     # Data Access Layer (JPA)
├── security/       # JWT, WebSocket Auth Interceptor
├── dto/            # Request/Response DTOs
├── exception/      # Custom Exceptions (ChatLocked, ChatAccessDenied...)
├── config/         # WebSocket, Security Config
└── util/           # PhoneMaskUtil, helpers
```

---

## 👨‍💻 Đội ngũ Phát triển

| Thành viên | MSSV | Vai trò |
| :--- | :--- | :--- |
| **Dương Ngọc Tú** | 22010052 | Fullstack Developer / Team Lead |
| **Đinh Thị Như Quỳnh** | 23010844 | Frontend Developer / Documentation |
| **Ngô Minh Quân** | 23017112 | Backend Developer / Database |

---

## � Tài liệu & Liên kết

| Tài liệu | Nội dung |
| :--- | :--- |
| [Documents/README.md](Documents/README.md) | Nhật ký phát triển (Tuần 1-7) |
| [PROJECT_STATE.md](PROJECT_STATE.md) | Bản đồ kiến trúc & tech stack |
| [CONSTRAINTS.md](CONSTRAINTS.md) | Quy chuẩn viết mã |
| [RECENT_CHANGES.md](RECENT_CHANGES.md) | Nhật ký thay đổi code |
| [modules/](modules/) | Tài liệu 6 module chuyên sâu |
| [CHANGELOG.md](CHANGELOG.md) | Lịch sử phiên bản |

---



*Cập nhật: 21/05/2026 | Dự án phát triển ứng dụng Web - Phân tích & Thiết kế Phần mềm*


