package com.aiops.platform.ingestion.infrastructure;

import com.aiops.platform.ingestion.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByName(String name);
}
