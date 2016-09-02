package com.aol.cyclops.types.stream;

import com.aol.cyclops.control.ReactiveSeq;

public interface ConvertableToReactiveSeq<T> {
    ReactiveSeq<T> reactiveSeq();
}
