package com.chrisvdalen.contracthawk.messaging.application;

public interface AnalysisJobPublisher {

    void publish(AnalysisJob job);
}
