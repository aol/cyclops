package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SkipOperator<T,R> extends BaseOperator<T,T> {


    long skip;

    public SkipOperator(Operator<T> source, long skip){
        super(source);
        this.skip = skip;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        long[] count = {0};
        boolean[] skipping ={true};
        StreamSubscription sub[] = {null};
        StreamSubscription res = new StreamSubscription(){
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                if(!isOpen)
                    return;
                super.request(n);
                sub[0].request(n);
            }

            @Override
            public void cancel() {
                sub[0].cancel();
                super.cancel();
            }
        };
        sub[0] = source.subscribe(e-> {
                    try {
                        if(skipping[0] && count[0]++<skip){
                            sub[0].request(1l);
                            if(count[0]>=skip)
                                skipping[0]=false;
                        }
                        else {
                            onNext.accept(e);
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        long[] count = {0};
        source.subscribeAll(e->{
            try {
                if (count[0]++ < skip) {

                } else {
                    onNext.accept(e);
                }
            }catch(Throwable t){
                onError.accept(t);
            }
        },onError,onCompleteDs);

    }
}
