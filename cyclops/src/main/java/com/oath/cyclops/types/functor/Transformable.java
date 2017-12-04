package com.oath.cyclops.types.functor;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Trampoline;

/**
 * An interface that represents a type that can transform a value from one type to another
 *
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) stored in this Transformable
 */
@FunctionalInterface
public interface Transformable<T> {



    /**
     * Transform this functor using the supplied transformation function
     *
     * <pre>
     * {@code
     *
     *
     *    of(1,2,3).map(i->i*2)
     *
     *    //[2,4,6]
     *
     * }
     * </pre>
     *
     * @param fn Transformation function
     * @return Transformed Transformable
     */
    <R> Transformable<R> map(Function<? super T, ? extends R> fn);

    /**
     * Peek at the current value of this Transformable, without transforming it
     *
      * <pre>
     * {@code
     *
     *
     *    of(1,2,3).map(System.out::println)
     *
     *    1
     *    2
     *    3
     *
     * }
     * </pre>
     * @param c Consumer that recieves each element from this Transformable
     * @return Transformable that will peek at each value
     */
    default Transformable<T> peek(final Consumer<? super T> c) {
        return map(input -> {
            c.accept(input);
            return input;
        });
    }

    /**
      * Performs a transform operation that can call a recursive method without running out of stack space
      * <pre>
      * {@code
      * ReactiveSeq.of(10,20,30,40)
    			 .trampoline(i-> fibonacci(i))
    			 .forEach(System.out::println);

    	Trampoline<Long> fibonacci(int i){
    		return fibonacci(i,1,0);
    	}
    	Trampoline<Long> fibonacci(int n, long a, long b) {
        	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
    	}

      * 55
    	6765
    	832040
    	102334155
      *
      *
      * ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
    			 .trampoline(i-> fibonacci(i))
    			 .forEach(System.out::println);


      * completes successfully
      * }
      * </pre>
      *
     * @param mapper TCO Transformation function
     * @return Transformable transformed by the supplied transformation function
     */
    default <R> Transformable<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return map(in -> mapper.apply(in)
                               .result());
    }

    /**
     * Retry a transformation if it fails. Default settings are to retry up to 7
     * times, with an doubling backoff period starting @ 2 seconds delay before
     * retry.
     *
     * <pre>
     * {@code
     * given(serviceMock.applyHKT(anyInt())).willThrow(
     * 				new RuntimeException(new SocketException("First")),
     * 				new RuntimeException(new IOException("Second"))).willReturn(
     * 				"42");
     *
     *
     * 		String result = ReactiveSeq.of( 1,  2, 3)
     * 				.retry(serviceMock)
     * 				.firstValue();
     *
     * 		//result = 42
     * }
     * </pre>
     *
     * @param fn
     *            Function to retry if fails
     *
     */
    default <R> Transformable<R> retry(final Function<? super T, ? extends R> fn) {
        return retry(fn, 7, 2, TimeUnit.SECONDS);
    }

    /**
     * Retry a transformation if it fails. Retries up to <b>retries</b>
     * times, with an doubling backoff period starting @ <b>delay</b> TimeUnits delay before
     * retry.
     *
     * <pre>
     * {@code
     * given(serviceMock.applyHKT(anyInt())).willThrow(
     * 				new RuntimeException(new SocketException("First")),
     * 				new RuntimeException(new IOException("Second"))).willReturn(
     * 				"42");
     *
     *
     * 		String result = ReactiveSeq.of( 1,  2, 3)
     * 				.retry(serviceMock, 7, 2, TimeUnit.SECONDS)
     * 				.firstValue();
     *
     * 		//result = 42
     * }
     * </pre>
     *
     * @param fn
     *            Function to retry if fails
     * @param retries
     *            Number of retries
     * @param delay
     *            Delay in TimeUnits
     * @param timeUnit
     *            TimeUnit to use for delay
     */
    default <R> Transformable<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        final Function<T, R> retry = t -> {
            final long[] sleep = { timeUnit.toMillis(delay) };
            Throwable exception = null;
            for (int count = retries; count >=0; count--) {
                try {
                    return fn.apply(t);
                } catch (final Throwable e) {
                    exception = e;
                    ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep[0]))
                                     .run();
                    sleep[0] = sleep[0] * 2;
                }
            }
            throw ExceptionSoftener.throwSoftenedException(exception);

        };
        return map(retry);
    }



}
