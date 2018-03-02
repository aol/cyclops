package com.oath.cyclops.internal.stream.spliterators.push;

import com.oath.cyclops.types.persistent.PersistentCollection;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class GroupingOperator<T,C extends PersistentCollection<? super T>,R> extends BaseOperator<T,R> {

    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final int groupSize;

    public GroupingOperator(Operator<T> source, Supplier<? extends C> factory,
                            Function<? super C, ? extends R> finalizer,
                int groupSize){
        super(source);
        this.factory = factory;
        this.finalizer = finalizer;
        this.groupSize = groupSize;
    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        PersistentCollection[] next = {factory.get()};
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

                 upstream[0].request(n ); //we can't multiply by groupSize - doesn't work with Sets


            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();

            }
        };
        upstream[0] = source.subscribe(e-> {
                    try {
                        next[0]=next[0].plus(e);
                        if(next[0].size()==groupSize){
                            onNext.accept(finalizer.apply((C)next[0]));

                            next[0] = factory.get();

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
                    if(next[0].size()>0) {
                        try {
                            onNext.accept(finalizer.apply((C) next[0]));
                        } catch(Throwable t){
                            onError.accept(t);
                        }
                        sub.requested.decrementAndGet();
                    }
                    onComplete.run();
                });
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        PersistentCollection[] next = {factory.get()};
        source.subscribeAll(e-> {
                    try {
                        next[0]=next[0].plus(e);
                        if(next[0].size()==groupSize){
                            onNext.accept(finalizer.apply((C)next[0]));
                            next[0] = factory.get();
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
