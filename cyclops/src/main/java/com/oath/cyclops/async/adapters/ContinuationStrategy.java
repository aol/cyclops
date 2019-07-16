package com.oath.cyclops.async.adapters;

import com.oath.cyclops.types.futurestream.Continuation;

public interface ContinuationStrategy {

    public void addContinuation(Continuation c);

    public void handleContinuation();

    default boolean isBlocking(){
        return false;
    }
}
