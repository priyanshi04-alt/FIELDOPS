package com.fieldops.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<OperationalAlert, Long> {

    List<OperationalAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    @Query("SELECT a FROM OperationalAlert a WHERE a.status IN ('OPEN', 'ACKNOWLEDGED') ORDER BY a.createdAt DESC")
    List<OperationalAlert> findActiveAlerts();

    @Query("SELECT a FROM OperationalAlert a WHERE a.fingerprint = :fingerprint AND a.status IN ('OPEN', 'ACKNOWLEDGED')")
    Optional<OperationalAlert> findActiveByFingerprint(@Param("fingerprint") String fingerprint);

    Optional<OperationalAlert> findFirstByAlertTypeAndRelatedEntityTypeAndRelatedEntityIdAndStatus(
            AlertType alertType, String relatedEntityType, Long relatedEntityId, AlertStatus status);

    List<OperationalAlert> findByIsPredictiveOrderByCreatedAtDesc(boolean isPredictive);

    @Query("SELECT COUNT(a) FROM OperationalAlert a WHERE a.status = 'OPEN'")
    long countOpenAlerts();

    @Query("SELECT COUNT(a) FROM OperationalAlert a WHERE a.status IN ('OPEN', 'ACKNOWLEDGED')")
    long countActiveAlerts();

    List<OperationalAlert> findTop50ByOrderByCreatedAtDesc();
}
