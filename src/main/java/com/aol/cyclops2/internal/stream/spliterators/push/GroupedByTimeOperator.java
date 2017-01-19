package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GroupedByTimeOperator<T,C extends Collection<? super T>,R> extends BaseOperator<T,R> {



    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final long time;
    private final TimeUnit t;

    public GroupedByTimeOperator(Operator<T> source, Supplier<? extends C> factory,
                                 Function<? super C, ? extends R> finalizer,long time,
                                 TimeUnit t){
        super(source);
        this.factory = factory;
        this.finalizer = finalizer;
        this.time = time;
        this.t = t;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        long toRun = t.toNanos(time);
        Collection[] next = {factory.get()};
        long[] start ={System.nanoTime()};
        StreamSubscription[] upstream = {null};
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                if(n==Long.MAX_VALUE)
                    upstream[0].request(n);
                else {
                    upstream[0].request(1);
                }
                super.request(n);
            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();
            }
        };
        upstream[0] = source.subscribe(e-> {
                    try {

                        next[0].add(e);
                        if(System.nanoTime()-start[0] > toRun){

                                onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
                            start[0] = System.nanoTime();
                            if(sub.requested.decrementAndGet()>0){
                                upstream[0].request(1l);
                            }
                        }
                        else{
                            upstream[0].request(1l);
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(next[0].size()>0)
                        onNext.accept(finalizer.apply((C)next[0]));
                    onComplete.run();
                });

        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        long toRun = t.toNanos(time);
        Collection[] next = {factory.get()};
        long[] start ={System.nanoTime()};
        source.subscribeAll(e-> {
                    try {

                        next[0].add(e);
                        if(System.nanoTime()-start[0] > toRun){
                            onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
                            start[0] = System.nanoTime();
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(next[0].size()>0)
                     onNext.accept(finalizer.apply((C)next[0]));
                    onCompleteDs.run();
                });
    }
}
