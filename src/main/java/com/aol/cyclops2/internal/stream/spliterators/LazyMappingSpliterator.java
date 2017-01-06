package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class LazyMappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R>
                                    implements CopyableSpliterator<R>,
                                                FunctionSpliterator<T,R>,
                                                ComposableFunction<R,T,LazyMappingSpliterator<T,?>> {
    Spliterator<T> source;
    Supplier<Function<? super T, ? extends R>> mapperSupplier;
    Function<? super T, ? extends R> mapper;
    public LazyMappingSpliterator(final Spliterator<T> source, Supplier<Function<? super T, ? extends R>> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapperSupplier=mapper;
        this.mapper = mapper.get();

    }
    @Override
    public <R2> LazyMappingSpliterator<T, ?> compose(Function<? super R, ? extends R2> fn) {
        return new LazyMappingSpliterator<T, R2>(CopyableSpliterator.copy(source),()->mapperSupplier.get().andThen(fn));
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
        return new LazyMappingSpliterator<T, R>(CopyableSpliterator.copy(source),mapperSupplier);
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
