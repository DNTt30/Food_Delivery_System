🍔 Food Delivery Management System

Hệ thống quản lý đặt đồ ăn trực tuyến
(Online Food Ordering and Delivery Management System)

📌 Giới thiệu

Dự án là một ứng dụng web hỗ trợ toàn bộ quy trình đặt đồ ăn trực tuyến, từ việc tìm kiếm món ăn, đặt hàng, thanh toán đến theo dõi và giao hàng.

Hệ thống đóng vai trò kết nối giữa Khách hàng, Nhà hàng và Shipper, giúp tối ưu hóa quy trình xử lý đơn hàng và nâng cao trải nghiệm người dùng.

🎯 Mục tiêu
Cung cấp trải nghiệm đặt đồ ăn nhanh chóng và thuận tiện
Hỗ trợ quy trình đặt hàng khép kín (tìm kiếm → đặt món → thanh toán → giao hàng → theo dõi)
Hỗ trợ quản lý hiệu quả cho nhà hàng và quản trị viên
👥 Tác nhân (Actors)
Khách hàng (Customer): xem menu, đặt hàng, theo dõi đơn
Nhà hàng / Admin: quản lý món ăn và đơn hàng
Shipper (Tùy chọn): giao hàng và cập nhật trạng thái
⚙️ Chức năng chính
👤 Khách hàng
Xem và tìm kiếm món ăn
Thêm món vào giỏ hàng
Đặt hàng
Theo dõi trạng thái đơn
Xem lịch sử đơn hàng
🧑‍🍳 Nhà hàng / Admin
Quản lý món ăn (thêm / sửa / xóa)
Xử lý đơn hàng
Cập nhật trạng thái đơn
Quản lý người dùng
Xem báo cáo (tùy chọn)
🚚 Shipper (Tùy chọn)
Nhận đơn giao hàng
Cập nhật trạng thái giao
🏗️ Kiến trúc hệ thống

Hệ thống được xây dựng theo mô hình Layered Architecture:

Controller → Service → Repository → Database
Controller: xử lý request từ client
Service: xử lý logic nghiệp vụ
Repository: truy xuất dữ liệu
Database: lưu trữ dữ liệu (MySQL)
🛠️ Công nghệ sử dụng
Backend: Spring Boot
Database: MySQL
Security: Spring Security
Testing: JUnit 5
Build Tool: Maven
📁 Cấu trúc dự án
src/
 ├── controller/
 ├── service/
 ├── repository/
 ├── model/
 ├── config/
 └── resources/
🚀 Hướng dẫn chạy dự án
1. Clone project
git clone https://github.com/DNTt30/Food_Delivery_System.git
cd Food_Delivery_System
2. Cấu hình database

Chỉnh file:

src/main/resources/application.properties

Ví dụ:

spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery
spring.datasource.username=root
spring.datasource.password=your_password
3. Chạy ứng dụng
mvn spring-boot:run
4. Truy cập hệ thống
http://localhost:8080
🧪 Kiểm thử

Chạy test:

mvn test
📊 UML (dự kiến)
Use Case Diagram
Class Diagram
Sequence Diagram
State Machine Diagram
👨‍💻 Thành viên nhóm
Dương Ngọc Tú – Backend, Database
Đinh Thị Như Quỳnh – UI/UX, SRS
Ngô Minh Quân – Testing, UML