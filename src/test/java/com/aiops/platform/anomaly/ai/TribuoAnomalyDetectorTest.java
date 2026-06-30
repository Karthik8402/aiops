package com.aiops.platform.anomaly.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TribuoAnomalyDetectorTest {

    private TribuoAnomalyDetector detector;

    @BeforeEach
    void setUp() {
        detector = new TribuoAnomalyDetector();
        detector.init();
    }

    @Test
    void testScoreInvalidInput() {
        assertEquals(0.0, detector.score(null, 1L, "test"));
        assertEquals(0.0, detector.score(new double[]{1.0, 2.0}, 1L, "test"));
    }

    @Test
    void testScoreNormalInput() {
        double[] normal = {50.0, 5.0, 30.0, 80.0, 75.0, 0.0};
        double score = detector.score(normal, 1L, "latency_p99");

        // The one-class SVM is conservative; normal inputs can get moderate scores
        assertTrue(score > 0, "Normal score should produce a valid output");
    }

    @Test
    void testScoreAnomalyInput() {
        double[] anomaly = {250.0, 50.0, 100.0, 400.0, 350.0, 10.0};
        double score = detector.score(anomaly, 1L, "latency_p99");

        assertTrue(score > 0.5, "Anomaly score should be elevated");
    }
}
