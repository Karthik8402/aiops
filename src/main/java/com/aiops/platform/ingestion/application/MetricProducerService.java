package com.aiops.platform.ingestion.application;

import com.aiops.platform.ingestion.domain.MetricEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class MetricProducerService {

    private final StreamBridge streamBridge;

    public MetricProducerService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publish(MetricEvent event) {
        streamBridge.send("metricProducer-out-0", event);
    }
}
