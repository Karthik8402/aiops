package com.aiops.platform.anomaly.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SmileAnomalyDetector implements AnomalyDetector {

    @Value("${aiops.anomaly.zscore.threshold:0.65}")
    private double threshold;

    private static final int FEATURE_DIM = 6;

    private final Map<String, WelfordStats> statsByKey = new ConcurrentHashMap<>();

    private String key(Long serviceId, String metricName) {
        return serviceId + "|" + metricName;
    }

    @Override
    public double score(double[] features, Long serviceId, String metricName) {
        if (features == null || features.length != FEATURE_DIM) {
            return 0.0;
        }

        WelfordStats stats = statsByKey.computeIfAbsent(key(serviceId, metricName), k -> new WelfordStats());
        return stats.update(features);
    }

    @Override
    public double threshold() {
        return threshold;
    }

    @Override
    public String methodName() {
        return "ZSCORE_WELFORD";
    }

    static class WelfordStats {
        final double[] mean = new double[FEATURE_DIM];
        final double[] m2 = new double[FEATURE_DIM];
        long count = 0;

        synchronized double update(double[] features) {
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

            return 1.0 / (1.0 + Math.exp(-maxZ + 2));
        }
    }
}
