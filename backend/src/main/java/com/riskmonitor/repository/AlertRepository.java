package com.riskmonitor.repository;

import com.riskmonitor.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    @Query("SELECT a FROM Alert a WHERE a.supplier.id = :supplierId ORDER BY a.timestamp DESC")
    List<Alert> findBySupplierIdOrderByTimestampDesc(@Param("supplierId") UUID supplierId);
    
    @Query("SELECT a FROM Alert a WHERE a.active = true AND a.acknowledged = false ORDER BY a.timestamp DESC")
    List<Alert> findActiveUnacknowledged();
}
