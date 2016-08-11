package com.aol.cyclops.internal.react.exceptions;

import java.util.concurrent.CompletionException;

public class SimpleReactCompletionException extends CompletionException {

    public SimpleReactCompletionException(Throwable cause) {
        super(cause);

    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
