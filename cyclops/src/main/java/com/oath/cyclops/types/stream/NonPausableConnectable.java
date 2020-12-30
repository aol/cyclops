package com.oath.cyclops.types.stream;

import com.oath.cyclops.internal.stream.BaseConnectableImpl;
import cyclops.companion.Eithers;
import cyclops.function.FluentFunctions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * A Connectable (Stream already emitting data) that can not be paused
 *
 * @author johnmcclean
 *
 * @param <T>  Data type of elements in the Stream
 */
public class NonPausableConnectable<T> extends BaseConnectableImpl<T> {
    public NonPausableConnectable(final Stream<T> stream) {
        super(stream);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.internal.stream.BaseConnectableImpl#init(java.util.concurrent.Executor)
     */
    @Override
    public Connectable<T> init(final Executor exec) {
        CompletableFuture.runAsync(() -> {
            pause.get()
                 .join();
            stream.forEach(a -> {

                final int local = connected;

                for (int i = 0; i < local; i++) {

                    Eithers.blocking(connections.get(i))
                              .fold(FluentFunctions.ofChecked(in -> {
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
