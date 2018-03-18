package com.oath.cyclops.anym.internal.monads;

import com.oath.cyclops.anym.extensability.MonadAdapter;
import cyclops.monads.AnyMs;
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
    protected final MonadAdapter<W> adapter;

    public MonadAdapter<W> adapter(){
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
