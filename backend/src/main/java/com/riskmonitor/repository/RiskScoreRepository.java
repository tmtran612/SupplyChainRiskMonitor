package com.riskmonitor.repository;

import com.riskmonitor.model.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {
    @Query("SELECT rs FROM RiskScore rs WHERE rs.supplier.id = :supplierId ORDER BY rs.timestamp DESC")
    List<RiskScore> findBySupplierIdOrderByTimestampDesc(@Param("supplierId") UUID supplierId);

    @Query("SELECT rs FROM RiskScore rs WHERE rs.timestamp = (SELECT MAX(rs2.timestamp) FROM RiskScore rs2 WHERE rs2.supplier.id = rs.supplier.id)")
    List<RiskScore> findLatestRiskScores();

    @Query("SELECT rs FROM RiskScore rs WHERE rs.supplier.id = :supplierId ORDER BY rs.timestamp DESC")
    List<RiskScore> findBySupplierIdJPQL(@Param("supplierId") UUID supplierId);
    
    default RiskScore findLatestForSupplier(UUID supplierId) {
        List<RiskScore> scores = findBySupplierIdJPQL(supplierId);
        return scores.isEmpty() ? null : scores.get(0);
    }

    default RiskScore findPreviousForSupplier(UUID supplierId) {
        List<RiskScore> scores = findBySupplierIdJPQL(supplierId);
        return scores.size() > 1 ? scores.get(1) : null;
    }
}
