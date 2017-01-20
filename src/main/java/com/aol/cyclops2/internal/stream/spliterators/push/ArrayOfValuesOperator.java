package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ArrayOfValuesOperator<T> implements Operator<T> {


    final Object[] values;


    public ArrayOfValuesOperator(T... values){
        this.values = values;
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final int index[] = {0};

        boolean[] completeSent = {false};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if (n == Long.MAX_VALUE) {
                    pushAll();

                    return;
                }


                while (isActive() && index[0]<values.length) {

                    ((Consumer) onNext).accept(values[index[0]++]);
                    requested.decrementAndGet();
                }

                if (index[0] >= values.length) {
                    if(!completeSent[0]) {
                        completeSent[0]=true;
                        onComplete.run();
                        cancel();
                    }

                }

            };
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                singleActiveRequest(n,work);

            }

            private void pushAll() {
                for (; index[0] < values.length; index[0]++) {
                    if(!isOpen)
                        break;
                   ((Consumer) onNext).accept(values[index[0]]);
                }
                requested.set(0);
                if(!completeSent[0]) {
                    completeSent[0]=true;
                    onComplete.run();
                    cancel();
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
        for(int i=0;i<values.length;i++)
            ((Consumer)onNext).accept(values[i]);
        onCompleteDs.run();
    }
}
