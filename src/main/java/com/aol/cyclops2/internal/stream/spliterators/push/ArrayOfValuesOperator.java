package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

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


        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                if(n==Long.MAX_VALUE){
                    for(;index[0]<values.length;index[0]++){
                        if(isOpen)
                            ((Consumer)onNext).accept(values[index[0]]);
                    }
                    onComplete.run();

                    return;
                }


                for(long x =0;index[0]<values.length && x++<n;index[0]++){
                    if(isOpen)
                        ((Consumer)onNext).accept(values[index[0]]);
                }
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
