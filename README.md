# 🍔 Food Delivery Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📌 Giới thiệu Dự án

Hệ thống **Food Delivery Management** là một giải pháp toàn diện kết nối **Khách hàng**, **Nhà hàng** và **Tài xế (Shipper)**. Dự án được xây dựng trên nền tảng **Spring Boot 3.3**, tập trung vào sự ổn định, hiệu năng cao và trải nghiệm người dùng hiện đại với giao diện Dashboard thông minh.

---

## 🏗️ Kiến trúc Hệ thống

```mermaid
graph TD
    subgraph Client_Side
        B[Browser - Thymeleaf]
        JS[Vanilla JS / Fetch API / Polling]
    end

    subgraph Server_Side
        direction TB
        Auth[Spring Security / JWT]
        WS[WebSocket / STOMP]
        Controller[Controllers]
        Service[Business Services]
        Repo[Spring Data JPA]
        
        B <--> Auth
        JS <--> Controller
        JS <--> WS
        Controller --> Service
        Service --> Repo
    end

    subgraph Database_Cloud
        DB[(MySQL - Aiven Cloud)]
        Repo <--> DB
    end
    
    subgraph External_Services
        SMTP[Gmail SMTP - OTP]
        Service --> SMTP
    end
```

---

## ⚙️ Chức năng Chính (Use Cases)

Hệ thống đã hoàn thiện **20/20 Use Case (100%)** chia cho 4 nhóm người dùng:

### 👤 Khách hàng (Customer)
-   **Tìm kiếm & Đặt hàng:** Search thông minh, filter nhà hàng, giỏ hàng Real-time (LocalStorage).
-   **Theo dõi đơn hàng:** Tracking Timeline với chế độ tự động cập nhật mỗi 15 giây.
-   **Đánh giá:** Gửi feedback và số sao sau khi nhận hàng.
-   **Chat trực tiếp:** Nhắn tin với Nhà hàng (PENDING/PREPARING/DELIVERING) và Tài xế (PREPARING/DELIVERING) ngay trong trang theo dõi đơn hàng.

### 🏪 Nhà hàng (Restaurant)
-   **Quản lý thực đơn:** CRUD món ăn chuyên nghiệp, upload hình ảnh banner/món ăn.
-   **Quản lý đơn hàng:** Tiếp nhận, chế biến và bàn giao cho tài xế với flow mượt mà.
-   **Dashboard:** Thống kê doanh thu trực quan bằng **Chart.js** (Bar & Doughnut charts).
-   **Chat trực tiếp:** Nhắn tin với Khách hàng và Tài xế theo từng giai đoạn đơn hàng.

### 🛵 Tài xế (Driver)
-   **Nhận đơn:** Xem danh sách các đơn hàng đang chờ và nhận đơn theo khu vực.
-   **Giao hàng:** Cập nhật trạng thái "Đang giao" và "Hoàn thành". Thu nhập tự động tính 10% phí ship.
-   **Chat trực tiếp:** Nhắn tin với Khách hàng và Nhà hàng khi đang xử lý đơn.

### 🛡️ Quản trị viên (Admin)
-   **Quản trị:** Khóa/mở tài khoản, duyệt đối tác nhà hàng mới.
-   **Khuyến mãi:** Hệ thống Voucher linh hoạt (giảm % hoặc số tiền cố định).

---

## 💬 Hệ thống Chat (WebSocket STOMP Architecture)

Tính năng chat được triển khai theo kiến trúc **WebSocket / STOMP** cho phép truyền tải tin nhắn tức thời (Real-time) với Widget pop-up phong cách ShopeeFood:

| Cặp chat | Trạng thái cho phép |
| :--- | :--- |
| Khách hàng ↔ Nhà hàng | PENDING, PREPARING, DELIVERING |
| Khách hàng ↔ Tài xế | PREPARING, DELIVERING |
| Tài xế ↔ Nhà hàng | PREPARING, DELIVERING |
| Tất cả | COMPLETED / CANCELLED → **Chỉ đọc** (phone bị mask) |

