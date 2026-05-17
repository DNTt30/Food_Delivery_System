package com.duong.salesmanagement.security;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final com.duong.salesmanagement.service.OrderService orderService;
    private final com.duong.salesmanagement.repository.UserRepository userRepository;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil, 
                                    UserDetailsService userDetailsService,
                                    @org.springframework.context.annotation.Lazy com.duong.salesmanagement.service.OrderService orderService,
                                    com.duong.salesmanagement.repository.UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                validateSubscription(accessor);
            }
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(auth);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) return;

        // Pattern: /topic/tracking/{orderId}
        if (destination.startsWith("/topic/tracking/")) {
            String orderIdStr = destination.substring("/topic/tracking/".length());
            try {
                Long orderId = Long.parseLong(orderIdStr);
                java.security.Principal principal = accessor.getUser();
                
                if (principal == null) {
                    throw new org.springframework.security.access.AccessDeniedException("Unauthorized subscription");
                }

                // Tìm User entity thực sự từ DB dựa trên username trong Principal
                com.duong.salesmanagement.model.User user = userRepository.findByUsername(principal.getName())
                        .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not found"));

                if (!orderService.hasPermissionToTrackOrder(orderId, user)) {
                    throw new org.springframework.security.access.AccessDeniedException("You do not have permission to track this order");
                }
            } catch (NumberFormatException e) {
                throw new org.springframework.security.access.AccessDeniedException("Invalid Order ID format");
            }
        }
    }
}
