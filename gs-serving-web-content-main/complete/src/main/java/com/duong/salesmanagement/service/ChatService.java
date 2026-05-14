package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.ChatMessageRequest;
import com.duong.salesmanagement.dto.ChatMessageResponse;
import com.duong.salesmanagement.exception.ChatAccessDeniedException;
import com.duong.salesmanagement.exception.ChatLockedException;
import com.duong.salesmanagement.model.ChatMessage;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.NotificationType;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.Role;
import com.duong.salesmanagement.model.User;
import com.duong.salesmanagement.repository.ChatMessageRepository;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Order-based Chat Service (Polling Architecture – no WebSocket).
 *
 * <h3>Business Rules</h3>
 * <ul>
 *   <li>Order #N → Chat Room #N (one order = one room)</li>
 *   <li>PENDING / PREPARING → Customer ↔ Restaurant</li>
 *   <li>DELIVERING → Customer ↔ Driver</li>
 *   <li>PREPARING / DELIVERING → Driver ↔ Restaurant</li>
 *   <li>COMPLETED / CANCELLED → chat is LOCKED (read-only)</li>
 * </ul>
 *
 * <h3>Security</h3>
 * Every request requires a valid JWT (enforced by Spring Security).
 * A user may only read/send messages if they are a participant of that order.
 */
