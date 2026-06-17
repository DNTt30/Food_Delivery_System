# 📁 Tài liệu Dự án – Hệ thống Quản lý Đặt đồ ăn Trực tuyến

> **Nhóm:** TEAM 12  
> **Môn học:** Phân tích & Thiết kế Phần mềm  
> **GitHub:** [Food_Delivery_System](https://github.com/DNTt30/Food_Delivery_System)

---

## 📊 Tiến độ Dự án

| Tuần | Mục tiêu | Trạng thái |
| :--- | :--- | :--- |
| 1–2 | Phân tích yêu cầu (SRS), Use Case Diagram | ✅ |
| 3 | Class Diagram, Code Skeleton, OTP Email | ✅ |
| 5 | Chat Real-time (WebSocket), UUID Migration | ✅ |
| 6 | Memory System, Bảo mật & Technical Debt | ✅ |
| 7 | Cập nhật README & Tài liệu | ✅ |

---

## ✅ Tuần 3 – Thiết kế Lớp & Code Skeleton

**Hoạt động:** 
- Trích xuất **16 lớp entity** từ use case (User, CustomerProfile, RestaurantProfile, DriverProfile, MenuItem, FoodOrder, OrderItem, Payment, Review, Voucher, etc.)
- Xác định **13+ quan hệ** giữa lớp với multiplicity đầy đủ
- Vẽ **Class Diagram** chi tiết (ClassDiagram_Team12_Tuan3.drawio)
- Tạo **Spring Boot Skeleton** với cấu trúc 3-tầng (Controller-Service-Repository)
- Triển khai **OTP Email xác thực** qua Gmail SMTP + giao diện hiện đại

**Sản phẩm:** ✅ Class Diagram | ✅ Code Skeleton | ✅ AuthService + OTP

---

## ✅ Tuần 5 – Hoàn thiện & UUID Migration

**Hoạt động:**
- **Chat Real-time:** WebSocket/STOMP thay Polling, widget pop-up, tự động khóa theo trạng thái đơn
- **Restaurant Dashboard:** Kanban Board, phân trang server-side, bộ lọc nhanh
- **Driver Mobile-First:** Bottom navigation, luồng xử lý đơn trực quan
- **UUID Migration:** Chuyển User ID từ `Long` → `UUID` (String) tăng bảo mật

**Sản phẩm:** ✅ WebSocket Chat | ✅ Kanban UI | ✅ Mobile Driver | ✅ UUID DB

---

## ✅ Tuần 6 – Memory System & Security Hardening

**Hoạt động:**
- **AI Memory System:** 
  - [PROJECT_STATE.md](../PROJECT_STATE.md) - Bản đồ kiến trúc, tech stack, module
  - [CONSTRAINTS.md](../CONSTRAINTS.md) - Quy chuẩn viết mã, phân tách 3 tầng
  - [project-manifest.json](../project-manifest.json) - API routes, DB schemas, WebSocket
  - [RECENT_CHANGES.md](../RECENT_CHANGES.md) - Nhật ký thay đổi
  - `modules/` - Tài liệu chuyên sâu 6 module (auth, profile, order, tracking, chat, notification)

- **Phân tích Bảo mật:**
  - Phát hiện lỗ hổng WebSocket auth (subscription `/topic/order.{orderId}`)
  - Rà soát rủi ro API Nominatim (thiếu User-Agent header)
  - Xác định technical debt (OrderContactService dư thừa)

**Sản phẩm:** ✅ Memory System | ✅ Docs Module | ✅ Security Analysis

---

## ✅ Tuần 7 – Tích hợp Thanh toán Trực tuyến, Sửa lỗi Geocoding & Giao diện Tài chính

**Hoạt động:**
- **Khắc phục lỗi định vị (Nominatim API):** Bổ sung header `User-Agent` (`FoodDeliveryApp/1.0`) cho RestTemplate của `GeocodingService` để giải quyết triệt để lỗi 403 Forbidden.
- **Tích hợp Map Picker tự động lưu tọa độ:** Nâng cấp bản đồ chọn vị trí (Leaflet Map) trên trang cá nhân của Khách hàng và Đối tác Nhà hàng, hỗ trợ tự động đồng bộ và lưu tọa độ `latitude`/`longitude`.
- **Cơ chế Snapshots & Fallback thông minh:** Cập nhật `OrderService` tự động geocode địa chỉ giao hàng, bổ sung cơ chế tự sửa lỗi (self-healing) tọa độ.
- **Thanh toán Trực tuyến (VNPAY/MoMo) & Hoàn tiền (Refund):** Xây dựng luồng thanh toán online `AWAITING_PAYMENT`. Cập nhật logic: Tự động đánh dấu `REFUNDED` nếu nhà hàng hủy đơn khách đã trả tiền, xử lý rủi ro đơn hàng bị treo khi khách thoát trang thanh toán.
- **Giao diện Lịch sử giao dịch (E-wallet UI):** Làm lại hoàn toàn giao diện Lịch sử thanh toán cho khách hàng, tách biệt khỏi lịch sử đơn hàng. Bổ sung các UI theo dạng danh sách ví điện tử, hiển thị +/- dòng tiền rõ ràng, biểu tượng icon trực quan (Bank, Wallet, Cash).
- **Thanh trạng thái tiến trình giao hàng (Tracking Progress UI Bar):** Thêm thanh tiến trình trực quan động trên trang theo dõi đơn hàng (`tracking.html`).

**Sản phẩm:** ✅ Bản vá lỗi Nominatim API 403 | ✅ Payment Gateway & Refund | ✅ E-Wallet Transaction UI | ✅ Map Picker Auto-Save | ✅ Tracking Progress Bar

---

## 📚 Tài liệu Tham khảo

| Tài liệu | Link | Mục đích |
| :--- | :--- | :--- |
| **Architecture** | [PROJECT_STATE.md](../PROJECT_STATE.md) | Bản đồ hệ thống & tech stack |
| **Constraints** | [CONSTRAINTS.md](../CONSTRAINTS.md) | Quy chuẩn, cấm tuyệt đối |
| **Recent Changes** | [RECENT_CHANGES.md](../RECENT_CHANGES.md) | Nhật ký thay đổi code |
| **Manifest** | [project-manifest.json](../project-manifest.json) | API routes, DB schemas |
| **Module: Auth** | [modules/auth.md](../modules/auth.md) | JWT, OTP, Security |
| **Module: Profile** | [modules/profile.md](../modules/profile.md) | 3 vai trò customer/restaurant/driver |
| **Module: Order** | [modules/order.md](../modules/order.md) | Quản lý đơn, vận chuyển |
| **Module: Tracking** | [modules/tracking.md](../modules/tracking.md) | GPS Real-time |
| **Module: Chat** | [modules/chat.md](../modules/chat.md) | WebSocket STOMP |
| **Module: Notification** | [modules/notification.md](../modules/notification.md) | Thông báo hệ thống |
| **Weekly Reports** | [Báo cáo tuần (Drive)](https://drive.google.com/drive/folders/19BwHr-GmpaY6r-E1r86X4kxD8mDm7mMH?usp=drive_link) | Thư mục Drive chứa báo cáo tiến độ hàng tuần |
| **Project Report** | [Báo cáo bài tập lớn (Drive)](https://drive.google.com/drive/folders/149eQSMiAWrHXUksXz_WqrA_sWgfAjusm?usp=drive_link) | Thư mục Drive chứa báo cáo tổng kết môn học (Đồ án) |

---

## 🚀 Công nghệ Stack

| Lớp | Công nghệ | Phiên bản |
| :--- | :--- | :--- |
| **Backend** | Spring Boot | 3.3.0 |
| **Language** | Java | 17 |
| **Database** | MySQL (Aiven Cloud) | 8.0 |
| **Authentication** | Spring Security + JJWT | Latest |
| **Real-time** | WebSocket + STOMP | Latest |
| **Email** | JavaMailSender (Gmail SMTP) | Latest |
| **Frontend** | Thymeleaf + Bootstrap 5 + Chart.js | Latest |

---

## ⭐ Đặc điểm Nổi bật

✅ **100% Use Case hoàn thiện** (20/20)  
✅ **Chat Real-time** WebSocket STOMP  
✅ **UUID Security** thay Long ID  
✅ **Kanban Dashboard** quản lý đơn  
✅ **Mobile-first UI** tối ưu di động  
✅ **OTP Email** xác thực người dùng  
✅ **Live Tracking GPS** tức thời  
✅ **Phân quyền 4 vai trò** (Customer, Restaurant, Driver, Admin)

---

*Cập nhật lần cuối: 02/06/2026*
