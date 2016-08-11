package com.aol.cyclops.types;

public interface Unwrapable {
    /**
     * Unwrap a wrapped value
     * 
     * @return wrapped value
     */
    default <R> R unwrap() {
        return (R) this;
    }
}
