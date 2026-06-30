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
        assertEquals(0.0, detector.score(null));
        assertEquals(0.0, detector.score(new double[]{1.0, 2.0}));
    }

    @Test
    void testScoreNormalInput() {
        // Normal features (mean latency ~50ms, low errors)
        double[] normal = {50.0, 5.0, 30.0, 80.0, 75.0, 0.0};
        double score = detector.score(normal);

        // A normal score should be below the threshold
        assertTrue(score < 0.6, "Normal score should be below threshold");
    }

    @Test
    void testScoreAnomalyInput() {
        // Anomaly features (massive latency spike, high errors)
        double[] anomaly = {250.0, 50.0, 100.0, 400.0, 350.0, 10.0};
        double score = detector.score(anomaly);

        // An anomaly score should be elevated
        assertTrue(score > 0.5, "Anomaly score should be elevated");
    }
}
