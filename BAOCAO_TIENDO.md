# 📋 SỔ TAY BÁO CÁO TIẾN ĐỘ DỰ ÁN
## Hệ thống Giao đồ ăn trực tuyến — Food Delivery System

> **Ngày báo cáo:** 09/05/2026  
> **Sinh viên:** Dương Ngọc Tú  
> **Email:** phamhoanganh3182005@gmail.com  
> **Công nghệ:** Spring Boot 3.3 · Spring Security · JWT · MySQL · Thymeleaf · Bootstrap 5

---

## 1. TỔNG QUAN DỰ ÁN

Xây dựng nền tảng đặt và giao đồ ăn trực tuyến hoàn chỉnh với **4 nhóm actor**, **20 use case** và kiến trúc REST API + Thymeleaf server-side rendering.

### Kiến trúc tổng thể

```
┌─────────────┐    JWT Token     ┌──────────────────────────┐
│  Browser    │ ◄─────────────► │   Spring Boot Application  │
│ (Thymeleaf) │    REST API     │                            │
└─────────────┘                 │  ┌─────────────────────┐  │
                                │  │  Spring Security    │  │
                                │  │  JWT Filter         │  │
                                │  └─────────────────────┘  │
                                │  ┌─────────────────────┐  │
                                │  │  Controllers        │  │
                                │  │  Services           │  │
                                │  │  Repositories (JPA) │  │
                                │  └─────────────────────┘  │
                                │             │              │
                                └─────────────┼──────────────┘
                                              │
                                   ┌──────────▼──────────┐
                                   │   MySQL (Aiven)     │
                                   └─────────────────────┘
```

### Stack công nghệ

| Thành phần | Công nghệ |
|---|---|
| **Backend** | Spring Boot 3.3.0, Spring MVC, Spring Data JPA |
| **Bảo mật** | Spring Security, JWT (HMAC-SHA256), BCrypt |
| **Database** | MySQL 8 — Cloud Aiven, Hibernate ORM |
| **Frontend** | Thymeleaf, Bootstrap 5.3, Bootstrap Icons |
| **JavaScript** | Vanilla JS, Fetch API, LocalStorage |
| **Email** | Gmail SMTP (OTP xác minh) |
| **Triển khai** | Localhost (Spring Boot Embedded Tomcat) |

---

## 2. CẤU TRÚC DATABASE

```
users ──────────────────────────────────────────────────────────┐
  id, username, password, fullName, email, role,                 │
  enabled, verificationCode, codeExpiry                          │
                                                                 │
customer_profiles ───────────────────────── (1-1 users)         │
  id, user_id, phoneNumber, deliveryAddress                      │
                                                                 │
restaurant_profiles ────────────────────── (1-1 users)          │
  id, user_id, restaurantName, address, bannerUrl,              │
  isOpen, averageRating                                          │
                                                                 │
driver_profiles ────────────────────────── (1-1 users)          │
  id, user_id, phoneNumber, licensePlate, isAvailable           │
                                                                 │
menu_items ─────────────────────────────── (M-1 restaurant)     │
  id, restaurant_id, name, description, price,                  │
  imageUrl, isAvailable                                          │
                                                                 │
food_orders ──────────────────────────────────────────────────  │
  id, customer_id, restaurant_id, driver_id,                    │
  status (ENUM), totalAmount, orderTime, deliveryAddress         │
                                                                 │
order_items ──────────────────────── (M-1 food_orders)          │
  id, order_id, menuItem_id, quantity, priceAtTimeOfOrder       │
                                                                 │
reviews ──────────────────────────── (1-1 food_orders)          │
  id, order_id, rating, comment, createdAt                      │
                                                                 │
vouchers                                                         │
  id, code, discountValue, discountType,                        │
  expirationDate, isActive                                      │
└───────────────────────────────────────────────────────────────┘
```

---

## 3. LUỒNG HOẠT ĐỘNG HỆ THỐNG

### 3.1 Luồng đặt hàng đầy đủ

