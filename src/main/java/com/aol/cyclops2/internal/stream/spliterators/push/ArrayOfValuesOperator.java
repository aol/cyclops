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
        System.out.println("Displaying " + values.length);
        for(int i=0;i<values.length;i++) {
            System.out.println(values[i]);
        }
        System.out.println("Displaying Complete ");
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final int index[] = {0};


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
                    onComplete.run();

                }

            };
            @Override
            public void request(long n) {
                singleActiveRequest(n,work);

            }

            private void pushAll() {
                for (; index[0] < values.length; index[0]++) {
                    if(!isOpen)
                        break;
                   ((Consumer) onNext).accept(values[index[0]]);
                }
                requested.set(0);
                onComplete.run();
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
