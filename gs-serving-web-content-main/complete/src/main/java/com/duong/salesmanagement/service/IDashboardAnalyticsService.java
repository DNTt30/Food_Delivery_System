package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.DashboardStatisticsDTO;
import com.duong.salesmanagement.model.RestaurantProfile;
import java.time.LocalDate;

/**
 * Service Interface cho Dashboard Analytics
 * Cung cấp các method lấy thống kê linh hoạt theo date range
 */
public interface IDashboardAnalyticsService {

    /**
     * Lấy thống kê dashboard với hỗ trợ flexible date range picker
     * @param restaurant Nhà hàng
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return DashboardStatisticsDTO với đầy đủ metrics
     */
    DashboardStatisticsDTO getStatisticsByDateRange(RestaurantProfile restaurant, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy thống kê tuần này
     */
    DashboardStatisticsDTO getStatisticsThisWeek(RestaurantProfile restaurant);

    /**
     * Lấy thống kê tháng này
     */
    DashboardStatisticsDTO getStatisticsThisMonth(RestaurantProfile restaurant);

    /**
     * Lấy thống kê tháng trước
     */
    DashboardStatisticsDTO getStatisticsPreviousMonth(RestaurantProfile restaurant);

    /**
     * Lấy thống kê năm nay
     */
    DashboardStatisticsDTO getStatisticsThisYear(RestaurantProfile restaurant);

    /**
     * Lấy Top 5 món ăn bán chạy nhất
     */
    DashboardStatisticsDTO.BestSellerDTO[] getTopBestSellers(RestaurantProfile restaurant, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy Top 5 món ăn ế (slow moving items)
     */
    DashboardStatisticsDTO.SlowMovingItemDTO[] getSlowMovingItems(RestaurantProfile restaurant, LocalDate startDate, LocalDate endDate);
}