**Tính năng nổi bật:**
- ⚡ Giao tiếp Real-time (WebSocket/STOMP) tức thời
- 🔒 Tự động khóa chat khi đơn hoàn thành/hủy
- 📞 Hiển thị số điện thoại (mask khi đơn đóng)
- 🎨 Màu header thay đổi theo vai trò (Nhà hàng: cam, Tài xế: xanh lá, Khách hàng: xanh dương)
- 🔐 Phân quyền chặt chẽ — chỉ người thuộc đơn mới đọc/gửi được

---

## 🛠️ Stack Công nghệ

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
├── service/        # Business Logic (ChatService, ContactService, OrderService...)
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

## 📝 Nhật ký Cập nhật

### 📅 12/05/2026 — Real-time Chat Migration
- **Chuyển đổi sang WebSocket STOMP:** Thay thế hoàn toàn cơ chế Polling cũ bằng WebSocket, giúp tin nhắn được gửi và nhận tức thời không có độ trễ.
- **Backend Implementation:** Triển khai `WebSocketChatController` xử lý message mapping và broadcasting theo order-specific topics (`/topic/messages/{orderId}`).
- **Frontend STOMP Integration:** Refactor `chat_widget.html` tích hợp `Stomp.js`, xử lý kết nối/ngắt kết nối tự động và cập nhật UI ngay khi có tin nhắn mới.
- **Tối ưu hóa UI/UX:** Fix lỗi giao diện trang chủ Khách hàng (`home.html`) và chi tiết món ăn (`detail.html`), cải thiện luồng xác thực tại `/common/auth`.
- **Refactor Core Logic:** Cập nhật `OrderService` và `RestaurantProfile` để hỗ trợ tốt hơn cho việc truy vấn dữ liệu liên quan đến chat và quản lý đơn hàng.

### 📅 11/05/2026 — Landing Page UI/UX
- **Redesign toàn diện Landing Page (`index.html`):** Nâng cấp giao diện người dùng theo phong cách ứng dụng Food Delivery thương mại hiện đại.
- **Tối ưu trải nghiệm UI:** Bổ sung hiệu ứng fade-up, floating animation, navbar glassmorphism và phối màu đồng bộ (Đỏ cam - Vàng).
- **Hoàn thiện Javascript Authentication Routing:** Xử lý logic nút "Đặt món ngay" kết hợp `localStorage` để điều hướng tự động dựa trên role (Customer, Restaurant, Driver, Admin) hoặc trả về trang `/common/auth` nếu chưa đăng nhập.

### 📅 10/05/2026 — Chat System
- **Hoàn thiện hệ thống Chat Polling:** Triển khai widget chat pop-up phong cách ShopeeFood cho 3 cặp vai trò.
- **Phân quyền chat theo trạng thái đơn:** Mở rộng visibility matrix — Customer có thể chat với Restaurant xuyên suốt quá trình giao hàng.
- **Fix lỗi tin nhắn trùng lặp:** Lọc tin nhắn theo cặp role (`myRole ↔ targetRole`) tránh hiện nhầm chat giữa các box.
- **Hiển thị SĐT trong header chat:** Số điện thoại Tài xế/Khách hiện dưới tên, tự động mask khi đơn đóng.
- **Fix lỗi DB schema:** Bảng `chat_messages` tạo lại đúng cấu trúc với FK constraints và cột `created_at`.
- **Tối ưu parse ngày giờ:** Hỗ trợ cả định dạng ISO string và mảng số từ Jackson/Spring.
- **Bảo mật code:** Fix `@NonNull` annotations, loại bỏ multi-catch redundant trong ChatApiController.

### 📅 08/05/2026
- **Hoàn thiện 100% Use Cases:** Tích hợp đầy đủ các chức năng từ Admin đến Shipper.
- **Nâng cấp Dashboard:** Sử dụng Chart.js cho các biểu đồ thống kê chuyên nghiệp.
- **Tối ưu UI/UX:** Tinh chỉnh layout, header/footer đồng bộ và hiệu ứng loading.
- **Bảo mật:** Hoàn thiện luồng OTP qua Gmail và phân quyền JWT chặt chẽ.
- **Kiến trúc:** Cập nhật sơ đồ kiến trúc hệ thống và luồng dữ liệu JWT.

---
*Dự án thuộc học phần thực hành Phát triển ứng dụng Web.*
