package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.box.Mutable;
import cyclops.box.MutableBoolean;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
@AllArgsConstructor
public class ZippingOperator<T1,T2,R> implements Operator<R> {


    Operator<? super T1> left;
    Operator<? super T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        OneToOneConcurrentArrayQueue<T1> leftQ = new OneToOneConcurrentArrayQueue<T1>(1024);
        OneToOneConcurrentArrayQueue<T2> rightQ = new OneToOneConcurrentArrayQueue<T2>(1024);
        StreamSubscription  leftSub[] = {null};
        StreamSubscription  rightSub[] = {null};

        AtomicBoolean completing = new AtomicBoolean(false);
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if(n==Long.MAX_VALUE){
                    while(leftSub[0].isOpen && rightSub[0].isOpen){
                        leftSub[0].request(1);
                        rightSub[0].request(1);
                    }
                    onComplete.run();
                    return;
                }
                requested.accumulateAndGet(n,(a,n2)->a-n2);
                leftSub[0].request(n);
                rightSub[0].request(n);
            };
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                this.singleActiveRequest(n,work);

            }

            @Override
            public void cancel() {
                leftSub[0].cancel();
                rightSub[0].cancel();
                super.cancel();
            }
        };
        leftSub[0]  = left.subscribe(e->{


            try {

                if (rightQ.size() > 0) {

                    onNext.accept(fn.apply((T1) e, rightQ.poll()));
                } else {
                    leftQ.offer((T1) e);


                }
            } catch (Throwable t) {
                onError.accept(t);
            }

        },onError,()->{

            drain(leftQ,rightQ,onNext);


            if (leftQ.size()==0 && !completing.get()) {
                completing.set(true);
                rightSub[0].cancel();
                onComplete.run();

            }




        });
        rightSub[0] = right.subscribe(e->{

            try {
                if (leftQ.size() > 0) {

                    onNext.accept(fn.apply(leftQ.poll(), (T2) e));

                } else {
                    rightQ.offer((T2) e);
                }
            }catch(Throwable t){
                onError.accept(t);
            }

        },onError,()->{


            drain(leftQ,rightQ,onNext);

            if (rightQ.size()==0 && !completing.get()) {
                completing.set(true);
                leftSub[0].cancel();
                onComplete.run();

            }


        });

        return sub;
    }

    private void drain(OneToOneConcurrentArrayQueue<T1> leftQ, OneToOneConcurrentArrayQueue<T2> rightQ, Consumer<? super R> onNext) {
        while(leftQ.size()>0 && rightQ.size()>0){
            onNext.accept(fn.apply(leftQ.poll(), rightQ.poll()));
        }
    }



    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        subscribe(onNext,onError,onCompleteDs).request(Long.MAX_VALUE);

    }
}
