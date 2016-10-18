package com.aol.cyclops.internal.react.async.future;

public class CompletedException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Object resut;

    public CompletedException(final Object resut) {

        this.resut = resut;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
