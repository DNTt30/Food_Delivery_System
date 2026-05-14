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

## 📝 Nhật ký Cập nhật

Chi tiết quá trình phát triển và các bản cập nhật tính năng (Tuần 1 - Tuần 6) được ghi chép đầy đủ tại:  
👉 [**Tài liệu Dự án – Nhật ký Phát triển**](file:///d:/review%20SPRING_BOOT/springBoot_template-main/Food_Delivery_System/Documents/README.md)

---
*Dự án thuộc học phần thực hành Phát triển ứng dụng Web.*

