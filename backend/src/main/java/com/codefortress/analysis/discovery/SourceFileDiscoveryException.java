package com.codefortress.analysis.discovery;

public class SourceFileDiscoveryException
        extends RuntimeException {

    private final String code;

    public SourceFileDiscoveryException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
    }

    public SourceFileDiscoveryException(
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