```
[CUSTOMER] Tìm nhà hàng → Chọn món → Thêm giỏ hàng → Đặt đơn
                                                           │
                                                    PENDING ▼
[RESTAURANT] Nhận thông báo → Xác nhận đơn → Chuẩn bị món
                                                    PREPARING ▼
[DRIVER] Xem danh sách đơn sẵn → Nhận đơn → Đến lấy hàng
                                                   DELIVERING ▼
[DRIVER] Giao đến khách → Xác nhận hoàn thành
                                                   COMPLETED ▼
[CUSTOMER] Nhận hàng → Đánh giá → Lịch sử đơn hàng
```

### 3.2 Luồng Xác thực

```
Đăng ký → Gửi OTP Gmail → Xác minh OTP → Kích hoạt tài khoản
    ↓                                              ↓
Đăng nhập ──── JWT Token (24h) ──────────── Truy cập API
    ↓
Quên mật khẩu → Gửi OTP → Xác nhận → Đặt mật khẩu mới
```

### 3.3 Phân quyền theo Role

| Role | Trang chủ | Quyền chính |
|---|---|---|
| `ROLE_CUSTOMER` | `/customer/home` | Đặt hàng, theo dõi, đánh giá |
| `ROLE_RESTAURANT` | `/restaurant/dashboard` | Quản lý menu, xác nhận đơn |
| `ROLE_DRIVER` | `/driver/dashboard` | Nhận đơn, giao hàng |
| `ROLE_ADMIN` | `/admin/dashboard` | Quản trị toàn hệ thống |

---

## 4. TIẾN ĐỘ 20 USE CASE

### Nhóm 1 — Xác thực & Tài khoản (UC-01 → UC-04)

| UC | Tên | Trạng thái | Ghi chú |
|---|---|---|---|
| UC-01 | Đăng ký tài khoản | ✅ Hoàn thành | OTP qua Gmail, xác minh email |
| UC-02 | Đăng nhập | ✅ Hoàn thành | JWT, redirect theo role |
| UC-03 | Quản lý hồ sơ | ✅ Hoàn thành | Cập nhật thông tin, upload banner |
| UC-04 | Đổi / Quên mật khẩu | ✅ Hoàn thành | OTP email, reset password |

### Nhóm 2 — Customer (UC-05 → UC-12)

| UC | Tên | Trạng thái | Ghi chú |
|---|---|---|---|
| UC-05 | Tìm kiếm nhà hàng/món | ✅ Hoàn thành | Search + filter + sort |
| UC-06 | Xem thực đơn nhà hàng | ✅ Hoàn thành | Xem món, hình ảnh, giá |
| UC-07 | Thêm vào giỏ hàng | ✅ Hoàn thành | LocalStorage, badge động |
| UC-08 | Đặt đơn hàng | ✅ Hoàn thành | Địa chỉ, ghi chú, thanh toán |
| UC-09 | Thanh toán | ✅ Hoàn thành | COD / Ví điện tử (demo) |
| UC-10 | Theo dõi trạng thái đơn | ✅ Hoàn thành | Timeline, auto-refresh 15s |
| UC-11 | Hủy đơn hàng | ✅ Hoàn thành | Chỉ được hủy khi PENDING |
| UC-12 | Đánh giá đơn hàng | ✅ Hoàn thành | 5 sao + bình luận |

### Nhóm 3 — Restaurant & Driver (UC-13 → UC-16)

| UC | Tên | Trạng thái | Ghi chú |
|---|---|---|---|
| UC-13 | Quản lý danh mục món ăn | ✅ Hoàn thành | CRUD menu items |
| UC-14 | Quản lý thực đơn | ✅ Hoàn thành | Upload ảnh, bật/tắt, giá |
| UC-15 | Xác nhận/từ chối đơn | ✅ Hoàn thành | Auto-refresh 30s, flow đầy đủ |
| UC-16 | Giao hàng (Driver) | ✅ Hoàn thành | Nhận đơn → Giao → Hoàn thành |

### Nhóm 4 — Admin (UC-17 → UC-20)

| UC | Tên | Trạng thái | Ghi chú |
|---|---|---|---|
| UC-17 | Quản lý tài khoản người dùng | ✅ Hoàn thành | Khóa/mở, xóa tài khoản |
| UC-18 | Duyệt/quản lý nhà hàng | ✅ Hoàn thành | Approve, toggle open/close |
| UC-19 | Xem báo cáo doanh thu | ✅ Hoàn thành | Chart.js, thống kê thực tế |
| UC-20 | Quản lý mã khuyến mãi | ✅ Hoàn thành | CRUD voucher, % & cố định |

