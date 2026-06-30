package com.aiops.platform.alert.application;

import com.aiops.platform.alert.domain.Alert;
import com.aiops.platform.alert.domain.AlertEvent;
import com.aiops.platform.alert.infrastructure.AlertRepository;
import com.aiops.platform.incident.domain.IncidentEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Consumer;

@Component
public class AlertStreamListener {

    private final AlertRepository alertRepository;
    private final StreamBridge streamBridge;

    public AlertStreamListener(AlertRepository alertRepository, StreamBridge streamBridge) {
        this.alertRepository = alertRepository;
        this.streamBridge = streamBridge;
    }

    @Bean
    @SuppressWarnings("null")
    public Consumer<IncidentEvent> incidentConsumerForAlert() {
        return incident -> {
            String message = "%s severity incident opened on service %d: %s"
                .formatted(incident.severity(), incident.serviceId(), incident.title());

            Alert alert = alertRepository.save(Alert.dashboardAlertFor(incident, message));

            streamBridge.send("alertProducer-out-0",
                new AlertEvent(alert.getId(), incident.incidentId(), message, Instant.now()));
        };
    }
}
