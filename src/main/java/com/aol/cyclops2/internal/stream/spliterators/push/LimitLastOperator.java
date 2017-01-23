package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.ArrayDeque;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitLastOperator<T,R> extends BaseOperator<T,T> {


     final int limit;

    public LimitLastOperator(Operator<T> source, int limit){
        super(source);
        this.limit = limit;

    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final ArrayDeque<T> buffer = new ArrayDeque<T>(limit);
        StreamSubscription upstream[] = {null};
        Runnable[] thunk = {()->{}};
        StreamSubscription result = new StreamSubscription(){
            @Override
            public void request(long n) {
                super.request(n);
                upstream[0].request(n );
                thunk[0].run();

            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();
            }
        };
        upstream[0] = source.subscribe(e-> {
                    if (buffer.size() == limit) {
                        buffer.poll();
                    }
                    upstream[0].request(1l);

                    buffer.offer(e);
                }
                ,onError,()->{
                 thunk[0] = ()->{
                    while(buffer.size()>0){
                        if(result.isActive())
                            onNext.accept(buffer.poll());
                        else
                            return;
                        result.requested.decrementAndGet();
                    }
                    onComplete.run();
                 };

                 thunk[0].run();
                });
        return result;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        final ArrayDeque<T> buffer = new ArrayDeque<T>(limit);
       
        source.subscribeAll(e-> {
                    if (buffer.size() == limit) {
                        buffer.poll();
                    }
                    buffer.offer(e);
                }
                ,onError,()->{
                    for(T next : buffer)
                        onNext.accept(next);

                    onCompleteDs.run();
                });
    }
}
