package com.duong.salesmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPAYConfig {
    @Value("${vnpay.tmn-code:9XOGBZ6J}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:6ODHBIH5SQZAWG8VY28UQ4DYF3BYESMF}")
    private String hashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.return-url:http://localhost:8080/api/payments/vnpay-callback}")
    private String returnUrl;

    public String getTmnCode() { return tmnCode; }
    public String getHashSecret() { return hashSecret; }
    public String getPayUrl() { return payUrl; }
    public String getReturnUrl() { return returnUrl; }

    public static String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String ipAdress = request.getHeader("X-FORWARDED-FOR");
        if (ipAdress == null) {
            ipAdress = request.getRemoteAddr();
        }
        return ipAdress;
    }

    public static String getRandomNumber(int len) {
        java.util.Random rnd = new java.util.Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
