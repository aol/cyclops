package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GroupingOperator<T,C extends Collection<? super T>,R> extends BaseOperator<T,R> {



    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final int groupSize;

    public GroupingOperator(Operator<T> source, Supplier<? extends C> factory,
                            Function<? super C, ? extends R> finalizer,
                int groupSize){
        super(source);
        this.factory = factory;
        this.finalizer = finalizer;
        this.groupSize = groupSize;



    }



    @Override
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Collection[] next = {factory.get()};
        source.subscribe(e-> {
                    try {
                        next[0].add(e);
                        if(next[0].size()==groupSize){
                            onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    onNext.accept(finalizer.apply((C)next[0]));
                    onCompleteDs.run();
                });
    }
}
