package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SingleValueOperator<T> implements Operator<T> {


    final T value;


    public SingleValueOperator(T value){
        this.value = value;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        boolean[] sent = {false};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if (n > 0 && !sent[0] && isActive()) {
                    onNext.accept(value);
                    requested.decrementAndGet();
                    sent[0] = true;
                    onComplete.run();
                }

            };
            @Override
            public void request(long n) {
                singleActiveRequest(1, work);

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
        try {
            onNext.accept(value);
        } catch (Throwable t) {
            onError.accept(t);
        }
        onCompleteDs.run();
    }
}
