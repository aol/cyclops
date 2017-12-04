package com.oath.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;


public class ScanLeftSpliterator<T,U> implements CopyableSpliterator<U> {

    private final Spliterator<T> source;
    private U current;
    private final U identity;
    private final BiFunction<? super U, ? super T, ? extends U> function;
    private final long size;
    private final int characteristics;
    public ScanLeftSpliterator(Spliterator<T> source, U identity,
            BiFunction<? super U, ? super T, ? extends U> function) {
        super();
        this.identity=identity;
        this.source = source;
        this.current = identity;
        this.function = function;

        size = source.estimateSize();
        characteristics= source.characteristics() & Spliterator.ORDERED;;
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator#forEachRemaining(java.util.function.Consumer)
     */
    @Override
    public void forEachRemaining(Consumer<? super U> action) {

        source.forEachRemaining(e->{
            action.accept( current=function.apply(current,e));

        });

    }

    boolean advance =true;
    @Override
    public boolean tryAdvance(Consumer<? super U> action) {

       return source.tryAdvance(e->{
           action.accept(current= function.apply(current, e));


       });


    }

    @Override
    public Spliterator<U> trySplit() {
        return this;
    }

    @Override
    public long estimateSize() {
        return  size;
    }

    @Override
    public int characteristics() {
       return characteristics;
    }


    @Override
    public Spliterator<U> copy() {
        return new ScanLeftSpliterator<T, U>(CopyableSpliterator.copy(source),identity,function);
    }
}