### Tổng tiến độ: **20/20 Use Case — 100% ✅**

---

## 5. DANH SÁCH API ENDPOINTS

### Auth API (`/api/auth`)
```
POST /api/auth/register          Đăng ký tài khoản
POST /api/auth/verify            Xác minh OTP email
POST /api/auth/login             Đăng nhập → JWT Token
POST /api/auth/resend-code       Gửi lại OTP
POST /api/auth/forgot-password   Quên mật khẩu
POST /api/auth/reset-password    Đặt lại mật khẩu
```

### Profile API (`/api/profile`)
```
GET  /api/profile/me             Lấy thông tin hồ sơ
PUT  /api/profile/me             Cập nhật hồ sơ
PUT  /api/profile/password       Đổi mật khẩu
POST /api/upload/image           Upload hình ảnh
```

### Customer API (`/api/customer`)
```
GET  /api/customer/restaurants           Danh sách nhà hàng (tìm kiếm)
GET  /api/customer/restaurants/{id}      Chi tiết nhà hàng + menu
POST /api/customer/orders                Đặt đơn hàng
GET  /api/customer/orders                Lịch sử đơn hàng
GET  /api/customer/orders/{id}           Chi tiết đơn hàng
PUT  /api/customer/orders/{id}/cancel    Hủy đơn hàng
POST /api/customer/orders/{id}/review    Đánh giá đơn hàng
```

### Restaurant API (`/api/restaurant`)
```
GET  /api/restaurant/dashboard           Thống kê nhà hàng
PUT  /api/restaurant/toggle-status       Mở/đóng cửa nhà hàng
GET  /api/restaurant/menu                Danh sách món ăn
POST /api/restaurant/menu                Thêm món mới
PUT  /api/restaurant/menu/{id}           Cập nhật món
DELETE /api/restaurant/menu/{id}         Xóa món
GET  /api/restaurant/orders              Đơn hàng cần xử lý
PUT  /api/restaurant/orders/{id}/status  Cập nhật trạng thái đơn
```

### Driver API (`/api/driver`)
```
GET  /api/driver/dashboard               Thống kê tài xế
PUT  /api/driver/availability            Bật/tắt trực tuyến
GET  /api/driver/available-orders        Đơn sẵn sàng giao (PREPARING + no driver)
POST /api/driver/orders/{id}/accept      Nhận đơn giao hàng
PUT  /api/driver/orders/{id}/complete    Hoàn thành giao hàng
GET  /api/driver/my-deliveries           Đơn đang giao
GET  /api/driver/history                 Lịch sử giao hàng
```

### Admin API (`/api/admin`)
```
GET  /api/admin/stats                    Thống kê hệ thống
GET  /api/admin/users                    Danh sách người dùng
PUT  /api/admin/users/{id}/toggle-status Khóa/mở tài khoản
DELETE /api/admin/users/{id}             Xóa tài khoản
GET  /api/admin/restaurants              Danh sách nhà hàng
PUT  /api/admin/restaurants/{id}/approve Duyệt nhà hàng
PUT  /api/admin/restaurants/{id}/toggle-open  Mở/đóng nhà hàng
GET  /api/admin/vouchers                 Danh sách voucher
POST /api/admin/vouchers                 Tạo voucher
PUT  /api/admin/vouchers/{id}            Cập nhật voucher
DELETE /api/admin/vouchers/{id}          Xóa voucher
```

---

## 6. DANH SÁCH GIAO DIỆN (PAGES)

### Trang chung
| Trang | URL | Mô tả |
|---|---|---|
| Landing Page | `/` | Trang giới thiệu, đăng nhập/đăng ký |
| Xác thực | `/common/auth` | Đăng nhập, đăng ký, quên mật khẩu |
| Hồ sơ | `/common/profile` | Xem và chỉnh sửa thông tin cá nhân |

