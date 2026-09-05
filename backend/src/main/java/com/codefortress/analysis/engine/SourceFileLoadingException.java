package com.codefortress.analysis.engine;

public class SourceFileLoadingException
        extends RuntimeException {

    private final String code;

    public SourceFileLoadingException(
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