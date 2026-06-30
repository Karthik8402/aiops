package com.aiops.platform.incident.application;

import com.aiops.platform.anomaly.domain.Anomaly;
import com.aiops.platform.anomaly.domain.AnomalyEvent;
import com.aiops.platform.anomaly.infrastructure.AnomalyRepository;
import com.aiops.platform.incident.domain.Incident;
import com.aiops.platform.incident.domain.IncidentEvent;
import com.aiops.platform.incident.infrastructure.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentStreamListenerTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private AnomalyRepository anomalyRepository;
    @Mock private StreamBridge streamBridge;

    @Captor private ArgumentCaptor<IncidentEvent> eventCaptor;

    private IncidentStreamListener listener;

    @BeforeEach
    void setUp() {
        listener = new IncidentStreamListener(incidentRepository, anomalyRepository, streamBridge);
    }

    @Test
    void shouldPublishEventForNewIncident() {
        when(anomalyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(incidentRepository.findOpenByServiceWithinWindow(eq(1L), any())).thenReturn(Optional.empty());
        when(incidentRepository.save(any())).thenAnswer(i -> {
            Incident inc = i.getArgument(0);
            inc.setId(42L);
            inc.setAnomalies(new HashSet<>());
            return inc;
        });

        AnomalyEvent event = new AnomalyEvent(
            1L, "latency_p99", 0.95, "zscore", "CRITICAL", Instant.now()
        );

        listener.anomalyConsumer().accept(event);

        verify(streamBridge, times(1)).send(eq("incidentProducer-out-0"), eventCaptor.capture());
        IncidentEvent published = eventCaptor.getValue();
        assertEquals(42L, published.incidentId());
        assertEquals(1L, published.serviceId());
    }

    @Test
    void shouldNotPublishEventForExistingIncident() {
        when(anomalyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Incident existing = new Incident();
        existing.setId(1L);
        existing.setStatus("OPEN");
        existing.setAnomalies(new HashSet<>());
        when(incidentRepository.findOpenByServiceWithinWindow(eq(1L), any()))
            .thenReturn(Optional.of(existing));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AnomalyEvent event = new AnomalyEvent(
            1L, "latency_p99", 0.95, "zscore", "CRITICAL", Instant.now()
        );

        listener.anomalyConsumer().accept(event);

        verify(streamBridge, never()).send(anyString(), any());
    }

    @Test
    void shouldLinkAnomalyToIncident() {
        when(anomalyRepository.save(any())).thenAnswer(i -> {
            Anomaly a = i.getArgument(0);
            a.setId(99L);
            return a;
        });

        Incident existing = new Incident();
        existing.setId(1L);
        existing.setStatus("OPEN");
        existing.setAnomalies(new HashSet<>());
        when(incidentRepository.findOpenByServiceWithinWindow(eq(1L), any()))
            .thenReturn(Optional.of(existing));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AnomalyEvent event = new AnomalyEvent(
            1L, "latency_p99", 0.95, "zscore", "CRITICAL", Instant.now()
        );

        listener.anomalyConsumer().accept(event);

        verify(incidentRepository, times(1)).save(argThat(inc ->
            inc.getAnomalies().stream().anyMatch(a -> a.getId() == 99L)
        ));
    }
}
