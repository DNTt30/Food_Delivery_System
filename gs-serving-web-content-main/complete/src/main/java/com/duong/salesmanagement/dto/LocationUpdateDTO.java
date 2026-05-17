package com.duong.salesmanagement.dto;

public class LocationUpdateDTO {
    private Long orderId;
    private Double latitude;
    private Double longitude;

    public LocationUpdateDTO() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
