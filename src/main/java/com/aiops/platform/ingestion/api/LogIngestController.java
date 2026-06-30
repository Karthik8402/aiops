package com.aiops.platform.ingestion.api;

import com.aiops.platform.ingestion.application.LogProducerService;
import com.aiops.platform.ingestion.domain.LogEvent;
import com.aiops.platform.ingestion.dto.LogIngestRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogIngestController {

    private final LogProducerService logProducer;

    public LogIngestController(LogProducerService logProducer) {
        this.logProducer = logProducer;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> ingestLog(@Valid @RequestBody LogIngestRequest request) {
        logProducer.publish(new LogEvent(
            request.serviceId(),
            request.logLevel(),
            request.message(),
            request.occurredAt()
        ));
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
            "status", "queued",
            "topic", "logs-topic"
        ));
    }
}
