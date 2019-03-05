package com.oath.cyclops.internal.stream.spliterators.push;

import com.oath.cyclops.types.persistent.PersistentCollection;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GroupedByTimeOperator<T,C extends PersistentCollection<? super T>,R> extends BaseOperator<T,R> {



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
        PersistentCollection[] next = {factory.get()};
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

                upstream[0].request(n);


            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();
            }
        };
        upstream[0] = source.subscribe(e-> {
                    try {

                        next[0] = next[0].plus(e);
                        if(System.nanoTime()-start[0] > toRun){

                            onNext.accept(finalizer.apply((C)next[0]));
                            sub.requested.decrementAndGet();
                            next[0] = factory.get();
                            start[0] = System.nanoTime();

                        }
                        else{
                            request( upstream,1l);
                        }

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{onError.accept(t);
                    sub.requested.decrementAndGet();
                    if(sub.isActive())
                        request( upstream,1);
                },()->{
                    if(next[0].size()>0) {
                        try {
                            onNext.accept(finalizer.apply((C) next[0]));
                        } catch(Throwable t){
                            onError.accept(t);
                        }
                        sub.requested.decrementAndGet();
                    }
                    sub.cancel();
                    onComplete.run();
                });

        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        long toRun = t.toNanos(time);
        PersistentCollection[] next = {factory.get()};
        long[] start ={System.nanoTime()};
        source.subscribeAll(e-> {
                    try {

                        next[0] = next[0].plus(e);
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
                    if(next[0].size()>0) {
                        try {
                            onNext.accept(finalizer.apply((C) next[0]));
                        } catch(Throwable t){
                            onError.accept(t);
                        }
                    }
                    onCompleteDs.run();
                });
    }
}
