package com.aol.cyclops.types.futurestream;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collector;

import cyclops.CyclopsCollectors;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.react.stream.EagerStreamWrapper;
import com.aol.cyclops.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops.util.ThrowsSoftened;

public interface BlockingStream<U> {

    Optional<Consumer<Throwable>> getErrorHandler();

    /**
     * React and <b>block</b>
     * 
     * <pre>
     * {@code 
    	List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
    			.then((it) -> it * 100)
    			.then((it) -> "*" + it)
    			.block();
    	}
    			
      </pre>
     * 
     * In this example, once the current thread of execution meets the React
     * block method, it will block until all tasks have been completed. The
     * result will be returned as a List. The Reactive tasks triggered by the
     * Suppliers are non-blocking, and are not impacted by the block method
     * until they are complete. Block, only blocks the current thread.
     * 
     * 
     * @return Results of currently active stage aggregated in a List throws
     *         InterruptedException,ExecutionException
     */
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    default ListX<U> block() {
        final Object lastActive = getLastActive();
        if (lastActive instanceof EagerStreamWrapper) {
            final EagerStreamWrapper last = (EagerStreamWrapper) lastActive;
            return BlockingStreamHelper.block(this, CyclopsCollectors.toListX(), last);
        } else {
            final LazyStreamWrapper<U> last = (LazyStreamWrapper) lastActive;
            return BlockingStreamHelper.block(this, CyclopsCollectors.toListX(), last);
        }
    }

    Object getLastActive();

    /**
     * @param collector
     *            to perform aggregation / reduction operation on the results
     *            (e.g. to Collect into a List or String)
     * @return Results of currently active stage in aggregated in form
     *         determined by collector throws
     *         InterruptedException,ExecutionException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    default <R, A> R block(final Collector<? super U, A, R> collector) {
        final Object lastActive = getLastActive();
        if (lastActive instanceof EagerStreamWrapper) {
            final EagerStreamWrapper last = (EagerStreamWrapper) lastActive;
            return (R) BlockingStreamHelper.block(this, collector, last);
        } else {
            final LazyStreamWrapper last = (LazyStreamWrapper) lastActive;
            return (R) BlockingStreamHelper.block(this, collector, last);
        }

    }

}
