package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.types.mixins.Printable;
import cyclops.box.Mutable;
import cyclops.box.MutableBoolean;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
public class ZippingOperator<T1,T2,R> implements Operator<R>, Printable {


    Operator<? super T1> left;
    Operator<? super T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;



    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        OneToOneConcurrentArrayQueue<T1> leftQ = new OneToOneConcurrentArrayQueue<T1>(1024);
        OneToOneConcurrentArrayQueue<T2> rightQ = new OneToOneConcurrentArrayQueue<T2>(1024);
        StreamSubscription  leftSub[] = {null};
        StreamSubscription  rightSub[] = {null};
        AtomicBoolean leftComplete = new AtomicBoolean(false); //left & right compelte can be merged into single integer
        AtomicBoolean rightComplete = new AtomicBoolean(false);
        AtomicLong leftActive = new AtomicLong(0);
        AtomicLong rightActive = new AtomicLong(0);
        AtomicBoolean completing = new AtomicBoolean(false);
        AtomicInteger status = new AtomicInteger(0); //1st bit for left, 2 bit for right pushing

        StreamSubscription sub   = new StreamSubscription(){
            LongConsumer work = n->{


                System.out.println("DEMAND ADDED " + requested.get() + " n is " + n + " Thread "+ Thread.currentThread().getId());

                leftSub[0].request(1);
                rightSub[0].request(1);


                System.out.println("End request cycle..");
            };
            @Override
            public void request(long n) {
                if(n<=0) {
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                    return;
                }
                System.out.println("Request recieved.. " + n + " Requested is " + requested.get()  + " Thread "+ Thread.currentThread().getId());
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
            if(!sub.isOpen)
                return;

            try {

                if (!rightQ.isEmpty() ) {
                    R value = fn.apply((T1) e, rightQ.poll());
                    System.out.println("LEFT Pushing " + value + "  Thread " + Thread.currentThread().getId() + " demenad "+  sub.requested.get());
                    sub.requested.decrementAndGet();
                    onNext.accept(value);
                    rightActive.decrementAndGet();
                    if(sub.isActive()){
                        leftSub[0].request(1);
                        rightSub[0].request(1);
                    }
                    /**
                     * request more / next!
                     */


                } else {
                    System.out.println("Left offering " + e);
                    if(status.compareAndSet(0,1) && rightQ.isEmpty()) {

                        leftActive.incrementAndGet();
                        leftQ.offer((T1) e);
                        System.out.println("Left offered " + leftQ.size() + " active " + leftActive.get());

                        status.set(0);
                    }else{
                        status.compareAndSet(1,0);
                        System.out.println("Left awaiting ");
                        while(rightQ.isEmpty()){ // VALUE IS COMING
                            if(rightComplete.get() && rightQ.isEmpty()){
                                handleComplete(completing,onComplete);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply((T1) e, rightQ.poll());
                        System.out.println("LEFT Pushing " + value + "  Thread " + Thread.currentThread().getId() + " demenad "+  sub.requested.get());
                        sub.requested.decrementAndGet();
                        onNext.accept(value);
                        if(sub.isActive()){
                            leftSub[0].request(1);
                            rightSub[0].request(1);
                        }
                        rightActive.decrementAndGet();
                    }


                }
            } catch (Throwable t) {
                onError.accept(t);
            }

            if( (rightComplete.get() && rightQ.isEmpty()) || (leftComplete.get() && leftQ.isEmpty())){
                leftSub[0].cancel();
                handleComplete(completing,onComplete);

            }


        },e->{
            onError.accept(e);
            leftSub[0].request(1l);
        },()->{
            leftComplete.set(true);

            System.out.println("LEFT COMPLETE.. " + rightComplete.get());

            System.out.println("LEFT QUEUE " + leftQ.isEmpty() + " size " + leftActive.get()  + " RIGHT COMPLETE " + rightComplete.get());


            if (leftActive.get()==0 || rightComplete.get()) {
                System.out.println("Running complete! LEFT SIDE " + + leftQ.size() + " RightQ " + rightQ.size() + " thread " + Thread.currentThread().getId());

                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                handleComplete(completing,onComplete);

            }




        });
        rightSub[0] = right.subscribe(e->{
            if(!sub.isOpen)
                return;
            try {
                if (!leftQ.isEmpty()) {
                    R value =fn.apply(leftQ.poll(), (T2) e);
                    System.out.println("RIGHT Pushing " + value + "  Thread " + Thread.currentThread().getId() + " demenad "+  sub.requested.get());
                    sub.requested.decrementAndGet();
                    onNext.accept(value);
                    leftActive.decrementAndGet();
                    if(sub.isActive()){
                        System.out.println("Requesting more!!");
                        leftSub[0].request(1l);
                        rightSub[0].request(1);
                    }


                } else {

                    System.out.println("Right offering " + e);
                    if(status.compareAndSet(0,2) && leftQ.isEmpty()) {

                        rightActive.incrementAndGet();
                        rightQ.offer((T2) e);

                        System.out.println("Right offered " + rightQ.size());

                        status.set(0);
                    }else {

                        System.out.println("Right awaiting awaiting " +  leftQ.isEmpty() + " status " + status.get());
                        status.compareAndSet(2,0);
                        while (leftQ.isEmpty()) { // VALUE IS COMING
                           if(leftComplete.get() && leftQ.isEmpty()){
                                handleComplete(completing,onComplete);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply(leftQ.poll(), (T2) e);
                        System.out.println("RIGHT Pushing " + value + "  Thread " + Thread.currentThread().getId() + " demenad " + sub.requested.get());
                        sub.requested.decrementAndGet();
                        onNext.accept(value);
                        if(sub.isActive()){
                            leftSub[0].request(1);
                            rightSub[0].request(1);
                        }
                        leftActive.decrementAndGet();
                    }

                }
            }catch(Throwable t){
                onError.accept(t);
            }

            if( (leftComplete.get() && leftQ.isEmpty()) || (rightComplete.get() && rightQ.isEmpty())){
                rightSub[0].cancel();
                handleComplete(completing,onComplete);

            }

        },e->{
            onError.accept(e);
            rightSub[0].request(1l);
        },()->{

            rightComplete.set(true);
            System.out.println("RIGHT COMPLETE..");

            System.out.println("RIGHT QUEUE " + rightQ.isEmpty() + " size " + rightActive.get()  + " LEFT COMPLETE " + leftComplete.get());
            if (rightActive.get()==0 || leftComplete.get()) {
                System.out.println("Running complete! RIGHT SIDE " + rightQ.size());
                if(leftSub[0]!=null)
                    leftSub[0].cancel();
                handleComplete(completing,onComplete);

            }



        });

        return sub;
    }

    private void handleComplete(AtomicBoolean completeSent,Runnable onComplete){
        if(completeSent.compareAndSet(false,true)){
            System.out.println("RUNNING ONCOMPLETE! " + Thread.currentThread().getId());
            onComplete.run();
        }
    }



    static class VolatileBoolean{
        volatile boolean value = false;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        OneToOneConcurrentArrayQueue<T1> leftQ = new OneToOneConcurrentArrayQueue<T1>(1024);
        OneToOneConcurrentArrayQueue<T2> rightQ = new OneToOneConcurrentArrayQueue<T2>(1024);

        StreamSubscription  rightSub[] = {null};
        AtomicBoolean leftComplete = new AtomicBoolean(false); //left & right compelte can be merged into single integer
        AtomicBoolean rightComplete = new AtomicBoolean(false);
        AtomicLong leftActive = new AtomicLong(0);
        AtomicLong rightActive = new AtomicLong(0);
        AtomicBoolean completing = new AtomicBoolean(false);
        AtomicInteger status = new AtomicInteger(0); //1st bit for left, 2 bit for right pushing



        rightSub[0] = right.subscribe(e->{
            if(completing.get())
                return;
            try {
                if (!leftQ.isEmpty()) {
                    R value =fn.apply(leftQ.poll(), (T2) e);
                    System.out.println("RIGHT Pushing " + value + "  Thread " + Thread.currentThread().getId());

                    onNext.accept(value);
                    leftActive.decrementAndGet();
                    System.out.println("Right sub is " + e);
                   // rightSub[0].request(1);


                } else {

                    System.out.println("Right offering " + e);
                    if(status.compareAndSet(0,2) && leftQ.isEmpty()) {

                        rightActive.incrementAndGet();
                        rightQ.offer((T2) e);

                        System.out.println("Right offered " + rightQ.size());

                        status.set(0);
                    }else {

                        System.out.println("Right awaiting awaiting " +  leftQ.isEmpty() + " status " + status.get());
                        status.compareAndSet(2,0);
                        while (leftQ.isEmpty()) { // VALUE IS COMING
                            if(leftComplete.get() && leftQ.isEmpty()){
                                handleComplete(completing,onCompleteDs);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply(leftQ.poll(), (T2) e);
                        System.out.println("RIGHT Pushing " + value + "  Thread " + Thread.currentThread().getId());

                        onNext.accept(value);
                        leftActive.decrementAndGet();
                    }

                }
            }catch(Throwable t){
                onError.accept(t);
            }

            System.out.println("Left complete ?" + leftComplete.get() +  " left q ? " + leftQ.isEmpty());
            if( (leftComplete.get() && leftQ.isEmpty()) || (rightComplete.get() && rightQ.isEmpty())){
                rightSub[0].cancel();
                handleComplete(completing,onCompleteDs);

            }

        },e->{
            onError.accept(e);
            //rightSub[0].request(1l);
        },()->{

            rightComplete.set(true);
            System.out.println("RIGHT COMPLETE..");

            System.out.println("RIGHT QUEUE " + rightQ.isEmpty() + " size " + rightActive.get()  + " LEFT COMPLETE " + leftComplete.get());
            if (rightActive.get()==0 || leftComplete.get()) {
                System.out.println("Running complete! RIGHT SIDE " + rightQ.size());
                rightSub[0].cancel();
                handleComplete(completing,onCompleteDs);

            }



        });
        left.subscribeAll(e->{

            if(completing.get())
                return;
            try {

                if (!rightQ.isEmpty() ) {
                    R value = fn.apply((T1) e, rightQ.poll());
                    System.out.println("LEFT Pushing " + value + "  Thread " + Thread.currentThread().getId());

                    onNext.accept(value);
                    rightActive.decrementAndGet();

                    /**
                     * request more / next!
                     */
                    rightSub[0].request(1);


                } else {
                    System.out.println("Left offering " + e);
                    if(status.compareAndSet(0,1) && rightQ.isEmpty()) {

                        leftActive.incrementAndGet();
                        leftQ.offer((T1) e);
                        System.out.println("Left offered " + leftQ.size() + " active " + leftActive.get());

                        status.set(0);
                    }else{
                        status.compareAndSet(1,0);
                        System.out.println("Left awaiting ");
                        while(rightQ.isEmpty()){ // VALUE IS COMING
                            if(rightComplete.get() && rightQ.isEmpty()){
                                handleComplete(completing,onCompleteDs);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply((T1) e, rightQ.poll());
                        System.out.println("LEFT Pushing " + value + "  Thread " + Thread.currentThread().getId());

                        onNext.accept(value);
                        rightActive.decrementAndGet();
                    }


                }
            } catch (Throwable t) {
                onError.accept(t);
            }

            if( (rightComplete.get() && rightQ.isEmpty()) || (leftComplete.get() && leftQ.isEmpty())){
                rightSub[0].cancel();
                handleComplete(completing,onCompleteDs);

            }


        },e->{
            onError.accept(e);


        },()->{
            leftComplete.set(true);

            System.out.println("LEFT COMPLETE.. " + rightComplete.get());

            System.out.println("LEFT QUEUE " + leftQ.isEmpty() + " size " + leftActive.get()  + " RIGHT COMPLETE " + rightComplete.get());


            if (leftActive.get()==0 || rightComplete.get()) {
                System.out.println("Running complete! LEFT SIDE " + + leftQ.size() + " RightQ " + rightQ.size() + " thread " + Thread.currentThread().getId());

                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                handleComplete(completing,onCompleteDs);

            }




        });
        rightSub[0].request(Long.MAX_VALUE);

    }
    private void drainAll(Queue<T1> leftQ, OneToOneConcurrentArrayQueue<T2> rightQ, Consumer<? super R> onNext) {
        while(leftQ.size()>0 && rightQ.size()>0){


            onNext.accept(fn.apply(leftQ.poll(), rightQ.poll()));

        }
    }

}
