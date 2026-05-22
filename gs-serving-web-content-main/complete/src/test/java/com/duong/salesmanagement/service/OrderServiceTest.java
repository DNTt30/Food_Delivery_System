package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════
 * Unit Test — OrderService
 * ═══════════════════════════════════════════════════════════════════
 * Framework: JUnit 5 + Mockito
 * Nguyên tắc:
 *   - Dùng @Mock để tạo "giả" (Mock) cho Repository, cô lập OrderService.
 *   - Không kết nối Database thật — test chạy hoàn toàn độc lập.
 *   - Mỗi @Test bao gồm: Given (chuẩn bị) → When (thực hiện) → Then (kiểm tra).
 *
 * Test Coverage mục tiêu: ≥ 60% tầng Service
 * Tổng số test cases: 13 (vượt yêu cầu tối thiểu 10)
 * ═══════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
@DisplayName("OrderService Unit Tests")
public class OrderServiceTest {

    // ─── Mock Objects (Repository, Service phụ thuộc) ─────────────
    @Mock private FoodOrderRepository     foodOrderRepository;
    @Mock private DriverProfileRepository driverProfileRepository;
    @Mock private SimpMessagingTemplate   messagingTemplate;
    @Mock private NotificationService     notificationService;

    /** Inject tất cả @Mock vào đây, không dùng new OrderService() thật */
    @InjectMocks
    private OrderService orderService;

    // ─── Dữ liệu mẫu (Seed Data) dùng chung cho các test ──────────
    private FoodOrder      order;
    private DriverProfile  driver;
    private CustomerProfile customer;

    @BeforeEach
    void setUp() {
        // Khởi tạo Khách hàng mẫu
        User customerUser = new User();
        customerUser.setFullName("Nguyễn Văn A");

        customer = new CustomerProfile();
        customer.setId(1L);
        customer.setUser(customerUser);

        // Khởi tạo Nhà hàng mẫu
        User restaurantUser = new User();
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setId(1L);
        restaurant.setUser(restaurantUser);
        restaurant.setRestaurantName("Quán Test");

        // Khởi tạo Đơn hàng mẫu
        order = new FoodOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PREPARING);
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setShippingFee(15000.0);
        order.setTotalAmount(100000.0);

        // Khởi tạo Tài xế mẫu
        User driverUser = new User();
        driverUser.setFullName("Tài Xế B");

