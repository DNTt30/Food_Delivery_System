package com.duong.salesmanagement.model;

/**
 * Xác định luồng chat nào trong một đơn hàng:
 * - RESTAURANT : Customer ↔ Restaurant  (từ lúc đặt cho đến khi tài xế nhận)
 * - DRIVER     : Customer ↔ Driver      (từ lúc tài xế nhận đến khi giao xong)
 */
public enum ChatType {
    RESTAURANT,
    DRIVER
}
