package com.aol.cyclops2.internal.stream.spliterators.push;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ArrayMergingOperator<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public ArrayMergingOperator(Operator<IN>[] sources){
        this.operators=sources;


    }

    private void subscribe(int index, Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs){
        operators[index].subscribeAll(e-> {
                    try {
                        onNext.accept(e);
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                        if(index+1 < operators.length)
                            subscribe(index+1,onNext,onError,onCompleteDs);
                        onCompleteDs.run();
                });
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);

        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if(isActive()) {

                    int toUse = index.incrementAndGet()-1;
                    if(toUse+1>=subs.size())
                        index.set(0);
                    subs.get(toUse).request(1l);



                }

            };
            @Override
            public void request(long n) {
                super.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };

        for(Operator<IN> next : operators){
            subs.add(next.subscribe(e-> {
                        try {
                            onNext.accept(e);
                            sub.requested.decrementAndGet();
                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{
                            if(sub.isActive()) {
                                int toUse = index.incrementAndGet()-1;
                                if(toUse+1>=subs.size())
                                    index.set(0);
                                subs.get(toUse).request(1l);




                            }
                        }
                    }
                    ,onError,()->{
                        if(completed.incrementAndGet()== subs.size()){
                            onComplete.run();
                        }

                    }));
        }

        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        subscribe(onNext,onError,onCompleteDs).request(Long.MAX_VALUE);
       //subscribeAll(0,onNext,onError,onCompleteDs);
    }
}
