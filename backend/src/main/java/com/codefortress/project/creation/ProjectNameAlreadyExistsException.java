package com.codefortress.project.creation;

public class ProjectNameAlreadyExistsException
        extends RuntimeException {

    public ProjectNameAlreadyExistsException() {
        super(
                "An active project with this name already exists"
        );
    }
}