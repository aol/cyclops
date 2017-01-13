package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitWhileClosedOperator<T,R> extends BaseOperator<T,T> {


    private final Predicate<? super T> predicate;

    public LimitWhileClosedOperator(Operator<T> source, final Predicate<? super T> predicate){
        super(source);
        this.predicate = predicate;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        boolean closed[] = {false};
        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    try {
                        onNext.accept(e);
                        if(closed[0]){
                            sub[0].cancel();
                            onComplete.run();
                        }
                        if(!predicate.test(e)){
                            closed[0]=true;
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
