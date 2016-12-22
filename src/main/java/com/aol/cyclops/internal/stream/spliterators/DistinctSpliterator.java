package com.aol.cyclops.internal.stream.spliterators;

import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class DistinctSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    Spliterator<T> source;
    Set<T> values;
    public DistinctSpliterator(final Spliterator<T> source) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.values = new HashSet<T>();

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        source.forEachRemaining(e->{
            if(!values.contains(e)){
                values.add(e);
                action.accept(e);
            }
        });


    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        boolean[] accepted = {false};
        boolean advance = true;
        do {

            advance = source.tryAdvance(t -> {
                if (!values.contains(t)) {
                    values.add(t);
                    action.accept(t);
                    accepted[0] = true;
                }
            });
        }while(!accepted[0] && advance);
        return accepted[0] && advance;

    }

    @Override
    public Spliterator<T> copy() {
        return new DistinctSpliterator<T>(CopyableSpliterator.copy(source));
    }
}
