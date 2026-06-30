package com.aiops.platform.anomaly.api;

import com.aiops.platform.anomaly.domain.Anomaly;
import com.aiops.platform.anomaly.dto.AnomalyResponse;
import com.aiops.platform.anomaly.infrastructure.AnomalyRepository;
import com.aiops.platform.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalyController {

    private final AnomalyRepository anomalyRepository;

    public AnomalyController(AnomalyRepository anomalyRepository) {
        this.anomalyRepository = anomalyRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AnomalyResponse>> getAnomalies(
        @RequestParam(required = false) Long serviceId,
        @RequestParam(required = false) String severity,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectedAt"));
        Page<Anomaly> anomalies = anomalyRepository.findByFilters(serviceId, severity, pageable);

        Page<AnomalyResponse> responsePage = anomalies.map(a -> new AnomalyResponse(
            a.getId(),
            a.getServiceId(),
            a.getMetricName(),
            a.getAnomalyScore(),
            a.getDetectionMethod(),
            a.getSeverity(),
            a.getDetectedAt()
        ));

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }
}
