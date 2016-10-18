package com.aol.cyclops.util.stream.pushable;

import java.util.stream.Stream;

import com.aol.cyclops.data.async.Queue;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is Stream&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableStream<T> extends AbstractPushableStream<T, Queue<T>, Stream<T>> {

    public PushableStream(final Queue<T> v1, final Stream<T> v2) {
        super(v1, v2);
    }

    private static final long serialVersionUID = 1L;

}
