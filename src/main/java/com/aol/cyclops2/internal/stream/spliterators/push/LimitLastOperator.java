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
        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    if (buffer.size() == limit) {
                        buffer.poll();
                    }
                    buffer.offer(e);
                }
                ,onError,()->{
                    for(T next : buffer)
                        onNext.accept(next);
                });
        return sub[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        final ArrayDeque<T> buffer = new ArrayDeque<T>(limit);
       
        source.subscribe(e-> {
                    if (buffer.size() == limit) {
                        buffer.poll();
                    }
                    buffer.offer(e);
                }
                ,onError,()->{
                    for(T next : buffer)
                        onNext.accept(next);
                });
    }
}
