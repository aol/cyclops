package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class MergeLatestOperator<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public MergeLatestOperator(Operator<IN>[] sources){
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
                System.out.println("n is "+ n);
                for(long k=0;k<Math.max(n,subs.size());k++) {
                    if(!isActive())
                        break;
                        int toUse = index.incrementAndGet() - 1;
                        if (toUse+1 >= subs.size()) {
                            index.set(0);

                        }
                        System.out.println("Requesting 1 from " +  subs.get(toUse));
                        subs.get(toUse).request(1l);




                }

            };
            @Override
            public void request(long n) {
                System.out.println("Request!! n is "+ n);
                super.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };

        for(int i=0;i<operators.length;i++){
            int current = i;
            subs.add(operators[current].subscribe(e-> {
                        try {
                            onNext.accept(e);
                            System.out.println("Merging! " + e);
                            sub.requested.decrementAndGet();
                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{
                            if(sub.isActive()) {
                                subs.get(current).request(1l);
                            }
                        }
                    }
                    ,onError,()->{

                        if(completed.incrementAndGet()== operators.length){
                            System.out.println("Running on complete");
                            onComplete.run();
                            sub.cancel();
                        }
                        System.out.println("Complete " + completed.get());

                    }));
        }

        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);




        for(int i=0;i<operators.length;i++){
            int current = i;
            operators[current].subscribeAll(e-> {
                        try {
                            onNext.accept(e);
                            System.out.println("Merging! " + e);

                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{

                        }
                    }
                    ,onError,()->{

                        if(completed.incrementAndGet()== operators.length){
                            System.out.println("Running on complete");
                            onCompleteDs.run();

                        }
                        System.out.println("Complete " + completed.get());

                    });
        }


    }
}
