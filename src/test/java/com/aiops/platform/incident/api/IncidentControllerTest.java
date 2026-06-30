package com.aiops.platform.incident.api;

import com.aiops.platform.common.exception.ApiException;
import com.aiops.platform.incident.domain.Incident;
import com.aiops.platform.incident.dto.IncidentStatusUpdateRequest;
import com.aiops.platform.incident.infrastructure.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentControllerTest {

    @Mock private IncidentRepository incidentRepository;

    private IncidentController controller;

    @BeforeEach
    void setUp() {
        controller = new IncidentController(incidentRepository);
    }

    @Test
    void shouldRejectAcknowledgedToOpen() {
        Incident incident = createIncident(1L, "ACKNOWLEDGED");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        ApiException ex = assertThrows(ApiException.class,
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("OPEN")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void shouldRejectResolvedToOpen() {
        Incident incident = createIncident(1L, "RESOLVED");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        ApiException ex = assertThrows(ApiException.class,
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("OPEN")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void shouldRejectResolvedToAcknowledged() {
        Incident incident = createIncident(1L, "RESOLVED");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        ApiException ex = assertThrows(ApiException.class,
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("ACKNOWLEDGED")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void shouldAllowOpenToAcknowledged() {
        Incident incident = createIncident(1L, "OPEN");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("ACKNOWLEDGED")));

        assertEquals("ACKNOWLEDGED", incident.getStatus());
    }

    @Test
    void shouldAllowOpenToResolved() {
        Incident incident = createIncident(1L, "OPEN");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("RESOLVED")));

        assertEquals("RESOLVED", incident.getStatus());
    }

    @Test
    void shouldAllowAcknowledgedToResolved() {
        Incident incident = createIncident(1L, "ACKNOWLEDGED");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("RESOLVED")));

        assertEquals("RESOLVED", incident.getStatus());
    }

    @Test
    void shouldRejectInvalidStatus() {
        Incident incident = createIncident(1L, "OPEN");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        ApiException ex = assertThrows(ApiException.class,
            () -> controller.updateIncidentStatus(1L, new IncidentStatusUpdateRequest("INVALID")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldReturn404ForMissingIncident() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
            () -> controller.updateIncidentStatus(99L, new IncidentStatusUpdateRequest("RESOLVED")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    private static Incident createIncident(Long id, String status) {
        Incident inc = new Incident();
        inc.setId(id);
        inc.setStatus(status);
        inc.setAnomalies(new HashSet<>());
        return inc;
    }
}
