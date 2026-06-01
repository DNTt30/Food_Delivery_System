package com.duong.salesmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duong.salesmanagement.dto.OrderStatusNotification;
import com.duong.salesmanagement.model.CustomerProfile;
import com.duong.salesmanagement.model.DiscountType;
import com.duong.salesmanagement.model.DriverProfile;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.OrderItem;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.Payment;
import com.duong.salesmanagement.model.PaymentMethod;
import com.duong.salesmanagement.model.PaymentStatus;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Review;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.model.Voucher;
import com.duong.salesmanagement.repository.DriverProfileRepository;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.MenuItemRepository;
import com.duong.salesmanagement.repository.OrderItemRepository;
import com.duong.salesmanagement.repository.PaymentRepository;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import com.duong.salesmanagement.repository.ReviewRepository;
import com.duong.salesmanagement.repository.VoucherRepository;

@Service

public class OrderService implements IOrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReviewRepository reviewRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final VoucherRepository voucherRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final GeocodingService geocodingService;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final IShippingCalculationService shippingCalculationService;
    private final PaymentRepository paymentRepository;

    public OrderService(FoodOrderRepository foodOrderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuItemRepository menuItemRepository,
                        ReviewRepository reviewRepository,
                        DriverProfileRepository driverProfileRepository,
                        VoucherRepository voucherRepository,
                        SimpMessagingTemplate messagingTemplate,
                        NotificationService notificationService,
                        GeocodingService geocodingService,
                        RestaurantProfileRepository restaurantProfileRepository,
                        IShippingCalculationService shippingCalculationService,
                        PaymentRepository paymentRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.reviewRepository = reviewRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.voucherRepository = voucherRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
        this.geocodingService = geocodingService;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.shippingCalculationService = shippingCalculationService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Broadcast trạng thái đơn hàng mới tới frontend qua WebSocket.
     * Topic: /topic/order-status.{orderId}
     */
    private void broadcastOrderStatus(FoodOrder order) {
        String driverName  = null;
        String driverPhone = null;
        if (order.getDriver() != null) {
            driverName  = order.getDriver().getUser().getFullName();
            driverPhone = order.getDriver().getPhoneNumber();
        }
        OrderStatusNotification notification = new OrderStatusNotification(
                order.getId(),
                order.getStatus().name(),
                driverName,
                driverPhone,
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend(
                "/topic/order-status." + order.getId(), notification);
    }

    @Transactional
    public FoodOrder createOrder(CustomerProfile customer, RestaurantProfile restaurant,
                                 List<OrderItemRequest> itemRequests, String deliveryAddress,
                                 String voucherCode, String paymentMethodStr) {
        FoodOrder order = new FoodOrder();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderTime(LocalDateTime.now());
        try {
            PaymentMethod pm = PaymentMethod.valueOf(paymentMethodStr);
            if (pm == PaymentMethod.VNPAY || pm == PaymentMethod.MOMO_E_WALLET) {
                order.setStatus(OrderStatus.PENDING_PAYMENT);
            } else {
                order.setStatus(OrderStatus.PENDING);
            }
        } catch (Exception e) {
            order.setStatus(OrderStatus.PENDING);
        }
        order.setDeliveryAddress(deliveryAddress);

        // Geolocation Snapshot Logic
        // 1. Restaurant Location
        if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) {
            order.setRestaurantLat(restaurant.getLatitude());
            order.setRestaurantLng(restaurant.getLongitude());
        } else {
            java.util.Map<String, Double> restCoords = geocodingService.getCoordinates(restaurant.getAddress());
            if (restCoords != null) {
                restaurant.setLatitude(restCoords.get("lat"));
                restaurant.setLongitude(restCoords.get("lng"));
                restaurantProfileRepository.save(restaurant);
                order.setRestaurantLat(restCoords.get("lat"));
                order.setRestaurantLng(restCoords.get("lng"));
            }
        }
        order.setRestaurantAddressSnapshot(restaurant.getAddress());

        // 2. Delivery Location Snapshot
        order.setDeliveryAddressSnapshot(deliveryAddress);
        
        boolean isProfileAddress = customer.getDeliveryAddress() != null && 
                                   customer.getDeliveryAddress().trim().equalsIgnoreCase(deliveryAddress.trim());
                                   
        if (isProfileAddress && customer.getLatitude() != null && customer.getLongitude() != null) {
            order.setDeliveryLat(customer.getLatitude());
            order.setDeliveryLng(customer.getLongitude());
        } else {
            java.util.Map<String, Double> deliveryCoords = geocodingService.getCoordinates(deliveryAddress);
            if (deliveryCoords != null) {
                order.setDeliveryLat(deliveryCoords.get("lat"));
                order.setDeliveryLng(deliveryCoords.get("lng"));
            } else if (customer.getLatitude() != null && customer.getLongitude() != null) {
                order.setDeliveryLat(customer.getLatitude());
                order.setDeliveryLng(customer.getLongitude());
            }
        }

        // 3. Shipping Engine (Distance, Fee, ETA)
        if (order.getRestaurantLat() != null && order.getDeliveryLat() != null) {
            double distance = shippingCalculationService.calculateDistance(
                    order.getRestaurantLat(), order.getRestaurantLng(),
                    order.getDeliveryLat(), order.getDeliveryLng()
            );
            order.setDistance(distance);
            order.setShippingFee(shippingCalculationService.calculateShippingFee(distance));
            order.setEstimatedTimeOfArrival(shippingCalculationService.estimateETA(distance));
        }

        FoodOrder savedOrder = foodOrderRepository.save(order);
        double totalAmount = savedOrder.getShippingFee() != null ? savedOrder.getShippingFee() : 0;

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
                boolean isGlobalOrBelongsToRestaurant = (v.getRestaurant() == null) || 
                                                        (v.getRestaurant().getId().equals(restaurant.getId()));
                
                if (v.isActive() && isGlobalOrBelongsToRestaurant && 
                   (v.getExpirationDate() == null || !v.getExpirationDate().isBefore(java.time.LocalDate.now()))) {
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
        FoodOrder finalOrder = foodOrderRepository.save(savedOrder);

        // 💳 Tạo bản ghi Payment
        PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
        if ("VNPAY".equalsIgnoreCase(paymentMethodStr)) {
            paymentMethod = PaymentMethod.VNPAY;
        } else if ("MOMO".equalsIgnoreCase(paymentMethodStr)) {
            paymentMethod = PaymentMethod.MOMO_E_WALLET;
        }
        Payment payment = new Payment();
        payment.setOrder(finalOrder);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(totalAmount);
        payment.setTransactionDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Lưu phương thức thanh toán vào FoodOrder
        finalOrder.setPaymentMethod(paymentMethodStr);
        finalOrder.setPaymentStatus(PaymentStatus.PENDING.name());
        foodOrderRepository.save(finalOrder);

        // 🔔 Notify: Customer đã đặt đơn
        notificationService.notifyOrderCreated(
                customer.getUser(), finalOrder.getId(),
                restaurant.getRestaurantName());
        // 🔔 Notify: Restaurant có đơn mới
        notificationService.notifyNewOrderForRestaurant(
                restaurant.getUser(), finalOrder.getId(),
                customer.getUser().getFullName());

        return finalOrder;
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
        broadcastOrderStatus(order); // 🔔 Real-time WebSocket

        // 🔔 Persist notification
        if (newStatus == OrderStatus.PREPARING) {
            notificationService.notifyOrderAccepted(
                    order.getCustomer().getUser(),
                    restaurant.getUser(), order.getId());
        } else if (newStatus == OrderStatus.CANCELLED) {
            notificationService.notifyOrderCancelledByRestaurant(
                    order.getCustomer().getUser(), order.getId());
        }
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
        broadcastOrderStatus(order); // 🔔 Real-time WebSocket

        // 🔔 Notify restaurant đơn bị hủy
        notificationService.notifyOrderCancelledByCustomer(
                order.getRestaurant().getUser(), order.getId());
    }

    @Transactional
    public Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment, String imageUrl) {
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
        review.setImageUrl(imageUrl);
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        // Cập nhật điểm đánh giá cho nhà hàng
        RestaurantProfile restaurant = order.getRestaurant();
        int currentCount = (restaurant.getReviewCount() != null) ? restaurant.getReviewCount() : 0;
        double currentAvg = (restaurant.getAverageRating() != null) ? restaurant.getAverageRating() : 0.0;

        double newAvg = ((currentAvg * currentCount) + rating) / (currentCount + 1);
        restaurant.setReviewCount(currentCount + 1);
        restaurant.setAverageRating(Math.round(newAvg * 10.0) / 10.0);

        // 🔔 Notify restaurant có đánh giá mới
        notificationService.notifyNewReview(
                restaurant.getUser(), orderId, rating);

        return savedReview;
    }

    @Transactional
    public FoodOrder acceptOrderByDriver(Long orderId, DriverProfile driver) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PREPARING)
            throw new RuntimeException("Đơn hàng chưa sẵn sàng để lấy");
        if (order.getDriver() != null)
            throw new RuntimeException("Đơn hàng đã được nhận bửi tài xế khác");

        // Kiểm tra xem tài xế đã có đơn hàng nào đang giao chưa
        List<FoodOrder> preparing = foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.PREPARING);
        List<FoodOrder> delivering = foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.DELIVERING);
        if (!preparing.isEmpty() || !delivering.isEmpty()) {
            throw new RuntimeException("Bạn đang có đơn hàng chưa hoàn thành. Vui lòng hoàn thành trước khi nhận đơn mới.");
        }

        order.setDriver(driver);
        // Chặng 1: Driver đang đến nhà hàng — giữ nguyên status PREPARING
        // (sẽ chuyển sang DELIVERING khi Driver bấm "Đã lấy hàng")
        driver.setAvailable(false);
        driverProfileRepository.save(driver);
        FoodOrder saved = foodOrderRepository.save(order);
        broadcastOrderStatus(saved); // 🔔 Real-time WebSocket

        // 🔔 Notify: Customer & Restaurant tài xế đã nhận đơn
        String driverName = driver.getUser().getFullName();
        notificationService.notifyDriverAssigned(
                saved.getCustomer().getUser(),
                saved.getRestaurant().getUser(),
                saved.getId(), driverName);

        return saved;
    }

    /**
     * Driver xác nhận đã lấy hàng tại nhà hàng → bắt đầu giao đến khách.
     * Chuyển OrderStatus: PREPARING → DELIVERING
     */
    @Transactional
    public void markAsPickedUp(Long orderId, DriverProfile driver) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId()))
            throw new RuntimeException("Không có quyền cập nhật đơn hàng này");
        if (order.getStatus() != OrderStatus.PREPARING)
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ lấy hàng");
        order.setStatus(OrderStatus.DELIVERING);
        foodOrderRepository.save(order);
        broadcastOrderStatus(order); // 🔔 Real-time WebSocket
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
        markCodPaymentCompletedIfNeeded(order);
        foodOrderRepository.save(order);

        // Update soldCount for menu items
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                MenuItem menuItem = item.getMenuItem();
                if (menuItem != null) {
                    int current = menuItem.getSoldCount() != null ? menuItem.getSoldCount() : 0;
                    menuItem.setSoldCount(current + item.getQuantity());
                    menuItemRepository.save(menuItem);
                }
            }
        }

        driver.setAvailable(true);
        driverProfileRepository.save(driver);
        broadcastOrderStatus(order); // 🔔 Real-time WebSocket

        // 🔔 Notify Customer: đơn hoàn thành
        notificationService.notifyOrderCompleted(
                order.getCustomer().getUser(), order.getId());
        // 🔔 Notify Driver: thu nhập (100% phí ship)
        double earnings = order.getShippingFee() != null ? order.getShippingFee() : 0;
        notificationService.notifyDeliveryCompletedForDriver(
                driver.getUser(), order.getId(), earnings);
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
        // Trả cả PREPARING (chặng 1: đến nhà hàng) và DELIVERING (chặng 2: giao đến khách)
        List<FoodOrder> preparing = foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.PREPARING);
        List<FoodOrder> delivering = foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.DELIVERING);
        List<FoodOrder> combined = new java.util.ArrayList<>(preparing);
        combined.addAll(delivering);
        return combined;
    }

    public List<FoodOrder> getDriverHistory(DriverProfile driver) {
        return foodOrderRepository.findByDriverOrderByOrderTimeDesc(driver);
    }

    private void markCodPaymentCompletedIfNeeded(FoodOrder order) {
        paymentRepository.findByOrder(order).ifPresent(payment -> {
            if (!isCashOnDelivery(order.getPaymentMethod(), payment.getPaymentMethod())) {
                return;
            }
            if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                return;
            }
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setTransactionDate(LocalDateTime.now());
            paymentRepository.save(payment);
            order.setPaymentStatus(PaymentStatus.COMPLETED.name());
        });
    }

    private boolean isCashOnDelivery(String method, PaymentMethod paymentMethodEnum) {
        if (method != null) {
            String normalized = method.trim().toUpperCase();
            return "CASH".equals(normalized)
                    || "COD".equals(normalized)
                    || "CASH_ON_DELIVERY".equals(normalized);
        }
        return paymentMethodEnum == PaymentMethod.CASH_ON_DELIVERY;
    }

    /**
     * Kiểm tra quyền theo dõi đơn hàng (Security Access Control)
     * Đảm bảo tính riêng tư của dữ liệu GPS và thông tin đơn hàng.
     */
    public boolean hasPermissionToTrackOrder(Long orderId, User user) {
        if (orderId == null || user == null) return false;
        
        Optional<FoodOrder> orderOpt = foodOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) return false;
        
        FoodOrder order = orderOpt.get();
        
        // 1. Admin: Toàn quyền
        if (user.getRole() == Role.ADMIN) return true;
        
        // 2. Customer: Phải là người đặt đơn
        if (user.getRole() == Role.CUSTOMER) {
            return order.getCustomer().getUser().getId().equals(user.getId());
        }
        
        // 3. Driver: Phải là người được gán cho đơn hàng
        if (user.getRole() == Role.DRIVER) {
            return order.getDriver() != null && 
                   order.getDriver().getUser().getId().equals(user.getId());
        }

        // 4. Restaurant: Phải là chủ của quán có đơn hàng này
        if (user.getRole() == Role.RESTAURANT) {
            return order.getRestaurant().getUser().getId().equals(user.getId());
        }
        
        return false;
    }

    public List<FoodOrder> getOrdersByUser(User user) {
        if (user == null) return java.util.Collections.emptyList();
        return foodOrderRepository.findByCustomer_User_Id(user.getId());
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
