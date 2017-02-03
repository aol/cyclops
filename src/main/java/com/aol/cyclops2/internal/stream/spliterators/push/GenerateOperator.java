package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GenerateOperator<T> implements Operator<T> {


    final Supplier<T> value;


    public GenerateOperator(Supplier<T> value){
        this.value = value;

    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        boolean[] sent = {false};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{

                long reqs = n;
                long delivered = 0;
                do {

                    while (delivered < reqs) {
                        if (!isOpen)
                            return;
                        onNext.accept(value.get());
                        delivered++;
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
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                if(!isOpen)
                    return;
                singleActiveRequest(n, work);

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
            onNext.accept(value.get());
        } catch (Throwable t) {
            onError.accept(t);
        }
        onCompleteDs.run();
    }
}
