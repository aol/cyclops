package com.aol.cyclops2.internal.stream.spliterators.push;

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
                long items = n;
                while(items-->0 && index[0] < end && isOpen) {
                    try {
                        ((Consumer) onNext).accept(index[0]++);
                    }catch(Throwable t){
                        onError.accept(t);
                    }

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
    public void subscribeAll(Consumer<? super Integer> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        for(int i=start;i<end;i++){
            try {
                ((Consumer) onNext).accept(i);
            }catch(Throwable t){
                onError.accept(t);
            }
        }
        onCompleteDs.run();
    }
}
