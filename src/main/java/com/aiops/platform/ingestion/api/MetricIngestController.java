package com.aiops.platform.ingestion.api;

import com.aiops.platform.ingestion.application.MetricProducerService;
import com.aiops.platform.ingestion.domain.MetricEvent;
import com.aiops.platform.ingestion.dto.MetricIngestRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
public class MetricIngestController {

    private final MetricProducerService metricProducer;

    public MetricIngestController(MetricProducerService metricProducer) {
        this.metricProducer = metricProducer;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> ingestMetric(@Valid @RequestBody MetricIngestRequest request) {
        metricProducer.publish(new MetricEvent(
            request.serviceId(),
            request.metricName(),
            request.metricValue(),
            request.recordedAt()
        ));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
            "status", "queued",
            "topic", "metrics-topic"
        ));
    }
}
