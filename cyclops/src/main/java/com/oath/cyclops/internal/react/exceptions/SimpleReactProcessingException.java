package com.oath.cyclops.internal.react.exceptions;

public class SimpleReactProcessingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SimpleReactProcessingException() {
        super();

    }

    public SimpleReactProcessingException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }

    public SimpleReactProcessingException(final String message, final Throwable cause) {
        super(message, cause);

    }

    public SimpleReactProcessingException(final String message) {
        super(message);

    }

    public SimpleReactProcessingException(final Throwable cause) {
        super(cause);

    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }


}
