package com.oath.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class LazyFilteringSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T>{
    Spliterator<T> source;
    Predicate<? super T> mapper;
    Supplier<Predicate<? super T>> mapperSupplier;
    public LazyFilteringSpliterator(final Spliterator<T> source, Supplier<Predicate<? super T>> mapperSupplier) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapperSupplier = mapperSupplier;
        this.mapper = mapperSupplier.get();

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        source.forEachRemaining(t->{

            if(mapper.test(t))
                action.accept(t);
        });

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        boolean[] accepted = {false};
        boolean advance = true;
        do {

            advance = source.tryAdvance(t -> {

                if (mapper.test(t)) {
                    action.accept(t);
                    accepted[0] = true;
                }
            });
        }while(!accepted[0] && advance);
        return accepted[0] && advance;
    }

    @Override
    public Spliterator<T> copy() {
        return new LazyFilteringSpliterator<T>(CopyableSpliterator.copy(source),mapperSupplier);
    }


}
