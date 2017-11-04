package com.oath.cyclops.internal.stream.spliterators.push;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class RangeIntOperator implements Operator<Integer> {


    final int start;
    final int end;

    public RangeIntOperator(int start, int end){
        this.start = start;
        this.end = end;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        int[] index = {start};
        AtomicBoolean completed = new AtomicBoolean(false);
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work =  n ->{
                if(n==Long.MAX_VALUE) {
                    pushAll();
                    return;
                }
                long reqs = n;
                int delivered = 0;
                do {



                    while (delivered < reqs && index[0] < end) {
                        if (!isOpen)
                            return;
                        try {

                            ((Consumer) onNext).accept(index[0]++);
                            delivered++;
                        } catch (Throwable t) {
                            onError.accept(t);
                        }

                    }
                    if (index[0] == end) {
                        if (!completed.get()) {
                            completed.set(true);
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
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                singleActiveRequest(n,work);
            }
            private void pushAll() {
                for(;index[0]<end;index[0]++){

                    try {
                        if(isOpen)
                            ((Consumer) onNext).accept(index[0]);
                        else
                            break;
                    }catch(Throwable t){
                        onError.accept(t);
                    }
                }
                if(index[0]==end){
                    if (!completed.get()) {
                        completed.set(true);
                        onComplete.run();
                    }

                }
                requested.set(0);
            }


            @Override
            public void cancel() {
                super.cancel();
            }
        };
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {


        for (int i = start; i < end; i++) {
            ((Consumer) onNext).accept(i);

        }

        onCompleteDs.run();
    }
}
