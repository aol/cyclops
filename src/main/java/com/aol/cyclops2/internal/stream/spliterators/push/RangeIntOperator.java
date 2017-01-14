package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.internal.stream.spliterators.push.util.Decorators;
import cyclops.async.Queue;

import java.util.function.Consumer;
import java.util.function.Function;

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
        int[] index = {0};
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                singleActiveRequest(n,()-> {
                    if(requested.get()==Long.MAX_VALUE) {
                        pushAll();
                        return true;
                    }

                    while (isActive() && index[0] < end) {
                        try {
                            requested.decrementAndGet();
                            ((Consumer) onNext).accept(index[0]++);
                        } catch (Throwable t) {
                            onError.accept(t);
                        }

                    }
                    if(index[0]==end){
                        onComplete.run();
                        return true;
                    }
                    return false;
                });
            }
            private void pushAll() {
                for(;index[0]<end;index[0]++){
                    if(!isOpen)
                        break;
                    try {
                        if(!isOpen)
                            ((Consumer) onNext).accept(index[0]);
                    }catch(Throwable t){
                        onError.accept(t);
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

        for(int i=start;i<end;i++){
             ((Consumer) onNext).accept(i);

        }
        onCompleteDs.run();
    }
}
