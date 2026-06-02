package com.duong.salesmanagement.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duong.salesmanagement.config.VNPAYConfig;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.PaymentStatus;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.PaymentRepository;
import com.duong.salesmanagement.service.NotificationService;
import com.duong.salesmanagement.util.PaymentUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentController.class);

    private final VNPAYConfig vnpayConfig;
    private final FoodOrderRepository foodOrderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    public PaymentController(
            VNPAYConfig vnpayConfig,
            FoodOrderRepository foodOrderRepository,
            PaymentRepository paymentRepository,
            NotificationService notificationService
    ) {

        this.vnpayConfig = vnpayConfig;
        this.foodOrderRepository = foodOrderRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
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
                if (success && order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    order.setStatus(OrderStatus.PENDING);
                    
                    // 🔔 Notify: Customer đã thanh toán thành công
                    notificationService.notifyOrderCreated(
                            order.getCustomer().getUser(), order.getId(),
                            order.getRestaurant().getRestaurantName());
                    // 🔔 Notify: Restaurant có đơn mới đã thanh toán
                    notificationService.notifyNewOrderForRestaurant(
                            order.getRestaurant().getUser(), order.getId(),
                            order.getCustomer().getUser().getFullName());
                            
                } else if (!success && order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    order.setStatus(OrderStatus.CANCELLED);
                }
                foodOrderRepository.save(order);
            });
        });
    }
}