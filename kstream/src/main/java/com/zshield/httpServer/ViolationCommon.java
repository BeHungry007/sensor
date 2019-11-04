package com.zshield.httpServer;

import com.zshield.httpServer.domain.Violation;

public class ViolationCommon {
    private ViolationCommon() {
        
    }

    public void detectionDependence(Violation violation) {
    }

    public void violationChange(Violation violation) {
    }

    public static class SingletonPatternHolder {
        public static final ViolationCommon violationCommon = new ViolationCommon();
    }
    
    public static final ViolationCommon getInstance(){
        return SingletonPatternHolder.violationCommon;
    }
}
