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
        assertEquals(0.0, detector.score(null));
        assertEquals(0.0, detector.score(new double[]{1.0, 2.0}));
    }

    @Test
    void testScoreBaselineTraining() {
        // Feed normal features (all close to baseline)
        double[] normal = {50.0, 5.0, 40.0, 60.0, 55.0, 0.0};
        
        // Feed multiple samples to establish running mean/variance
        for (int i = 0; i < 10; i++) {
            detector.score(normal);
        }

        // Now feed an anomaly spike
        double[] spike = {250.0, 50.0, 40.0, 300.0, 280.0, 10.0};
        double score = detector.score(spike);

        // The anomaly score should be high (above threshold of 0.6)
        assertTrue(score > 0.7, "Score should be high for anomaly spike");
    }
}
