package com.riskmonitor.repository;

import com.riskmonitor.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE e.supplier.id = :supplierId ORDER BY e.timestamp DESC")
    List<Event> findBySupplierIdOrderByTimestampDesc(@Param("supplierId") UUID supplierId);
}
