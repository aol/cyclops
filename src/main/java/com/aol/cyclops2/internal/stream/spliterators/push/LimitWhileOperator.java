package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitWhileOperator<T,R> extends BaseOperator<T,T> {


    private final Predicate<? super T> predicate;

    public LimitWhileOperator(Operator<T> source, final Predicate<? super T> predicate){
        super(source);
        this.predicate = predicate;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    try {
                        if(predicate.test(e))
                            onNext.accept(e);
                        else{
                            sub[0].cancel();
                            onComplete.run();
                        }
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return sub[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        subscribe(onNext,onError,onCompleteDs).request(Long.MAX_VALUE);
    }
}
