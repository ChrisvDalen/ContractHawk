package com.chrisvdalen.contracthawk.analysis.application;

public class ContractProcessingException extends RuntimeException {

    public ContractProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
