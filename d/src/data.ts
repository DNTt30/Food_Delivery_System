import { Order, CodeSnippet } from './types';

export const initialOrders: Order[] = [
  {
    id: "2408",
    restaurantName: "Phở Gia Truyền Bát Đàn",
    restaurantAddress: "49 Bát Đàn, Cửa Đông, Hoàn Kiếm, Hà Nội",
    totalPrice: 135000,
    status: "COMPLETED",
    createdAt: "2026-05-28T08:15:00Z",
    items: [
      {
        id: "item_1",
        foodName: "Phở Bò Tái Nạm",
        quantity: 2,
        price: 60000,
        imageUrl: "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=300&auto=format&fit=crop&q=80"
      },
      {
        id: "item_2",
        foodName: "Quẩy Giòn (Bộ 3 chiếc)",
        quantity: 1,
        price: 15000,
        imageUrl: "https://images.unsplash.com/photo-1555126634-323283e090fa?w=300&auto=format&fit=crop&q=80"
      }
    ]
  },
  {
    id: "2412",
    restaurantName: "Cơm Tấm sườn bì chả Ba Sơn",
    restaurantAddress: "128 Đinh Tiên Hoàng, Đa Kao, Quận 1, TP. HCM",
    totalPrice: 85000,
    status: "PREPARING",
    createdAt: "2026-05-28T09:30:00Z",
    items: [
      {
        id: "item_3",
        foodName: "Cơm Tấm Sườn Bì Chả Đặc Biệt",
        quantity: 1,
        price: 75000,
        imageUrl: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300&auto=format&fit=crop&q=80"
      },
      {
        id: "item_4",
        foodName: "Trà Đá hoa lài",
        quantity: 2,
        price: 5000,
        imageUrl: "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=300&auto=format&fit=crop&q=80"
      }
    ]
  }
];

export const dbSchemaDetails = {
  tableName: "reviews",
  description: "Cấu trúc bảng lưu trữ đánh giá khách hàng và phản hồi của nhà hàng. Cơ chế 'spring.jpa.hibernate.ddl-auto=update' tự động đồng bộ thực thể Java xuống MySQL mà không cần gõ lệnh SQL bằng tay.",
  columns: [
    { name: "id", type: "BIGINT (PK, Auto-Increment)", description: "Khóa chính tự tăng định danh đánh giá" },
    { name: "order_id", type: "BIGINT (FK)", description: "Khóa ngoại liên kết tới bảng orders" },
    { name: "rating", type: "INT", description: "Số sao đánh giá (1-5 sao)" },
    { name: "comment", type: "TEXT", description: "Lời nhận xét từ phía khách hàng" },
    { name: "image_url", type: "VARCHAR(512)", isNew: true, description: "URL ảnh đánh giá khách hàng đăng (Mới bổ sung)" },
    { name: "restaurant_reply", type: "TEXT", isNew: true, description: "Nội dung phản hồi từ nhà hàng (Mới bổ sung)" },
    { name: "replied_at", type: "DATETIME", isNew: true, description: "Thời gian nhà hàng phản hồi (Mới bổ sung)" }
  ]
};

