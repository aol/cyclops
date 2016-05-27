package com.aol.cyclops.types;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Visitable<T> {
    <R> R visit(Function<? super T,? extends R> present,Supplier<? extends R> absent);
}
