package com.aol.cyclops.util.stream.pushable;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is LazyFutureStream&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableLazyFutureStream<T> extends AbstractPushableStream<T, Queue<T>, LazyFutureStream<T>> {

    public PushableLazyFutureStream(Queue<T> v1, LazyFutureStream<T> v2) {
        super(v1, v2);

    }

    private static final long serialVersionUID = 1L;

}