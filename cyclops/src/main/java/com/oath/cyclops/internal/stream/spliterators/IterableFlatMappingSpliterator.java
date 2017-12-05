package com.oath.cyclops.internal.stream.spliterators;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class IterableFlatMappingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R> implements CopyableSpliterator<R> {
    Spliterator<T> source;
    Function<? super T, ? extends Iterable<R>> mapper;
    public IterableFlatMappingSpliterator(final Spliterator<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.mapper = (Function<? super T, ? extends Iterable<R>>)mapper;

    }
    public static <T2,T,R> IterableFlatMappingSpliterator<T2,R> compose(FunctionSpliterator<T2,T> fnS,Function<? super T, ? extends Iterable<? extends R>> mapper){
        Function<? super T2,? extends T> fn = fnS.function();
        return new IterableFlatMappingSpliterator<T2,R>(CopyableSpliterator.copy(fnS.source()),mapper.<T2>compose(fn));

    }
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        if(active!=null){
            active.forEachRemaining(action);
        }
        source.forEachRemaining(t->{
            Iterable<R> flatten = mapper.apply(t);
            flatten.forEach(action);
        });

    }

    Iterator<R> active;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        boolean pushed = false;
        for(;;) {
            if (active != null){
                if(active.hasNext()) {

                    action.accept(active.next());
                    pushed=true;
                    if (active.hasNext())
                        return true;
                    else
                        active = null;
                }else{
                    active = null;
                }

            }
            //next iterator
            boolean advance = source.tryAdvance(t -> {
                if (active == null || !active.hasNext()) {
                    active = (Iterator<R>) mapper.apply(t).iterator();
                }


            });
            if(!advance && active==null)
                return false;
            else if(pushed && advance){
                return true;
            }
        }
    }

    @Override
    public Spliterator<R> copy() {
        return new IterableFlatMappingSpliterator<>(CopyableSpliterator.copy(source),mapper);
    }
}