        driver = new DriverProfile();
        driver.setId(1L);
        driver.setUser(driverUser);
    }

    // ══════════════════════════════════════════════════════════════
    // NHÓM 1: Tài xế nhận đơn (acceptOrderByDriver)
    // ══════════════════════════════════════════════════════════════

    /**
     * TC-01 ✅ HAPPY PATH: Tài xế nhận đơn thành công.
     * Điều kiện: Đơn ở trạng thái PREPARING, chưa có tài xế, tài xế chưa có đơn.
     */
    @Test
    @DisplayName("TC-01 | ACCEPT: Tài xế nhận đơn thành công")
    public void whenAcceptOrder_validConditions_thenSuccess() {
        // Given
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.PREPARING)).thenReturn(Collections.emptyList());
        when(foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.DELIVERING)).thenReturn(Collections.emptyList());
        when(foodOrderRepository.save(any(FoodOrder.class))).thenReturn(order);

        // When
        FoodOrder result = orderService.acceptOrderByDriver(1L, driver);

        // Then
        assertNotNull(result, "Kết quả không được null");
        assertEquals(driver, result.getDriver(), "Tài xế phải được gán vào đơn hàng");
        verify(foodOrderRepository, times(1)).save(order);
    }

    /**
     * TC-02 ❌ FAIL: Đơn hàng không ở trạng thái PREPARING thì từ chối nhận.
     */
    @Test
    @DisplayName("TC-02 | ACCEPT: Từ chối nếu đơn chưa sẵn sàng (không phải PREPARING)")
    public void whenAcceptOrder_statusNotPreparing_thenThrow() {
        // Given
        order.setStatus(OrderStatus.PENDING);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.acceptOrderByDriver(1L, driver));
        assertTrue(ex.getMessage().contains("chưa sẵn sàng"), "Thông báo lỗi phải đề cập 'chưa sẵn sàng'");
    }

    /**
     * TC-03 ❌ FAIL: Đơn đã có tài xế khác nhận thì từ chối.
     */
    @Test
    @DisplayName("TC-03 | ACCEPT: Từ chối nếu đơn đã có tài xế khác")
    public void whenAcceptOrder_driverAlreadyAssigned_thenThrow() {
        // Given
        DriverProfile anotherDriver = new DriverProfile();
        anotherDriver.setId(99L);
        order.setDriver(anotherDriver);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.acceptOrderByDriver(1L, driver));
        assertTrue(ex.getMessage().contains("đã được nhận"), "Thông báo phải đề cập 'đã được nhận'");
    }

    /**
     * TC-04 ❌ FAIL: Tài xế đang có đơn dang dở không được nhận thêm.
     */
    @Test
    @DisplayName("TC-04 | ACCEPT: Từ chối nếu tài xế đang có đơn chưa xong")
    public void whenAcceptOrder_driverHasActiveOrder_thenThrow() {
        // Given
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.PREPARING)).thenReturn(List.of(new FoodOrder()));
        when(foodOrderRepository.findByDriverAndStatus(driver, OrderStatus.DELIVERING)).thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.acceptOrderByDriver(1L, driver));
        assertTrue(ex.getMessage().contains("chưa hoàn thành"), "Thông báo phải đề cập 'chưa hoàn thành'");
    }

    // ══════════════════════════════════════════════════════════════
    // NHÓM 2: Khách hàng hủy đơn (cancelOrder)
    // ══════════════════════════════════════════════════════════════

    /**
     * TC-05 ✅ HAPPY PATH: Khách hàng hủy đơn thành công.
     * Điều kiện: Đơn đang ở trạng thái PENDING.
     */
    @Test
    @DisplayName("TC-05 | CANCEL: Khách hàng hủy đơn PENDING thành công")
    public void whenCancelOrder_statusPending_thenSuccess() {
        // Given
        order.setStatus(OrderStatus.PENDING);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        orderService.cancelOrder(1L, customer);

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus(), "Trạng thái phải đổi sang CANCELLED");
        verify(foodOrderRepository, times(1)).save(order);
    }

    /**
     * TC-06 ❌ FAIL: Không thể hủy đơn đã được nhà hàng xác nhận (PREPARING).
     */
    @Test
    @DisplayName("TC-06 | CANCEL: Không thể hủy đơn đang ở PREPARING")
    public void whenCancelOrder_statusPreparing_thenThrow() {
        // Given — order đang PREPARING (đã được nhà hàng xác nhận)
        order.setStatus(OrderStatus.PREPARING);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(1L, customer));
        assertTrue(ex.getMessage().contains("đang chờ xác nhận"), "Thông báo lỗi phải đúng");
    }

    /**
     * TC-07 ❌ FAIL: Khách A không thể hủy đơn của Khách B.
     */
    @Test
    @DisplayName("TC-07 | CANCEL: Không có quyền hủy đơn của khách khác")
    public void whenCancelOrder_wrongCustomer_thenThrow() {
        // Given
        order.setStatus(OrderStatus.PENDING);
        CustomerProfile otherCustomer = new CustomerProfile();
        otherCustomer.setId(999L); // Khác ID với customer trong order (id=1L)
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(1L, otherCustomer));
        assertTrue(ex.getMessage().contains("quyền"), "Thông báo phải đề cập không có quyền");
    }

    // ══════════════════════════════════════════════════════════════
    // NHÓM 3: Tài xế xác nhận lấy hàng (markAsPickedUp)
    // ══════════════════════════════════════════════════════════════

    /**
     * TC-08 ✅ HAPPY PATH: Tài xế xác nhận lấy hàng, trạng thái PREPARING → DELIVERING.
     */
    @Test
    @DisplayName("TC-08 | PICKUP: Xác nhận lấy hàng thành công → DELIVERING")
    public void whenMarkAsPickedUp_validDriver_thenStatusChangesToDelivering() {
        // Given
        order.setDriver(driver);
        order.setStatus(OrderStatus.PREPARING);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        orderService.markAsPickedUp(1L, driver);

        // Then
        assertEquals(OrderStatus.DELIVERING, order.getStatus(), "Trạng thái phải là DELIVERING");
        verify(foodOrderRepository, times(1)).save(order);
    }

    /**
     * TC-09 ❌ FAIL: Tài xế không phải người được gán không thể xác nhận lấy hàng.
     */
    @Test
    @DisplayName("TC-09 | PICKUP: Từ chối nếu tài xế không được gán")
    public void whenMarkAsPickedUp_wrongDriver_thenThrow() {
        // Given — order được gán cho driver ID=1, nhưng anotherDriver ID=2 cố cập nhật
        order.setDriver(driver);
        order.setStatus(OrderStatus.PREPARING);

        DriverProfile anotherDriver = new DriverProfile();
        anotherDriver.setId(2L);
        User u = new User(); u.setFullName("Driver khác");
        anotherDriver.setUser(u);

        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.markAsPickedUp(1L, anotherDriver));
    }

    // ══════════════════════════════════════════════════════════════
    // NHÓM 4: Tài xế hoàn thành giao hàng (completeDelivery)
    // ══════════════════════════════════════════════════════════════

    /**
     * TC-10 ✅ HAPPY PATH: Tài xế giao hàng thành công, trạng thái DELIVERING → COMPLETED.
     */
    @Test
    @DisplayName("TC-10 | COMPLETE: Giao hàng thành công → COMPLETED, tài xế available lại")
    public void whenCompleteDelivery_validState_thenSuccess() {
        // Given
        order.setDriver(driver);
        order.setStatus(OrderStatus.DELIVERING);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        orderService.completeDelivery(1L, driver);

        // Then
        assertEquals(OrderStatus.COMPLETED, order.getStatus(), "Trạng thái phải là COMPLETED");
        assertTrue(driver.isAvailable(), "Tài xế phải trở thành available sau khi hoàn thành");
        verify(driverProfileRepository, times(1)).save(driver);
    }

    /**
     * TC-11 ❌ FAIL: Không thể hoàn thành đơn chưa ở trạng thái DELIVERING.
     */
    @Test
    @DisplayName("TC-11 | COMPLETE: Từ chối hoàn thành nếu đơn chưa ở DELIVERING")
    public void whenCompleteDelivery_wrongStatus_thenThrow() {
        // Given — đơn vẫn đang PREPARING (chưa lấy hàng)
        order.setDriver(driver);
        order.setStatus(OrderStatus.PREPARING);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.completeDelivery(1L, driver));
        assertTrue(ex.getMessage().contains("giao"), "Thông báo phải đề cập trạng thái giao");
    }

    // ══════════════════════════════════════════════════════════════
    // NHÓM 5: Quyền truy cập và tìm kiếm đơn hàng
    // ══════════════════════════════════════════════════════════════

    /**
     * TC-12 ✅ HAPPY PATH: Admin có quyền xem mọi đơn hàng.
     */
    @Test
    @DisplayName("TC-12 | PERMISSION: Admin có quyền xem tất cả đơn hàng")
    public void whenCheckPermission_adminRole_thenAlwaysTrue() {
        // Given
        User adminUser = new User();
        adminUser.setId("admin-1");
        adminUser.setRole(Role.ADMIN);
        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        boolean hasPermission = orderService.hasPermissionToTrackOrder(1L, adminUser);

        // Then
        assertTrue(hasPermission, "Admin phải luôn có quyền xem đơn hàng");
    }

    /**
     * TC-13 ❌ EDGE CASE: Tìm đơn với ID null phải trả về false thay vì crash.
     */
    @Test
    @DisplayName("TC-13 | PERMISSION: OrderID null phải trả về false, không throw exception")
    public void whenCheckPermission_nullOrderId_thenReturnFalse() {
        // Given
        User anyUser = new User();
        anyUser.setId("user-1");
        anyUser.setRole(Role.CUSTOMER);

        // When
        boolean hasPermission = orderService.hasPermissionToTrackOrder(null, anyUser);

        // Then
        assertFalse(hasPermission, "Phải trả về false khi OrderID là null");
        verifyNoInteractions(foodOrderRepository); // Không được gọi DB khi ID null
    }
}
