package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Function;

/**
 * Created by johnmcclean on 30/12/2016.
 */
public interface FunctionSpliterator<S,T> extends Spliterator<T>{
     Spliterator<S> source();

     Function<? super S,? extends T> function();
}
