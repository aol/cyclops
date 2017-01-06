package com.aol.cyclops2.internal.stream.spliterators;

import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class DistinctSpliterator<IN,T> extends BaseComposableSpliterator<IN,T,DistinctSpliterator<IN,?>> implements CopyableSpliterator<T> {
    Spliterator<IN> source;
    Set<IN> values;
    public DistinctSpliterator(final Spliterator<IN> source) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED,null);

        this.source = source;
        this.values = new HashSet<>();

    }
    DistinctSpliterator(Function<? super IN, ? extends T> fn,final Spliterator<IN> source) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED,fn);

        this.source = source;
        this.values = new HashSet<>();

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        final Consumer<? super IN> toUse = apply(action);
        source.forEachRemaining(e->{
            if(!values.contains(e)){
                values.add(e);
                toUse.accept(e);
            }
        });


    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        final Consumer<? super IN> toUse = apply(action);
        boolean[] accepted = {false};
        boolean advance = true;
        do {

            advance = source.tryAdvance(t -> {
                if (!values.contains(t)) {
                    values.add(t);
                    toUse.accept(t);
                    accepted[0] = true;
                }
            });
        }while(!accepted[0] && advance);
        return accepted[0] && advance;

    }

    @Override
    public Spliterator<T> copy() {
        return new DistinctSpliterator<IN,T>(fn,CopyableSpliterator.copy(source));
    }

    @Override
    <R2> DistinctSpliterator<IN, ?> create(Function<? super IN, ? extends R2> after) {
        return new DistinctSpliterator(after,CopyableSpliterator.copy(source));
    }
}
