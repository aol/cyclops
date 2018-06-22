package com.oath.cyclops.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import com.oath.cyclops.types.stream.PausableConnectable;
import cyclops.companion.Eithers;
import cyclops.function.FluentFunctions;

public class PausableConnectableImpl<T> extends BaseConnectableImpl<T> implements PausableConnectable<T> {
    public PausableConnectableImpl(final Stream<T> stream) {
        super(stream);
    }

    @Override
    public PausableConnectable<T> init(final Executor exec) {
        CompletableFuture.runAsync(() -> {

            stream.forEach(a -> {
                pause.get()
                     .join();
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

    @Override
    public PausableConnectable<T> paused(final Executor exec) {
        super.paused(exec);
        return this;
    }

    @Override
    public void unpause() {
        super.unpause();
    }

    @Override
    public void pause() {
        super.pause();
    }
}
