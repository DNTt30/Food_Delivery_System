package com.duong.salesmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duong.salesmanagement.model.CustomerProfile;
import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(FoodOrder order);
    List<Payment> findByOrder_Customer(CustomerProfile customer);
}
