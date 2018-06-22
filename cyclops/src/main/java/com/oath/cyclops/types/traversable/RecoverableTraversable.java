package com.oath.cyclops.types.traversable;

import java.util.function.Function;

public interface RecoverableTraversable<T> extends Iterable<T> {


    /**
     * Recover from an exception with an alternative value
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4)
     * 						   .map(i->i+2)
     * 						   .map(u->{throw new RuntimeException();})
     * 						   .recover(e->"hello")
     * 						   .firstValue(),equalTo("hello"));
     * }
     * </pre>
     *
     * @param fn
     *            Function that accepts a Throwable and returns an alternative
     *            value
     * @return ReactiveSeq that can recover from an Exception
     */
     RecoverableTraversable<T> recover(final Function<? super Throwable, ? extends T> fn);

    /**
     * Recover from a particular exception type
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4)
     * 					.map(i->i+2)
     * 					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
     * 					.recover(IOException.class,e->"hello")
     * 					.firstValue(),equalTo("hello"));
     *
     * }
     * </pre>
     *
     * @param exceptionClass
     *            Type to recover from
     * @param fn
     *            That accepts an error and returns an alternative value
     * @return Traversable that can recover from a particular exception
     */
    <EX extends Throwable> RecoverableTraversable<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn);
}
