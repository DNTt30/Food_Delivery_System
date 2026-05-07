package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.OrderItem;
import com.duong.salesmanagement.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(FoodOrder order);
}
