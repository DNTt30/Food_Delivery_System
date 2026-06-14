# Tổng hợp các Sơ đồ UML (Mã Mermaid)

> **Hướng dẫn sử dụng với Draw.io:**
> Bạn có thể copy trực tiếp các đoạn mã Mermaid dưới đây, vào Draw.io chọn **Arrange -> Insert -> Advanced -> Mermaid...** (hoặc **Sắp xếp -> Chèn -> Nâng cao -> Mermaid...**), dán mã vào và Draw.io sẽ tự động vẽ ra hình cho bạn!

---

### 1. Sơ đồ Use Case Tổng quát (General Use Case Diagram)
*Mô tả: Thể hiện tương tác giữa 4 Actor (Khách hàng, Nhà hàng, Tài xế, Quản trị viên) với các tính năng chính của hệ thống.*

```mermaid
flowchart LR
    C(("Khách hàng\n(Customer)"))
    R(("Nhà hàng\n(Restaurant)"))
    D(("Tài xế\n(Driver)"))
    A(("Quản trị\n(Admin)"))

    subgraph System ["Food Delivery System"]
        direction TB
        UC1(["Đăng ký / Đăng nhập (OTP)"])
        UC2(["Quản lý Profile"])
        UC3(["Tìm kiếm & Đặt món"])
        UC4(["Thanh toán Online / Hoàn tiền"])
        UC5(["Quản lý Thực đơn"])
        UC6(["Xác nhận & Chuẩn bị đơn"])
        UC7(["Nhận đơn giao hàng"])
        UC8(["Cập nhật vị trí (GPS)"])
        UC9(["Chat Trực tiếp (Real-time)"])
        UC10(["Quản lý Người dùng"])
        UC11(["Quản lý Mã giảm giá"])
    end

    C --- UC1
    R --- UC1
    D --- UC1
    
    C --- UC2
    R --- UC2
    D --- UC2
    
    C --- UC3
    C --- UC4
    C --- UC9
    
    R --- UC5
    R --- UC6
    R --- UC9
    
    D --- UC7
    D --- UC8
    D --- UC9
    
    A --- UC10
    A --- UC11
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
