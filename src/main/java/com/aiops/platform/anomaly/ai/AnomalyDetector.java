package com.aiops.platform.anomaly.ai;

public interface AnomalyDetector {
    double score(double[] features);
    double threshold();
    String methodName();
}
