package com.aol.cyclops;

import java.util.function.Function;

public interface MonadicSequence<T> {
    
    <R> MonadicSequence<R> flatMap(Function<? super T, ? extends MonadicSequence<? extends R>> mapper);
}
