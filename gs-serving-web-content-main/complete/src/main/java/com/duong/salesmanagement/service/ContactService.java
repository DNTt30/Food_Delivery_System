package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.ContactInfoResponse;
import com.duong.salesmanagement.dto.ContactInfoResponse.ContactDto;
import com.duong.salesmanagement.exception.ChatAccessDeniedException;
import com.duong.salesmanagement.model.CustomerProfile;
import com.duong.salesmanagement.model.DriverProfile;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.util.PhoneMaskUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Resolves which contacts a user may see for a given order,
 * applying phone masking when the order is closed.
 *
 * <h3>Visibility matrix</h3>
 * <pre>
 * My Role      │ Can see                          │ When
 * ─────────────┼──────────────────────────────────┼───────────────────────
 * CUSTOMER     │ Restaurant                        │ PENDING, PREPARING
 *              │ Driver                            │ DELIVERING
 * RESTAURANT   │ Customer                          │ PENDING, PREPARING
 *              │ Driver                            │ PREPARING, DELIVERING
 * DRIVER       │ Customer                          │ DELIVERING
 *              │ Restaurant                        │ PREPARING, DELIVERING
 * ─────────────┴──────────────────────────────────┴───────────────────────
 * (All contacts still shown when COMPLETED / CANCELLED, phone is masked)
 * </pre>
 */
@Service
@Transactional(readOnly = true)
public class ContactService {

    private final FoodOrderRepository foodOrderRepository;

    public ContactService(FoodOrderRepository foodOrderRepository) {
        this.foodOrderRepository = foodOrderRepository;
    }

    /**
     * Returns contact info for {@code currentUsername} within the given order.
     *
     * @param orderId         food order ID
     * @param currentUsername JWT-authenticated username
     * @return response DTO with participant list and lock flag
     * @throws ChatAccessDeniedException when the user does not belong to this order
     */
    public ContactInfoResponse getContactInfo(Long orderId, String currentUsername) {
        final Long id = Objects.requireNonNull(orderId, "orderId must not be null");
        FoodOrder order = foodOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + id));

        User currentUser = resolveParticipant(order, currentUsername);
        if (currentUser == null) {
            throw new ChatAccessDeniedException(
                    "Bạn không có quyền xem thông tin liên lạc của đơn hàng này");
        }

        OrderStatus status   = order.getStatus();
        boolean closed       = isClosed(status);
        List<ContactDto> contacts = buildContactList(order, currentUser.getRole(), status, closed);

        return new ContactInfoResponse(orderId, status.name(), closed, contacts);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Returns the {@link User} entity for {@code username} if they are a
     * customer, restaurant, or driver of the order; otherwise {@code null}.
     */
    private User resolveParticipant(FoodOrder order, String username) {
        User customer = order.getCustomer().getUser();
        if (customer.getUsername().equals(username)) return customer;

        User restaurant = order.getRestaurant().getUser();
        if (restaurant.getUsername().equals(username)) return restaurant;

        if (order.getDriver() != null) {
            User driver = order.getDriver().getUser();
            if (driver.getUsername().equals(username)) return driver;
        }
        return null;
    }

    /** Builds the list of visible contacts based on the caller's role and order status. */
    private List<ContactDto> buildContactList(FoodOrder order, Role myRole,
                                              OrderStatus status, boolean closed) {
        List<ContactDto> result = new ArrayList<>();

        switch (myRole) {
            case CUSTOMER -> {
                // Có thể chat với nhà hàng từ lúc đặt đến lúc giao xong
                if (isPending(status) || isPreparing(status) || isDelivering(status) || closed) {
                    result.add(restaurantContact(order));
                }
                // Có thể chat với tài xế ngay khi có tài xế nhận đơn
                if ((isPreparing(status) || isDelivering(status) || closed) && order.getDriver() != null) {
                    result.add(driverContact(order, closed));
                }
            }
            case RESTAURANT -> {
                // Nhà hàng luôn thấy khách
                if (isPending(status) || isPreparing(status) || isDelivering(status) || closed) {
                    result.add(customerContact(order, closed));
                }
                // Thấy tài xế khi tài xế đã nhận đơn
                if ((isPreparing(status) || isDelivering(status) || closed)
                        && order.getDriver() != null) {
                    result.add(driverContact(order, closed));
                }
            }
            case DRIVER -> {
                // Tài xế thấy khách từ lúc chuẩn bị (để báo đã đến quán) đến lúc giao
                if (isPreparing(status) || isDelivering(status) || closed) {
                    result.add(customerContact(order, closed));
                }
                // Tài xế luôn thấy nhà hàng để liên lạc lấy món
                if (isPreparing(status) || isDelivering(status) || closed) {
                    result.add(restaurantContact(order));
                }
            }
            default -> { /* ADMIN – no chat contacts */ }
        }
        return result;
    }

    private ContactDto customerContact(FoodOrder order, boolean closed) {
        CustomerProfile cp = order.getCustomer();
        User user = cp.getUser();
        return new ContactDto(user.getId(), user.getFullName(), "CUSTOMER",
                PhoneMaskUtil.maskIf(cp.getPhoneNumber(), closed), null);
    }

    private ContactDto restaurantContact(FoodOrder order) {
        RestaurantProfile rp = order.getRestaurant();
        // Restaurant phone is intentionally not exposed; avatar = banner
        return new ContactDto(rp.getUser().getId(), rp.getRestaurantName(),
                "RESTAURANT", null, rp.getBannerUrl());
    }

    private ContactDto driverContact(FoodOrder order, boolean closed) {
        DriverProfile dp = order.getDriver();
        User user = dp.getUser();
        return new ContactDto(user.getId(), user.getFullName(), "DRIVER",
                PhoneMaskUtil.maskIf(dp.getPhoneNumber(), closed), null);
    }

    // ----------------------------------------------------------------
    // Status predicates (avoids repeated == comparisons inline)
    // ----------------------------------------------------------------

    private boolean isClosed(OrderStatus s)    { return s == OrderStatus.COMPLETED || s == OrderStatus.CANCELLED; }
    private boolean isPending(OrderStatus s)    { return s == OrderStatus.PENDING; }
    private boolean isPreparing(OrderStatus s)  { return s == OrderStatus.PREPARING; }
    private boolean isDelivering(OrderStatus s) { return s == OrderStatus.DELIVERING; }
}
