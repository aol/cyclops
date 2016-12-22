package com.aol.cyclops.util.stream.pushable;

import cyclops.async.Queue;
import cyclops.stream.FutureStream;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is LazyFutureStream&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableLazyFutureStream<T> extends AbstractPushableStream<T, Queue<T>, FutureStream<T>> {

    public PushableLazyFutureStream(final Queue<T> v1, final FutureStream<T> v2) {
        super(v1, v2);

    }

    private static final long serialVersionUID = 1L;

}