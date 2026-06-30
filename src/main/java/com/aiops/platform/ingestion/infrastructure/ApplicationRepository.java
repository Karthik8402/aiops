package com.aiops.platform.ingestion.infrastructure;

import com.aiops.platform.ingestion.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByName(String name);
}
