package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.*;
import java.util.List;
import java.util.Optional;

/**
 * IOrderService — Interface tầng Service cho nghiệp vụ Đơn hàng.
 *
 * Nguyên lý SOLID áp dụng:
 *  - D (Dependency Inversion Principle): Tầng Controller chỉ phụ thuộc vào
 *    interface này, không phụ thuộc trực tiếp vào lớp OrderService cụ thể.
 *  - I (Interface Segregation Principle): Interface chỉ khai báo các method
 *    thuộc nghiệp vụ đơn hàng, tách biệt với các nghiệp vụ khác.
 *  - O (Open/Closed Principle): Có thể thêm implementation mới (vd: MockOrderService
 *    để test) mà không cần sửa code tầng Controller.
 */
public interface IOrderService {

    /**
     * Tạo đơn hàng mới.
     * Business rule: Validate menu items, tính phí ship tự động bằng Haversine.
     */
    FoodOrder createOrder(CustomerProfile customer, RestaurantProfile restaurant,
                          List<OrderService.OrderItemRequest> itemRequests,
                          String deliveryAddress, String voucherCode);

    /**
     * Nhà hàng xác nhận hoặc từ chối đơn hàng.
     * Business rule: Chỉ được phép set PREPARING hoặc CANCELLED.
     */
    void updateOrderStatus(Long orderId, OrderStatus newStatus, RestaurantProfile restaurant);

    /**
     * Khách hàng hủy đơn.
     * Business rule: Chỉ được hủy khi đơn đang PENDING.
     */
    void cancelOrder(Long orderId, CustomerProfile customer);

    /**
     * Khách hàng đánh giá đơn hàng đã hoàn thành.
     * Business rule: Chỉ đánh giá được 1 lần sau khi đơn COMPLETED.
     */
    Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment, String imageUrl);

    /**
     * Tài xế nhận đơn giao hàng.
     * Business rule: Tài xế không được nhận đơn mới khi đang có đơn chưa xong.
     */
    FoodOrder acceptOrderByDriver(Long orderId, DriverProfile driver);

    /**
     * Tài xế xác nhận đã đến lấy hàng tại nhà hàng.
     * Chuyển trạng thái: PREPARING → DELIVERING
     */
    void markAsPickedUp(Long orderId, DriverProfile driver);

    /**
     * Tài xế xác nhận đã giao hàng thành công cho khách.
     * Chuyển trạng thái: DELIVERING → COMPLETED
     */
    void completeDelivery(Long orderId, DriverProfile driver);

    /**
     * Lấy tất cả đơn hàng của một nhà hàng.
     */
    List<FoodOrder> getRestaurantOrders(RestaurantProfile restaurant);

    /**
     * Lấy tất cả đơn hàng của một khách hàng.
     */
    List<FoodOrder> getCustomerOrders(CustomerProfile customer);

    /**
     * Tìm đơn hàng theo ID.
     */
    Optional<FoodOrder> getOrderById(Long orderId);

    /**
     * Lấy danh sách đơn hàng chờ tài xế nhận (status=PREPARING, chưa có driver).
     */
    List<FoodOrder> getAvailableOrdersForDriver();

    /**
     * Lấy danh sách đơn hàng đang giao của tài xế (PREPARING + DELIVERING).
     */
    List<FoodOrder> getDriverActiveDeliveries(DriverProfile driver);

    /**
     * Lấy lịch sử giao hàng của tài xế (tất cả trạng thái).
     */
    List<FoodOrder> getDriverHistory(DriverProfile driver);

    /**
     * Kiểm tra quyền theo dõi đơn hàng (Access Control).
     */
    boolean hasPermissionToTrackOrder(Long orderId, User user);

    /**
     * Lấy tất cả đơn hàng của một User (dùng cho Admin/Tracking).
     */
    List<FoodOrder> getOrdersByUser(User user);
}
