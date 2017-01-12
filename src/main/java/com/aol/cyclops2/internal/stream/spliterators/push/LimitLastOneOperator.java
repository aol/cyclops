package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitLastOneOperator<T,R> extends BaseOperator<T,T> {




    public LimitLastOneOperator(Operator<T> source){
        super(source);

    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Object UNSET = new Object();
        Object[] last = {UNSET};
        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    last[0] = e;
                }
                ,onError,()->{
                    if(last[0]!=UNSET)
                        onNext.accept((T)last[0]);
                });
        return sub[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        Object UNSET = new Object();
        Object[] last = {UNSET};

        source.subscribeAll(e-> {
                    last[0] = e;
                }
                ,onError,()->{
                    if(last[0]!=UNSET)
                        onNext.accept((T)last[0]);
                });
    }
}
