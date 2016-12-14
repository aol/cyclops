package com.aol.cyclops.internal.stream.spliterators;



import com.aol.cyclops.util.ExceptionSoftener;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OnEmptyThrowSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
    private final Spliterator<T> source;
    private final Supplier<? extends Throwable> t;
    private boolean found=false;


    public OnEmptyThrowSpliterator(Spliterator<T> source, Supplier<? extends Throwable> t) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);
        this.source = source;
        this.t=t;
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(found)
            return source.tryAdvance(action);
        else{
            if(!source.tryAdvance(e->{
                found =true;
                action.accept(e);
            })){
                throw ExceptionSoftener.throwSoftenedException(t.get());
            }
        }

        return found;
    }
}
