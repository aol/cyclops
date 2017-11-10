package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SkipLastOneOperator<T,R> extends BaseOperator<T,T> {


    static final Object UNSET = new Object();

    public SkipLastOneOperator(Operator<T> source){
        super(source);
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        Object[] buffer = {UNSET};
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
                        if(buffer[0]!=UNSET){
                            onNext.accept((T)buffer[0]);
                        }
                        else {

                            sub[0].request(1);

                        }
                        buffer[0]=e;

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Object[] buffer = {UNSET};
        source.subscribeAll(e->{
            try {
                if (buffer[0] == UNSET) {

                } else {

                    onNext.accept((T)buffer[0]);

                }
                buffer[0]=e;
            }catch(Throwable t){
                onError.accept(t);
            }
        },onError,onCompleteDs);

    }
}
