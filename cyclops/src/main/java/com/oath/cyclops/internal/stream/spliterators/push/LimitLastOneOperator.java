package com.oath.cyclops.internal.stream.spliterators.push;

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
        StreamSubscription upstream[] = {null};


        StreamSubscription result = new StreamSubscription(){
            @Override
            public void request(long n) {
                super.request(n);
                upstream[0].request(n );


            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();
            }
        };
        upstream[0] = source.subscribe(e-> {
                    last[0] = e;
                    upstream[0].request(1l);
                }
                ,onError,()->{

                    if (result.isActive() && last[0] != UNSET) {
                        onNext.accept((T) last[0]);
                    }
                    onComplete.run();

                });
        return result;
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
                    onCompleteDs.run();
                });
    }
}
