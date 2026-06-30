package com.aiops.platform.alert.api;

import com.aiops.platform.alert.domain.Alert;
import com.aiops.platform.alert.dto.AlertResponse;
import com.aiops.platform.alert.infrastructure.AlertRepository;
import com.aiops.platform.common.dto.PageResponse;
import com.aiops.platform.common.exception.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AlertResponse>> getAlerts(
        @RequestParam(defaultValue = "false") boolean acknowledged,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<Alert> alerts = alertRepository.findByAcknowledged(acknowledged, pageable);

        Page<AlertResponse> responsePage = alerts.map(a -> new AlertResponse(
            a.getId(),
            a.getIncidentId(),
            a.getChannel(),
            a.getMessage(),
            a.isAcknowledged(),
            a.getSentAt()
        ));

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @PatchMapping("/{id}/ack")
    public ResponseEntity<Map<String, Object>> acknowledgeAlert(@PathVariable @NonNull Long id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Alert not found: " + id));

        alert.setAcknowledged(true);
        alertRepository.save(alert);

        return ResponseEntity.ok(Map.of(
            "id", id,
            "acknowledged", true
        ));
    }
}
