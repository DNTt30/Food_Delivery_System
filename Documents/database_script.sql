-- ============================================================================
-- PHỤ LỤC B: SCRIPT SQL ĐẦY ĐỦ (DATABASE INITIALIZATION & SEED SCRIPT)
-- Hệ thống: Food Delivery Management System
-- Database: MySQL 8.0+
-- Thiết kế tương thích hoàn toàn với Spring Boot 3.3 / Hibernate 6 / Java 17
-- ============================================================================

-- Tắt kiểm tra khóa ngoại tạm thời để xóa/khởi tạo bảng không bị lỗi ràng buộc
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. XÓA CÁC BẢNG NẾU ĐÃ TỒN TẠI (Dọn dẹp database trước khi khởi tạo)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS order_tracking_locations;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS food_reviews;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS food_orders;
DROP TABLE IF EXISTS vouchers;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS restaurant_profiles;
DROP TABLE IF EXISTS driver_profiles;
DROP TABLE IF EXISTS customer_profiles;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS broadcast_logs;
DROP TABLE IF EXISTS users;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 2. TẠO CÁC BẢNG (DDL)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Bảng 1: users (Thông tin tài khoản đăng nhập)
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 0,
    verification_code VARCHAR(6) NULL,
    code_expiry DATETIME NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 2: broadcast_logs (Nhật ký gửi thông báo hàng loạt của Admin)
