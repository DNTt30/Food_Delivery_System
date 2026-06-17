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
- ✅ **Xác thực & Đăng ký (UC-01, UC-02, UC-04):** Đăng ký tài khoản mới & xác thực OTP qua Email (UC-01), Đăng nhập hệ thống (UC-02), Quên mật khẩu & cấp lại (UC-04).
- ✅ **Quản lý Hồ sơ (UC-03):** Cập nhật thông tin cá nhân, định vị vị trí. Trải nghiệm Premium UI/UX với giao diện Glassmorphism, Dark Mode hiện đại, Micro-animations cực mượt.
- ✅ **Tìm kiếm, Xem Thực đơn & Giỏ hàng (UC-05, UC-06, UC-07):** Tìm kiếm nhà hàng thông minh (UC-05), xem chi tiết thực đơn món ăn (UC-06), quản lý giỏ hàng Global Real-time (UC-07). Hỗ trợ **Geocoding Search** tính phí ship tự động qua Haversine. Cho phép áp dụng song song **Mã Freeship** và **Mã giảm giá món ăn**.
- ✅ **Đặt đơn hàng (UC-08):** Đặt món và khởi tạo chu trình xử lý đơn hàng.
- ✅ **Thanh toán Trực tuyến (UC-09):** Tích hợp cổng thanh toán trực tuyến (VNPAY/MoMo) và cơ chế tự động hoàn tiền (Refund) khi bị hủy đơn.
- ✅ **Theo dõi đơn (UC-10):** Theo dõi tiến trình đơn hàng động (Tracking Progress UI Bar) và vị trí tài xế thời gian thực.
- ✅ **Hủy đơn hàng (UC-11):** Khách hàng tự hủy đơn hàng khi còn trong trạng thái chờ nhà hàng xác nhận.
- ✅ **Đánh giá đơn (UC-12):** Gửi feedback sao và bình luận sau khi nhận hàng thành công.
- ✅ **Lịch sử Giao dịch (E-Wallet):** Xem lại toàn bộ lịch sử nạp/rút/thanh toán với giao diện Ví điện tử chuyên nghiệp, tách biệt khỏi Lịch sử đơn hàng.
- ✅ **Chat Trực tiếp:** Nhắn tin với Nhà hàng (PENDING/PREPARING/DELIVERING) và Tài xế (PREPARING/DELIVERING) qua WebSocket.
- **Món Ăn Yêu Thích:** Thả tim video món ăn để lưu vào danh sách Yêu thích. Dữ liệu được bảo toàn tuyệt đối kể cả khi đăng xuất.

### 🏪 Nhà hàng (Restaurant)
- ✅ **Quản lý Hồ sơ (UC-03):** Cập nhật thông tin, bản đồ chọn vị trí (Leaflet Map Picker) tự động đồng bộ tọa độ, upload banner.
- ✅ **Quản lý Thực đơn & Danh mục (UC-13, UC-14):** CRUD danh mục món ăn (UC-13) và món ăn chi tiết (UC-14), upload hình ảnh chi tiết, cấu hình giá.
- ✅ **Xử lý Đơn hàng (UC-15):** Tiếp nhận, chuẩn bị chế biến, bàn giao cho Tài xế qua bảng Kanban (Mới/Đang chuẩn bị/Xong).
- ✅ **Báo cáo Doanh thu (UC-19):** Thống kê doanh thu trực quan dưới dạng biểu đồ (Chart.js), xem chi tiết đơn theo ngày/tháng trên Dashboard.
- ✅ **Chat Trực tiếp:** Nhắn tin với Khách hàng và Tài xế theo từng giai đoạn đơn hàng.

### 🛵 Tài xế (Driver)
- ✅ **Quản lý Hồ sơ (UC-03):** Cập nhật thông tin cá nhân, chọn vị trí làm việc.
- ✅ **Nhận & Giao hàng (UC-16):** Xem danh sách đơn đang chờ, nhận đơn (giới hạn **1 đơn hàng cùng lúc**), cập nhật trạng thái (PREPARING → DELIVERING → COMPLETED).
- ✅ **Lịch sử Giao hàng:** Xem lại lịch sử với giao diện **Accordion** thông minh.
- ✅ **Tracking:** Gửi vị trí GPS Real-time cho Khách hàng.
- ✅ **Chat Trực tiếp:** Nhắn tin với Khách hàng và Nhà hàng khi đang xử lý đơn.

### 🛡️ Quản trị viên (Admin)
- ✅ **Quản lý Người dùng (UC-17):** Khóa/mở khóa tài khoản người dùng, quản trị vai trò.
- ✅ **Duyệt Nhà hàng (UC-18):** Xét duyệt đối tác nhà hàng mới đăng ký tham gia hệ thống.
- ✅ **Báo cáo Doanh thu Hệ thống (UC-19):** Xem tổng quan doanh thu toàn hệ thống với **Line Chart** trực quan, danh sách đơn hàng gần đây hỗ trợ **Server-Side Pagination**.
- ✅ **Quản lý Voucher (UC-20):** Tạo/sửa/xóa Voucher khuyến mãi (% hoặc số tiền cố định), thiết lập **Giới hạn số lượt sử dụng** (Toàn hệ thống và Cá nhân) để ngăn chặn trục lợi.
- ✅ **Broadcast Notifications:** Gửi thông báo hệ thống đồng loạt đến toàn bộ người dùng hoặc theo vai trò cụ thể.

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
| [Báo cáo tuần (Google Drive)](https://drive.google.com/drive/folders/19BwHr-GmpaY6r-E1r86X4kxD8mDm7mMH?usp=drive_link) | Thư mục Drive chứa báo cáo tiến độ tuần |
| [Báo cáo bài tập lớn (Google Drive)](https://drive.google.com/drive/folders/149eQSMiAWrHXUksXz_WqrA_sWgfAjusm?usp=drive_link) | Thư mục Drive chứa báo cáo bài tập lớn (Đồ án) |

---

## 📞 Liên hệ & Support

- **GitHub Issues:** [Báo cáo bug](https://github.com/DNTt30/Food_Delivery_System/issues)
- **Giấy phép:** [MIT License](LICENSE)

---

*Cập nhật: 02/06/2026 | Dự án phát triển ứng dụng Web - Phân tích & Thiết kế Phần mềm*
- **Email:** [contact@example.com]
- **Issues:** GitHub Issues
- **Documentation:** [Project Wiki]

---

## 📄 Giấy phép

Dự án này được cấp phép theo [MIT License](LICENSE)

---

## ✨ Cảm ơn

Cảm ơn tất cả những người đã góp phần vào dự án này!

