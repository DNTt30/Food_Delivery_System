package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class OrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReviewRepository reviewRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final VoucherRepository voucherRepository;

    public OrderService(FoodOrderRepository foodOrderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuItemRepository menuItemRepository,
                        ReviewRepository reviewRepository,
                        DriverProfileRepository driverProfileRepository,
                        VoucherRepository voucherRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.reviewRepository = reviewRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.voucherRepository = voucherRepository;
    }

    @Transactional
    public FoodOrder createOrder(CustomerProfile customer, RestaurantProfile restaurant,
                                 List<OrderItemRequest> itemRequests, String deliveryAddress, String voucherCode) {
        FoodOrder order = new FoodOrder();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(deliveryAddress);

        FoodOrder savedOrder = foodOrderRepository.save(order);
        double totalAmount = 0;

        for (OrderItemRequest req : itemRequests) {
            Long itemId = req.getMenuItemId();
            if (itemId == null) throw new IllegalArgumentException("Menu item ID cannot be null");

            MenuItem menuItem = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));
            if (!menuItem.isAvailable())
                throw new RuntimeException("Item is not available: " + menuItem.getName());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(req.getQuantity());
            orderItem.setPriceAtTimeOfOrder(menuItem.getPrice());
            orderItemRepository.save(orderItem);
            totalAmount += menuItem.getPrice() * req.getQuantity();
        }

        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Optional<Voucher> vOpt = voucherRepository.findByCode(voucherCode.trim());
            if (vOpt.isPresent()) {
                Voucher v = vOpt.get();
                if (v.isActive() && (v.getExpirationDate() == null || !v.getExpirationDate().isBefore(java.time.LocalDate.now()))) {
                    double discount = 0;
                    if (v.getDiscountType() == DiscountType.PERCENTAGE) {
                        discount = totalAmount * (v.getDiscountValue() / 100.0);
                    } else if (v.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                        discount = v.getDiscountValue();
                    }
                    totalAmount -= discount;
                    if (totalAmount < 0) totalAmount = 0;
                }
            }
        }

        savedOrder.setTotalAmount(totalAmount);
        return foodOrderRepository.save(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, RestaurantProfile restaurant) {
        if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null");
        if (newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.CANCELLED)
            throw new RuntimeException("Nhà hàng chỉ được xác nhận (PREPARING) hoặc từ chối (CANCELLED) đơn hàng");
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getRestaurant().getId().equals(restaurant.getId()))
            throw new RuntimeException("Unauthorized: Order does not belong to this restaurant");
        order.setStatus(newStatus);
        foodOrderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, CustomerProfile customer) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getId().equals(customer.getId()))
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
        if (order.getStatus() != OrderStatus.PENDING)
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        order.setStatus(OrderStatus.CANCELLED);
        foodOrderRepository.save(order);
    }

    @Transactional
    public Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getId().equals(customer.getId()))
            throw new RuntimeException("Không có quyền đánh giá đơn hàng này");
        if (order.getStatus() != OrderStatus.COMPLETED)
            throw new RuntimeException("Chỉ đánh giá được đơn đã hoàn thành");
        if (reviewRepository.existsByOrder(order))
            throw new RuntimeException("Đơn hàng này đã được đánh giá");

        Review review = new Review();
        review.setOrder(order);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        // Cập nhật điểm đánh giá cho nhà hàng
        RestaurantProfile restaurant = order.getRestaurant();
        int currentCount = (restaurant.getReviewCount() != null) ? restaurant.getReviewCount() : 0;
        double currentAvg = (restaurant.getAverageRating() != null) ? restaurant.getAverageRating() : 0.0;
        
        double newAvg = ((currentAvg * currentCount) + rating) / (currentCount + 1);
        restaurant.setReviewCount(currentCount + 1);
        restaurant.setAverageRating(Math.round(newAvg * 10.0) / 10.0);

        return savedReview;
    }

    @Transactional
    public FoodOrder acceptOrderByDriver(Long orderId, DriverProfile driver) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PREPARING)
            throw new RuntimeException("Đơn hàng chưa sẵn sàng để lấy");
        if (order.getDriver() != null)
            throw new RuntimeException("Đơn hàng đã được nhận bởi tài xế khác");
        order.setDriver(driver);
        order.setStatus(OrderStatus.DELIVERING);
        driver.setAvailable(false);
        driverProfileRepository.save(driver);
        return foodOrderRepository.save(order);
    }

    @Transactional
    public void completeDelivery(Long orderId, DriverProfile driver) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId()))
            throw new RuntimeException("Không có quyền cập nhật đơn hàng này");
        if (order.getStatus() != OrderStatus.DELIVERING)
            throw new RuntimeException("Đơn hàng không đang trong trạng thái giao");
        order.setStatus(OrderStatus.COMPLETED);
        foodOrderRepository.save(order);
        driver.setAvailable(true);
        driverProfileRepository.save(driver);
    }

    public List<FoodOrder> getRestaurantOrders(RestaurantProfile restaurant) {
        return foodOrderRepository.findByRestaurant(restaurant);
    }

    public List<FoodOrder> getCustomerOrders(CustomerProfile customer) {
        return foodOrderRepository.findByCustomerOrderByOrderTimeDesc(customer);
    }

    public Optional<FoodOrder> getOrderById(Long orderId) {
        return foodOrderRepository.findById(orderId);
    }

    public List<FoodOrder> getAvailableOrdersForDriver() {
        return foodOrderRepository.findByDriverIsNullAndStatus(OrderStatus.PREPARING);
    }

    public List<FoodOrder> getDriverActiveDeliveries(DriverProfile driver) {
        return foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.DELIVERING);
    }

    public List<FoodOrder> getDriverHistory(DriverProfile driver) {
        return foodOrderRepository.findByDriverOrderByOrderTimeDesc(driver);
    }

    public static class OrderItemRequest {
        private Long menuItemId;
        private int quantity;

        public Long getMenuItemId() { return menuItemId; }
        public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
