package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Function;

/**
 * Created by johnmcclean on 30/12/2016.
 */
public interface ComposableFunction<R,T, X extends Spliterator<?>> {
   <R2> X compose(Function<? super R,? extends R2> fn);
}
