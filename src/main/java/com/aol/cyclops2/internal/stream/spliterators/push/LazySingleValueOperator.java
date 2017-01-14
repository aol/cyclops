package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LazySingleValueOperator<T,R> implements Operator<R> {


    final T value;
    private final Function<? super T,? extends R> fn;

    public LazySingleValueOperator(T value,Function<? super T,? extends R> fn){
        this.value = value;
        this.fn = fn;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        boolean[] sent = {false};
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                singleActiveRequest(1, () -> {

                    if (n > 0 && !sent[0]) {
                        onNext.accept(fn.apply(value));
                        sent[0] = true;
                    }
                    return true;
                });
            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        onNext.accept(fn.apply(value));
        onCompleteDs.run();
    }
}
