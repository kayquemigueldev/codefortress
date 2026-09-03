package com.codefortress.analysis.lifecycle;

public class AnalysisNotFoundException
        extends RuntimeException {

    public AnalysisNotFoundException() {
        super("Analysis not found");
    }
}