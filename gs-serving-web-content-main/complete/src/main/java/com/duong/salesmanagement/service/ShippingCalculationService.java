package com.duong.salesmanagement.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ShippingCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double BASE_FEE = 15000.0;
    private static final double EXTRA_KM_FEE = 5000.0;
    private static final double MAX_FEE = 75000.0;
    private static final int BASE_KM = 3;

    /**
     * Tính phí ship dựa trên khoảng cách
     * Logic: 15k cho 3km đầu, +5k mỗi km tiếp theo, max 75k.
     */
    public double calculateShippingFee(double distanceKm) {
        if (distanceKm <= BASE_KM) {
            return BASE_FEE;
        }
        
        double extraKm = Math.ceil(distanceKm - BASE_KM);
        double fee = BASE_FEE + (extraKm * EXTRA_KM_FEE);
        
        return Math.min(fee, MAX_FEE);
    }

    /**
     * Ước tính thời gian giao hàng (ETA)
     * Logic: 15p nấu + 2p/km + 5p dự phòng
     */
    public LocalDateTime estimateETA(double distanceKm) {
        int preparationTime = 15;
        int travelTime = (int) Math.ceil(distanceKm * 2);
        int bufferTime = 5;
        
        int totalMinutes = preparationTime + travelTime + bufferTime;
        return LocalDateTime.now().plusMinutes(totalMinutes);
    }

    /**
     * Tính khoảng cách Haversine giữa 2 điểm tọa độ
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
