package com.aol.cyclops.react;

import com.aol.cyclops.control.Matchable.MXor;
import com.aol.cyclops.control.Xor;

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

    public static MXor<Throwable, SimpleReactFailedStageException> matchable(final Throwable t) {
        final Xor<Throwable, SimpleReactFailedStageException> error = t instanceof SimpleReactFailedStageException
                ? Xor.primary((SimpleReactFailedStageException) t) : Xor.secondary(t);
        return () -> error;
    }
}