export const codeSnippets: Record<string, CodeSnippet[]> = {
  database: [
    {
      title: "Review.java (Entity)",
      description: "Thêm 3 thuộc tính quản lý ảnh, phản hồi của nhà hàng và thời gian phản hồi. Hibernate JPA sẽ tự động tạo các cột tương ứng (image_url, restaurant_reply, replied_at) trong bảng reviews của MySQL.",
      language: "java",
      code: `package com.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private int rating; // 1 to 5 stars
    private String comment;

    // ============================================
    // BỔ SUNG 3 TRƯỜNG MỚI THEO YÊU CẦU:
    // ============================================
    
    @Column(name = "image_url")
    private String imageUrl;        // URL ảnh đánh giá của khách hàng

    @Column(name = "restaurant_reply", columnDefinition = "TEXT")
    private String restaurantReply; // Nội dung phản hồi của nhà hàng

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;// Thời gian nhà hàng phản hồi
}`
    }
  ],
  service: [
    {
      title: "IOrderService.java (Interface)",
      description: "Thay đổi signature phương thức đánh giá đơn hàng 'reviewOrder' nhận thêm tham số imageUrl để lưu trữ ảnh chụp thực phẩm.",
      language: "java",
      code: `package com.fooddelivery.service;

import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.CustomerProfile;

public interface IOrderService {
    // Cập nhật: Thêm tham số String imageUrl ở cuối
    Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment, String imageUrl);
}`
    },
    {
      title: "OrderService.java (Implementation)",
      description: "Triển khai phương thức lưu đánh giá khách hàng xuống CSDL chứa đường dẫn ảnh tương đối do Client upload.",
      language: "java",
      code: `@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public Review reviewOrder(Long orderId, CustomerProfile customer, int rating, String comment, String imageUrl) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));

        // Kiểm tra quyền sở hữu đơn hàng
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Bạn không thể đánh giá đơn hàng của người khác");
        }

        // Tạo đối tượng đánh giá mới
        Review review = Review.builder()
                .order(order)
                .rating(rating)
                .comment(comment)
                .imageUrl(imageUrl) // Cập nhật: Lưu link ảnh đánh giá
                .build();

        review = reviewRepository.save(review);
        
        // Gắn review vào order và cập nhật trạng thái
        order.setReview(review);
        orderRepository.save(order);

        return review;
    }
}`
    },
    {
      title: "NotificationService.java (Thông báo)",
      description: "Gửi thông báo có tiêu đề và nội dung sinh động đến cho Khách hàng ngay sau khi Nhà hàng hoàn tất phản hồi đánh giá.",
      language: "java",
      code: `package com.fooddelivery.service;

import com.fooddelivery.entity.User;
import com.fooddelivery.entity.Notification;
import com.fooddelivery.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notifyRestaurantReplied(User customerUser, Long orderId) {
        // Tạo thông báo mới thời gian thực đến khách hàng
        createNotification(customerUser, 
                "💬 Phản hồi đánh giá mới!", 
                "Nhà hàng đã phản hồi đánh giá của bạn cho đơn #" + orderId + ".", 
                NotificationType.NEW_REVIEW, 
                orderId);
    }

    private void createNotification(User user, String title, String content, NotificationType type, Long orderId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .associationId(orderId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        
        // (Tùy chọn) Gửi qua WebSocket STOMP tới topic "/topic/notifications/{userId}"
    }
}`
    }
  ],
  controller: [
    {
      title: "CustomerApiController.java",
      description: "Cập nhật DTO ReviewRequest và endpoints viết đánh giá kèm ảnh của khách hàng.",
      language: "java",
      code: `package com.fooddelivery.controller;

import com.fooddelivery.dto.ReviewRequest;
import com.fooddelivery.entity.CustomerProfile;
import com.fooddelivery.service.IOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerApiController {

    private final IOrderService orderService;

    @PostMapping("/orders/{orderId}/review")
    public ResponseEntity<?> reviewOrder(@PathVariable Long orderId,
                                         @RequestBody ReviewRequest request,
                                         @ModelAttribute CustomerProfile customer) {
        
        // Lấy trường imageUrl từ payload gửi về từ Client
        Review review = orderService.reviewOrder(
                orderId, 
                customer, 
                request.getRating(), 
                request.getComment(), 
                request.getImageUrl()
        );
        
        return ResponseEntity.ok(review);
    }

    // Định nghĩa class DTO tĩnh nhận JSON payload
    public static class ReviewRequest {
        private int rating;
        private String comment;
        private String imageUrl; // Trường URL ảnh được truyền từ Client lên
        
        // Getters & Setters...
    }
}`
    },
    {
      title: "RestaurantApiController.java",
      description: "Thêm API nhận phản hồi từ nhà hàng, cập nhật trường 'restaurantReply' và gửi thông báo cho khách hàng qua NotificationService.",
      language: "java",
      code: `package com.fooddelivery.controller;

import com.fooddelivery.entity.Review;
import com.fooddelivery.service.NotificationService;
import com.fooddelivery.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantApiController {

    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    @PostMapping("/reviews/{id}/reply")
    public ResponseEntity<?> replyToReview(Authentication auth, 
                                           @PathVariable Long id, 
                                           @RequestBody Map<String, String> body) {
        // 1. Lấy thông tin tài khoản Nhà hàng từ Authentication
        // (Giả định lấy được thông tin quán restaurantProfile)
        
        // 2. Tìm đánh giá trong CSDL
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá không tồn tại"));

        // 3. Kiểm tra tính sở hữu của review (chỉ được trả lời review của khách cho quán của mình)
        /*
        if (!review.getOrder().getRestaurant().getId().equals(restaurantProfile.getId())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền phản hồi");
        }
        */

        // Lấy nội dung phản hồi từ JSON Body
        String replyText = body.get("reply");
        if (replyText == null || replyText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nội dung phản hồi không được để trống");
        }

        // 4. Lưu trường restaurantReply, repliedAt và lưu database
        review.setRestaurantReply(replyText);
        review.setRepliedAt(LocalDateTime.now());
        reviewRepository.save(review);

        // 5. Gửi thông báo đến Khách hàng tương ứng qua NotificationService
        notificationService.notifyRestaurantReplied(review.getOrder().getCustomer().getUser(), review.getOrder().getId());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Phản hồi đánh giá thành công!",
            "repliedAt", review.getRepliedAt()
        ));
    }
}`
    }
  ],
  frontend: [
    {
      title: "history.html (Customer Modal & AJAX)",
      description: "Tải ảnh từ Khách hàng lên máy chủ và gán URL tương đối nhận về vào form gửi review.",
      language: "javascript",
      code: `<!-- Bên trong Modal Đánh giá (history.html) -->
<div class="mb-3">
    <label class="form-label font-semibold">Hình ảnh đánh giá</label>
    <input type="file" id="reviewImageFile" accept="image/*" class="form-control">
    <div id="imageUploadProgress" class="text-xs text-muted mt-1 select-none"></div>
    <div id="imagePreviewContainer" class="mt-2 text-center d-none">
        <img id="imagePreview" src="" alt="Preview" class="img-thumbnail" style="max-height: 120px;">
        <input type="hidden" id="reviewImageUrlVal" value="">
    </div>
</div>

<script>
// Logic khi click nút Viết đánh giá
document.getElementById('reviewImageFile').addEventListener('change', async function() {
    const fileInput = this;
    if (fileInput.files.length === 0) return;

    const progressDiv = document.getElementById('imageUploadProgress');
    const previewContainer = document.getElementById('imagePreviewContainer');
    const previewImg = document.getElementById('imagePreview');
    const hiddenUrlInput = document.getElementById('reviewImageUrlVal');

    progressDiv.innerText = "⏳ Đang tải ảnh lên...";
    
    // Khởi tạo FormData
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        // Gửi tệp ảnh lên API upload chung của Spring Boot
        const res = await fetch('/api/upload/image', { 
            method: 'POST', 
            headers: { 'Authorization': 'Bearer ' + token }, 
            body: formData 
        });

        if (res.ok) {
            const data = await res.json(); // Ví dụ trả về: { "url": "/images/uuid-photo.jpg" }
            hiddenUrlInput.value = data.url;
            previewImg.src = data.url;
            previewContainer.classList.remove('d-none');
            progressDiv.innerHTML = "<span class='text-success'>✅ Tải ảnh thành công!</span>";
        } else {
            progressDiv.innerHTML = "<span class='text-danger'>❌ Lỗi khi tải ảnh lên. Thử lại!</span>";
        }
    } catch (err) {
        progressDiv.innerHTML = "<span class='text-danger'>❌ Không kết nối được với Server.</span>";
    }
});
</script>`
    },
    {
      title: "reviews.html (Restaurant View & Reply)",
      description: "Nhà hàng duyệt danh sách đánh giá của quán, hiển thị ảnh chụp (nếu có), bấm phản hồi nhanh để phản hồi đánh giá.",
      language: "javascript",
      code: `<!-- Layout hiển thị danh sách đánh giá của nhà hàng (reviews.html) -->
<div class="card shadow-sm mb-3" th:each="r : \${reviews}">
    <div class="card-body">
        <h5 class="card-title text-warning" th:text="\${'⭐'.repeat(r.rating)}">⭐⭐⭐⭐⭐</h5>
        <p class="card-text text-dark" th:text="\${r.comment}">Đồ ăn ngon, giao hàng siêu nhanh.</p>
        
        <!-- HIỂN THỊ ẢNH KHÁCH ĐĂNG (MỚI BỔ SUNG) -->
        <div class="mb-3" th:if="\${r.imageUrl != null}">
            <a th:href="\${r.imageUrl}" target="_blank" title="Bấm để xem ảnh phóng to">
                <img th:src="\${r.imageUrl}" class="img-thumbnail" style="max-height: 100px; width: auto; cursor: zoom-in;">
            </a>
        </div>

        <!-- PHẢN HỒI CỦA CHỦ QUÁN -->
        <div class="bg-light p-3 rounded mt-2" th:if="\${r.restaurantReply != null}">
            <p class="mb-1 text-muted small"><strong>Phản hồi từ Nhà hàng:</strong></p>
            <p class="mb-0 italic font-serif text-secondary" th:text="\${r.restaurantReply}">Dạ, cảm ơn bạn nhiều!</p>
            <span class="text-xs text-muted" th:text="\${#temporals.format(r.repliedAt, 'dd-MM-yyyy HH:mm')}">Thời điểm</span>
        </div>

        <!-- NÚT PHẢN HỒI NHANH NẾU CHƯA TRẢ LỜI (MỚI BỔ SUNG) -->
        <div th:id="'replyBox-' + \${r.id}" th:if="\${r.restaurantReply == null}" class="mt-3">
            <button class="btn btn-outline-primary btn-sm" th:onclick="'showReplyForm(' + \${r.id} + ')'">💬 Phản hồi</button>
            <div th:id="'form-container-' + \${r.id}" class="d-none mt-2">
                <textarea th:id="'replyInput-' + \${r.id}" class="form-control mb-2" rows="2" placeholder="Gửi câu trả lời của bạn tới khách hàng..."></textarea>
                <div class="text-end">
                    <button class="btn btn-secondary btn-sm me-1" th:onclick="'hideReplyForm(' + \${r.id} + ')'">Hủy</button>
                    <button class="btn btn-success btn-sm" th:onclick="'submitReply(' + \${r.id} + ')'">Gửi</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
function showReplyForm(id) {
    document.getElementById('form-container-' + id).classList.remove('d-none');
}
function hideReplyForm(id) {
    document.getElementById('form-container-' + id).classList.add('d-none');
}

async function submitReply(id) {
    const input = document.getElementById('replyInput-' + id);
    const text = input.value.trim();
    if (!text) return alert("Nội dung phản hồi không được rỗng!");

    try {
        const res = await fetch('/api/restaurant/reviews/' + id + '/reply', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ reply: text })
        });

        if (res.ok) {
            alert("Đã gửi phản hồi thành công!");
            window.location.reload(); // Làm mới trang để tải lại giao diện
        } else {
            alert("Lỗi khi gửi phản hồi.");
        }
    } catch (err) {
        console.error(err);
    }
}
</script>`
    }
  ]
};
