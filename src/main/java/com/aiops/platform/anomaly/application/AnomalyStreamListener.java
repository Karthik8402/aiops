package com.aiops.platform.anomaly.application;

import com.aiops.platform.anomaly.ai.AnomalyDetector;
import com.aiops.platform.anomaly.domain.AnomalyEvent;
import com.aiops.platform.ingestion.domain.LogEvent;
import com.aiops.platform.ingestion.domain.MetricEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Consumer;

@Component
public class AnomalyStreamListener {

    private final FeatureWindowBuffer windowBuffer;
    private final AnomalyDetector detector;
    private final StreamBridge streamBridge;

    public AnomalyStreamListener(FeatureWindowBuffer windowBuffer,
                                  AnomalyDetector detector,
                                  StreamBridge streamBridge) {
        this.windowBuffer = windowBuffer;
        this.detector     = detector;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<LogEvent> logsConsumer() {
        return windowBuffer::recordLog;
    }

    @Bean
    public Consumer<MetricEvent> metricsConsumer() {
        return event -> {
            windowBuffer.recordMetric(event);
            double[] features = windowBuffer.extractFeatures(
                event.serviceId(), event.metricName());

            if (features == null) return; // window not yet full enough

            double score = detector.score(features);

            if (score >= detector.threshold()) {
                streamBridge.send("anomalyProducer-out-0", new AnomalyEvent(
                    event.serviceId(),
                    event.metricName(),
                    score,
                    detector.methodName(),
                    severityFor(score),
                    Instant.now()
                ));
                windowBuffer.resetErrorCount(event.serviceId());
            }
        };
    }

    private static String severityFor(double score) {
        if (score >= 0.90) return "CRITICAL";
        if (score >= 0.75) return "HIGH";
        if (score >= 0.60) return "MEDIUM";
        return "LOW";
    }
}
