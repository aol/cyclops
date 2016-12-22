package com.aol.cyclops.util.stream.pushable;

import cyclops.stream.ReactiveSeq;
import cyclops.async.Queue;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is Seq&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableReactiveSeq<T> extends AbstractPushableStream<T, Queue<T>, ReactiveSeq<T>> {

    public PushableReactiveSeq(final Queue<T> v1, final ReactiveSeq<T> v2) {
        super(v1, v2);

    }

    private static final long serialVersionUID = 1L;

}
