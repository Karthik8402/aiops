package com.aiops.platform.dashboard.websocket;

import com.aiops.platform.alert.domain.AlertEvent;
import com.aiops.platform.incident.domain.IncidentEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class DashboardBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public DashboardBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Bean
    public Consumer<IncidentEvent> incidentConsumerForDashboard() {
        return incident -> {
            if (incident != null) {
                messagingTemplate.convertAndSend("/topic/incidents", incident);
            }
        };
    }

    @Bean
    public Consumer<AlertEvent> alertConsumerForDashboard() {
        return alert -> {
            if (alert != null) {
                messagingTemplate.convertAndSend("/topic/alerts", alert);
            }
        };
    }
}
