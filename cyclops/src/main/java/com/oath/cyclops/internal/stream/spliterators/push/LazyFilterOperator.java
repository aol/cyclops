package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LazyFilterOperator<T> extends BaseOperator<T,T> {


    Supplier<Predicate<? super T>> predicateSupplier;

    public LazyFilterOperator(Operator<T> source, Supplier<Predicate<? super T>> predicateSupplier){
        super(source);
        this.predicateSupplier = predicateSupplier;


    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Predicate<? super T> predicate = predicateSupplier.get();
        StreamSubscription[] upstream = {null};
        upstream[0]= source.subscribe(e-> {
                    try {
                        if(predicate.test(e))
                            onNext.accept(e);
                        else{
                            upstream[0].request(1);
                        }
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return upstream[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Predicate<? super T> predicate = predicateSupplier.get();
        source.subscribeAll(e-> {
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
