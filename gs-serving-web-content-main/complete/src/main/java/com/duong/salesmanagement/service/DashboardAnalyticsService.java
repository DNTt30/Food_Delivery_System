package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.DashboardStatisticsDTO;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.repository.DashboardAnalyticsRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation cho Dashboard Analytics
 * Xử lý logic tính toán metrics, growth %, trend arrows
 */
@Service
public class DashboardAnalyticsService implements IDashboardAnalyticsService {

    private final DashboardAnalyticsRepository analyticsRepository;

    public DashboardAnalyticsService(DashboardAnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public DashboardStatisticsDTO getStatisticsByDateRange(RestaurantProfile restaurant, LocalDate startDate, LocalDate endDate) {
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO();
        
        // Set period info
        stats.setPeriodStartDate(startDate);
        stats.setPeriodEndDate(endDate);
        stats.setPeriodLabel(formatDateRangeLabel(startDate, endDate));

        // Calculate previous period (same duration)
        long daysDifference = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate previousStart = startDate.minusDays(daysDifference + 1);
        LocalDate previousEnd = startDate.minusDays(1);
        stats.setPreviousPeriodStartDate(previousStart);
        stats.setPreviousPeriodEndDate(previousEnd);

        LocalDateTime currentStart = startDate.atStartOfDay();
        LocalDateTime currentEnd = endDate.atTime(LocalTime.MAX);
        LocalDateTime previousStartDt = previousStart.atStartOfDay();
        LocalDateTime previousEndDt = previousEnd.atTime(LocalTime.MAX);

        // Get revenue data
        Double currentRevenue = analyticsRepository.findTotalRevenueByRestaurant(restaurant.getId(), currentStart, currentEnd);
        Double previousRevenue = analyticsRepository.findTotalRevenueByRestaurant(restaurant.getId(), previousStartDt, previousEndDt);
        
        currentRevenue = currentRevenue != null ? currentRevenue : 0.0;
        previousRevenue = previousRevenue != null ? previousRevenue : 0.0;

        stats.setCurrentPeriodRevenue(currentRevenue);
        stats.setPreviousPeriodRevenue(previousRevenue);

        // Calculate revenue growth
        double revenueGrowth = calculateGrowthPercent(previousRevenue, currentRevenue);
        stats.setRevenueGrowthPercent(revenueGrowth);
        stats.setGrowthTrendIcon(revenueGrowth >= 0 ? "↑" : "↓");
        stats.setGrowthTrendColor(revenueGrowth >= 0 ? "text-success" : "text-danger");

        // Get order counts
        Long currentOrders = analyticsRepository.countCompletedOrders(restaurant.getId(), currentStart, currentEnd);
        Long previousOrders = analyticsRepository.countCompletedOrders(restaurant.getId(), previousStartDt, previousEndDt);
        
        currentOrders = currentOrders != null ? currentOrders : 0L;
        previousOrders = previousOrders != null ? previousOrders : 0L;

        stats.setCurrentPeriodOrders(currentOrders);
        stats.setPreviousPeriodOrders(previousOrders);

        // Calculate order growth
        double orderGrowth = calculateGrowthPercent(previousOrders.doubleValue(), currentOrders.doubleValue());
        stats.setOrderGrowthPercent(orderGrowth);
        stats.setOrderTrendIcon(orderGrowth >= 0 ? "↑" : "↓");
        stats.setOrderTrendColor(orderGrowth >= 0 ? "text-success" : "text-danger");

        // Get cancellation data
        Long cancelledOrders = analyticsRepository.countCancelledOrders(restaurant.getId(), currentStart, currentEnd);
        cancelledOrders = cancelledOrders != null ? cancelledOrders : 0L;
        
        stats.setCompletedOrders(currentOrders);
        stats.setCancelledOrders(cancelledOrders);
        
        // Completion rate
        long totalOrders = currentOrders + cancelledOrders;
        double completionRate = totalOrders > 0 ? (currentOrders.doubleValue() / totalOrders) * 100 : 0.0;
        stats.setCompletionRate(completionRate);

        // Get daily revenue chart data
        List<Map<String, Object>> currentChartData = getDailyRevenueChartData(restaurant.getId(), currentStart, currentEnd);
        List<Map<String, Object>> previousChartData = getDailyRevenueChartData(restaurant.getId(), previousStartDt, previousEndDt);
        
        stats.setChartDataCurrent(currentChartData);
        stats.setChartDataPrevious(previousChartData);

        // Get top sellers
        BestSellerDTO[] topSellers = getTopBestSellers(restaurant, startDate, endDate);
        stats.setTopFiveBestSellers(Arrays.asList(topSellers));

        // Get slow moving items
        SlowMovingItemDTO[] slowItems = getSlowMovingItems(restaurant, startDate, endDate);
        stats.setSlowMovingItems(Arrays.asList(slowItems));

        return stats;
    }

    @Override
    public DashboardStatisticsDTO getStatisticsThisWeek(RestaurantProfile restaurant) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return getStatisticsByDateRange(restaurant, startOfWeek, now);
    }

