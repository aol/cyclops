package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class StreamFlatMappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R> implements CopyableSpliterator<R> {
    Spliterator<T> source;
    Function<? super T, ? extends Stream<? extends R>> mapper;
    public StreamFlatMappingSpliterator(final Spliterator<T> source, Function<? super T, ? extends Stream<? extends R>> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = mapper;

    }
    public static <T2,T,R> StreamFlatMappingSpliterator<T2,R> compose(FunctionSpliterator<T2,T> fnS,Function<? super T, ? extends Stream<? extends R>> mapper){
        Function<? super T2,? extends T> fn = fnS.function();
        return new StreamFlatMappingSpliterator<T2,R>(CopyableSpliterator.copy(fnS.source()),mapper.<T2>compose(fn));

    }
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        if(active!=null){
            active.forEachRemaining(action);
        }
        source.forEachRemaining(t->{


            Stream<R> flatten = (Stream<R>)mapper.apply(t);
            flatten.forEach(action);
        });

    }

    Spliterator<R> active;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {

        for(;;) {
            if (active != null) {
                if(active.tryAdvance(action)){
                    return true;
                }else{
                    active = null;
                }



            }
            //next spliterator
            boolean advance = source.tryAdvance(t -> {

                active = (Spliterator<R>) mapper.apply(t).spliterator();



            });

            if(!advance && active==null)
                return false;
        }

    }
    @Override
    public Spliterator<R> copy() {
        return new StreamFlatMappingSpliterator<>(CopyableSpliterator.copy(source),mapper);
    }
}
