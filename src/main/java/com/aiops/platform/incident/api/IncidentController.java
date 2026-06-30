package com.aiops.platform.incident.api;

import com.aiops.platform.common.dto.PageResponse;
import com.aiops.platform.common.exception.ApiException;
import com.aiops.platform.incident.domain.Incident;
import com.aiops.platform.incident.dto.IncidentResponse;
import com.aiops.platform.incident.dto.IncidentStatusUpdateRequest;
import com.aiops.platform.incident.infrastructure.IncidentRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;

    public IncidentController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncidentResponse>> getIncidents(
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "openedAt"));
        Page<Incident> incidents = incidentRepository.findByStatus(status, pageable);

        Page<IncidentResponse> responsePage = incidents.map(this::mapToResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable @NonNull Long id) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Incident not found: " + id));
        return ResponseEntity.ok(mapToResponse(incident));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncidentResponse> updateIncidentStatus(
        @PathVariable @NonNull Long id,
        @Valid @RequestBody IncidentStatusUpdateRequest request
    ) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Incident not found: " + id));

        String oldStatus = incident.getStatus();
        String newStatus = request.status().toUpperCase();

        if (!List.of("OPEN", "ACKNOWLEDGED", "RESOLVED").contains(newStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status: " + newStatus);
        }

        // Enforce transition rules
        if ("RESOLVED".equals(oldStatus) && ("OPEN".equals(newStatus) || "ACKNOWLEDGED".equals(newStatus))) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot move RESOLVED incident back to " + newStatus);
        }

        incident.setStatus(newStatus);
        if ("RESOLVED".equals(newStatus)) {
            incident.setResolvedAt(Instant.now());
        }

        Incident saved = incidentRepository.save(incident);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    private IncidentResponse mapToResponse(Incident i) {
        return new IncidentResponse(
            i.getId(),
            i.getServiceId(),
            i.getTitle(),
            i.getStatus(),
            i.getSeverity(),
            i.getRootCauseSummary(),
            i.getOpenedAt(),
            i.getResolvedAt(),
            i.getAnomalies().stream().map(a -> a.getId()).toList()
        );
    }
}
