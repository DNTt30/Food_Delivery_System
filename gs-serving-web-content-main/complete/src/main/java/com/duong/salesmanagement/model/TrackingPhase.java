package com.duong.salesmanagement.model;

public enum TrackingPhase {
    DRIVER_ACCEPTED,       // Tài xế vừa nhận đơn
    GOING_TO_RESTAURANT,   // Đang đi đến nhà hàng
    WAITING_AT_RESTAURANT, // Đang chờ món tại nhà hàng
    DELIVERING,            // Đã lấy hàng, đang trên đường giao
    ARRIVED                // Đã đến nơi / Hoàn thành
}
