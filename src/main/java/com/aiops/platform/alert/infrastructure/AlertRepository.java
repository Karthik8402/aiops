package com.aiops.platform.alert.infrastructure;

import com.aiops.platform.alert.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByAcknowledged(boolean acknowledged, Pageable pageable);
}
