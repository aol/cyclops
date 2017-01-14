package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
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
            @Override
            public void request(long n) {
                long items = n;
                while(items-->0 && isOpen) {
                    next.accept( current[0] = (current[0]!=null ? fn.apply((T)current[0]) : in));

                }

            }

            @Override
            public void cancel() {
                onComplete.run();
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
