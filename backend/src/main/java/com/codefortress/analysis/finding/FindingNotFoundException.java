package com.codefortress.analysis.finding;

public class FindingNotFoundException
        extends RuntimeException {

    public FindingNotFoundException() {
        super("Finding not found");
    }
}