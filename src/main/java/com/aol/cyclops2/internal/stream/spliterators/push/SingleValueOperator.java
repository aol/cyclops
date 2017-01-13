package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

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
            @Override
            public void request(long n) {
                if(n>0 && !sent[0]) {
                    onNext.accept(value);
                    sent[0] = true;
                }

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

        onNext.accept(value);
        onCompleteDs.run();
    }
}
