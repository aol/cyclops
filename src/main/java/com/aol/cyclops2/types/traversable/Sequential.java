package com.aol.cyclops2.types.traversable;

import java.util.concurrent.TimeUnit;

import cyclops.stream.ReactiveSeq;

/**
 * A sequential non-scalar data type
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of the elments stored in this Sequential data type
 */
public interface Sequential<T> {

    /**
     * @return This Sequential converted to a Stream (@link {@link ReactiveSeq}
     */
    ReactiveSeq<T> stream();

    /**
     * emit x elements per time period
     * 
     * <pre>
     * {@code
     *  SimpleTimer timer = new SimpleTimer();
        ReactiveSeq.of(1, 2, 3, 4, 5, 6)
                  .xPer(6, 100000000, TimeUnit.NANOSECONDS)
                  .collect(CyclopsCollectors.toList())
                  .size()
                  
       //6           
     * 
     * }
     * </pre>
     * 
     * @param x
     *            number of elements to emit
     * @param time
     *            period
     * @param t
     *            Time unit
     * @return ReactiveSeq that emits x elements per time period
     */
    default ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        return stream().xPer(x, time, t);
    }

    /**
     * emit one element per time period
     * 
     * <pre>
     * {@code 
     * ReactiveSeq.iterate("", last -> "next")
     *              .limit(100)
     *              .batchBySize(10)
     *              .onePer(1, TimeUnit.MICROSECONDS)
     *              .peek(batch -> System.out.println("batched : " + batch))
     *              .flatMap(Collection::reactiveStream)
     *              .peek(individual -> System.out.println("Flattened : "
     *                      + individual))
     *              .forEach(a->{});
     * }
     * </pre>
     * @param time period
     * @param t Time unit
     * @return SequenceM that emits 1 element per time period
     */
    default ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        return stream().onePer(time, t);
    }

    /**
     * emit elements after a fixed delay
     * 
     * <pre>
     * {@code
     *  SimpleTimer timer = new SimpleTimer();
     *  ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *             .fixedDelay(10000, TimeUnit.NANOSECONDS)
     *             .collect(CyclopsCollectors.toList())
     *             .size()
     *  //6           
     *  //timer.getElapsedNanoseconds() > greaterThan(60000l)
     * }
     * </pre>
     * 
     * @param l
     *            time length in nanos of the delay
     * @param unit
     *            for the delay
     * @return SequenceM that emits each element after a fixed delay
     */
    default ReactiveSeq<T> fixedDelay(final long l, final TimeUnit unit) {
        return stream().fixedDelay(l, unit);
    }
}
