# 🍔 Food Delivery Management System
**Dự án môn học: Phân Tích và Thiết Kế Phần Mềm**
- Tên Repository Git: `PTTKPM25-26_ClassNx_Nhom<x>`
- Môn học: Phân tích và thiết kế phần mềm 2025-2026
- Nhóm: <Điền số thứ tự của nhóm>
- Tên Lớp: <Điền mã lớp>

**Các giảng viên đã được cấp quyền:**
1. Thầy vqdzung@gmail.com
2. Thầy/Cô que.nguyenxuan@phenikaa-uni.edu.vn
**Hệ thống quản lý đặt đồ ăn trực tuyến**

---

## 📌 Giới thiệu

Đây là ứng dụng web hỗ trợ đặt đồ ăn trực tuyến, cho phép người dùng tìm kiếm món ăn, đặt hàng và theo dõi trạng thái đơn.

Hệ thống kết nối **Khách hàng – Nhà hàng – Shipper**, giúp quản lý và xử lý đơn hàng hiệu quả.

---

## ⚙️ Chức năng chính

- Khách hàng: xem menu, đặt hàng, theo dõi đơn  
- Admin/Nhà hàng: quản lý món ăn và đơn hàng  
- Shipper: giao hàng và cập nhật trạng thái  

---

## 🛠️ Công nghệ

- Spring Boot  
- MySQL  
- Spring Security  
- Maven  

---

## 🚀 Hướng dẫn chạy dự án

### 1. Clone project

```bash
git clone https://github.com/DNTt30/Food_Delivery_System.git
cd Food_Delivery_System\gs-serving-web-content-main\complete
```
### 2. Cấu hình database

Chỉnh file:

src/main/resources/application.properties

Ví dụ:

spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery

spring.datasource.username=root

spring.datasource.password=your_password

### 3. Chạy ứng dụng

mvn spring-boot:run

### 4. Truy cập

http://localhost:8080

###👨‍💻 Nhóm phát triển

Dương Ngọc Tú

Đinh Thị Như Quỳnh

Ngô Minh Quân
