package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class CollectAllOperator<T,A,R> extends BaseOperator<T,R> {

    private final Collector<? super T, A, R> collector;


    public CollectAllOperator(Operator<T> source, Collector<? super T, A, R> collector){
        super(source);
        this.collector = collector;

    }


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        Object[] next = {collector.supplier().get()};
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
                        A nextA = (A)next[0];

                        collector.accumulator().accept(nextA,e);

                        upstream[0].request(1l);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,t->{onError.accept(t);
                    sub.requested.decrementAndGet();
                    if(sub.isActive())
                     upstream[0].request(1);
                },()->{
                    A nextA = (A)next[0];
                    try {
                        System.out.println("Finishing! Collect All!");
                        onNext.accept(collector.finisher().apply(nextA));
                    }catch(Throwable t){
                        onError.accept(t);
                    }
                    onComplete.run();
                });
        return sub;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Object[] next = {collector.supplier().get()};
        source.subscribeAll(e-> {
                    try {

                        A nextA = (A)next[0];
                        collector.accumulator().accept(nextA,e);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    A nextA = (A)next[0];
                    try {

                        onNext.accept(collector.finisher().apply(nextA));
                    }catch(Throwable t){
                        onError.accept(t);
                    }
                    onCompleteDs.run();
                });
    }
}
