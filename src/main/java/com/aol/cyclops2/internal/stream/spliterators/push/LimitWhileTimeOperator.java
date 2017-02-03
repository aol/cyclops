package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class LimitWhileTimeOperator<T,R> extends BaseOperator<T,T> {



    private final long time;
    private final TimeUnit t;
    public LimitWhileTimeOperator(Operator<T> source, final long time, final TimeUnit t){
        super(source);
        this.time = time;
        this.t = t;



    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        final  long toRun = t.toNanos(time);
        final  long start = System.nanoTime();
        StreamSubscription sub[] = {null};
        sub[0] = source.subscribe(e-> {
                    try {

                        if(System.nanoTime()-start < toRun)
                            onNext.accept(e);
                        else{

                            sub[0].cancel();
                            onComplete.run();
                        }
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onComplete);
        return sub[0];
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {

        final  long toRun = t.toNanos(time);
        final  long start = System.nanoTime();
        source.subscribeAll(e-> {
                    try {

                        if(System.nanoTime()-start < toRun)
                            onNext.accept(e);
                        else{


                            onCompleteDs.run();
                        }
                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,onCompleteDs);

    }
}
