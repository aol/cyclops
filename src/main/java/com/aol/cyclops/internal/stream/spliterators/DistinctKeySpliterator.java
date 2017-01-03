package com.aol.cyclops.internal.stream.spliterators;

import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class DistinctKeySpliterator<IN,T,U> extends BaseComposableSpliterator<IN,T,DistinctKeySpliterator<IN,?,U>> implements CopyableSpliterator<T> {
    Spliterator<IN> source;
    Set<U> values;
    Function<? super IN, ? extends U> keyExtractor;
    public DistinctKeySpliterator(Function<? super IN, ? extends U> keyExtractor,final Spliterator<IN> source) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED,null);

        this.source = source;
        this.values = new HashSet<>();
        this.keyExtractor = keyExtractor;

    }
    DistinctKeySpliterator(Function<? super IN, ? extends U> keyExtractor,Function<? super IN, ? extends T> fn, final Spliterator<IN> source) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED,fn);

        this.source = source;
        this.keyExtractor = keyExtractor;
        this.values = new HashSet<>();

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        final Consumer<? super IN> toUse = apply(action);
        source.forEachRemaining(e->{

            if(!values.add(keyExtractor.apply(e))){
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

                if (!values.add(keyExtractor.apply(t))) {

                    toUse.accept(t);
                    accepted[0] = true;
                }
            });
        }while(!accepted[0] && advance);
        return accepted[0] && advance;

    }

    @Override
    public Spliterator<T> copy() {
        return new DistinctKeySpliterator<IN,T,U>(keyExtractor,fn,CopyableSpliterator.copy(source));
    }

    @Override
    <R2> DistinctKeySpliterator<IN, ?, U> create(Function<? super IN, ? extends R2> after) {
        return new DistinctKeySpliterator(keyExtractor,after,CopyableSpliterator.copy(source));
    }
}
