# 📁 Tài liệu Dự án – Hệ thống Quản lý Đặt đồ ăn Trực tuyến

> **Nhóm:** TEAM 14  
> **Môn học:** Phân tích & Thiết kế Phần mềm  
> **GitHub:** [Food_Delivery_System](https://github.com/DNTt30/Food_Delivery_System)

---

## 📋 Mục lục tài liệu

| Loại tài liệu | Mô tả |
| :--- | :--- |
| Kế hoạch (Plan) | Lịch trình, phân công công việc theo tuần |
| Phân tích yêu cầu (SRS) | Đặc tả yêu cầu phần mềm |
| Báo cáo hàng tuần | Tổng kết hoạt động mỗi tuần |
| Class Diagram | Sơ đồ lớp thiết kế hệ thống |

---

## ✅ Tuần 3 – Thiết kế Lớp và Tạo cơ sở Code

**Mục tiêu:** Xây dựng Biểu đồ Lớp đầy đủ và tạo khung mã nguồn (code skeleton) cho toàn bộ hệ thống.

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

- **File:** `ClassDiagram_Team14_Tuan3.drawio`
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
| 1 | Biểu đồ Lớp (Class Diagram) | `ClassDiagram_Team14_Tuan3.drawio` | ✅ |
| 2 | Tài liệu thiết kế lớp (Word) | `ClassDiagram_Tuan3_Team14.docx` | ✅ |
| 3 | Khung mã nguồn (Skeleton) | `src/.../model/*.java` | ✅ |
| 4 | Hệ thống xác thực OTP/Mail | `EmailService.java`, `AuthService.java` | ✅ |
| 5 | Giao diện Đăng ký & Reset PW | `register.html`, `forgot-password.html` | ✅ |
| 6 | README hướng dẫn chạy | `README.md` (root) | ✅ |

---

## 📌 Các tuần trước

### Tuần 1–2
- Phân tích yêu cầu, viết SRS
- Xây dựng Use Case Diagram và các kịch bản
- Tạo repository Git, cấu trúc ban đầu

---

*Cập nhật lần cuối: Tuần 3 – Tháng 4/2026*
