package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ReduceAllOperator<T,A,R> extends BaseOperator<T,R> {


    private final R identity;
    private final BiFunction<R, ? super T, R> accumulator;

    public ReduceAllOperator(Operator<T> source, R identity,BiFunction<R, ? super T, R>  accumulator){
        super(source);
        this.identity = identity;
        this.accumulator = accumulator;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
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
                        current[0]= accumulator.apply((R)current[0],e);


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
                    try {
                        onNext.accept((R) current[0]);
                    }catch (Throwable t) {

                        onError.accept(t);
                    }
                    onComplete.run();
                });
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Object[] current = {identity};
        source.subscribeAll(e-> {
                    try {
                        current[0]= accumulator.apply((R)current[0],e);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    try {
                        onNext.accept((R) current[0]);
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                    onCompleteDs.run();
                });
    }
}
