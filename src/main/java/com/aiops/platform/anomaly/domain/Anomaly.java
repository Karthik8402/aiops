package com.aiops.platform.anomaly.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "anomalies")
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "metric_name", length = 100)
    private String metricName;

    @Column(name = "anomaly_score", nullable = false)
    private double anomalyScore;

    @Column(name = "detection_method", nullable = false, length = 50)
    private String detectionMethod;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "raw_features", columnDefinition = "jsonb")
    private String rawFeatures;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    public Anomaly() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRawFeatures() {
        return rawFeatures;
    }

    public void setRawFeatures(String rawFeatures) {
        this.rawFeatures = rawFeatures;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }
}
