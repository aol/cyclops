package com.aol.cyclops.internal.react.exceptions;

import java.util.concurrent.CompletionException;

public class SimpleReactCompletionException extends CompletionException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SimpleReactCompletionException(final Throwable cause) {
        super(cause);

    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
