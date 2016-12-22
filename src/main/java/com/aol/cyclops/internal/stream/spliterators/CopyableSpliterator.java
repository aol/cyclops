package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public interface CopyableSpliterator<T> extends Spliterator<T> {

    Spliterator<T> copy();
}
