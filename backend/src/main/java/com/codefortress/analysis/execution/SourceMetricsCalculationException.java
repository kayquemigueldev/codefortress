package com.codefortress.analysis.execution;

public class SourceMetricsCalculationException
        extends RuntimeException {

    private final String code;

    public SourceMetricsCalculationException(
            String code,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}