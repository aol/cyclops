package com.aol.cyclops.internal.react.stream;

import com.aol.cyclops.internal.react.exceptions.SimpleReactProcessingException;

public class InfiniteProcessingException extends SimpleReactProcessingException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InfiniteProcessingException(final String message) {
        super(message);
    }

}