@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final NotificationService notificationService;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       FoodOrderRepository foodOrderRepository,
                       NotificationService notificationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.notificationService = notificationService;
    }

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    /**
     * Send a message within an order's chat room.
     *
     * <p>Validation pipeline:
     * <ol>
     *   <li>Order exists</li>
     *   <li>Sender is a participant of this order</li>
     *   <li>Receiver is a participant of this order</li>
     *   <li>Order is not COMPLETED / CANCELLED</li>
     *   <li>Sender ↔ Receiver pair is allowed by current order status</li>
     *   <li>Content is non-blank and ≤ 1 000 characters</li>
     * </ol>
     *
     * @param request        DTO containing orderId, receiverId, content
     * @param senderUsername username extracted from the JWT token
     * @return saved message as a DTO
     */
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderUsername) {
        FoodOrder order  = requireOrder(request.getOrderId());
        User sender      = requireUserByUsername(senderUsername);
        User receiver    = requireUserById(request.getReceiverId());

        assertParticipant(order, sender, "Bạn không thuộc đơn hàng này");
        assertParticipant(order, receiver, "Người nhận không thuộc đơn hàng này");
        assertNotLocked(order);
        assertAllowedChatPair(order, sender, receiver);
        assertValidContent(request.getContent());

        ChatMessage saved = chatMessageRepository.save(
                new ChatMessage(order, sender, receiver, request.getContent().trim()));

        // 🔔 Notify receiver: có tin nhắn mới
        _notifyNewMessage(saved, order, sender, receiver);

        return toResponse(saved);
    }

    /**
     * Retrieve full chat history for an order (used by polling).
     *
     * @param orderId  food order ID
     * @param username username extracted from the JWT token
     * @return messages ordered by creation time (ascending)
     */
    public List<ChatMessageResponse> getOrderMessages(Long orderId, String username) {
        FoodOrder order = requireOrder(orderId);
        User user       = requireUserByUsername(username);
        assertParticipant(order, user, "Bạn không có quyền xem chat của đơn hàng này");

        return chatMessageRepository
                .findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(this::toResponse)
                .toList();  // immutable list, Java 16+
    }

    // ----------------------------------------------------------------
    // Chat Notification
    // ----------------------------------------------------------------

    /**
     * Tạo notification DB cho người nhận tin nhắn.
     * Dùng type NEW_MESSAGE, KHÔNG dedup (mỗi tin là notification riêng).
     * Lưu relatedOrderId để Frontend biết trang đích khi click.
     */
    private void _notifyNewMessage(ChatMessage msg, FoodOrder order, User sender, User receiver) {
        try {
            String senderLabel = switch (sender.getRole()) {
                case CUSTOMER   -> "Khách hàng";
                case RESTAURANT -> "Nhà hàng";
                case DRIVER     -> "Tài xế";
                default         -> sender.getFullName();
            };
            String preview = msg.getContent().length() > 60
                    ? msg.getContent().substring(0, 60) + "…"
                    : msg.getContent();

            String title   = "💬 " + senderLabel + " nhắn tin";
            String content = "[Đơn #" + order.getId() + "] " + sender.getFullName() + ": " + preview;

            // Tạo notification trực tiếp (bypass dedup — mỗi tin nhắn là notification riêng)
            com.duong.salesmanagement.model.Notification n =
                    new com.duong.salesmanagement.model.Notification(
                            receiver, title, content,
                            NotificationType.NEW_MESSAGE,
                            order.getId()          // relatedOrderId → action URL sẽ link đúng
                    );
            notificationService.save(n);

        } catch (Exception e) {
            // Silent: lỗi notification không được phép làm fail chat
        }
    }

    // ----------------------------------------------------------------
    // Validation helpers
    // ----------------------------------------------------------------

    /**
     * Ensures the given user is a participant (customer, restaurant, or driver) of the order.
     */
    private void assertParticipant(FoodOrder order, User user, String errorMsg) {
        boolean isCustomer   = order.getCustomer().getUser().getId().equals(user.getId());
        boolean isRestaurant = order.getRestaurant().getUser().getId().equals(user.getId());
        boolean isDriver     = order.getDriver() != null
                               && order.getDriver().getUser().getId().equals(user.getId());

        if (!isCustomer && !isRestaurant && !isDriver) {
            throw new ChatAccessDeniedException(errorMsg);
        }
    }

    /** COMPLETED or CANCELLED orders are read-only. */
    private void assertNotLocked(FoodOrder order) {
        OrderStatus status = order.getStatus();
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            String reason = (status == OrderStatus.COMPLETED) ? "hoàn thành" : "bị hủy";
            throw new ChatLockedException("Chat đã bị khóa. Đơn hàng đã " + reason + ".");
        }
    }

    /**
     * Validates that the sender–receiver pair is allowed for the current order status.
     *
     * <table>
     *   <tr><th>Pair</th>             <th>Allowed statuses</th></tr>
     *   <tr><td>Customer ↔ Restaurant</td><td>PENDING, PREPARING</td></tr>
     *   <tr><td>Customer ↔ Driver</td>    <td>DELIVERING</td></tr>
     *   <tr><td>Driver ↔ Restaurant</td>  <td>PREPARING, DELIVERING</td></tr>
     * </table>
     */
    private void assertAllowedChatPair(FoodOrder order, User sender, User receiver) {
        Role senderRole   = sender.getRole();
        Role receiverRole = receiver.getRole();
        OrderStatus status = order.getStatus();

        boolean allowed;

        if (isRolePair(senderRole, receiverRole, Role.CUSTOMER, Role.RESTAURANT)) {
            allowed = status == OrderStatus.PENDING || status == OrderStatus.PREPARING || status == OrderStatus.DELIVERING;
        } else if (isRolePair(senderRole, receiverRole, Role.CUSTOMER, Role.DRIVER)) {
            allowed = status == OrderStatus.PREPARING || status == OrderStatus.DELIVERING;
        } else if (isRolePair(senderRole, receiverRole, Role.DRIVER, Role.RESTAURANT)) {
            allowed = status == OrderStatus.PREPARING || status == OrderStatus.DELIVERING;
        } else {
            allowed = false;
        }

        if (!allowed) {
            throw new ChatAccessDeniedException(
                    "Không được phép nhắn tin với người này trong trạng thái hiện tại của đơn hàng.");
        }
    }

    /** Returns {@code true} when {a,b} == {x,y} regardless of order. */
    private boolean isRolePair(Role a, Role b, Role x, Role y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    /** Content must be non-blank and at most 1 000 characters (after trimming). */
    private void assertValidContent(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được trống");
        }
        if (raw.trim().length() > 1000) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được vượt quá 1000 ký tự");
        }
    }

    // ----------------------------------------------------------------
    // Entity finders
    // ----------------------------------------------------------------

    private FoodOrder requireOrder(Long orderId) {
        final Long id = Objects.requireNonNull(orderId, "orderId must not be null");
        return foodOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + id));
    }

    private User requireUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));
    }

    private User requireUserById(UUID userId) {
        final UUID id = Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: " + id));
    }

    // ----------------------------------------------------------------
    // Mapping
    // ----------------------------------------------------------------

    /** Maps a {@link ChatMessage} entity to a response DTO (no sensitive fields). */
    private ChatMessageResponse toResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .orderId(m.getOrder().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .senderRole(m.getSender().getRole().name())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getFullName())
                .receiverRole(m.getReceiver().getRole().name())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
