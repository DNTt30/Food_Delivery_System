package com.duong.salesmanagement.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "driver_profiles")
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String licensePlate;
    private String phoneNumber;
    private boolean isAvailable;

    @OneToMany(mappedBy = "driver", fetch = FetchType.LAZY)
    private List<FoodOrder> orders;

    public DriverProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public List<FoodOrder> getOrders() { return orders; }
    public void setOrders(List<FoodOrder> orders) { this.orders = orders; }
}
