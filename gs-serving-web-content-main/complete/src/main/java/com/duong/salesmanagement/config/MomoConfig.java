package com.duong.salesmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MomoConfig {

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:F8BBA842ECF85}")
    private String accessKey;

    @Value("${momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Value("${momo.redirect-url:http://localhost:8080/api/payments/momo-callback}")
    private String redirectUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/payments/momo-ipn}")
    private String ipnUrl;

    public String getPartnerCode() { return partnerCode; }
    public String getAccessKey()   { return accessKey; }
    public String getSecretKey()   { return secretKey; }
    public String getEndpoint()    { return endpoint; }
    public String getRedirectUrl() { return redirectUrl; }
    public String getIpnUrl()      { return ipnUrl; }
}
