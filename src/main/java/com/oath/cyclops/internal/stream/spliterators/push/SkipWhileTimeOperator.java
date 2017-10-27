package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SkipWhileTimeOperator<T,R> extends BaseOperator<T,T> {


    private final long time;
    private final TimeUnit t;

    public SkipWhileTimeOperator(Operator<T> source, long time, TimeUnit t){
        super(source);
        this.time = time;
        this.t = t;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final  long toRun = t.toNanos(time);
        long start = System.nanoTime();

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
                        if(System.nanoTime()-start >= toRun){
                            onNext.accept(e);
                        }else{
                            sub[0].request(1l);
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
        final  long toRun = t.toNanos(time);
        long start = System.nanoTime();
        source.subscribeAll(e->{
            try {
                if (System.nanoTime() - start >= toRun) {
                    onNext.accept(e);
                }
            }catch (Throwable t) {

                onError.accept(t);
            }
        },onError,onCompleteDs);

    }
}
