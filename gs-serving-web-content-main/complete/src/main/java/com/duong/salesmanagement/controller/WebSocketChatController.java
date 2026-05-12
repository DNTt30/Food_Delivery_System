package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.dto.ChatMessageRequest;
import com.duong.salesmanagement.dto.ChatMessageResponse;
import com.duong.salesmanagement.exception.ChatAccessDeniedException;
import com.duong.salesmanagement.exception.ChatLockedException;
import com.duong.salesmanagement.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket STOMP Controller for Real-time Chat.
 *
 * <pre>
 * Client SEND → /app/chat.send → broadcast to /topic/order.{orderId}
 * </pre>
 *
 * JWT is validated at STOMP CONNECT level by WebSocketAuthInterceptor.
 * Messages are saved to DB via REST POST /api/chat/send (guaranteed delivery),
 * then the server broadcasts to WebSocket subscribers via this controller.
 */
@Controller
@SuppressWarnings("null")
public class WebSocketChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public WebSocketChatController(SimpMessagingTemplate messagingTemplate,
                                   ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    /**
     * Receives a chat send request via STOMP, saves to DB, and broadcasts to order topic.
     * Uses manual validation since @Valid does not work reliably with @Payload STOMP.
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null) return;

        // Manual null/blank validation
        if (request == null
                || request.getOrderId() == null
                || request.getReceiverId() == null
                || request.getContent() == null
                || request.getContent().isBlank()) {
            sendError(principal.getName(), "Dữ liệu tin nhắn không hợp lệ");
            return;
        }

        try {
            ChatMessageResponse saved = chatService.sendMessage(request, principal.getName());
            // Broadcast saved message to all order participants in real-time
            messagingTemplate.convertAndSend("/topic/order." + request.getOrderId(), saved);
        } catch (ChatLockedException | ChatAccessDeniedException e) {
            sendError(principal.getName(), e.getMessage());
        } catch (Exception e) {
            sendError(principal.getName(), "Không thể gửi tin nhắn: " + e.getMessage());
        }
    }

    private void sendError(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/errors", Map.of("error", message));
    }
}
