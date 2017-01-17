package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.UnaryOperator;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class IterateOperator<T> implements Operator<T> {


    private final T in;
    private final UnaryOperator<T> fn;

    public IterateOperator(T in, UnaryOperator<T> fn){
        this.in=  in;
        this.fn = fn;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Object[] current = {in};
        Consumer next = onNext;
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n-> {
                if(n==Long.MAX_VALUE){
                    pushAll();
                    return;
                }
                while (isActive()) {

                    next.accept(current[0] = (current[0] != null ? fn.apply((T) current[0]) : in));
                    requested.decrementAndGet();

                }

            };
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                this.singleActiveRequest(n,work);

            }

            private void pushAll() {
                for(;;){
                    if(!isOpen)
                        break;
                    try {
                        next.accept(current[0] = (current[0] != null ? fn.apply((T) current[0]) : in));
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
        T current = in;
        for(;;){
            try {
                onNext.accept(current = (current != null ? fn.apply(current) : in));
            }catch(Throwable t){
                onError.accept(t);
            }
        }
    }
}
