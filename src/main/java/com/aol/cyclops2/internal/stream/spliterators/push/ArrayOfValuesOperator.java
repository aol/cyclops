package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

        int[] index ={0};
        AtomicBoolean completeSent = new AtomicBoolean(false);
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if (n == Long.MAX_VALUE) {
                    pushAll();

                    return;
                }
                long reqs = n;
                int delivered = 0;
                do{


                    while (delivered < reqs && index[0] < values.length) {
                        if (!isOpen)
                            return;
                        ((Consumer) onNext).accept(values[index[0]++]);
                        delivered++;

                    }


                    if (index[0] >= values.length) {
                        if (!completeSent.get()) {
                            completeSent.set(true);
                            onComplete.run();
                          
                            return;
                        }

                    }
                    reqs = requested.get();
                    if(reqs==delivered) {
                        reqs = requested.accumulateAndGet(delivered, (a, b) -> a - b);
                        if(reqs==0)
                            return;
                        delivered=0;

                    }

                }while(true);


            };
            @Override
            public void request(long n) {
                if(n<=0)
                    onError.accept(new IllegalArgumentException( "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                singleActiveRequest(n,work);

            }

            private void pushAll() {
                int local = index[0];
                for (; local < values.length; local++) {
                    if(!isOpen)
                        break;
                   ((Consumer) onNext).accept(values[local]);
                }

                if(!completeSent.get()) {
                    completeSent.set(true);
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
