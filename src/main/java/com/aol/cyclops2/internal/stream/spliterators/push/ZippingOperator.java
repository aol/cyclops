package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.util.box.Mutable;
import com.aol.cyclops2.util.box.MutableBoolean;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
@AllArgsConstructor
public class ZippingOperator<T1,T2,R> implements Operator<R>{


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


                leftSub[0].request(1);
                rightSub[0].request(1);



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
            if(!sub.isOpen)
                return;

            try {

                if (!rightQ.isEmpty() ) {
                    R value = fn.apply((T1) e, rightQ.poll());
                    sub.requested.decrementAndGet();
                    onNext.accept(value);
                    rightActive.decrementAndGet();
                    if(sub.requested.get()>1){
                        leftSub[0].request(1);
                        rightSub[0].request(1);
                    }
                    /**
                     * request more / next!
                     */


                } else {

                    if(status.compareAndSet(0,1) && rightQ.isEmpty()) {

                        leftActive.incrementAndGet();
                        leftQ.offer((T1) e);


                        status.set(0);
                    }else{
                        status.compareAndSet(1,0);

                        while(rightQ.isEmpty()){ // VALUE IS COMING - RIGHT IS ADDING TO Q
                            if(rightComplete.get() && rightQ.isEmpty()){
                                handleComplete(completing,onComplete);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply((T1) e, rightQ.poll());
                        sub.requested.decrementAndGet();
                        onNext.accept(value);
                        if(sub.requested.get()>1){
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


            if (leftActive.get()==0 || rightComplete.get()) {
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

                    sub.requested.decrementAndGet();
                    onNext.accept(value);
                    leftActive.decrementAndGet();
                    if(sub.requested.get()>1){

                        leftSub[0].request(1l);
                        rightSub[0].request(1);
                    }


                } else {


                    if(status.compareAndSet(0,2) && leftQ.isEmpty()) {

                        rightActive.incrementAndGet();
                        rightQ.offer((T2) e);



                        status.set(0);
                    }else {

                        status.compareAndSet(2,0);
                        while (leftQ.isEmpty()) { // VALUE IS COMING  - LEFT IS ADDING TO Q
                           if(leftComplete.get() && leftQ.isEmpty()){
                                handleComplete(completing,onComplete);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply(leftQ.poll(), (T2) e);

                        sub.requested.decrementAndGet();
                        onNext.accept(value);
                        if(sub.requested.get()>1){
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

            if (rightActive.get()==0 || leftComplete.get()) {
                if(leftSub[0]!=null)
                    leftSub[0].cancel();
                handleComplete(completing,onComplete);

            }



        });

        return sub;
    }

    private void handleComplete(AtomicBoolean completeSent,Runnable onComplete){
        if(completeSent.compareAndSet(false,true)){
            onComplete.run();
        }
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


                    onNext.accept(value);
                    leftActive.decrementAndGet();



                } else {


                    if(status.compareAndSet(0,2) && leftQ.isEmpty()) {

                        rightActive.incrementAndGet();
                        rightQ.offer((T2) e);



                        status.set(0);
                    }else {

                        status.compareAndSet(2,0);
                        while (leftQ.isEmpty()) { // VALUE IS COMING
                            if(leftComplete.get() && leftQ.isEmpty()){
                                handleComplete(completing,onCompleteDs);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply(leftQ.poll(), (T2) e);

                        onNext.accept(value);
                        leftActive.decrementAndGet();
                    }

                }
            }catch(Throwable t){
                onError.accept(t);
            }


            if( (leftComplete.get() && leftQ.isEmpty()) || (rightComplete.get() && rightQ.isEmpty())){
                rightSub[0].cancel();
                handleComplete(completing,onCompleteDs);

            }

        },e->{
            onError.accept(e);

        },()->{

            rightComplete.set(true);

            if (rightActive.get()==0 || leftComplete.get()) {

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


                    onNext.accept(value);
                    rightActive.decrementAndGet();

                    /**
                     * request more / next!
                     */
                    rightSub[0].request(1);


                } else {

                    if(status.compareAndSet(0,1) && rightQ.isEmpty()) {

                        leftActive.incrementAndGet();
                        leftQ.offer((T1) e);


                        status.set(0);
                    }else{
                        status.compareAndSet(1,0);

                        while(rightQ.isEmpty()){ // VALUE IS COMING
                            if(rightComplete.get() && rightQ.isEmpty()){
                                handleComplete(completing,onCompleteDs);
                                return;
                            }
                            LockSupport.parkNanos(0);
                        }
                        R value = fn.apply((T1) e, rightQ.poll());
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


            if (leftActive.get()==0 || rightComplete.get()) {
                if(rightSub[0]!=null)
                    rightSub[0].cancel();
                handleComplete(completing,onCompleteDs);

            }




        });
        rightSub[0].request(Long.MAX_VALUE);

    }


}
