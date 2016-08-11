package com.aol.cyclops.internal.matcher2;

import java.util.function.Function;

/**
 * Interface representing an extracting from one value to another
 * 
 * extends Function and adds Serializability
 * 
 * @author johnmcclean
 *
 * @param <T> Input type
 * @param <R> Return type
 */
public interface Extractor<T, R> extends Function<T, R> {

    /* 
     * @see java.util.function.Function#apply(java.lang.Object)
     */
    @Override
    public R apply(T t);

}