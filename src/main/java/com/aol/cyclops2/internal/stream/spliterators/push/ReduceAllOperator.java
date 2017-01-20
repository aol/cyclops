package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Collector;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ReduceAllOperator<T,A,R> extends BaseOperator<T,T> {


    private final T identity;
    private final BinaryOperator<T> accumulator;

    public ReduceAllOperator(Operator<T> source, T identity,BinaryOperator<T> accumulator){
        super(source);
        this.identity = identity;
        this.accumulator = accumulator;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Object[] current = {identity};
        StreamSubscription[] upstream = {null};
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                if(!isOpen)
                    return;
                super.request(n);

                 upstream[0].request(n ); //we can't multiply by groupSize - doesn't work with Sets


            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();

            }
        };
        upstream[0] = source.subscribe(e-> {
                    try {
                        current[0]= accumulator.apply((T)current[0],e);


                        upstream[0].request(1l);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{onError.accept(t);
                    sub.requested.decrementAndGet();
                    if(sub.isActive())
                     upstream[0].request(1);
                },()->{

                    onNext.accept((T)current[0]);
                    onComplete.run();
                });
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Object[] current = {identity};
        source.subscribeAll(e-> {
                    try {
                        current[0]= accumulator.apply((T)current[0],e);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    onNext.accept((T)current[0]);
                    onCompleteDs.run();
                });
    }
}
