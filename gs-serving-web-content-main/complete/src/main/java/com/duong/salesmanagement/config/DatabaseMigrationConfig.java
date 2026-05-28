package com.duong.salesmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationConfig.class);

    @Bean
    public ApplicationRunner alterPaymentMethodColumn(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE payments MODIFY COLUMN payment_method VARCHAR(50)");
                log.info("Successfully altered payments.payment_method to VARCHAR(50)");
            } catch (Exception e) {
                log.warn("Could not alter payments.payment_method column. It might already be modified or table doesn't exist. Details: " + e.getMessage());
            }
        };
    }
}
