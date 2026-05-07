# 🍔 Food Delivery Management System


---

## 📌 Giới thiệu

Hệ thống cung cấp giải pháp đặt món, quản lý đơn hàng và kết nối hiệu quả giữa **Khách hàng – Nhà hàng – Shipper**. Dự án được xây dựng với mục tiêu tối ưu hóa trải nghiệm người dùng và quy trình vận hành đơn hàng.

## ⚙️ Chức năng chính

- **Khách hàng:** Tìm kiếm món ăn, đặt hàng, quản lý giỏ hàng và theo dõi trạng thái đơn hàng.
- **Nhà hàng/Admin:** Quản lý thực đơn, xử lý đơn hàng và cập nhật thông tin cửa hàng.
- **Shipper:** Nhận đơn, cập nhật lộ trình và trạng thái giao hàng.

---

## 🛠️ Công nghệ sử dụng

- **Backend:** Java 17, Spring Boot 3.3.0
- **Database:** MySQL (Aiven Cloud / Local)
- **Security:** Spring Security & JWT
- **Build Tool:** Maven

---

## 🚀 Hướng dẫn chạy dự án

### 1. Yêu cầu hệ thống
- **Java JDK 17** trở lên.
- **MySQL** (nếu chạy database cục bộ).
- IDE khuyên dùng: IntelliJ IDEA hoặc VS Code.

### 2. Clone dự án
Mở Terminal và chạy lệnh:
```bash
git clone https://github.com/DNTt30/Food_Delivery_System.git
cd Food_Delivery_System/gs-serving-web-content-main/complete
```

### 3. Cấu hình Database
Chỉnh sửa cấu hình kết nối tại file:  
`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Khởi chạy ứng dụng
Sử dụng Maven Wrapper (không cần cài đặt Maven thủ công):

**Đối với Windows (PowerShell/CMD):**
```powershell
.\mvnw spring-boot:run
```

**Đối với Linux/MacOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### 5. Truy cập ứng dụng
Sau khi ứng dụng khởi chạy thành công, truy cập:  
👉 [http://localhost:8080](http://localhost:8080)

---

## 👨‍💻 Đội ngũ phát triển

| Tên thành viên | Mã sinh viên |
| :--- | :--- |
| **Dương Ngọc Tú** | 22010052 |
| **Đinh Thị Như Quỳnh** | 23010844 |
| **Ngô Minh Quân** | 23017112 |

---
## 📝 Nhật ký cập nhật (Progress Log)

### 📅 06/05/2026
- **Giao diện Dashboard Nhà hàng:** Thiết kế lại hoàn toàn trang dashboard (`restaurant/dashboard.html`) với Thẻ thống kê (Stat Cards), biểu đồ **Chart.js**, và hiệu ứng **Skeleton Loader**.
- **Hình ảnh & Banner:** 
    - Thiết kế và thêm banner quảng cáo chuyên nghiệp cho Dashboard Khách hàng.
    - Cập nhật hình ảnh món ăn thực tế (Pizza, Burger, Phở) vào hệ thống.
- **Tính năng Upload ảnh:**
    - Triển khai `FileUploadController.java` xử lý nhận file ảnh từ client.
    - Tích hợp tính năng tải ảnh trực tiếp vào trang Quản lý Thực đơn của Nhà hàng.
    - Hỗ trợ xem trước ảnh (Preview) và tự động điền URL sau khi tải lên thành công.

---
*Dự án thuộc học phần thực hành Phát triển ứng dụng Web.*
