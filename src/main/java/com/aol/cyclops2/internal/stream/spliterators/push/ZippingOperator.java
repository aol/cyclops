package com.aol.cyclops2.internal.stream.spliterators.push;

import lombok.AllArgsConstructor;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
        StreamSubscription sub = new StreamSubscription(){
            @Override
            public void request(long n) {
                leftSub[0].request(1);
                rightSub[0].request(1);
                super.request(n-1);
            }

            @Override
            public void cancel() {
                leftSub[0].cancel();
                rightSub[0].cancel();
                super.cancel();
            }
        };
        leftSub[0]  = left.subscribe(e->{
            if(rightQ.size()>0){
                onNext.accept(fn.apply((T1)e,rightQ.poll()));
            }else{
                leftQ.offer((T1)e);


            }
            createDemand(leftQ, rightQ, leftSub, rightSub, sub);
        },onError,onComplete);
        rightSub[0] = right.subscribe(e->{
            if(leftQ.size()>0){
                onNext.accept(fn.apply(leftQ.poll(),(T2)e));
            }else{
                rightQ.offer((T2)e);
            }
            createDemand(leftQ, rightQ, leftSub, rightSub, sub);
        },onError,onComplete);

        return sub;
    }

    private void createDemand(OneToOneConcurrentArrayQueue<T1> leftQ, OneToOneConcurrentArrayQueue<T2> rightQ, StreamSubscription[] leftSub, StreamSubscription[] rightSub, StreamSubscription sub) {
        if(sub.isActive() && leftQ.size() < 256 && rightQ.size()<256){
            long request = Math.min(256,sub.requested.get());
            rightSub[0].request(request);
            leftSub[0].request(request);
            sub.requested.accumulateAndGet(request,(a,b)->a-b);
        }
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        //would likely get better performance with a SPSC Queue, but would be bounded
        ConcurrentLinkedQueue<T1> leftQ = new ConcurrentLinkedQueue<T1>();
        ConcurrentLinkedQueue<T2> rightQ = new ConcurrentLinkedQueue<T2>();
        left.subscribeAll(e->{
            if(rightQ.size()>0){
                onNext.accept(fn.apply((T1)e,rightQ.poll()));
            }else{
                leftQ.offer((T1)e);
            }
        },onError,onCompleteDs);
        right.subscribeAll(e->{
            if(leftQ.size()>0){
                onNext.accept(fn.apply(leftQ.poll(),(T2)e));
            }else{
                rightQ.offer((T2)e);
            }
        },onError,onCompleteDs);

    }
}
