package com.codefortress.analysis.extraction;

public class SourceArchiveExtractionException
        extends RuntimeException {

    private final String code;

    public SourceArchiveExtractionException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
    }

    public SourceArchiveExtractionException(
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