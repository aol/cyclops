package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SpliteratorToOperator<T> implements Operator<T> {


    final Spliterator<T> split;
    Runnable run;

    public SpliteratorToOperator(Spliterator<? super T> split){
         this.split = (Spliterator<T>)split;


    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        boolean closed[]= {false};
        boolean canAdvance[] = {true};
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n-> {


                    while(isActive() && canAdvance[0]) {
                        try {

                            canAdvance[0] = split.tryAdvance(onNext);
                            if(canAdvance[0])
                                requested.decrementAndGet();


                        } catch (Throwable t) {
                            t.printStackTrace();
                            onError.accept(t);
                        }
                    }
                    if(!canAdvance[0] || !isOpen) {
                        if(!closed[0]) {
                            closed[0] = true;
                            onComplete.run();
                        }
                    }


            };
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                if(isOpen)
                    this.singleActiveRequest(n,work);

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

        boolean canAdvance = true;
        while(canAdvance){
                try {
                    canAdvance = split.tryAdvance(onNext);
                } catch (Throwable t) {
                    onError.accept(t);
                }
        }


        onCompleteDs.run();




    }
}
