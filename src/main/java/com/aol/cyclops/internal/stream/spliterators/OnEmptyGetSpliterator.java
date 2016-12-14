package com.aol.cyclops.internal.stream.spliterators;



import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OnEmptyGetSpliterator<T> extends Spliterators.AbstractSpliterator<T> {
    private final Spliterator<T> source;
    private final Supplier<? extends T> value;
    private boolean found=false;


    public OnEmptyGetSpliterator(Spliterator<T> source, Supplier<? extends T> value) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);
        this.source = source;
        this.value = value;
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
                action.accept(value.get());
            }
        }

        return found;
    }
}
