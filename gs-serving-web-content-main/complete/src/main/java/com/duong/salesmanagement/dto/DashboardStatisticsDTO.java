package com.duong.salesmanagement.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO cho Dashboard Statistics với hỗ trợ Date Range Picker
 * Dùng cho báo cáo linh hoạt: Tuần này, Tháng này, Tháng trước, Năm nay, Custom range
 */
public class DashboardStatisticsDTO {

    // ===== REVENUE & GROWTH =====
    public Double currentPeriodRevenue;      // Doanh thu kỳ này
    public Double previousPeriodRevenue;     // Doanh thu kỳ trước
    public Double revenueGrowthPercent;      // % tăng trưởng (có thể âm)
    public String growthTrendIcon;           // "↑" (xanh), "↓" (đỏ)
    public String growthTrendColor;          // "text-success" hoặc "text-danger"

    // ===== ORDER STATISTICS =====
    public Long currentPeriodOrders;         // Số đơn kỳ này
    public Long previousPeriodOrders;        // Số đơn kỳ trước
    public Double orderGrowthPercent;        // % tăng trưởng đơn hàng
    public String orderTrendIcon;            // "↑" hoặc "↓"
    public String orderTrendColor;

    // ===== COMPLETION RATE =====
    public Long completedOrders;             // Đơn hoàn thành
    public Long cancelledOrders;             // Đơn hủy
    public Double completionRate;            // Tỷ lệ % hoàn thành

    // ===== MENU METRICS =====
    public Long menuCount;                   // Tổng số món
    public Double avgRating;                 // Đánh giá trung bình

    // ===== TOP SELLERS =====
    public List<BestSellerDTO> topFiveBestSellers;    // Top 5 món bán chạy
    public List<SlowMovingItemDTO> slowMovingItems;   // Top 5 món ế

    // ===== CHART DATA =====
    public List<Map<String, Object>> chartDataCurrent;      // Doanh thu từng ngày (kỳ này)
    public List<Map<String, Object>> chartDataPrevious;     // Doanh thu từng ngày (kỳ trước)

    // ===== DATE RANGE INFO =====
    public LocalDate periodStartDate;
    public LocalDate periodEndDate;
    public LocalDate previousPeriodStartDate;
    public LocalDate previousPeriodEndDate;
    public String periodLabel;               // "Tuần này", "Tháng này", etc.

    // ===== RESTAURANT INFO =====
    public Boolean isOpen;
    public Integer activeOrders;            // Số đơn đang xử lý (PENDING + PREPARING)

    public DashboardStatisticsDTO() {}

    // ===== NESTED DTOs =====

    /**
     * DTO cho Top 5 Món Ăn Bán Chạy
     */
    public static class BestSellerDTO {
        public Long menuItemId;
        public String itemName;
        public Double itemPrice;
        public Integer soldCount;           // Số lượng bán
        public Double totalRevenue;         // Tổng doanh thu từ món này
        public String imageUrl;
        public Integer rank;                // Vị trí (1-5)

        public BestSellerDTO() {}

        public BestSellerDTO(Long menuItemId, String itemName, Double itemPrice, 
                           Integer soldCount, Double totalRevenue, String imageUrl, Integer rank) {
            this.menuItemId = menuItemId;
            this.itemName = itemName;
            this.itemPrice = itemPrice;
            this.soldCount = soldCount;
            this.totalRevenue = totalRevenue;
            this.imageUrl = imageUrl;
            this.rank = rank;
        }
    }

    /**
     * DTO cho Món Ăn Ế (Slow Moving Items)
     */
    public static class SlowMovingItemDTO {
        public Long menuItemId;
        public String itemName;
        public Double itemPrice;
        public Integer soldCount;           // Số lượng bán (rất thấp)
        public Integer cancellationCount;   // Số lần bị hủy
        public Double cancellationRate;     // % bị hủy so với đơn có món này
        public String imageUrl;
        public Integer rank;

        public SlowMovingItemDTO() {}

        public SlowMovingItemDTO(Long menuItemId, String itemName, Double itemPrice,
                               Integer soldCount, Integer cancellationCount, Double cancellationRate,
                               String imageUrl, Integer rank) {
            this.menuItemId = menuItemId;
            this.itemName = itemName;
            this.itemPrice = itemPrice;
            this.soldCount = soldCount;
            this.cancellationCount = cancellationCount;
            this.cancellationRate = cancellationRate;
            this.imageUrl = imageUrl;
            this.rank = rank;
        }
    }

    // Getters & Setters
    public Double getCurrentPeriodRevenue() { return currentPeriodRevenue; }
    public void setCurrentPeriodRevenue(Double currentPeriodRevenue) { this.currentPeriodRevenue = currentPeriodRevenue; }
    public Double getPreviousPeriodRevenue() { return previousPeriodRevenue; }
    public void setPreviousPeriodRevenue(Double previousPeriodRevenue) { this.previousPeriodRevenue = previousPeriodRevenue; }
    public Double getRevenueGrowthPercent() { return revenueGrowthPercent; }
    public void setRevenueGrowthPercent(Double revenueGrowthPercent) { this.revenueGrowthPercent = revenueGrowthPercent; }
    public String getGrowthTrendIcon() { return growthTrendIcon; }
    public void setGrowthTrendIcon(String growthTrendIcon) { this.growthTrendIcon = growthTrendIcon; }
    public String getGrowthTrendColor() { return growthTrendColor; }
    public void setGrowthTrendColor(String growthTrendColor) { this.growthTrendColor = growthTrendColor; }
    public List<BestSellerDTO> getTopFiveBestSellers() { return topFiveBestSellers; }
    public void setTopFiveBestSellers(List<BestSellerDTO> topFiveBestSellers) { this.topFiveBestSellers = topFiveBestSellers; }
    public List<SlowMovingItemDTO> getSlowMovingItems() { return slowMovingItems; }
    public void setSlowMovingItems(List<SlowMovingItemDTO> slowMovingItems) { this.slowMovingItems = slowMovingItems; }
    public List<Map<String, Object>> getChartDataCurrent() { return chartDataCurrent; }
    public void setChartDataCurrent(List<Map<String, Object>> chartDataCurrent) { this.chartDataCurrent = chartDataCurrent; }
    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public void setPeriodStartDate(LocalDate periodStartDate) { this.periodStartDate = periodStartDate; }
    public LocalDate getPeriodEndDate() { return periodEndDate; }
    public void setPeriodEndDate(LocalDate periodEndDate) { this.periodEndDate = periodEndDate; }
    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    public Boolean getIsOpen() { return isOpen; }
    public void setIsOpen(Boolean isOpen) { this.isOpen = isOpen; }
}
