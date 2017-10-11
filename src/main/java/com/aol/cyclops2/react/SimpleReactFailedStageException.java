package com.aol.cyclops2.react;


import cyclops.control.Either;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SimpleReactFailedStageException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Object value;
    @Getter
    private final Throwable cause;

    public <T> T getValue() {
        return (T) value;
    }

    public static Either<Throwable, SimpleReactFailedStageException> matchable(final Throwable t) {
        final Either<Throwable, SimpleReactFailedStageException> error = t instanceof SimpleReactFailedStageException
                ? Either.right((SimpleReactFailedStageException) t) : Either.left(t);
        return error;
    }
}
