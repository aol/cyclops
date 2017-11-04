package com.oath.anym.internal.monads;

import com.oath.anym.extensability.FunctionalAdapter;
import cyclops.monads.WitnessType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 *
 * Wrapper for Any Monad type
 * @see AnyMs companion class for static helper methods
 *
 * @author johnmcclean
 *
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAnyMImpl<W extends WitnessType<W>,T> {

    protected final Object monad;
    protected final FunctionalAdapter<W> adapter;

    public FunctionalAdapter<W> adapter(){
        return adapter;
    }


    public <R> R unwrap(){
        return (R)monad;
    }


    @Override
    public String toString() {
        return String.format("AnyM(%s)", monad);
    }



}
