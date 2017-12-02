package com.oath.cyclops.internal.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import com.oath.cyclops.types.stream.PausableHotStream;
import cyclops.companion.Eithers;
import cyclops.function.FluentFunctions;

public class PausableHotStreamImpl<T> extends BaseHotStreamImpl<T>implements PausableHotStream<T> {
    public PausableHotStreamImpl(final Stream<T> stream) {
        super(stream);
    }

    @Override
    public PausableHotStream<T> init(final Executor exec) {
        CompletableFuture.runAsync(() -> {

            stream.forEach(a -> {
                pause.get()
                     .join();
                final int local = connected;

                for (int i = 0; i < local; i++) {

                    Eithers.blocking(connections.get(i))
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

    @Override
    public PausableHotStream<T> paused(final Executor exec) {
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
