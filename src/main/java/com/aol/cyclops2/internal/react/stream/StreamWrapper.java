package com.aol.cyclops2.internal.react.stream;

import java.util.stream.Stream;

public interface StreamWrapper<U> {
    public Stream<U> stream();

}
