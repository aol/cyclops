package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;

/**
 * Created by johnmcclean on 05/01/2017.
 */
public interface Indexable<T> {

    Spliterator<T> skip(long start);
    Spliterator<T> take(long end);

}
