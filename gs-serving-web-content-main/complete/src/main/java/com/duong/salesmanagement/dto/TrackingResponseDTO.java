package com.duong.salesmanagement.dto;

import com.duong.salesmanagement.model.TrackingPhase;
import java.time.LocalDateTime;

public class TrackingResponseDTO {
    private Long orderId;
    private Double latitude;
    private Double longitude;
    private TrackingPhase phase;
    private String driverName;
    private LocalDateTime timestamp;

    public TrackingResponseDTO() {
    }

    public TrackingResponseDTO(Long orderId, Double latitude, Double longitude, 
                               TrackingPhase phase, String driverName, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phase = phase;
        this.driverName = driverName;
        this.timestamp = timestamp;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public TrackingPhase getPhase() { return phase; }
    public void setPhase(TrackingPhase phase) { this.phase = phase; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
