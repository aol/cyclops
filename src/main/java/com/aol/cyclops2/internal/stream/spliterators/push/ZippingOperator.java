package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.box.Mutable;
import cyclops.box.MutableBoolean;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
        boolean[] stopRequests = {false};
        AtomicBoolean completing = new AtomicBoolean(false);
        StreamSubscription sub = new StreamSubscription(){
            LongConsumer work = n->{
                if(n==Long.MAX_VALUE){
                    while(leftSub[0].isOpen && rightSub[0].isOpen && !stopRequests[0]){
                        leftSub[0].request(1);
                        rightSub[0].request(1);
                    }

                    return;
                }
                requested.accumulateAndGet(Math.min(n,256),(a,n2)->a-n2);
                leftSub[0].request(Math.min(n,256));
                rightSub[0].request(Math.min(n,256));
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
                if(leftSub[0]!=null)
                     leftSub[0].cancel();
                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                super.cancel();
            }
        };
        leftSub[0]  = left.subscribe(e->{


            try {

                if (rightQ.size() > 0) {
                    onNext.accept(fn.apply((T1) e, rightQ.poll()));
                    if(sub.isActive() && !stopRequests[0]){
                        leftSub[0].request(1);
                        rightSub[0].request(1);
                        sub.requested.decrementAndGet();

                    }

                    if(stopRequests[0] && rightQ.size()==0){
                        leftSub[0].cancel();
                        onComplete.run();

                    }
                } else {
                    leftQ.offer((T1) e);


                }
            } catch (Throwable t) {
                onError.accept(t);
            }

        },onError,()->{

            drain(leftQ,rightQ,onNext);



            if (leftQ.size()==0 || stopRequests[0]) {
                completing.set(true);
                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                onComplete.run();

            }
            stopRequests[0]=true;



        });
        rightSub[0] = right.subscribe(e->{

            try {
                if (leftQ.size() > 0) {

                    onNext.accept(fn.apply(leftQ.poll(), (T2) e));
                    if(sub.isActive() &&!stopRequests[0]){
                        leftSub[0].request(1);
                        rightSub[0].request(1);
                        sub.requested.decrementAndGet();

                    }

                    if(stopRequests[0] && leftQ.size()==0){
                        rightSub[0].cancel();
                        onComplete.run();

                    }
                } else {
                        rightQ.offer((T2) e);
                }
            }catch(Throwable t){
                onError.accept(t);
            }

        },onError,()->{


            drain(leftQ,rightQ,onNext);

            if (rightQ.size()==0 || stopRequests[0]) {
                if(leftSub[0]!=null)
                 leftSub[0].cancel();
                onComplete.run();

            }
            stopRequests[0]=true;


        });

        return sub;
    }

    private void drain(Queue<T1> leftQ, OneToOneConcurrentArrayQueue<T2> rightQ, Consumer<? super R> onNext) {
        while(leftQ.size()>0 && rightQ.size()>0){
            onNext.accept(fn.apply(leftQ.poll(), rightQ.poll()));
        }
    }

    static class VolatileBoolean{
        volatile boolean value = false;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        LinkedBlockingDeque<T1> leftQ = new LinkedBlockingDeque<>();
        OneToOneConcurrentArrayQueue<T2> rightQ = new OneToOneConcurrentArrayQueue<T2>(1024);
        StreamSubscription  rightSub[] = {null};
        VolatileBoolean leftComplete = new VolatileBoolean();
        VolatileBoolean rightComplete =new VolatileBoolean();
        left.subscribeAll(e->{


            try {

                if (rightQ.size() > 0) {
                    onNext.accept(fn.apply((T1) e, rightQ.poll()));
                    if(!rightComplete.value){

                        rightSub[0].request(1);


                    }

                    if(rightComplete.value && rightQ.size()==0){

                        onCompleteDs.run();

                    }
                } else {
                    leftQ.offer((T1) e);


                }
            } catch (Throwable t) {
                onError.accept(t);
            }

        },onError,()->{

            drain(leftQ,rightQ,onNext);



            if (leftQ.size()==0 || rightComplete.value) {

                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                onCompleteDs.run();

            }
            leftComplete.value=true;



        });
        rightSub[0] = right.subscribe(e->{

            try {
                if (leftQ.size() > 0) {

                    onNext.accept(fn.apply(leftQ.poll(), (T2) e));
                    if(!rightComplete.value){

                        rightSub[0].request(1);


                    }

                    if(leftComplete.value && leftQ.size()==0){
                        rightSub[0].cancel();
                        onCompleteDs.run();

                    }
                } else {
                    rightQ.offer((T2) e);
                }
            }catch(Throwable t){
                onError.accept(t);
            }

        },onError,()->{


            drain(leftQ,rightQ,onNext);

            if (rightQ.size()==0 || leftComplete.value) {

                onCompleteDs.run();

            }
            rightComplete.value=true;


        });
        rightSub[0].request(1l);

    }

}
