# Tổng hợp các Sơ đồ UML (Mã Mermaid)

> **Hướng dẫn sử dụng với Draw.io:**
> Bạn có thể copy trực tiếp các đoạn mã Mermaid dưới đây, vào Draw.io chọn **Arrange -> Insert -> Advanced -> Mermaid...** (hoặc **Sắp xếp -> Chèn -> Nâng cao -> Mermaid...**), dán mã vào và Draw.io sẽ tự động vẽ ra hình cho bạn!

---

### 1. Sơ đồ Use Case Tổng quát (General Use Case Diagram)
*Mô tả: Thể hiện tương tác giữa 4 Actor (Khách hàng, Nhà hàng, Tài xế, Quản trị viên) với 20 Use Cases chính của hệ thống.*

```mermaid
flowchart LR
    %% Actors
    C(("Khách hàng\n(Customer)"))
    R(("Nhà hàng\n(Restaurant)"))
    D(("Tài xế\n(Driver)"))
    A(("Quản trị viên\n(Admin)"))

    %% System Boundary
    subgraph System ["Hệ thống Giao đồ ăn (Food Delivery System)"]
        direction TB
        
        %% Authentication & Profiles
        UC1(["UC-01: Đăng ký & OTP"])
        UC2(["UC-02: Đăng nhập"])
        UC3(["UC-03: Quản lý hồ sơ"])
        UC4(["UC-04: Quên mật khẩu"])

        %% Customer Use Cases
        UC5(["UC-05: Tìm nhà hàng"])
        UC6(["UC-06: Xem thực đơn"])
        UC7(["UC-07: Quản lý giỏ hàng"])
        UC8(["UC-08: Đặt đơn hàng"])
        UC9(["UC-09: Thanh toán"])
        UC10(["UC-10: Theo dõi đơn"])
        UC11(["UC-11: Hủy đơn hàng"])
        UC12(["UC-12: Đánh giá đơn"])

        %% Restaurant Use Cases
        UC13(["UC-13: Quản lý danh mục"])
        UC14(["UC-14: Quản lý thực đơn"])
        UC15(["UC-15: Xử lý đơn hàng"])

        %% Driver Use Cases
        UC16(["UC-16: Nhận giao hàng"])

        %% Admin Use Cases
        UC17(["UC-17: Quản lý người dùng"])
        UC18(["UC-18: Duyệt nhà hàng"])
        UC19(["UC-19: Báo cáo doanh thu"])
        UC20(["UC-20: Quản lý voucher"])
    end

    %% Customer Associations
    C --- UC1
    C --- UC2
    C --- UC3
    C --- UC4
    C --- UC5
    C --- UC6
    C --- UC7
    C --- UC8
    C --- UC9
    C --- UC10
    C --- UC11
    C --- UC12

    %% Restaurant Associations
    R --- UC1
    R --- UC2
    R --- UC3
    R --- UC4
    R --- UC13
    R --- UC14
    R --- UC15
    R --- UC19

    %% Driver Associations
    D --- UC1
    D --- UC2
    D --- UC3
    D --- UC4
    D --- UC16
    D --- UC19

    %% Admin Associations
    A --- UC2
    A --- UC3
    A --- UC17
    A --- UC18
    A --- UC19
    A --- UC20
```

---

### 2. Sơ đồ Luồng Hoạt động (Activity Diagram) - Luồng Đặt món & Thanh toán
*Mô tả: Luồng xử lý chi tiết từ lúc Khách hàng đặt món, hệ thống tính phí, chọn thanh toán VNPAY/MoMo cho đến khi Nhà hàng xác nhận hoặc Hủy (hoàn tiền).*

```mermaid
stateDiagram-v2
    [*] --> KhachHangChonMon : Khách thêm món vào giỏ
    KhachHangChonMon --> TinhPhiShip : Nhập địa chỉ giao
    TinhPhiShip --> ChonThanhToan : Hệ thống tính khoảng cách (Haversine)
    
    state ChonThanhToan {
        [*] --> COD
        [*] --> VNPAY_MoMo
    }
    
    COD --> ChoXacNhan : Đơn tạo thành công (PENDING)
    VNPAY_MoMo --> CongThanhToan : Chuyển hướng VNPay/MoMo
    
    CongThanhToan --> ChoXacNhan : Thanh toán thành công (PENDING)
    CongThanhToan --> [*] : Hủy thanh toán (CANCELLED)
    
    ChoXacNhan --> NhaHangChuanBi : Nhà hàng Nhận đơn (PREPARING)
    ChoXacNhan --> HoanTien : Nhà hàng Từ chối (CANCELLED)
    
    HoanTien --> [*] : Hệ thống tự động Refund (Nếu thanh toán Online)
    
    NhaHangChuanBi --> TaiXeNhanDon : Đang chờ tài xế
    TaiXeNhanDon --> GiaoHang : Tài xế lấy hàng (DELIVERING)
    GiaoHang --> HoanThanh : Khách nhận hàng (COMPLETED)
    HoanThanh --> [*]
```

---

### 3. Sơ đồ Tuần tự (Sequence Diagram) - Luồng Chat Real-time
*Mô tả: Mô tả cách WebSocket (STOMP) hoạt động khi Khách hàng và Tài xế chat trực tiếp với nhau, bao gồm quá trình phân quyền và lưu trữ.*

```mermaid
sequenceDiagram
    actor C as Khách hàng
    participant UI as Giao diện Web (Client)
    participant WS as WebSocket Controller
    participant CS as ChatService
    participant DB as MySQL Database
    actor D as Tài xế

    C->>UI: Mở khung chat & Gõ tin nhắn
    UI->>WS: Gửi STOMP Message (/app/chat.sendMessage)
    WS->>CS: processMessage(messageDTO)
    
    activate CS
    CS->>CS: Kiểm tra quyền (Order Status, Role)
    CS->>DB: Lưu ChatMessage vào Database
    DB-->>CS: Trả về Entity đã lưu
    CS-->>WS: Message hợp lệ
    deactivate CS
    
    WS->>UI: Broadcast (/topic/order.{orderId})
    WS->>D: Broadcast (/topic/order.{orderId})
    
    D-->>UI: Tài xế nhận tin nhắn tức thì
```

---

### 4. Sơ đồ Triển khai Kiến trúc (Deployment / Architecture Diagram)
*Mô tả: Cấu trúc hệ thống 3-tier, giao tiếp với các dịch vụ bên ngoài như Email SMTP, OpenStreetMap, VNPAY.*

```mermaid
graph TD
    subgraph Client Tier
        Web[Trình duyệt Web Khách, Quán, Tài xế]
    end

    subgraph Server Tier Spring Boot 3
        WS[WebSocket STOMP]
        REST[RESTful API Controllers]
        SEC[Spring Security & JWT]
        SER[Service Layer Business Logic]
        REP[Spring Data JPA Repositories]
        
        Web <-->|HTTP/REST| SEC
        Web <-->|ws://| SEC
        SEC <--> REST
        SEC <--> WS
        REST <--> SER
        WS <--> SER
        SER <--> REP
    end

    subgraph Data Tier
        DB[(MySQL 8.0 Cloud)]
        REP <-->|Hibernate / SQL| DB
    end

    subgraph External Services
        SMTP[Gmail SMTP]
        MAPS[OpenStreetMap / Nominatim]
        PAY[VNPAY / MoMo Gateway]
        
        SER -->|Gửi OTP| SMTP
        SER -->|Geocoding| MAPS
        SER -->|Tạo URL| PAY
    end
```
