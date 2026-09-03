package com.codefortress.analysis.upload;

public class SourceArchiveStorageException extends RuntimeException {

    private final String code;

    public SourceArchiveStorageException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
    }

    public SourceArchiveStorageException(
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