    @Override
    public DashboardStatisticsDTO getStatisticsThisMonth(RestaurantProfile restaurant) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        return getStatisticsByDateRange(restaurant, startOfMonth, now);
    }

    @Override
    public DashboardStatisticsDTO getStatisticsPreviousMonth(RestaurantProfile restaurant) {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        LocalDate startOfMonth = previousMonth.atDay(1);
        LocalDate endOfMonth = previousMonth.atEndOfMonth();
        return getStatisticsByDateRange(restaurant, startOfMonth, endOfMonth);
    }

    @Override
    public DashboardStatisticsDTO getStatisticsThisYear(RestaurantProfile restaurant) {
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = now.withDayOfYear(1);
        return getStatisticsByDateRange(restaurant, startOfYear, now);
    }

    @Override
    public BestSellerDTO[] getTopBestSellers(RestaurantProfile restaurant, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(LocalTime.MAX);
        
        List<Object[]> results = analyticsRepository.findTopBestSellersByRestaurant(
                restaurant.getId(), startDt, endDt, 5
        );

        return results.stream().map((row, index) -> new BestSellerDTO(
                ((Number) row[0]).longValue(),      // menuItemId
                (String) row[1],                     // itemName
                ((Number) row[2]).doubleValue(),     // itemPrice
                row[3] != null ? ((Number) row[3]).intValue() : 0,  // soldCount
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0,  // totalRevenue
                (String) row[5],                     // imageUrl
                index + 1                            // rank
        )).collect(Collectors.toList()).toArray(new BestSellerDTO[0]);
    }

    @Override
    public SlowMovingItemDTO[] getSlowMovingItems(RestaurantProfile restaurant, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(LocalTime.MAX);
        
        List<Object[]> results = analyticsRepository.findSlowMovingItemsByRestaurant(
                restaurant.getId(), startDt, endDt, 5
        );

        return results.stream().map((row, index) -> new SlowMovingItemDTO(
                ((Number) row[0]).longValue(),      // menuItemId
                (String) row[1],                     // itemName
                ((Number) row[2]).doubleValue(),     // itemPrice
                row[3] != null ? ((Number) row[3]).intValue() : 0,  // soldCount
                row[4] != null ? ((Number) row[4]).intValue() : 0,  // cancellationCount
                row[5] != null ? ((Number) row[5]).doubleValue() : 0.0,  // cancellationRate
                (String) row[6],                     // imageUrl
                index + 1                            // rank
        )).collect(Collectors.toList()).toArray(new SlowMovingItemDTO[0]);
    }

    // ===== HELPER METHODS =====

    /**
     * Tính % tăng trưởng so với kỳ trước
     */
    private double calculateGrowthPercent(double previousValue, double currentValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? 100.0 : 0.0;
        }
        return ((currentValue - previousValue) / previousValue) * 100;
    }

    /**
     * Lấy dữ liệu doanh thu theo ngày cho chart
     */
    private List<Map<String, Object>> getDailyRevenueChartData(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = analyticsRepository.findDailyRevenueByRestaurant(restaurantId, startDate, endDate);
        
        return results.stream().map(row -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", row[0].toString());
            point.put("revenue", ((Number) row[1]).doubleValue());
            point.put("orders", ((Number) row[2]).longValue());
            return point;
        }).collect(Collectors.toList());
    }

    /**
     * Format label cho date range
     */
    private String formatDateRangeLabel(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfYear = now.withDayOfYear(1);

        if (startDate.equals(startOfWeek) && endDate.equals(now)) {
            return "Tuần này";
        } else if (startDate.equals(startOfMonth) && endDate.equals(now)) {
            return "Tháng này";
        } else if (startDate.equals(startOfYear) && endDate.equals(now)) {
            return "Năm nay";
        } else if (startDate.getMonthValue() == now.getMonthValue() - 1) {
            return "Tháng trước";
        } else {
            return startDate + " đến " + endDate;
        }
    }
}
