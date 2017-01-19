package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.box.Mutable;
import cyclops.collections.immutable.PVectorX;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class SlidingOperator<T,C extends Collection<? super T>,R> extends BaseOperator<T,R> {




    private final Function<? super PVectorX<T>, ? extends R> finalizer;
    private final int windowSize;
    private final int increment;

    public SlidingOperator(Operator<T> source,  Function<? super PVectorX<T>, ? extends R> finalizer,
                           int windowSize, int increment){
        super(source);

        this.finalizer = finalizer;
        this.windowSize = windowSize;
        this.increment = increment;



    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

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
                    upstream[0].request(n);
                }

            }

            @Override
            public void cancel() {
                upstream[0].cancel();
                super.cancel();
            }
        };
        final Mutable<PVector<T>> list = cyclops.box.Mutable.of(TreePVector.empty());
        boolean[] sent = {false};
        upstream[0] = source.subscribe(e-> {
                    try {
                        list.mutate(var -> var.plus(Math.max(0, var.size()),e));
                        if(list.get().size()==windowSize) {

                            onNext.accept(finalizer.apply(PVectorX.fromIterable(list.get())));
                            sub.requested.decrementAndGet();
                            sent[0] = true;
                            for (int i = 0; i < increment && list.get()
                                    .size() > 0; i++)
                                list.mutate(var -> var.minus(0));
                        }else if(sub.isOpen){
                            upstream[0].request(1l);
                            sent[0]=false;
                        }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{
                    onError.accept(t);
                    sub.requested.decrementAndGet();
                    if(sub.isActive())
                        upstream[0].request(1);
                },()->{
                    if(!sent[0] && list.get().size()>0)
                        onNext.accept(finalizer.apply(PVectorX.fromIterable(list.get())));
                    sub.requested.decrementAndGet();
                    onComplete.run();
                });
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        final Mutable<PVector<T>> list = cyclops.box.Mutable.of(TreePVector.empty());
        boolean[] sent = {false};
        source.subscribeAll(e-> {
                    try {
                        list.mutate(var -> var.plus(Math.max(0, var.size()),e));
                        if(list.get().size()==windowSize) {

                            onNext.accept(finalizer.apply(PVectorX.fromIterable(list.get())));
                            sent[0] = true;
                            for (int i = 0; i < increment && list.get()
                                    .size() > 0; i++)
                                list.mutate(var -> var.minus(0));
                        }else{
                            sent[0]=false;
                        }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    if(!sent[0]  && list.get().size()>0)
                        onNext.accept(finalizer.apply(PVectorX.fromIterable(list.get())));
                    onCompleteDs.run();
                });
    }
}
