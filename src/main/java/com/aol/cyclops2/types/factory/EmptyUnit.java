package com.aol.cyclops2.types.factory;

/**
 * Represents a type that can be instantiated in empty form.
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) storeable within this EmptyUnit
 */
public interface EmptyUnit<T> extends Unit<T> {
    /**
     * @return A new, empty instance of this EmptyUnit type
     */
    public <T> Unit<T> emptyUnit();
}
