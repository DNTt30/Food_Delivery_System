package com.duong.salesmanagement.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_orders")
public class FoodOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private DriverProfile driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private Double totalAmount;
    private LocalDateTime orderTime;
    private String deliveryAddress;
    
    // Geolocation Snapshots
    private String deliveryAddressSnapshot;
    private String restaurantAddressSnapshot;
    private Double deliveryLat;
    private Double deliveryLng;
    private Double restaurantLat;
    private Double restaurantLng;
    private LocalDateTime estimatedTimeOfArrival;
    private Double distance; // in KM
    private Double shippingFee;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    public FoodOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CustomerProfile getCustomer() { return customer; }
    public void setCustomer(CustomerProfile customer) { this.customer = customer; }
    public RestaurantProfile getRestaurant() { return restaurant; }
    public void setRestaurant(RestaurantProfile restaurant) { this.restaurant = restaurant; }
    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Double totalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getDeliveryAddressSnapshot() { return deliveryAddressSnapshot; }
    public void setDeliveryAddressSnapshot(String deliveryAddressSnapshot) { this.deliveryAddressSnapshot = deliveryAddressSnapshot; }
    public String getRestaurantAddressSnapshot() { return restaurantAddressSnapshot; }
    public void setRestaurantAddressSnapshot(String restaurantAddressSnapshot) { this.restaurantAddressSnapshot = restaurantAddressSnapshot; }
    public Double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(Double deliveryLat) { this.deliveryLat = deliveryLat; }
    public Double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(Double deliveryLng) { this.deliveryLng = deliveryLng; }
    public Double getRestaurantLat() { return restaurantLat; }
    public void setRestaurantLat(Double restaurantLat) { this.restaurantLat = restaurantLat; }
    public Double getRestaurantLng() { return restaurantLng; }
    public void setRestaurantLng(Double restaurantLng) { this.restaurantLng = restaurantLng; }
    public LocalDateTime getEstimatedTimeOfArrival() { return estimatedTimeOfArrival; }
    public void setEstimatedTimeOfArrival(LocalDateTime estimatedTimeOfArrival) { this.estimatedTimeOfArrival = estimatedTimeOfArrival; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getShippingFee() { return shippingFee; }
    public void setShippingFee(Double shippingFee) { this.shippingFee = shippingFee; }

    public String getPaymentMethod() {
    return paymentMethod;
}

public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
}

public String getPaymentStatus() {
    return paymentStatus;
}

public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
}

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}
