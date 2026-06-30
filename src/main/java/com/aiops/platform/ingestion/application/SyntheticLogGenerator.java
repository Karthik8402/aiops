package com.aiops.platform.ingestion.application;

import com.aiops.platform.ingestion.domain.LogEvent;
import com.aiops.platform.ingestion.domain.MetricEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Profile("demo")
@Component
public class SyntheticLogGenerator {

    private final LogProducerService logProducer;
    private final MetricProducerService metricProducer;

    private static final List<Long> SERVICE_IDS = List.of(1L, 2L, 3L);
    private static final List<String> METRIC_NAMES = List.of("latency_p99", "cpu_usage", "error_rate", "throughput");

    private final Random rng = new Random();

    public SyntheticLogGenerator(LogProducerService logProducer, MetricProducerService metricProducer) {
        this.logProducer = logProducer;
        this.metricProducer = metricProducer;
    }

    @Scheduled(fixedRate = 500)
    public void emitNormal() {
        Long svcId = SERVICE_IDS.get(rng.nextInt(SERVICE_IDS.size()));
        String metric = METRIC_NAMES.get(rng.nextInt(METRIC_NAMES.size()));

        metricProducer.publish(new MetricEvent(
            svcId,
            metric,
            50 + rng.nextGaussian() * 10,  // realistic normal range
            Instant.now()
        ));

        logProducer.publish(new LogEvent(
            svcId,
            rng.nextDouble() > 0.9 ? "WARN" : "INFO", // 10% WARN
            "Service " + svcId + " processed request successfully",
            Instant.now()
        ));
    }

    @Scheduled(initialDelay = 15000, fixedRate = 60000)
    public void emitAnomaly() {
        // Spike latency 5x normal to trigger anomaly detection
        Long svcId = SERVICE_IDS.get(rng.nextInt(SERVICE_IDS.size()));
        for (int i = 0; i < 5; i++) {
            metricProducer.publish(new MetricEvent(
                svcId,
                "latency_p99",
                250 + rng.nextGaussian() * 20,  // 5x normal
                Instant.now()
            ));
            logProducer.publish(new LogEvent(
                svcId,
                "ERROR",
                "CRITICAL: latency spike detected on service " + svcId,
                Instant.now()
            ));
        }
    }
}
