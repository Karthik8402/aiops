package com.aiops.platform.anomaly.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmileAnomalyDetector implements AnomalyDetector {

    @Value("${aiops.anomaly.zscore.threshold:0.65}")
    private double threshold;

    private static final int FEATURE_DIM = 6;
    private final double[] mean = new double[FEATURE_DIM];
    private final double[] m2 = new double[FEATURE_DIM];
    private long count = 0;

    @Override
    public synchronized double score(double[] features) {
        if (features == null || features.length != FEATURE_DIM) {
            return 0.0;
        }

        count++;
        double maxZ = 0.0;

        for (int i = 0; i < FEATURE_DIM; i++) {
            double delta = features[i] - mean[i];
            mean[i] += delta / count;
            double delta2 = features[i] - mean[i];
            m2[i] += delta * delta2;

            if (count > 1) {
                double variance = m2[i] / (count - 1);
                double stddev = Math.sqrt(variance);
                if (stddev > 0) {
                    double z = Math.abs(features[i] - mean[i]) / stddev;
                    if (z > maxZ) {
                        maxZ = z;
                    }
                }
            }
        }

        // Sigmoid of max Z-score: Z=0 -> 0.5, Z=3 -> 0.95, Z=5 -> 0.99
        return 1.0 / (1.0 + Math.exp(-maxZ + 2));
    }

    @Override
    public double threshold() {
        return threshold;
    }

    @Override
    public String methodName() {
        return "ZSCORE_WELFORD";
    }
}
