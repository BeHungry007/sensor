package com.zshield.stream.violation.detection;


import com.zshield.httpServer.domain.RuleType;

import java.util.Objects;
import java.util.PrimitiveIterator;

public class DetectionResult {
    private String ruleId;
    private String sensorId;
    private String baseViolationId;
    private RuleType ruleType;
    private String userName;
    private String message;
    private Long timestamp;

    public String getDetectionResultId() {
        return ruleId + "-" + sensorId + "-" + ruleType + "-" + userName + "-" + baseViolationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectionResult that = (DetectionResult) o;
        return Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(sensorId, that.sensorId) &&
                Objects.equals(baseViolationId, that.baseViolationId) &&
                Objects.equals(ruleType, that.ruleType) &&
                Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ruleId, sensorId, baseViolationId, ruleType, userName);
    }

    public DetectionResult(String ruleId, String sensorId, String baseViolationId, RuleType ruleType, String userName, String message) {
        this.ruleId = ruleId;
        this.sensorId = sensorId;
        this.baseViolationId = baseViolationId;
        this.ruleType = ruleType;
        this.userName = userName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getBaseViolationId() {
        return baseViolationId;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
