package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class IteratePredicateOperator<T> implements Operator<T> {


    private final T in;
    private final UnaryOperator<T> fn;
    private final Predicate<? super T> pred;

    public IteratePredicateOperator(T in, UnaryOperator<T> fn,Predicate<? super T> pred){
        this.in=  in;
        this.fn = fn;
        this.pred = pred;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Object[] current = {null};
        Consumer next = onNext;
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n-> {
                if(n==Long.MAX_VALUE){
                    pushAll();
                    return;
                }

                while (isActive()) {
                    current[0] = (current[0] != null ? fn.apply((T) current[0]) : in);
                    if(pred.test((T)current[0])) {
                        next.accept(current[0]);
                        requested.decrementAndGet();
                    }else{
                        cancel();
                        onComplete.run();
                        return;
                    }

                }


            };
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                if(!isOpen)
                    return;
                this.singleActiveRequest(n,work);

            }

            private void pushAll() {
                for(;;){
                    if(!isOpen)
                        break;
                    try {
                        current[0] = (current[0] != null ? fn.apply((T) current[0]) : in);
                        if(pred.test((T)current[0])) {
                            next.accept(current[0]);
                        }
                        else{
                            cancel();
                            onComplete.run();
                            return;
                        }
                    }catch(Throwable t){
                        onError.accept(t);
                    }
                }
                requested.set(0);
            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        T current = null;
        for(;;){
            try {
                current = (current != null ? fn.apply(current) : in);
                if(pred.test((T)current))
                    onNext.accept(current);
               else {
                    onCompleteDs.run();
                    return;
                }
            }catch(Throwable t){
                onError.accept(t);
            }
        }
    }
}
