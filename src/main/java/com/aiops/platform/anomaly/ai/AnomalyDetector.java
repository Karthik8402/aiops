package com.aiops.platform.anomaly.ai;

public interface AnomalyDetector {
    double score(double[] features, Long serviceId, String metricName);
    double threshold();
    String methodName();
}
