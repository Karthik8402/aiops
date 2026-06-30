package com.aiops.platform.incident.application;

import com.aiops.platform.anomaly.domain.Anomaly;
import com.aiops.platform.anomaly.domain.AnomalyEvent;
import com.aiops.platform.anomaly.infrastructure.AnomalyRepository;
import com.aiops.platform.incident.domain.Incident;
import com.aiops.platform.incident.domain.IncidentEvent;
import com.aiops.platform.incident.infrastructure.IncidentRepository;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

@Component
public class IncidentStreamListener {

    private static final Duration CORRELATION_WINDOW = Duration.ofMinutes(5);

    private final IncidentRepository incidentRepository;
    private final AnomalyRepository anomalyRepository;
    private final StreamBridge streamBridge;

    public IncidentStreamListener(
        IncidentRepository incidentRepository,
        AnomalyRepository anomalyRepository,
        StreamBridge streamBridge
    ) {
        this.incidentRepository = incidentRepository;
        this.anomalyRepository = anomalyRepository;
        this.streamBridge = streamBridge;
    }

    @Bean
    @SuppressWarnings("null")
    public Consumer<AnomalyEvent> anomalyConsumer() {
        return event -> {
            // 1. Save anomaly first
            Anomaly anomaly = new Anomaly();
            anomaly.setServiceId(event.serviceId());
            anomaly.setMetricName(event.metricName());
            anomaly.setAnomalyScore(event.anomalyScore());
            anomaly.setDetectionMethod(event.detectionMethod());
            anomaly.setSeverity(event.severity());
            anomaly.setRawFeatures("[]");
            anomaly.setDetectedAt(event.detectedAt());
            anomaly = anomalyRepository.save(anomaly);

            // 2. Fetch or create incident within 5 minutes correlation window
            Anomaly finalAnomaly = anomaly;
            var existing = incidentRepository
                .findOpenByServiceWithinWindow(event.serviceId(), CORRELATION_WINDOW);

            boolean isNew;
            Incident incident;
            if (existing.isPresent()) {
                incident = existing.get();
                isNew = false;
            } else {
                incident = incidentRepository.save(Incident.openFrom(event));
                isNew = true;
            }

            // 3. Link anomaly
            incident.getAnomalies().add(finalAnomaly);
            incidentRepository.save(incident);

            // 4. Publish incident correlation event only for new incidents
            if (isNew) {
                streamBridge.send("incidentProducer-out-0", new IncidentEvent(
                    incident.getId(),
                    incident.getServiceId(),
                    incident.getTitle(),
                    incident.getSeverity(),
                    Instant.now()
                ));
            }
        };
    }
}
