package com.duong.salesmanagement.dto;

public class FoodReviewDTO {
    public Long id;
    public Long menuItemId;
    public String customerName;
    public Integer rating;
    public String comment;
    public String createdAt;

    public String ratingLevel;

    public FoodReviewDTO() {}

    public FoodReviewDTO(Long id, Long menuItemId, String customerName, Integer rating, String comment, String createdAt, String ratingLevel) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.ratingLevel = ratingLevel;
    }
}
