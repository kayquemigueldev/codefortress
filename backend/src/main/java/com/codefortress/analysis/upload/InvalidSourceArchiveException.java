package com.codefortress.analysis.upload;

public class InvalidSourceArchiveException extends RuntimeException {

    private final String code;

    public InvalidSourceArchiveException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}