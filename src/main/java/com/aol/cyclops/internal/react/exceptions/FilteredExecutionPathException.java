package com.aol.cyclops.internal.react.exceptions;

public class FilteredExecutionPathException extends SimpleReactProcessingException {

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    private static final long serialVersionUID = 1L;

}