-- AIOps Log & Metrics Anomaly Detection Platform
-- PostgreSQL schema

-- Users & roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- ADMIN, OPERATOR, VIEWER
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role_id);

-- What's being monitored
CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id),
    name VARCHAR(150) NOT NULL,
    environment VARCHAR(50) NOT NULL DEFAULT 'production',
    UNIQUE(application_id, name)
);
CREATE INDEX IF NOT EXISTS idx_services_app ON services(application_id);

-- Logs: a sampled/recent slice, not the full Kafka firehose
CREATE TABLE IF NOT EXISTS logs (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id),
    log_level VARCHAR(20) NOT NULL, -- INFO / WARN / ERROR
    message TEXT NOT NULL,
    raw_payload JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_logs_service_time ON logs(service_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_logs_level ON logs(log_level);

-- Metrics (time-series)
CREATE TABLE IF NOT EXISTS metrics (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id),
    metric_name VARCHAR(100) NOT NULL, -- cpu_usage, latency_p99, error_rate
    metric_value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_metrics_service_name_time ON metrics(service_id, metric_name, recorded_at DESC);

-- Anomalies: AI engine output
CREATE TABLE IF NOT EXISTS anomalies (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id),
    metric_name VARCHAR(100),
    anomaly_score DOUBLE PRECISION NOT NULL,
    detection_method VARCHAR(50) NOT NULL, -- ONE_CLASS_SVM, ZSCORE_WELFORD, etc.
    severity VARCHAR(20) NOT NULL, -- LOW / MEDIUM / HIGH / CRITICAL
    raw_features JSONB,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_anomalies_service_time ON anomalies(service_id, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_anomalies_severity ON anomalies(severity);

-- Incidents: correlated anomalies
CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id),
    title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN / ACKNOWLEDGED / RESOLVED
    severity VARCHAR(20) NOT NULL,
    root_cause_summary TEXT,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_service ON incidents(service_id);

CREATE TABLE IF NOT EXISTS incident_anomalies (
    incident_id BIGINT NOT NULL REFERENCES incidents(id),
    anomaly_id BIGINT NOT NULL REFERENCES anomalies(id),
    PRIMARY KEY (incident_id, anomaly_id)
);

-- Alerts: notifications sent for an incident
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents(id),
    channel VARCHAR(50) NOT NULL DEFAULT 'DASHBOARD', -- DASHBOARD / EMAIL / WEBHOOK
    message TEXT NOT NULL,
    acknowledged BOOLEAN NOT NULL DEFAULT false,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_alerts_incident ON alerts(incident_id);

-- Recommendations
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents(id),
    recommendation_text TEXT NOT NULL,
    confidence_score DOUBLE PRECISION,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_recommendations_incident ON recommendations(incident_id);

-- Audit logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
