package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class SkipWhileSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    private final Spliterator<T> source;
    private final Predicate<? super T> predicate;
    boolean closed = false;
    public SkipWhileSpliterator(final Spliterator<T> source, Predicate<? super T> predicate) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.predicate = predicate;

    }
    boolean open = false;
    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        while(!closed){
            boolean canAdvance = source.tryAdvance(t -> {
                if(!open) {
                    open = !predicate.test(t);

                    if (open)
                        action.accept(t);
                }else{
                    action.accept(t);
                }
            });
            if(!canAdvance){
                closed = true;
                return;
            }

        }


    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(closed)
            return true;
        boolean canAdvance = source.tryAdvance(t -> {
            if(!open) {
                open = !predicate.test(t);

                if (open)
                    action.accept(t);
            }else{
                action.accept(t);
            }
            });

        return !(closed = !canAdvance);
    }

    @Override
    public Spliterator<T> copy() {
        return new SkipWhileSpliterator<>(CopyableSpliterator.copy(source),predicate);
    }
}
