package com.aol.cyclops.internal.stream.spliterators;


import com.aol.cyclops.types.Unit;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 02/01/2017.
 */
public abstract class BaseComposableSpliterator<T,R,X extends Spliterator<?>> extends Spliterators.AbstractSpliterator<R>
                                                        implements ComposableFunction<R,T,X>
                                                                    {

    final Function<? super T, ? extends R> fn;

    /**
     * Creates a spliterator reporting the given estimated size and
     * additionalCharacteristics.
     *
     * @param est                       the estimated size of this spliterator if known, otherwise
     *                                  {@code Long.MAX_VALUE}.
     * @param additionalCharacteristics properties of this spliterator's
     *                                  source or elements.  If {@code SIZED} is reported then this
     *                                  spliterator will additionally report {@code SUBSIZED}.
     */
    protected BaseComposableSpliterator(long est, int additionalCharacteristics,Function<? super T, ? extends R> fn) {
        super(est, additionalCharacteristics);
        this.fn = fn;
    }



                                                                        @Override
    public <R2> X compose(Function<? super R, ? extends R2> after) {
        if(fn==null){

            return (X)create((Function)after);
        }
        return create(fn.andThen(after));

    }


     Consumer<? super T> apply(Consumer<? super R> consumer){

        if(fn==null){
            return (Consumer<T>)consumer;
        }

        return in-> consumer.accept(fn.apply(in));

    }


    abstract <R2> X create(Function<? super T, ? extends R2> after);

}
