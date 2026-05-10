package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.dto.ChatMessageRequest;
import com.duong.salesmanagement.dto.ChatMessageResponse;
import com.duong.salesmanagement.dto.ContactInfoResponse;
import com.duong.salesmanagement.exception.ChatAccessDeniedException;
import com.duong.salesmanagement.exception.ChatLockedException;
import com.duong.salesmanagement.service.ChatService;
import com.duong.salesmanagement.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the order-based chat system.
 *
 * <pre>
 * GET  /api/orders/{id}/contact-info   – list participants + phone (masked if closed)
 * GET  /api/chat/{orderId}             – message history (polling endpoint)
 * POST /api/chat/send                  – send a message
 * </pre>
 *
 * All endpoints require a valid JWT Bearer token.
 */
@RestController
@RequestMapping("/api")
public class ChatApiController {

    private final ChatService chatService;
    private final ContactService contactService;

    public ChatApiController(ChatService chatService, ContactService contactService) {
        this.chatService = chatService;
        this.contactService = contactService;
    }

    // ----------------------------------------------------------------
    // GET /api/orders/{id}/contact-info
    // ----------------------------------------------------------------

    /**
     * Returns the contacts visible to the authenticated user for the given order.
     *
     * <p>The frontend uses this to:
     * <ul>
     *   <li>Decide which chat buttons (NH / TX) to show</li>
     *   <li>Obtain the {@code receiverId} needed for {@code POST /api/chat/send}</li>
     *   <li>Detect a closed order ({@code isCompletedOrCancelled = true}) and lock the UI</li>
     * </ul>
     */
    @GetMapping("/orders/{id}/contact-info")
    public ResponseEntity<?> getContactInfo(@PathVariable Long id, Authentication auth) {
        if (auth == null) {
            return unauthorized();
        }
        try {
            ContactInfoResponse response = contactService.getContactInfo(id, auth.getName());
            return ResponseEntity.ok(response);
        } catch (ChatAccessDeniedException e) {
            return forbidden(e.getMessage());
        } catch (RuntimeException e) {
            return notFound(e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // GET /api/chat/{orderId}
    // ----------------------------------------------------------------

    /**
     * Returns all messages for an order, sorted by time (ascending).
     *
     * <p>Intended for the polling loop: call every 3–5 s while the chat panel is open.
     */
    @GetMapping("/chat/{orderId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long orderId, Authentication auth) {
        if (auth == null) {
            return unauthorized();
        }
        try {
            List<ChatMessageResponse> messages = chatService.getOrderMessages(orderId, auth.getName());
            return ResponseEntity.ok(messages);
        } catch (ChatAccessDeniedException e) {
            return forbidden(e.getMessage());
        } catch (RuntimeException e) {
            return notFound(e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // POST /api/chat/send
    // ----------------------------------------------------------------

    /**
     * Sends a message in an order's chat room.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "orderId":    1001,
     *   "receiverId": 7,
     *   "content":    "Xác nhận đơn chưa ạ?"
     * }
     * }</pre>
     */
    @PostMapping("/chat/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody ChatMessageRequest request,
                                         Authentication auth) {
        if (auth == null) {
            return unauthorized();
        }
        try {
            ChatMessageResponse saved = chatService.sendMessage(request, auth.getName());
            return ResponseEntity.ok(saved);
        } catch (ChatLockedException | ChatAccessDeniedException e) {
            return forbidden(e.getMessage());
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Response helpers
    // ----------------------------------------------------------------

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Bạn chưa đăng nhập"));
    }

    private ResponseEntity<Map<String, String>> forbidden(String msg) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(msg));
    }

    private ResponseEntity<Map<String, String>> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(msg));
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(msg));
    }

    private Map<String, String> error(String message) {
        return Map.of("error", message);
    }
}