### Customer Pages
| Trang | URL | Mô tả |
|---|---|---|
| Trang chủ | `/customer/home` | Danh sách nhà hàng, tìm kiếm |
| Chi tiết | `/customer/detail` | Menu nhà hàng, thêm giỏ hàng |
| Giỏ hàng | `/customer/cart` | Xem giỏ, đặt đơn |
| Theo dõi | `/customer/tracking` | Timeline trạng thái đơn |
| Lịch sử | `/customer/history` | Lịch sử, đánh giá, hủy đơn |

### Restaurant Pages
| Trang | URL | Mô tả |
|---|---|---|
| Dashboard | `/restaurant/dashboard` | Thống kê, biểu đồ, mở/đóng cửa |
| Đơn hàng | `/restaurant/orders` | Xử lý đơn, xác nhận/từ chối |
| Thực đơn | `/restaurant/menu` | CRUD món ăn, upload ảnh |

### Driver Pages
| Trang | URL | Mô tả |
|---|---|---|
| Dashboard | `/driver/dashboard` | Thống kê, trạng thái online |
| Nhận đơn | `/driver/new_orders` | Danh sách đơn cần giao |
| Đang giao | `/driver/delivering` | Đơn đang giao + lịch sử |

### Admin Pages
| Trang | URL | Mô tả |
|---|---|---|
| Dashboard | `/admin/dashboard` | Thống kê hệ thống, biểu đồ |
| Đối tác | `/admin/partners` | Quản lý user + nhà hàng |
| Khuyến mãi | `/admin/promotions` | CRUD voucher |

---

## 7. CÁC TÍNH NĂNG NỔI BẬT

### 🔐 Bảo mật
- JWT stateless authentication (không session)
- OTP 6 chữ số qua Gmail SMTP, hết hạn sau 15 phút
- Role-based authorization trên từng API endpoint
- Auto-redirect về trang login khi token hết hạn

### 🛒 Giỏ hàng thông minh
- Lưu trên LocalStorage theo từng nhà hàng (`fd_cart_{id}`)
- Badge động trên navbar cập nhật realtime
- Tự chuyển đến đúng giỏ khi click icon cart

### 📊 Dashboard thực tế
- Chart.js bar chart + doughnut chart cho restaurant
- Admin stats từ database thực: orders, users, revenue
- Driver earnings = 10% tổng giá trị đơn hoàn thành

### ⚡ Real-time Updates
- Customer tracking: auto-refresh 15 giây
- Restaurant orders: auto-refresh 30 giây
- Driver new_orders: auto-refresh 20 giây

### 📱 Responsive Design
- Bootstrap 5 grid system
- Mobile-friendly navbar với hamburger menu
- Sidebar dashboard ẩn/hiện trên mobile

---

## 8. CẤU TRÚC PROJECT

```
Food_Delivery_System/
├── src/main/java/com/duong/salesmanagement/
│   ├── controller/
│   │   ├── AuthController.java          # Đăng ký, đăng nhập, OTP
│   │   ├── CustomerApiController.java   # UC-05 → UC-12
│   │   ├── RestaurantApiController.java # UC-13 → UC-15
│   │   ├── DriverApiController.java     # UC-16
│   │   ├── AdminApiController.java      # UC-17 → UC-20
│   │   ├── ProfileApiController.java    # UC-03, UC-04
│   │   └── WebController.java           # Route mapping Thymeleaf pages
│   ├── model/
│   │   ├── User.java, Role.java
│   │   ├── CustomerProfile.java
│   │   ├── RestaurantProfile.java
│   │   ├── DriverProfile.java
│   │   ├── FoodOrder.java, OrderItem.java, OrderStatus.java
│   │   ├── MenuItem.java
│   │   ├── Review.java
│   │   └── Voucher.java, DiscountType.java
│   ├── repository/           # Spring Data JPA interfaces
│   ├── service/
│   │   ├── AuthService.java    # Đăng ký, OTP, reset password
│   │   ├── OrderService.java   # Nghiệp vụ đặt hàng, giao hàng
│   │   ├── ProfileService.java # Quản lý hồ sơ
│   │   └── EmailService.java   # Gửi OTP qua Gmail
│   └── security/
│       ├── SecurityConfig.java        # Spring Security configuration
│       ├── JwtUtil.java               # Tạo/xác minh JWT
│       └── JwtAuthenticationFilter.java
├── src/main/resources/
│   ├── templates/
│   │   ├── layouts/
│   │   │   ├── main_layout.html       # Layout trang chủ
│   │   │   ├── customer_layout.html   # Layout khách hàng
│   │   │   └── dashboard_layout.html  # Layout dashboard (sidebar)
│   │   ├── fragments/
│   │   │   ├── header.html            # Header landing + dashboard
│   │   │   ├── footer.html            # Footer fragments
│   │   │   ├── navbar_customer.html   # Navbar customer
│   │   │   ├── sidebar_dashboard.html # Sidebar với navigation
│   │   │   └── scripts.html           # Bootstrap JS + global utils
│   │   ├── common/ (auth.html, profile.html)
│   │   ├── customer/ (home, detail, cart, tracking, history)
│   │   ├── restaurant/ (dashboard, orders, menu)
│   │   ├── driver/ (dashboard, new_orders, delivering)
│   │   └── admin/ (dashboard, partners, promotions)
│   ├── static/ (css, js, images)
│   └── application.properties
└── pom.xml
```

