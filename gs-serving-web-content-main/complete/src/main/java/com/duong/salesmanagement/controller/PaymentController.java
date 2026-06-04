package com.duong.salesmanagement.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.duong.salesmanagement.config.MomoConfig;
import com.duong.salesmanagement.config.VNPAYConfig;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.PaymentMethod;
import com.duong.salesmanagement.model.PaymentStatus;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.PaymentRepository;
import com.duong.salesmanagement.service.NotificationService;
import com.duong.salesmanagement.service.OrderService;
import com.duong.salesmanagement.util.PaymentUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentController.class);

    private final VNPAYConfig vnpayConfig;
    private final MomoConfig momoConfig;
    private final FoodOrderRepository foodOrderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final OrderService orderService;

    public PaymentController(
            VNPAYConfig vnpayConfig,
            MomoConfig momoConfig,
            FoodOrderRepository foodOrderRepository,
            PaymentRepository paymentRepository,
            NotificationService notificationService,
            OrderService orderService
    ) {
        this.vnpayConfig = vnpayConfig;
        this.momoConfig = momoConfig;
        this.foodOrderRepository = foodOrderRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.orderService = orderService;
    }

    /**
     * Tạo URL thanh toán VNPay
     */
    @GetMapping("/create-vnpay-payment")
    public ResponseEntity<?> createPayment(
            HttpServletRequest request,
            @RequestParam("orderId") Long orderId
    ) {

        Optional<FoodOrder> orderOpt =
                foodOrderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Không tìm thấy đơn hàng"
                            )
                    );
        }

        FoodOrder order = orderOpt.get();

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Đơn hàng không ở trạng thái chờ thanh toán"));
        }

        // amount = VND * 100
        long amount = (long) (
                (order.getTotalAmount() != null
                        ? order.getTotalAmount()
                        : 0.0
                ) * 100
        );

        log.info("VNPay amount = {}", amount);

        String vnp_TxnRef =
                VNPAYConfig.getRandomNumber(8)
                        + "_"
                        + orderId;

        String vnp_IpAddr =
                VNPAYConfig.getIpAddress(request);

        // TIME VN
        Calendar cld =
    Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate =
                formatter.format(cld.getTime());

        cld.add(Calendar.MINUTE, 15);

        String vnp_ExpireDate =
                formatter.format(cld.getTime());

        Map<String, String> vnp_Params =
                new TreeMap<>();

        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");

        vnp_Params.put(
                "vnp_TmnCode",
                vnpayConfig.getTmnCode()
        );

        vnp_Params.put(
                "vnp_Amount",
                String.valueOf(amount)
        );

        vnp_Params.put(
                "vnp_CurrCode",
                "VND"
        );

        vnp_Params.put(
                "vnp_TxnRef",
                vnp_TxnRef
        );

        vnp_Params.put(
                "vnp_OrderInfo",
                "Thanh toan don hang_" + vnp_TxnRef
        );

    
       vnp_Params.put("vnp_OrderType", "other");

        vnp_Params.put(
                "vnp_Locale",
                "vn"
        );

        vnp_Params.put(
                "vnp_ReturnUrl",
                vnpayConfig.getReturnUrl()
        );

        vnp_Params.put(
                "vnp_IpAddr",
                vnp_IpAddr
        );

        vnp_Params.put(
                "vnp_CreateDate",
                vnp_CreateDate
        );

        vnp_Params.put(
                "vnp_ExpireDate",
                vnp_ExpireDate
        );

        try {

            StringBuilder hashData =
                    new StringBuilder();

            StringBuilder query =
                    new StringBuilder();

            boolean first = true;

           for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {

    String fieldName = entry.getKey();
    String fieldValue = entry.getValue();

    if (fieldValue != null && !fieldValue.isEmpty()) {

        String encodedValue = URLEncoder.encode(
                fieldValue,
                StandardCharsets.US_ASCII.toString()
        );

        if (!first) {
            hashData.append("&");
            query.append("&");
        }

        // HASH DATA
        hashData.append(fieldName)
                .append("=")
                .append(encodedValue);

        // QUERY
        query.append(
                URLEncoder.encode(
                        fieldName,
                        StandardCharsets.US_ASCII.toString()
                )
        )
        .append("=")
        .append(encodedValue);

        first = false;
    }
}

            log.info("HASH DATA = {}", hashData);

            String vnp_SecureHash =
                    PaymentUtil.hmacSHA512(
                            vnpayConfig.getHashSecret(),
                            hashData.toString()
                    );

            String paymentUrl =
                    vnpayConfig.getPayUrl()
                            + "?"
                            + query
                            + "&vnp_SecureHash="
                            + vnp_SecureHash;

            log.info("PAYMENT URL = {}", paymentUrl);

            return ResponseEntity.ok(
                    Map.of(
                            "paymentUrl",
                            paymentUrl
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Lỗi tạo URL thanh toán: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    "Không thể tạo URL thanh toán"
                            )
                    );
        }
    }

    /**
     * Callback
     */
    @GetMapping("/vnpay-callback")
    public void vnpayCallback(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        Map<String, String> vnpParams =
                new TreeMap<>();

        Enumeration<String> params =
                request.getParameterNames();

        while (params.hasMoreElements()) {

            String fieldName =
                    params.nextElement();

            vnpParams.put(
                    fieldName,
                    request.getParameter(fieldName)
            );
        }

        String vnp_SecureHash =
                vnpParams.remove("vnp_SecureHash");

        vnpParams.remove("vnp_SecureHashType");

        String signValue =
                buildHashData(vnpParams);

        if (signValue.equals(vnp_SecureHash)) {

            String vnp_ResponseCode =
                    request.getParameter("vnp_ResponseCode");

            String vnp_TxnRef =
                    request.getParameter("vnp_TxnRef");

            Long orderId =
                    parseOrderId(vnp_TxnRef);

            if (orderId != null) {

                updatePaymentStatus(
                        orderId,
                        "00".equals(vnp_ResponseCode)
                );

                String status =
                        "00".equals(vnp_ResponseCode)
                                ? "success"
                                : "fail";

                response.sendRedirect(
                        "/payment-result?status="
                                + status
                                + "&orderId="
                                + orderId
                );

            } else {

                response.sendRedirect(
                        "/payment-result?status=fail"
                );
            }

        } else {

            response.sendRedirect(
                    "/payment-result?status=fail"
            );
        }
    }

    /**
     * IPN
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<?> vnpayIpn(
            HttpServletRequest request
    ) {

        try {

            Map<String, String> vnpParams =
                    new TreeMap<>();

            Enumeration<String> params =
                    request.getParameterNames();

            while (params.hasMoreElements()) {

                String fieldName =
                        params.nextElement();

                vnpParams.put(
                        fieldName,
                        request.getParameter(fieldName)
                );
            }

            String vnp_SecureHash =
                    vnpParams.remove("vnp_SecureHash");

            vnpParams.remove("vnp_SecureHashType");

            String signValue =
                    buildHashData(vnpParams);

            if (!signValue.equals(vnp_SecureHash)) {

                return ResponseEntity.ok(
                        Map.of(
                                "RspCode",
                                "97",
                                "Message",
                                "Invalid signature"
                        )
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "RspCode",
                            "00",
                            "Message",
                            "Confirm Success"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.ok(
                    Map.of(
                            "RspCode",
                            "99",
                            "Message",
                            "Unknown error"
                    )
            );
        }
    }

    /**
     * Build hash data
     */
    private String buildHashData(
            Map<String, String> fields
    ) {

        StringBuilder sb =
                new StringBuilder();

        boolean first = true;

        for (Map.Entry<String, String> entry
                : fields.entrySet()) {

            String fieldValue =
                    entry.getValue();

            if (fieldValue != null
                    && !fieldValue.isEmpty()) {

                if (!first) {
                    sb.append('&');
                }

                sb.append(entry.getKey())
                  .append("=")
                  .append(
                      URLEncoder.encode(
                           fieldValue,
                        StandardCharsets.US_ASCII
      )
  );

                first = false;
            }
        }

        return PaymentUtil.hmacSHA512(
                vnpayConfig.getHashSecret(),
                sb.toString()
        );
    }

    /**
     * Parse orderId
     */
    private Long parseOrderId(
            String vnp_TxnRef
    ) {

        try {

            if (vnp_TxnRef == null
                    || !vnp_TxnRef.contains("_")) {

                return null;
            }

            String[] parts =
                    vnp_TxnRef.split("_");

            return Long.parseLong(parts[1]);

        } catch (Exception e) {

            log.warn(
                    "Không parse được orderId: {}",
                    vnp_TxnRef
            );

            return null;
        }
    }

    /**
     * Update payment status
     */
    private void updatePaymentStatus(
            Long orderId,
            boolean success
    ) {

        foodOrderRepository.findById(orderId).ifPresent(order -> {
            paymentRepository.findByOrder(order).ifPresent(payment -> {
                PaymentStatus newStatus = success
                        ? PaymentStatus.COMPLETED
                        : PaymentStatus.FAILED;
                payment.setPaymentStatus(newStatus);
                paymentRepository.save(payment);

                order.setPaymentStatus(newStatus.name());
                if (success && order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
                    order.setStatus(OrderStatus.PENDING);
                    
                    // 🔔 Notify: Customer đã thanh toán thành công
                    notificationService.notifyOrderCreated(
                            order.getCustomer().getUser(), order.getId(),
                            order.getRestaurant().getRestaurantName());
                    // 🔔 Notify: Restaurant có đơn mới đã thanh toán
                    notificationService.notifyNewOrderForRestaurant(
                            order.getRestaurant().getUser(), order.getId(),
                            order.getCustomer().getUser().getFullName());
                            
                } else if (!success && order.getStatus() == OrderStatus.AWAITING_PAYMENT) {
                    order.setStatus(OrderStatus.CANCELLED);
                }
                foodOrderRepository.save(order);

                if (success) {
                    orderService.activateOrderAfterOnlinePayment(orderId);
                } else {
                    orderService.cancelUnpaidOnlineOrder(orderId);
                }
            });
        });
    }

    // ═══════════════════════════════════════════════
    // MOMO PAYMENT
    // ═══════════════════════════════════════════════

    /**
     * Tạo URL thanh toán MoMo
     */
    @GetMapping("/create-momo-payment")
    public ResponseEntity<?> createMomoPayment(
            @RequestParam("orderId") Long orderId
    ) {
        Optional<FoodOrder> orderOpt = foodOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy đơn hàng"));
        }
        FoodOrder order = orderOpt.get();
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Đơn hàng không ở trạng thái chờ thanh toán"));
        }

        try {
            long amount = (long)(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
            String requestId   = momoConfig.getPartnerCode() + System.currentTimeMillis();
            String momoOrderId = requestId;
            String orderInfo   = "Thanh toan don hang #" + orderId;
            String extraData   = "";
            String requestType = "payWithMethod";

            // Build raw signature string
            String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + momoConfig.getIpnUrl()
                    + "&orderId=" + momoOrderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + momoConfig.getPartnerCode()
                    + "&redirectUrl=" + momoConfig.getRedirectUrl() + "?internalOrderId=" + orderId
                    + "&requestId=" + requestId
                    + "&requestType=" + requestType;

            String signature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);
            log.info("[MoMo] rawSignature = {}", rawSignature);
            log.info("[MoMo] signature    = {}", signature);

            // Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode",  momoConfig.getPartnerCode());
            body.put("accessKey",    momoConfig.getAccessKey());
            body.put("requestId",    requestId);
            body.put("amount",       String.valueOf(amount));
            body.put("orderId",      momoOrderId);
            body.put("orderInfo",    orderInfo);
            body.put("redirectUrl",  momoConfig.getRedirectUrl() + "?internalOrderId=" + orderId);
            body.put("ipnUrl",       momoConfig.getIpnUrl());
            body.put("lang",         "vi");
            body.put("extraData",    extraData);
            body.put("requestType",  requestType);
            body.put("signature",    signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> momoResp = restTemplate.postForEntity(
                    momoConfig.getEndpoint(), entity, Map.class);

            Map<?, ?> momoBody = momoResp.getBody();
            log.info("[MoMo] Response = {}", momoBody);

            if (momoBody != null && momoBody.containsKey("payUrl")) {
                return ResponseEntity.ok(Map.of("paymentUrl", momoBody.get("payUrl")));
            } else {
                String errMsg = momoBody != null ? String.valueOf(((Map<Object,Object>)(Map<?,?>)momoBody).getOrDefault("message", "Unknown")) : "No response";
                log.error("[MoMo] Error: {}", errMsg);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", "MoMo từ chối: " + errMsg));
            }

        } catch (Exception e) {
            log.error("[MoMo] Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi kết nối MoMo: " + e.getMessage()));
        }
    }

    /**
     * MoMo Callback (redirect sau khi user thanh toán)
     */
    @GetMapping("/momo-callback")
    public void momoCallback(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        String resultCode      = request.getParameter("resultCode");
        String internalOrderId = request.getParameter("internalOrderId");

        boolean success = "0".equals(resultCode);
        Long orderId = null;
        try { orderId = Long.parseLong(internalOrderId); } catch (Exception ignored) {}

        if (orderId != null) {
            updatePaymentStatus(orderId, success);
            String status = success ? "success" : "fail";
            response.sendRedirect("/payment-result?status=" + status + "&orderId=" + orderId);
        } else {
            response.sendRedirect("/payment-result?status=fail");
        }
    }

    /**
     * MoMo IPN (server-to-server)
     */
    @GetMapping("/momo-ipn")
    public ResponseEntity<?> momoIpn(HttpServletRequest request) {
        String resultCode      = request.getParameter("resultCode");
        String internalOrderId = request.getParameter("internalOrderId");
        boolean success = "0".equals(resultCode);
        try {
            Long orderId = Long.parseLong(internalOrderId);
            updatePaymentStatus(orderId, success);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("status", "received"));
    }

    /** HMAC-SHA256 helper cho MoMo */
    private String hmacSHA256(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(keySpec);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * Hoàn tiền cho đơn hàng đã thanh toán online
     */
    @GetMapping("/refund")
    public ResponseEntity<?> refundPayment(
            @RequestParam("orderId") Long orderId
    ) {
        Optional<FoodOrder> orderOpt = foodOrderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Không tìm thấy đơn hàng"));
        }

        FoodOrder order = orderOpt.get();

        // Check if payment exists and is completed
        Optional<com.duong.salesmanagement.model.Payment> paymentOpt =
                paymentRepository.findByOrder(order);

        if (paymentOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Không tìm thấy thông tin thanh toán"));
        }

        com.duong.salesmanagement.model.Payment payment = paymentOpt.get();

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chỉ có thể hoàn tiền cho giao dịch đã thành công"));
        }

        // Check if payment method is online
        if (payment.getPaymentMethod() != com.duong.salesmanagement.model.PaymentMethod.VNPAY &&
            payment.getPaymentMethod() != com.duong.salesmanagement.model.PaymentMethod.MOMO_E_WALLET) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chỉ hỗ trợ hoàn tiền cho thanh toán online (VNPAY, MoMo)"));
        }

        // Mark payment as refunded
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        order.setPaymentStatus(PaymentStatus.REFUNDED.name());
        foodOrderRepository.save(order);

        log.info("Đã hoàn tiền cho đơn hàng {}: {}", orderId, payment.getPaymentMethod());

        return ResponseEntity.ok(Map.of(
                "message", "Đã yêu cầu hoàn tiền thành công",
                "orderId", orderId,
                "refundAmount", payment.getAmount()
        ));
    }
}