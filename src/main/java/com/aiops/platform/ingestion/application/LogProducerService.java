package com.aiops.platform.ingestion.application;

import com.aiops.platform.ingestion.domain.LogEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class LogProducerService {

    private final StreamBridge streamBridge;

    public LogProducerService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publish(LogEvent event) {
        streamBridge.send("logProducer-out-0", event);
    }
}
