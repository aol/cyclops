package com.aol.cyclops.internal.stream.spliterators;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class MappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R>
                                    implements CopyableSpliterator<R>,
                                               //Composable<R>,
                                               FunctionSpliterator<T,R>,
                                                ComposableFunction<R,T,MappingSpliterator<T,?>> {
    Spliterator<T> source;
    Function<? super T, ? extends R> mapper;
    public MappingSpliterator(final Spliterator<T> source,Function<? super T, ? extends R> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = mapper;

    }
    @Override
    public <R2> MappingSpliterator<T, ?> compose(Function<? super R, ? extends R2> fn) {
        return new MappingSpliterator<T, R2>(CopyableSpliterator.copy(source),mapper.andThen(fn));
    }
    /**
    public Spliterator<R> compose(){

        if(source instanceof MappingSpliterator){
            return compose((MappingSpliterator)source,this);
        }
        return this;
    }
    public static <T1,T2,R> MappingSpliterator<T1,R> compose(FunctionSpliterator<T1,T2> before, MappingSpliterator<T2,R> after){
        return new MappingSpliterator<T1, R>(before.source(),before.mapper.andThen(after.mapper));
    }**/

    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        source.forEachRemaining(t->{
            action.accept(mapper.apply(t));
        });

    }

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        return source.tryAdvance(t->{
            action.accept(mapper.apply(t));
        });
    }

    @Override
    public Spliterator<R> copy() {
        return new MappingSpliterator<T, R>(CopyableSpliterator.copy(source),mapper);
    }


    @Override
    public Spliterator<T> source() {
        return source;
    }

    @Override
    public Function<? super T, ? extends R> function() {
        return mapper;
    }
}
