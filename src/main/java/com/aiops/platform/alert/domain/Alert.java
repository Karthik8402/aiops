package com.aiops.platform.alert.domain;

import com.aiops.platform.incident.domain.IncidentEvent;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(nullable = false, length = 50)
    private String channel = "DASHBOARD";

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    private boolean acknowledged = false;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    public Alert() {}

    public static Alert dashboardAlertFor(IncidentEvent incident, String message) {
        Alert alert = new Alert();
        alert.setIncidentId(incident.incidentId());
        alert.setChannel("DASHBOARD");
        alert.setMessage(message);
        alert.setAcknowledged(false);
        alert.setSentAt(Instant.now());
        return alert;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
