package com.zshield.stream.violation.metric;

import java.util.Set;

public interface Metric {
    public  String getMetricId();

    public Set<MetricUpdate> getMetricUpdate();

    public String getSensorId();

    public String getMetricInfo();
}
