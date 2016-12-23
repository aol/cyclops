package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class ForEachWithError<T> extends Spliterators.AbstractSpliterator<T> {
    private final Spliterator<T> source;
    private final Consumer<? super Throwable> onError;
    private final Runnable onComplete;
    public ForEachWithError (final Spliterator<T> source, Consumer<? super Throwable> onError) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.onError = onError;
        this.onComplete=()->{};


    }
    public ForEachWithError (final Spliterator<T> source, Consumer<? super Throwable> onError, Runnable onComplete) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.onError = onError;
        this.onComplete = onComplete;


    }
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        boolean result = true;
        while(result) { //loop while more data, but erroring
            try {
                result = source.tryAdvance(e -> {

                    action.accept(e);

                });
                if (!result) //completed
                    onComplete.run();
                return result;
            } catch (Throwable t) {

                onError.accept(t);

            }
        }
        onComplete.run(); //completed
        return false;
    }

}
