package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.Voucher;
import com.duong.salesmanagement.model.RestaurantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    List<Voucher> findByIsActiveTrue();
    List<Voucher> findByRestaurant(RestaurantProfile restaurant);
    List<Voucher> findByRestaurantIsNull();

    @org.springframework.data.jpa.repository.Query("SELECT v FROM Voucher v WHERE v.isActive = true AND (v.expirationDate IS NULL OR v.expirationDate >= :currentDate) AND (v.restaurant IS NULL OR v.restaurant.id = :restaurantId)")
    List<Voucher> findAvailableVouchers(@org.springframework.data.repository.query.Param("restaurantId") Long restaurantId, @org.springframework.data.repository.query.Param("currentDate") java.time.LocalDate currentDate);

    @org.springframework.data.jpa.repository.Query("SELECT v FROM Voucher v WHERE v.isActive = true AND (v.expirationDate IS NULL OR v.expirationDate >= :currentDate) AND v.restaurant IS NULL")
    List<Voucher> findGlobalAvailableVouchers(@org.springframework.data.repository.query.Param("currentDate") java.time.LocalDate currentDate);
}
