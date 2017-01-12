package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitOperator<T,R> extends BaseOperator<T,T> {


    long limit;

    public LimitOperator(Operator<T> source,long limit){
        super(source);
        this.limit = limit;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        long[] count = {0};
        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    try {
                        if(count[0]++<limit)
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
