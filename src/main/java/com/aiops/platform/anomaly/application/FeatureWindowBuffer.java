package com.aiops.platform.anomaly.application;

import com.aiops.platform.ingestion.domain.LogEvent;
import com.aiops.platform.ingestion.domain.MetricEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FeatureWindowBuffer {

    private static final int WINDOW_SIZE = 20;

    private final Map<String, ArrayDeque<Double>> metricWindows = new ConcurrentHashMap<>();
    private final Map<Long, Integer> errorCounts = new ConcurrentHashMap<>();

    public void recordMetric(MetricEvent event) {
        String key = event.serviceId() + "|" + event.metricName();
        ArrayDeque<Double> deque = metricWindows.computeIfAbsent(key, k -> new ArrayDeque<>(WINDOW_SIZE));
        synchronized (deque) {
            if (deque.size() >= WINDOW_SIZE) {
                deque.pollFirst();
            }
            deque.addLast(event.metricValue());
        }
    }

    public void recordLog(LogEvent event) {
        if ("ERROR".equalsIgnoreCase(event.logLevel())) {
            errorCounts.merge(event.serviceId(), 1, (oldVal, newVal) -> {
                int oldValInt = oldVal != null ? oldVal : 0;
                int newValInt = newVal != null ? newVal : 0;
                return oldValInt + newValInt;
            });
        }
    }

    public double[] extractFeatures(Long serviceId, String metricName) {
        String key = serviceId + "|" + metricName;
        ArrayDeque<Double> window = metricWindows.get(key);

        if (window == null) {
            return null;
        }

        double[] values;
        synchronized (window) {
            if (window.size() < WINDOW_SIZE / 2) {
                return null;
            }
            values = window.stream().mapToDouble(d -> d).toArray();
        }

        Arrays.sort(values);

        double mean = Arrays.stream(values).average().orElse(0);
        double stddev = Math.sqrt(
            Arrays.stream(values).map(v -> Math.pow(v - mean, 2)).average().orElse(0)
        );
        double min = values[0];
        double max = values[values.length - 1];
        double p95 = values[(int) (values.length * 0.95)];
        double errors = errorCounts.getOrDefault(serviceId, 0).doubleValue();

        return new double[]{mean, stddev, min, max, p95, errors};
    }

    public void resetErrorCount(Long serviceId) {
        errorCounts.put(serviceId, 0);
    }
}
