package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;

/**
 * Created by johnmcclean on 23/12/2016.
 */
public interface Composable<R> {

    public Spliterator<R> compose();
}
