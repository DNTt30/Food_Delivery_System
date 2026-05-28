package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(FoodOrder order);
}
