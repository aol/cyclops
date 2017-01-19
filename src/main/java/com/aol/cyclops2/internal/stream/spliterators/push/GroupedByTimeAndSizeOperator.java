package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GroupedByTimeAndSizeOperator<T,C extends Collection<? super T>,R> extends BaseOperator<T,R> {



    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final long time;
    private final TimeUnit t;
    private final int groupSize;


    public GroupedByTimeAndSizeOperator(Operator<T> source, Supplier<? extends C> factory,
                                        Function<? super C, ? extends R> finalizer, long time,
                                        TimeUnit t,
                                        int groupSize){
        super(source);
        this.factory = factory;
        this.finalizer = finalizer;
        this.time = time;
        this.t = t;
        this.groupSize = groupSize;



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
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                if(!isOpen)
                    return;
                super.request(n);
                if(n==Long.MAX_VALUE)
                    upstream[0].request(n);
                else {
                    upstream[0].request(1);
                }

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
                        if(next[0].size()==groupSize || System.nanoTime()-start[0] > toRun){
                            onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
                            start[0] = System.nanoTime();
                            if(sub.requested.decrementAndGet()>0 && sub.isOpen){
                                upstream[0].request(1l);
                            }
                        }else{
                            upstream[0].request(1l);
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{onError.accept(t);
                    sub.requested.decrementAndGet();
                    if(sub.isActive())
                        upstream[0].request(1);
                },()->{
                    if(next[0].size()>0)
                        onNext.accept(finalizer.apply((C)next[0]));
                    sub.cancel();
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
                        if(next[0].size()==groupSize || System.nanoTime()-start[0] > toRun){
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