-- ----------------------------------------------------------------------------
CREATE TABLE broadcast_logs (
    id BIGINT AUTO_INCREMENT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    target_audience VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 3: categories (Danh mục món ăn)
-- ----------------------------------------------------------------------------
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 4: customer_profiles (Hồ sơ Khách hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE customer_profiles (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NULL,
    delivery_address VARCHAR(255) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cust_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 5: driver_profiles (Hồ sơ Tài xế giao hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE driver_profiles (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    license_plate VARCHAR(255) NULL,
    phone_number VARCHAR(255) NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_driv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 6: restaurant_profiles (Hồ sơ Đối tác Nhà hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE restaurant_profiles (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    restaurant_name VARCHAR(255) NULL,
    address VARCHAR(255) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    banner_url VARCHAR(255) NULL,
    is_open TINYINT(1) NOT NULL DEFAULT 1,
    average_rating DOUBLE NULL DEFAULT 0.0,
    review_count INT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_rest_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 7: menu_items (Thực đơn món ăn của Nhà hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE menu_items (
    id BIGINT AUTO_INCREMENT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    price DOUBLE NOT NULL,
    image_url VARCHAR(255) NULL,
    video_url VARCHAR(255) NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1,
    sold_count INT NOT NULL DEFAULT 0,
    average_rating DOUBLE NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_cat FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 8: vouchers (Khuyến mãi - Voucher hệ thống hoặc Voucher của riêng Nhà hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE vouchers (
    id BIGINT AUTO_INCREMENT NOT NULL,
    code VARCHAR(255) NOT NULL,
    discount_value DOUBLE NOT NULL,
    discount_type VARCHAR(255) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT
    discount_scope VARCHAR(255) NOT NULL, -- ORDER_TOTAL, SHIPPING_FEE
    start_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    min_order_amount DOUBLE NULL DEFAULT 0.0,
    max_discount DOUBLE NULL,
    description VARCHAR(255) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    restaurant_id BIGINT NULL, -- Nullable (nếu null là voucher toàn hệ thống)
    max_global_usage INT NULL,
    current_global_usage INT NOT NULL DEFAULT 0,
    max_usage_per_user INT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_vouc_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 9: food_orders (Quản lý đơn hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE food_orders (
    id BIGINT AUTO_INCREMENT NOT NULL,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    driver_id BIGINT NULL, -- Có thể Null khi chờ tài xế nhận đơn
    status VARCHAR(32) NOT NULL, -- PENDING, PREPARING, DELIVERING, COMPLETED, CANCELLED
    total_amount DOUBLE NOT NULL,
    order_time DATETIME NOT NULL,
    delivery_address VARCHAR(255) NULL,
    delivery_address_snapshot VARCHAR(255) NULL,
    restaurant_address_snapshot VARCHAR(255) NULL,
    delivery_lat DOUBLE NULL,
    delivery_lng DOUBLE NULL,
    restaurant_lat DOUBLE NULL,
    restaurant_lng DOUBLE NULL,
    estimated_time_of_arrival DATETIME NULL,
    distance DOUBLE NULL, -- Khoảng cách (km)
    shipping_fee DOUBLE NULL,
    food_voucher_code VARCHAR(255) NULL,
    food_discount_amount DOUBLE NULL DEFAULT 0.0,
    shipping_voucher_code VARCHAR(255) NULL,
    shipping_discount_amount DOUBLE NULL DEFAULT 0.0,
    payment_method VARCHAR(255) NULL, -- CASH_ON_DELIVERY, VNPAY, MOMO_E_WALLET...
    payment_status VARCHAR(255) NULL, -- PENDING, COMPLETED, FAILED, REFUNDED
    PRIMARY KEY (id),
    CONSTRAINT fk_ord_cust FOREIGN KEY (customer_id) REFERENCES customer_profiles(id),
    CONSTRAINT fk_ord_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant_profiles(id),
    CONSTRAINT fk_ord_driv FOREIGN KEY (driver_id) REFERENCES driver_profiles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 10: order_items (Chi tiết món ăn trong từng đơn hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_time_of_order DOUBLE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 11: payments (Giao dịch Thanh toán liên kết với Đơn hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(255) NOT NULL, -- CASH_ON_DELIVERY, CREDIT_CARD, MOMO_E_WALLET, ZALOPAY, VNPAY
    payment_status VARCHAR(255) NOT NULL, -- PENDING, COMPLETED, FAILED, REFUNDED
    amount DOUBLE NOT NULL,
    transaction_date DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 12: reviews (Đánh giá chất lượng dịch vụ của đơn hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL, -- 1 đến 5 sao
    comment TEXT NULL,
    original_comment TEXT NULL,
    has_inappropriate_words TINYINT(1) NULL DEFAULT 0,
    image_url VARCHAR(255) NULL,
    image_url_json TEXT NULL,
    created_at DATETIME NOT NULL,
    restaurant_reply TEXT NULL,
    replied_at DATETIME NULL,
    helpful_count INT NULL DEFAULT 0,
    is_verified_purchase TINYINT(1) NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_rev_order FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 13: food_reviews (Đánh giá món ăn cụ thể từ khách hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE food_reviews (
    id BIGINT AUTO_INCREMENT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL,
    comment TEXT NULL,
    created_at DATETIME NOT NULL,
    rating_level VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fr_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_fr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 14: notifications (Thông báo trong hệ thống cho người dùng)
-- ----------------------------------------------------------------------------
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NULL,
    message TEXT NULL,
    type VARCHAR(50) NULL,
    related_order_id BIGINT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    broadcast_log_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index bổ trợ hiệu suất truy vấn thông báo như cấu hình trong Entity Index
CREATE INDEX idx_notif_user_id ON notifications(user_id);
CREATE INDEX idx_notif_is_read ON notifications(is_read);
CREATE INDEX idx_notif_created_at ON notifications(created_at);

-- ----------------------------------------------------------------------------
-- Bảng 15: chat_messages (Nhắn tin Real-time giữa Khách hàng và Tài xế)
-- ----------------------------------------------------------------------------
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    receiver_id VARCHAR(36) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_order FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_chat_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Bảng 16: order_tracking_locations (Theo dõi vị trí tài xế Real-time khi giao hàng)
-- ----------------------------------------------------------------------------
CREATE TABLE order_tracking_locations (
    id BIGINT AUTO_INCREMENT NOT NULL,
    order_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    tracking_phase VARCHAR(255) NOT NULL,
    timestamp DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_track_order FOREIGN KEY (order_id) REFERENCES food_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- 3. DỮ LIỆU MẪU BAN ĐẦU (DML - SEED DATA)
-- Mật khẩu mặc định của tất cả tài khoản mẫu là: password123 
-- (Được mã hóa BCrypt: $2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Bảng users (Dữ liệu 4 Vai trò: ADMIN, CUSTOMER, RESTAURANT, DRIVER)
-- ----------------------------------------------------------------------------
INSERT INTO users (id, username, password, full_name, email, role, enabled) VALUES 
('u-admin-01', 'admin', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Hệ Thống Admin', 'admin@fooddelivery.com', 'ADMIN', 1),
('u-rest-01', 'pho_giatruyen', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Trần Hữu Quyết (Phở Gia Truyền)', 'dec@pho_giatruyen.vn', 'RESTAURANT', 1),
('u-rest-02', 'pizza_home', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Lê Khánh Linh (Pizza Home)', 'pizza_home@gmail.com', 'RESTAURANT', 1),
('u-cust-01', 'tu_duong22', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Dương Ngọc Tú', 'duongngoctund2004@gmail.com', 'CUSTOMER', 1),
('u-cust-02', 'van_an_pham', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Phạm Văn An', 'customer2@gmail.com', 'CUSTOMER', 1),
('u-driv-01', 'driver_hung', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Nguyễn Mạnh Hùng', 'hung_driver@gmail.com', 'DRIVER', 1),
('u-driv-02', 'driver_long', '$2a$10$ByI78b4rU.50.J5Xm6Lh/OaT5Hn4k1F4yM66O.bHjYF1mR0P0bFvW', 'Lê Hoàng Long', 'long_driver@gmail.com', 'DRIVER', 1);

-- ----------------------------------------------------------------------------
-- Bảng categories (Danh mục thực đơn)
-- ----------------------------------------------------------------------------
INSERT INTO categories (id, name, description) VALUES 
(1, 'Cơm Trưa', 'Cơm văn phòng, cơm gia đình đầy đủ dinh dưỡng'),
(2, 'Phở & Bún', 'Các món nước truyền thống như Phở bò, Phở gà, Bún chả...'),
(3, 'Pizza & Fast Food', 'Đồ ăn nhanh, Pizza Ý, Gà rán và Khoai tây chiên'),
(4, 'Trà sữa & Đồ uống', 'Trà sữa trân châu, cà phê, nước ép trái cây giải nhiệt'),
(5, 'Ăn vặt', 'Các món ăn nhẹ đường phố hấp dẫn');

-- ----------------------------------------------------------------------------
-- Bảng customer_profiles (Thông tin phụ của Khách hàng kèm tọa độ)
-- ----------------------------------------------------------------------------
INSERT INTO customer_profiles (id, user_id, phone_number, delivery_address, latitude, longitude) VALUES 
(1, 'u-cust-01', '0987654321', 'Đại học Phenikaa, Yên Nghĩa, Hà Đông, Hà Nội', 20.9625, 105.7489),
(2, 'u-cust-02', '0912345678', 'Keangnam Landmark 72, Mễ Trì, Nam Từ Liêm, Hà Nội', 21.0167, 105.7838);

-- ----------------------------------------------------------------------------
-- Bảng driver_profiles (Thông tin phụ của Tài xế: biển số xe, trạng thái trực tuyến)
-- ----------------------------------------------------------------------------
INSERT INTO driver_profiles (id, user_id, license_plate, phone_number, is_available) VALUES 
(1, 'u-driv-01', '29-A1 12345', '0999888777', 1),
(2, 'u-driv-02', '30-B2 54321', '0977666555', 1);

-- ----------------------------------------------------------------------------
-- Bảng restaurant_profiles (Thông tin Nhà hàng kèm tọa độ và rating)
-- ----------------------------------------------------------------------------
INSERT INTO restaurant_profiles (id, user_id, restaurant_name, address, latitude, longitude, banner_url, is_open, average_rating, review_count) VALUES 
(1, 'u-rest-01', 'Phở Gia Truyền Hà Nội', '144 Xuân Thủy, Cầu Giấy, Hà Nội', 21.0366, 105.7821, '/uploads/pho_banner.jpg', 1, 4.8, 1),
(2, 'u-rest-02', 'Pizza Home & Burger', '10 Trần Đại Nghĩa, Hai Bà Trưng, Hà Nội', 21.0065, 105.8428, '/uploads/pizza_banner.jpg', 1, 4.5, 1);

-- ----------------------------------------------------------------------------
-- Bảng menu_items (Thực đơn các món ăn của mỗi quán)
-- ----------------------------------------------------------------------------
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, video_url, is_available, sold_count, average_rating, review_count) VALUES 
-- Thực đơn của Phở Gia Truyền Hà Nội (Restaurant ID: 1)
(1, 1, 2, 'Phở Bò Tái Nạm', 'Bánh phở tươi ngon, nước dùng ninh xương bò ngọt thanh kèm thịt bò tái mềm', 50000.0, '/uploads/pho_tai_nam.jpg', NULL, 1, 45, 4.8, 15),
(2, 1, 2, 'Phở Gà Ta Chặt', 'Nước dùng trong vắt ngọt béo từ gà ta, thịt gà chặt miếng giòn da', 45000.0, '/uploads/pho_ga.jpg', NULL, 1, 24, 4.6, 8),
(3, 1, 2, 'Bún Chả Hà Nội', 'Chả nướng than hoa thơm nức mũi ăn kèm nước mắm chua ngọt, đu đủ xanh', 40000.0, '/uploads/bun_cha.jpg', NULL, 1, 62, 4.7, 18),
-- Thực đơn của Pizza Home & Burger (Restaurant ID: 2)
(4, 2, 3, 'Pizza Hải Sản Cỡ Vừa', 'Đế bánh mỏng giòn, sốt Thousand Island, tôm mực thanh cua và phô mai Mozzarella kéo sợi', 129000.0, '/uploads/pizza_hai_san.jpg', NULL, 1, 38, 4.5, 12),
(5, 2, 3, 'Burger Bò Phô Mai Double', 'Double vỏ bánh burger mè mềm mại, double bò viên nướng, phô mai lát và sốt BBQ', 49000.0, '/uploads/burger_bo.jpg', NULL, 1, 85, 4.7, 23),
(6, 2, 4, 'Trà Sữa Trân Châu Đường Đen', 'Trà sữa thơm đậm đà kết hợp trân châu caramel dai giòn sần sật', 35000.0, '/uploads/tra_sua.jpg', NULL, 1, 150, 4.4, 40),
(7, 2, 5, 'Khoai Tây Chiên Lắc Phô Mai', 'Khoai tây cắt múi cau chiên vàng ruộm lắc bột phô mai mặn ngọt', 25000.0, '/uploads/khoai_tay.jpg', NULL, 1, 90, 4.3, 14);

-- ----------------------------------------------------------------------------
-- Bảng vouchers (Các mã giảm giá hệ thống và giảm giá của nhà hàng)
-- ----------------------------------------------------------------------------
INSERT INTO vouchers (id, code, discount_value, discount_type, discount_scope, start_date, expiration_date, min_order_amount, max_discount, description, is_active, restaurant_id, max_global_usage, current_global_usage, max_usage_per_user) VALUES 
-- Voucher toàn bộ hệ thống (restaurant_id = null)
(1, 'FOOD50', 50000.0, 'FIXED_AMOUNT', 'ORDER_TOTAL', '2026-06-01', '2026-12-31', 150000.0, NULL, 'Giảm ngay 50K cho đơn hàng từ 150K trở lên', 1, NULL, 500, 10, 1),
(2, 'FREESHIP15', 15000.0, 'FIXED_AMOUNT', 'SHIPPING_FEE', '2026-06-01', '2026-12-31', 50000.0, NULL, 'Miễn phí vận chuyển lên tới 15K cho đơn hàng từ 50K', 1, NULL, 1000, 45, 2),
-- Voucher riêng của Nhà hàng 2 (Pizza Home)
(3, 'PIZZA20', 20.0, 'PERCENTAGE', 'ORDER_TOTAL', '2026-06-01', '2026-08-30', 100000.0, 40000.0, 'Giảm 20% tổng hóa đơn (tối đa 40K) của Pizza Home', 1, 2, 200, 5, 2);

-- ----------------------------------------------------------------------------
-- Bảng food_orders (Bản ghi Đơn đặt hàng)
-- ----------------------------------------------------------------------------
INSERT INTO food_orders (id, customer_id, restaurant_id, driver_id, status, total_amount, order_time, delivery_address, delivery_address_snapshot, restaurant_address_snapshot, delivery_lat, delivery_lng, restaurant_lat, restaurant_lng, estimated_time_of_arrival, distance, shipping_fee, food_voucher_code, food_discount_amount, shipping_voucher_code, shipping_discount_amount, payment_method, payment_status) VALUES 
(1, 1, 1, 1, 'COMPLETED', 90000.0, '2026-06-17 12:00:00', 
 'Đại học Phenikaa, Yên Nghĩa, Hà Đông, Hà Nội', 
 'Đại học Phenikaa, Yên Nghĩa, Hà Đông, Hà Nội', 
 '144 Xuân Thủy, Cầu Giấy, Hà Nội', 
 20.9625, 105.7489, 21.0366, 105.7821, 
 '2026-06-17 12:45:00', 12.5, 35000.0, NULL, 0.0, 'FREESHIP15', 15000.0, 
 'CASH_ON_DELIVERY', 'COMPLETED'),
(2, 2, 2, 2, 'DELIVERING', 178000.0, '2026-06-17 15:30:00', 
 'Keangnam Landmark 72, Mễ Trì, Nam Từ Liêm, Hà Nội', 
 'Keangnam Landmark 72, Mễ Trì, Nam Từ Liêm, Hà Nội', 
 '10 Trần Đại Nghĩa, Hai Bà Trưng, Hà Nội', 
 21.0167, 105.7838, 21.0065, 105.8428, 
 '2026-06-17 16:15:00', 7.2, 22000.0, 'PIZZA20', 35600.0, NULL, 0.0, 
 'VNPAY', 'COMPLETED');

-- ----------------------------------------------------------------------------
-- Bảng order_items (Món ăn chi tiết của các đơn hàng trên)
-- ----------------------------------------------------------------------------
INSERT INTO order_items (id, order_id, menu_item_id, quantity, price_at_time_of_order) VALUES 
-- Đơn hàng 1: Phở Bò Tái Nạm (x1) và Bún Chả Hà Nội (x1)
(1, 1, 1, 1, 50000.0),
(2, 1, 3, 1, 40000.0),
-- Đơn hàng 2: Pizza Hải Sản (x1) và Burger Double (x1)
(3, 2, 4, 1, 129000.0),
(4, 2, 5, 1, 49000.0);

-- ----------------------------------------------------------------------------
-- Bảng payments (Lịch sử thanh toán giao dịch)
-- ----------------------------------------------------------------------------
INSERT INTO payments (id, order_id, payment_method, payment_status, amount, transaction_date) VALUES 
(1, 1, 'CASH_ON_DELIVERY', 'COMPLETED', 110000.0, '2026-06-17 12:45:00'), -- Tổng Đơn + Ship - Khuyến mãi
(2, 2, 'VNPAY', 'COMPLETED', 164400.0, '2026-06-17 15:32:00');

-- ----------------------------------------------------------------------------
-- Bảng reviews (Đánh giá dịch vụ nhà hàng sau khi hoàn tất đơn hàng)
-- ----------------------------------------------------------------------------
INSERT INTO reviews (id, order_id, rating, comment, original_comment, has_inappropriate_words, image_url, image_url_json, created_at, restaurant_reply, replied_at, helpful_count, is_verified_purchase) VALUES 
(1, 1, 5, 'Phở nước dùng rất ngọt, thịt bò nhiều và mềm. Giao hàng đúng giờ.', 'Phở nước dùng rất ngọt, thịt bò nhiều và mềm. Giao hàng đúng giờ.', 0, '/uploads/review_pho.jpg', '["/uploads/review_pho.jpg"]', '2026-06-17 13:00:00', 'Cảm ơn bạn đã ủng hộ nhà hàng, rất hân hạnh được phục vụ bạn lần sau!', '2026-06-17 14:00:00', 3, 1);

-- ----------------------------------------------------------------------------
-- Bảng food_reviews (Đánh giá món ăn cụ thể phục vụ thuật toán Recommendation)
-- ----------------------------------------------------------------------------
INSERT INTO food_reviews (id, menu_item_id, user_id, rating, comment, created_at, rating_level) VALUES 
(1, 1, 'u-cust-01', 5, 'Hương vị tuyệt hảo đậm đà vị phở Hà Nội xưa', '2026-06-17 13:02:00', 'Rất ngon');

-- ----------------------------------------------------------------------------
-- Bảng notifications (Thông báo trong app gửi tới tài khoản)
-- ----------------------------------------------------------------------------
INSERT INTO notifications (id, user_id, title, message, type, related_order_id, is_read, created_at, broadcast_log_id) VALUES 
('1', 'u-cust-01', 'Đơn hàng hoàn tất', 'Đơn hàng #1 của bạn đã được giao thành công bởi tài xế Nguyễn Mạnh Hùng.', 'ORDER_STATUS', 1, 1, '2026-06-17 12:45:00', NULL),
('2', 'u-cust-02', 'Tài xế đang giao hàng', 'Tài xế Lê Hoàng Long đang mang đơn hàng đến địa chỉ Keangnam Landmark 72.', 'ORDER_STATUS', 2, 0, '2026-06-17 15:45:00', NULL);

-- ----------------------------------------------------------------------------
-- Bảng chat_messages (Nhật ký chat giữa Customer và Shipper)
-- ----------------------------------------------------------------------------
INSERT INTO chat_messages (id, order_id, sender_id, receiver_id, content, created_at) VALUES 
(1, 1, 'u-driv-01', 'u-cust-01', 'Chào bạn, mình đang lấy phở ở quán và sẽ đi giao ngay cho bạn nhé!', '2026-06-17 12:10:00'),
(2, 1, 'u-cust-01', 'u-driv-01', 'Dạ vâng ạ, cảm ơn anh. Đến cổng trường Phenikaa anh gọi em ra lấy nhé.', '2026-06-17 12:12:00');

-- ----------------------------------------------------------------------------
-- Bảng order_tracking_locations (Nhật ký toạ độ di chuyển thực tế)
-- ----------------------------------------------------------------------------
INSERT INTO order_tracking_locations (id, order_id, latitude, longitude, tracking_phase, timestamp) VALUES 
(1, 1, 21.0366, 105.7821, 'DEPARTED', '2026-06-17 12:15:00'),
(2, 1, 21.0005, 105.7650, 'IN_TRANSIT', '2026-06-17 12:30:00'),
(3, 1, 20.9625, 105.7489, 'ARRIVED', '2026-06-17 12:42:00');

-- ============================================================================
-- KẾT THÚC SCRIPT SQL KHỞI TẠO ĐẦY ĐỦ
-- ============================================================================
