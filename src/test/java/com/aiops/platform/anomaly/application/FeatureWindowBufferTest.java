package com.aiops.platform.anomaly.application;

import com.aiops.platform.ingestion.domain.LogEvent;
import com.aiops.platform.ingestion.domain.MetricEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FeatureWindowBufferTest {

    private FeatureWindowBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new FeatureWindowBuffer();
    }

    @Test
    void testExtractFeaturesEmptyWindow() {
        double[] features = buffer.extractFeatures(1L, "latency_p99");
        assertNull(features);
    }

    @Test
    void testExtractFeaturesPartialWindow() {
        for (int i = 0; i < 5; i++) {
            buffer.recordMetric(new MetricEvent(1L, "latency_p99", 50.0, Instant.now()));
        }
        double[] features = buffer.extractFeatures(1L, "latency_p99");
        // Window size is 20, half is 10. 5 points is not enough.
        assertNull(features);
    }

    @Test
    void testExtractFeaturesFullWindow() {
        for (int i = 0; i < 12; i++) {
            buffer.recordMetric(new MetricEvent(1L, "latency_p99", 50.0 + i, Instant.now()));
        }
        buffer.recordLog(new LogEvent(1L, "ERROR", "Something broke", Instant.now()));
        buffer.recordLog(new LogEvent(1L, "INFO", "Success", Instant.now()));
        buffer.recordLog(new LogEvent(1L, "ERROR", "Something else broke", Instant.now()));

        double[] features = buffer.extractFeatures(1L, "latency_p99");
        assertNotNull(features);
        assertEquals(6, features.length);

        // Features order: [mean, stddev, min, max, p95, errorCount]
        double mean = features[0];
        double stddev = features[1];
        double min = features[2];
        double max = features[3];
        double p95 = features[4];
        double errors = features[5];

        assertTrue(mean >= 50.0 && mean <= 62.0);
        assertTrue(stddev > 0.0);
        assertEquals(50.0, min);
        assertEquals(61.0, max);
        assertEquals(61.0, p95);
        assertEquals(2.0, errors);
    }

    @Test
    void testResetErrorCount() {
        buffer.recordLog(new LogEvent(1L, "ERROR", "Error 1", Instant.now()));
        buffer.resetErrorCount(1L);
        
        for (int i = 0; i < 10; i++) {
            buffer.recordMetric(new MetricEvent(1L, "latency_p99", 50.0, Instant.now()));
        }
        
        double[] features = buffer.extractFeatures(1L, "latency_p99");
        assertNotNull(features);
        assertEquals(0.0, features[5]); // error count should be reset
    }
}
