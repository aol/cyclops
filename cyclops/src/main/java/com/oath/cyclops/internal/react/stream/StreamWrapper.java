package com.oath.cyclops.internal.react.stream;

import java.util.stream.Stream;

public interface StreamWrapper<U> {
    public Stream<U> stream();

}
