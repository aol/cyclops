package com.aol.cyclops2.internal.stream.spliterators.push;

import lombok.AllArgsConstructor;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
@AllArgsConstructor
public class ZippingLatestOperator<T1,T2,R> implements Operator<R> {


    Operator<? super T1> left;
    Operator<? super T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;

    final static Object UNSET = new Object();

    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        OneToOneConcurrentArrayQueue<T1> leftQ = new OneToOneConcurrentArrayQueue<T1>(1024);
        OneToOneConcurrentArrayQueue<T2> rightQ = new OneToOneConcurrentArrayQueue<T2>(1024);
        StreamSubscription  leftSub[] = {null};
        StreamSubscription  rightSub[] = {null};
        boolean[] stopRequests = {false};
        AtomicBoolean completing = new AtomicBoolean(false);
        Object[] lastLeft = {UNSET};
        Object[] lastRight = {UNSET};

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
                leftSub[0].cancel();
                rightSub[0].cancel();
                super.cancel();
            }
        };
        leftSub[0]  = left.subscribe(e->{

            lastLeft[0]=e;
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
                }else if(lastRight[0]!=UNSET) {
                    onNext.accept(fn.apply((T1)e, (T2) lastRight[0]));

                    if(sub.isActive() &&!stopRequests[0]){
                        leftSub[0].request(1);
                        sub.requested.decrementAndGet();

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
                rightSub[0].cancel();
                onComplete.run();

            }
            stopRequests[0]=true;



        });
        rightSub[0] = right.subscribe(e->{
            lastRight[0]=e;
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
                } else if(lastLeft[0]!=UNSET) {
                    onNext.accept(fn.apply((T1)lastLeft[0], (T2) e));

                    if(sub.isActive() &&!stopRequests[0]){
                        rightSub[0].request(1);
                        sub.requested.decrementAndGet();

                    }
                }else{
                     rightQ.offer((T2) e);
                }

            }catch(Throwable t){
                onError.accept(t);
            }

        },onError,()->{


            drain(leftQ,rightQ,onNext);

            if (rightQ.size()==0 || stopRequests[0]) {

                leftSub[0].cancel();
                onComplete.run();

            }
            stopRequests[0]=true;


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
