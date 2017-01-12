package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LazyFilterOperator<T> extends BaseOperator<T,T> {


    Supplier<Predicate<? super T>> predicateSupplier;

    public LazyFilterOperator(Operator<T> source, Supplier<Predicate<? super T>> predicate){
        super(source);
        this.predicateSupplier = predicate;


    }



    @Override
    public void subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Predicate<? super T> predicate = predicateSupplier.get();
        source.subscribe(e-> {
                    try {
                        if(predicate.test(e))
                            onNext.accept(e);
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onCompleteDs);
    }
}
