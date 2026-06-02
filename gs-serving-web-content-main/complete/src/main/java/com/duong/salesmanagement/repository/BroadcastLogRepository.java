package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.BroadcastLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastLogRepository extends JpaRepository<BroadcastLog, Long> {
    List<BroadcastLog> findAllByOrderByCreatedAtDesc();
}
