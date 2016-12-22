package com.aol.cyclops.types.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import cyclops.Matchables;
import cyclops.function.FluentFunctions;
import com.aol.cyclops.internal.stream.BaseHotStreamImpl;

/**
 * A HotStream (Stream already emitting data) that can not be paused
 * 
 * @author johnmcclean
 *
 * @param <T>  Data type of elements in the Stream
 */
public class NonPausableHotStream<T> extends BaseHotStreamImpl<T> {
    public NonPausableHotStream(final Stream<T> stream) {
        super(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.internal.stream.BaseHotStreamImpl#init(java.util.concurrent.Executor)
     */
    @Override
    public HotStream<T> init(final Executor exec) {
        CompletableFuture.runAsync(() -> {
            pause.get()
                 .join();
            stream.forEach(a -> {

                final int local = connected;

                for (int i = 0; i < local; i++) {

                    Matchables.blocking(connections.get(i))
                              .visit(FluentFunctions.ofChecked(in -> {
                        in.put(a);
                        return true;
                    }), q -> q.offer(a));
                }

            });

            open.set(false);

        } , exec);
        return this;
    }
}