---

## 9. HƯỚNG DẪN CHẠY PROJECT

### Yêu cầu môi trường
- Java 17+
- Maven 3.8+
- MySQL (hoặc dùng cloud Aiven đã cấu hình sẵn)
- Kết nối Internet (Gmail SMTP, Bootstrap CDN)

### Chạy ứng dụng
```bash
cd gs-serving-web-content-main/complete
mvn spring-boot:run
```

Truy cập: **http://localhost:8080**

### Tài khoản test (ví dụ)
| Role | Username | Password |
|---|---|---|
| Customer | *(đăng ký mới)* | *(OTP Gmail)* |
| Restaurant | *(đăng ký mới)* | *(OTP Gmail)* |
| Driver | *(đăng ký mới)* | *(OTP Gmail)* |
| Admin | *(tạo thẳng DB)* | *(set role=ADMIN)* |

---

## 10. NHỮNG VẤN ĐỀ ĐÃ GIẢI QUYẾT

| # | Vấn đề | Giải pháp |
|---|---|---|
| 1 | Profile không được tạo tự động sau đăng ký | `orElseGet()` auto-create trong mỗi `getAuthenticated*()` |
| 2 | Restaurant/Driver không thấy đơn hàng | Fix `null` return → auto-create RestaurantProfile/DriverProfile |
| 3 | 2 footer xuất hiện trên landing page | Chuyển footer vào layout, không để từng trang include lẻ |
| 4 | Đăng xuất không hoạt động | Inline `localStorage.removeItem()` trực tiếp trong `onclick` |
| 5 | `DiscountType.PERCENT` không tồn tại | Đổi thành `DiscountType.PERCENTAGE` theo enum |
| 6 | `th:inline="javascript"` gây lỗi parse | Bỏ inline JS, dùng `data-portal-role` attribute thay thế |
| 7 | Trang profile không đồng bộ UI với các trang khác | Viết lại `renderHeaderFooter()` theo từng role |
| 8 | Duplicate `class` attribute trong navbar | Merge 2 `class` thành 1 attribute duy nhất |

---

## 11. HẠN CHẾ & HƯỚNG PHÁT TRIỂN

### Hạn chế hiện tại
- Thanh toán online chỉ là demo (chưa tích hợp VNPAY/MoMo thật)
- Chưa có chức năng chat real-time giữa customer - restaurant - driver
- Trang ứng dụng mobile (App Store / Google Play) chỉ là placeholder
- Voucher chưa được áp dụng vào tổng tiền khi đặt hàng
- Chưa có push notification (websocket)

### Hướng phát triển tiếp theo
- Tích hợp thanh toán VNPAY hoặc MoMo
- WebSocket real-time notification
- Bản đồ giao hàng (Google Maps API)
- Admin báo cáo doanh thu theo tháng/năm
- App mobile (React Native hoặc Flutter)
- Docker + CI/CD deployment

---

*© 2026 — Food Delivery System | Spring Boot Project*
