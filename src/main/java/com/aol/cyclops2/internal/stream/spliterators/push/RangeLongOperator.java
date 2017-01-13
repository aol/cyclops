package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class RangeLongOperator<Long> implements Operator<Long> {


    final long start;
    final long end;

    public RangeLongOperator(long start, long end){
        this.start = start;
        this.end = end;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super Long> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
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
                if(index[0]>=end)
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
    public void subscribeAll(Consumer<? super Long> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        for(long i=start;i<end;i++){
            try {
                ((Consumer) onNext).accept(i);
            }catch(Throwable t){
                onError.accept(t);
            }
        }
        onCompleteDs.run();
    }
}
