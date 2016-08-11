package com.aol.cyclops.data.async;

public interface QueueFactory<T> {

    public Queue<T> build();
}
