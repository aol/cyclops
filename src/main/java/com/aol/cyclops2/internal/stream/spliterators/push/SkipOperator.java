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
        StreamSubscription sub[] = {null};
        StreamSubscription res = new StreamSubscription(){
            @Override
            public void request(long n) {
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
                        if(count[0]++<skip){
                            sub[0].request(1l);
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
