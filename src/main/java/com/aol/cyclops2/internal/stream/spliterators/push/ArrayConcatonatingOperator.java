package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class ArrayConcatonatingOperator<IN> implements Operator<IN> {


    private final Operator<IN>[] operators;


    public ArrayConcatonatingOperator(Operator<IN>[] sources){
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
                        subscribe(index+1,onNext,onError,onCompleteDs);
                        onCompleteDs.run();
                });
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        List<StreamSubscription> subs = new ArrayList<>(operators.length);
        int index[] = {0};
        boolean[] finished = {false};
        long[] count = {0};
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                if(isOpen && index[0]++ < subs.size() && !finished[0]) {
                    subs.get(index[0]).request(1l);
                    count[0]++;
                }
                super.request(n-1);
            }

            @Override
            public void cancel() {
                super.cancel();
            }
        };

        for(Operator<IN> next : operators){
            next.subscribe(e-> {
                        try {
                            onNext.accept(e);
                        } catch (Throwable t) {

                            onError.accept(t);
                        }finally{
                            if(sub.isOpen && count[0]< sub.requested.get()) {
                                subs.get(index[0]).request(1l);
                                count[0]++;
                            }
                        }
                    }
                    ,onError,()->{

                        if(index[0]++ >= subs.size()) {
                            onComplete.run();
                            finished [0] = true;
                        }
                        else{
                            if(sub.isOpen && count[0]< sub.requested.get()) {
                                subs.get(index[0]).request(1l);
                                count[0]++;
                            }
                        }
                    });
        }

        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

       subscribe(0,onNext,onError,onCompleteDs);
    }
}
