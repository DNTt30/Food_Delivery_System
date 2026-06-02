package com.duong.salesmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MomoConfig {
    @Value("${momo.partner-code:MOMO_TEST}")
    private String partnerCode;

    @Value("${momo.access-key:MOMO_ACCESS_KEY}")
    private String accessKey;

    @Value("${momo.secret-key:MOMO_SECRET_KEY}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    public String getPartnerCode() { return partnerCode; }
    public String getAccessKey() { return accessKey; }
    public String getSecretKey() { return secretKey; }
    public String getEndpoint() { return endpoint; }
}
