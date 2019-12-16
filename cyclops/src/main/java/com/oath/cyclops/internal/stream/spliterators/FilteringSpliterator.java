package com.oath.cyclops.internal.stream.spliterators;

import lombok.Getter;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class FilteringSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T>, Composable<T>{
    Spliterator<T> source;
    @Getter
    Predicate<? super T> mapper;
    public FilteringSpliterator(final Spliterator<T> source, Predicate<? super T> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = mapper;

    }
    FilteringSpliterator(final Spliterator<T> source) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = getMapper();

    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        source.forEachRemaining(t->{
            if(getMapper().test(t))
                action.accept(t);
        });

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        boolean[] accepted = {false};
        boolean advance = true;
        do {

            advance = source.tryAdvance(t -> {
                if (getMapper().test(t)) {
                    action.accept(t);
                    accepted[0] = true;
                }
            });
        }while(!accepted[0] && advance);
        return accepted[0] && advance;
    }

    @Override
    public Spliterator<T> copy() {
        return new FilteringSpliterator<T>(CopyableSpliterator.copy(source),mapper);
    }

    @Override
    public Spliterator<T> compose() {
        if(source instanceof FilteringSpliterator){
            return compose((FilteringSpliterator)source,this);
        }
        if(source instanceof LazyFilteringSpliterator){
            return compose((LazyFilteringSpliterator)source,this);
        }

        return this;
    }
    public static <T> FilteringSpliterator<T> compose(FilteringSpliterator<T> before, FilteringSpliterator<T> after){

        return new FilteringSpliterator<>(before.source,((Predicate<T>)before.mapper).and(after.getMapper()));
    }
    public static <T> FilteringSpliterator<T> compose(LazyFilteringSpliterator<T> before, FilteringSpliterator<T> after){

        return new FilteringSpliterator<>(before.source,((Predicate<T>)before.mapperSupplier.get()).and(after.getMapper()));
    }
}
