package com.aol.cyclops.types.stream;

public interface PausableHotStream<T> extends HotStream<T> {
    void unpause();

    void pause();
}
