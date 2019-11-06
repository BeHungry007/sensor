package com.zshield.httpServer.common;

import com.zshield.stream.violation.detection.Detection;
import com.zshield.stream.violation.detection.DetectionResult;
import com.zshield.stream.violation.metric.Metric;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationShared {

    public Set<Metric> metrics = ConcurrentHashMap.newKeySet();
    private Map<String, Set<Detection>> ruleIdToDetections = new ConcurrentHashMap();
    private Map<String, DetectionResult> caveatMessage = new ConcurrentHashMap();

    private ViolationShared() {}

    private static class SingletonPatternHolder {
        private static final ViolationShared violatrionShared = new ViolationShared();
    }

    public static ViolationShared getInstance() {
        return SingletonPatternHolder.violatrionShared;
    }

    private boolean addMetric(Metric metric) {
        if (metric != null) {
            return this.metrics.add(metric);
        }
        return false;
    }

    public Set<Detection> getDetections(String metricId) {
        return ruleIdToDetections.get(metricId);
    }

    public void sendCaveat(DetectionResult detectionResult) {
        if (detectionResult != null) {
            caveatMessage.put(detectionResult.getDetectionResultId(), detectionResult);
        }
    }





    public void retainAll(Set<Metric> metric) {
        if (metric != null && metric.size() > 0) {
            //metric是否包含所有的this.metrics的数据。
            metric.retainAll(this.metrics);
        }
    }
}
