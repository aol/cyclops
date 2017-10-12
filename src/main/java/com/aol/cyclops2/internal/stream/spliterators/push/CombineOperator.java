package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;

import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class CombineOperator<T,A,R> extends BaseOperator<T,ReactiveSeq<T>> {

    private final BiPredicate<? super T, ? super T> predicate;

    private final BinaryOperator<T> accumulator;

    static final Object UNSET = new Object();

    public CombineOperator(Operator<T> source, BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> accumulator){
        super(source);
        this.predicate = predicate;

        this.accumulator = accumulator;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super ReactiveSeq<T>> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final Object[] current = {UNSET};
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

                upstream[0].request(n );


            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();

            }
        };
        upstream[0] = source.subscribe(next-> {
                    try {
                        if(current[0]== UNSET){
                            current[0]=next;
                        } else if (predicate.test((T)current[0], next)) {
                            current[0] = accumulator.apply((T)current[0], next);

                        } else {
                            final T result = (T)current[0];
                            current[0] = (T) UNSET;
                            onNext.accept(Spouts.of(result, next));
                            return;
                        }


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
                    if(current[0]!= UNSET)
                        onNext.accept(Spouts.of((T)current[0]));
                    onComplete.run();
                });
        return sub;
    }

    volatile int test = 0;
    @Override
    public void subscribeAll(Consumer<? super ReactiveSeq<T>> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        final Object[] current = {UNSET};
        boolean[] completed = {false};
        source.subscribeAll(next-> {

                    try {
                        if(current[0]== UNSET){
                            current[0]=next;
                        } else if (predicate.test((T)current[0], next)) {
                            current[0] = accumulator.apply((T)current[0], next);

                        } else {
                            final T result = (T)current[0];
                            current[0] = (T) UNSET;

                            onNext.accept(Spouts.of(result, next));
                            return;
                        }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(!completed[0]) {

                        if (current[0] != UNSET)
                            onNext.accept(Spouts.of((T) current[0]));
                        onCompleteDs.run();
                        completed[0]=true;
                    }
                });
    }
}
