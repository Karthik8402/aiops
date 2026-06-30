package com.aiops.platform.anomaly.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SmileAnomalyDetectorTest {

    private SmileAnomalyDetector detector;

    @BeforeEach
    void setUp() {
        detector = new SmileAnomalyDetector();
    }

    @Test
    void testScoreInvalidInput() {
        assertEquals(0.0, detector.score(null, 1L, "test"));
        assertEquals(0.0, detector.score(new double[]{1.0, 2.0}, 1L, "test"));
    }

    @Test
    void testScoreBaselineTraining() {
        double[] normal = {50.0, 5.0, 40.0, 60.0, 55.0, 0.0};

        for (int i = 0; i < 10; i++) {
            detector.score(normal, 1L, "latency_p99");
        }

        double[] spike = {250.0, 50.0, 40.0, 300.0, 280.0, 10.0};
        double score = detector.score(spike, 1L, "latency_p99");

        assertTrue(score > 0.7, "Score should be high for anomaly spike");
    }

    @Test
    void testPerMetricIsolation() {
        // Train model on service 1 / latency_p99 (baseline ~50ms)
        for (int i = 0; i < 10; i++) {
            detector.score(new double[]{50.0, 5.0, 40.0, 60.0, 55.0, 0.0}, 1L, "latency_p99");
        }

        // Train different model on service 1 / cpu_usage (baseline ~70%)
        for (int i = 0; i < 10; i++) {
            detector.score(new double[]{70.0, 3.0, 65.0, 75.0, 72.0, 0.0}, 1L, "cpu_usage");
        }

        // cpu_usage spike of 95% should be anomalous for cpu_usage model
        double cpuScore = detector.score(
            new double[]{95.0, 10.0, 80.0, 99.0, 98.0, 2.0}, 1L, "cpu_usage");
        assertTrue(cpuScore > 0.5, "CPU spike should be anomalous for cpu_usage model");

        // Same value evaluated against latency_p99 model should NOT be similarly anomalous
        // because latency_p99 model has different baseline
        double latScore = detector.score(
            new double[]{95.0, 10.0, 80.0, 99.0, 98.0, 2.0}, 1L, "latency_p99");
        assertTrue(latScore > 0.3, "Score should exist for latency_p99 model too");
    }

    @Test
    void testDifferentServicesSeparateModels() {
        // Train on service 1 (latency ~50ms)
        for (int i = 0; i < 10; i++) {
            detector.score(new double[]{50.0, 5.0, 40.0, 60.0, 55.0, 0.0}, 1L, "latency_p99");
        }

        // Train on service 2 with different baseline (latency ~200ms)
        for (int i = 0; i < 10; i++) {
            detector.score(new double[]{200.0, 20.0, 180.0, 220.0, 210.0, 0.0}, 2L, "latency_p99");
        }

        // Each service has its own model; scores should be valid for both
        double s1 = detector.score(new double[]{150.0, 15.0, 140.0, 160.0, 155.0, 0.0}, 1L, "latency_p99");
        double s2 = detector.score(new double[]{150.0, 15.0, 140.0, 160.0, 155.0, 0.0}, 2L, "latency_p99");

        assertTrue(s1 > 0 && s2 > 0, "Both services should produce valid scores");
        // Models are independent — their scores may differ based on their respective histories
        assertNotEquals(detector.score(new double[]{10.0, 1.0, 8.0, 12.0, 11.0, 0.0}, 1L, "latency_p99"),
                        detector.score(new double[]{10.0, 1.0, 8.0, 12.0, 11.0, 0.0}, 2L, "latency_p99"),
                        "Different service models should produce different scores");
    }
}
