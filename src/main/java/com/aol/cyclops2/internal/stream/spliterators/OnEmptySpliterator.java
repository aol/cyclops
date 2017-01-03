package com.aol.cyclops2.internal.stream.spliterators;



import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public class OnEmptySpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    private final Spliterator<T> source;
    private final T value;
    private boolean found=false;
    private boolean sent=false;


    public OnEmptySpliterator(Spliterator<T> source, T value) {
        super(source.estimateSize(), source.characteristics() & Spliterator.ORDERED);
        this.source = source;
        this.value = value;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        source.forEachRemaining(e->{
            found =true;
            action.accept(e);
        });
        if(!found) {
            action.accept(value);
            sent =true;
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
                System.out.println("Not found - sending!!");
                sent =true;
                action.accept(value);
            }
            return result;
        }


    }

    @Override
    public Spliterator<T> copy() {
        return new OnEmptySpliterator<T>(CopyableSpliterator.copy(source),value);
    }
}
