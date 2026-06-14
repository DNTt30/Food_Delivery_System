package com.duong.salesmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    private Double discountValue;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    public enum DiscountScope {
        ORDER_TOTAL,
        SHIPPING_FEE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_scope")
    private DiscountScope discountScope = DiscountScope.ORDER_TOTAL;

    private LocalDate startDate;
    private LocalDate expirationDate;
    private Double minOrderAmount;
    private Double maxDiscount;
    private String description;
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private RestaurantProfile restaurant;

    public Voucher() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public DiscountScope getDiscountScope() { return discountScope; }
    public void setDiscountScope(DiscountScope discountScope) { this.discountScope = discountScope; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public Double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(Double maxDiscount) { this.maxDiscount = maxDiscount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public RestaurantProfile getRestaurant() { return restaurant; }
    public void setRestaurant(RestaurantProfile restaurant) { this.restaurant = restaurant; }

    @Column(name = "max_global_usage")
    private Integer maxGlobalUsage;

    @Column(name = "current_global_usage")
    private Integer currentGlobalUsage = 0;

    @Column(name = "max_usage_per_user")
    private Integer maxUsagePerUser;

    public Integer getMaxGlobalUsage() { return maxGlobalUsage; }
    public void setMaxGlobalUsage(Integer maxGlobalUsage) { this.maxGlobalUsage = maxGlobalUsage; }
    public Integer getCurrentGlobalUsage() { return currentGlobalUsage; }
    public void setCurrentGlobalUsage(Integer currentGlobalUsage) { this.currentGlobalUsage = currentGlobalUsage; }
    public Integer getMaxUsagePerUser() { return maxUsagePerUser; }
    public void setMaxUsagePerUser(Integer maxUsagePerUser) { this.maxUsagePerUser = maxUsagePerUser; }
}
