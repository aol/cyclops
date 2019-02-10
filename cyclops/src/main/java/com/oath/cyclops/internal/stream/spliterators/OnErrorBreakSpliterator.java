package com.oath.cyclops.internal.stream.spliterators;

import com.oath.cyclops.util.ExceptionSoftener;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 15/12/2016.
 */
//@AllArgsConstructor
public class OnErrorBreakSpliterator<T, X extends Throwable> implements CopyableSpliterator<T>{
    private final Spliterator<T> source;
    private final Function<? super X, ? extends T> fn;
    private final Class<X> type;

    public OnErrorBreakSpliterator(Spliterator<T> source, Function<? super X, ? extends T> fn, Class<X> type) {
        this.source=source;
        this.fn = fn;
        this.type = type;
    }



    @Override
    public boolean tryAdvance(Consumer<? super T> action) {

         try {
             return source.tryAdvance(action);
         }catch(Throwable t){
             if (type.isAssignableFrom(t.getClass())) {
                 action.accept(fn.apply((X)t));
                 return false;
             }
             throw ExceptionSoftener.throwSoftenedException(t);

         }

    }



    @Override
    public Spliterator<T> copy() {
        return new OnErrorBreakSpliterator(CopyableSpliterator.copy(source),fn,type);
    }

    @Override
    public Spliterator<T> trySplit() {
        return this;
    }


    @Override
    public long estimateSize() {
        return source.estimateSize();
    }


    @Override
    public int characteristics() {
        return source.characteristics() & ~(SORTED | DISTINCT);
    }
}
