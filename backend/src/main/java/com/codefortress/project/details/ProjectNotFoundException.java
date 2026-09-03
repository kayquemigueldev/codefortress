package com.codefortress.project.details;

public class ProjectNotFoundException
        extends RuntimeException {

    public ProjectNotFoundException() {
        super("Project not found");
    }
}