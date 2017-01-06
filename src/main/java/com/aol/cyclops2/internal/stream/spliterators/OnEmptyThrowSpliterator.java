package com.aol.cyclops2.internal.stream.spliterators;



import com.aol.cyclops2.util.ExceptionSoftener;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OnEmptyThrowSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    private final Spliterator<T> source;
    private final Supplier<? extends Throwable> t;
    private boolean found=false;
    private boolean sent=false;

    public OnEmptyThrowSpliterator(Spliterator<T> source, Supplier<? extends Throwable> t) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);
        this.source = source;
        this.t=t;
    }


    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        source.forEachRemaining(e->{
            found =true;
            action.accept(e);
        });
        if(!found) {
            sent =true;
            throw ExceptionSoftener.throwSoftenedException(t.get());

        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(sent)
            return false;
        if(found)
            return source.tryAdvance(action);
        else{
            boolean result = source.tryAdvance(e->{
                found =true;
                action.accept(e);
            });
            if(!found){

                sent =true;
                throw ExceptionSoftener.throwSoftenedException(t.get());
            }
            return result;
        }


    }

    @Override
    public Spliterator<T> copy() {
        return new OnEmptyThrowSpliterator<T>(CopyableSpliterator.copy(source),t);
    }
}
