package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.control.either.Either5;
import lombok.Getter;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.agrona.concurrent.QueuedPipe;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 26/01/2017.
 */
@Getter
public class Merger<T> implements Subscriber<T> {
    private final LongConsumer onFail;
    final QueuedPipe<T>  queue;
    volatile boolean complete = false;
    volatile Throwable error;
    private final  StreamSubscription sub;
    private final Consumer<? super Throwable> errorAction;

    private final AtomicLong requested = new AtomicLong(0);
    private final AtomicLong produced =  new AtomicLong(0);
    private final Runnable completionHandler;
    private final Consumer<? super T> action;
    private final AtomicBoolean wip;
    public Merger(AtomicBoolean wip,QueuedPipe<T> queue,Operator<T> operator,Consumer<? super T> action,final Consumer<? super Throwable> error,
                  LongConsumer onFail,Runnable completionHandler) {
        this.wip = wip;
        this.queue =queue;
       sub = operator.subscribe(this::onNext,this::onError,this::onComplete);
       this.action = action;
       this.onFail = onFail;
       this.errorAction = error;
       this.completionHandler = completionHandler;
    }

    final AtomicBoolean requestingOrCompleting = new AtomicBoolean();

    public void request(long next){
        if(!sub.isOpen)
            return;
        System.out.println("Merger request.. " + next + " merger "+ System.identityHashCode(this));
        if(next==Long.MAX_VALUE){
            System.out.println("Requesting ALL!");
            sub.request(next);
            requested.set(next);
        }
        if(requested.get()!=Long.MAX_VALUE) {
            System.out.println("Signalling demand!! " + next + " "
                    + " Merger " + System.identityHashCode(this) + " "
                    + complete + " unused " + (requested.get()-produced.get())
                    + " produced " + produced.get() + " requested " + requested.get()
                    + " thread " + Thread.currentThread().getId());

            long toRequest = 0;
            if(requestingOrCompleting.compareAndSet(false,true)) {
                System.out.println("Requesting " + next + " "
                        + " Merger " + System.identityHashCode(this) + " "
                        + complete + " unused " + (requested.get()-produced.get())
                        + " produced " + produced.get() + " requested " + requested.get()
                        + " thread " + Thread.currentThread().getId());

                requested.accumulateAndGet(next, (a, b) -> a + b);
                toRequest= next;
               // sub.request(next);
                requestingOrCompleting.set(false);

                System.out.println("Finished Requesting " + next + " "
                        + " Merger " + System.identityHashCode(this) + " "
                        + complete + " unused " + (requested.get()-produced.get())
                        + " produced " + produced.get() + " requested " + requested.get()
                        + " thread " + Thread.currentThread().getId());

            }
            if(toRequest>0)
               sub.request(toRequest);

        }

    }
    public void drain(){
        while(!queue.isEmpty()) {
            System.out.println("WIP is " + wip.get());
            if (!wip.compareAndSet(false, true)) {

                return;
            }
            System.out.println("GOT WIP is " + wip.get());
            AtomicLong drained = new AtomicLong(0);
            System.out.println("Drain loop starting..");
            if (error != null) {
                errorAction.accept(error);
                error = null;
            }
            int times = 0;

                queue.drain(n -> {
                    System.out.println("Draining... " + n + " Merger " + System.identityHashCode(this));
                    drained.incrementAndGet();
                    action.accept(n);
                });


            System.out.println("Drain loop complete.. " + drained.get());

            wip.set(false);
        }
    }
    @Override
    public void onSubscribe(Subscription s) {

    }

    @Override
    public void onNext(T t) {
        if(!sub.isOpen)
            return;
        System.out.println("In merger on next " +  t);
        produced.incrementAndGet();
        while(!queue.offer(t)){
            drain();
        }
        drain();
    }

    @Override
    public void onError(Throwable t) {
        if(!sub.isOpen)
            return;
        System.out.println("On error for Merger "+ System.identityHashCode(this));
        error = t;
        t.printStackTrace();
        drain();

    }

    public Long unusedDemand(){

        drain();
        return requested.get()-produced.get();

    }
    @Override
    public void onComplete() {
        drain();


        System.out.println("On complete for Merger "+ System.identityHashCode(this)  + " thread " + Thread.currentThread().getId() + " reqOrComplete?" + requestingOrCompleting.get());

        while(!requestingOrCompleting.compareAndSet(false,true)) {
            //update to number of requested elements coming - demand to return
        }
        System.out.println("Processing on complete "+ System.identityHashCode(this)  + " thread " + Thread.currentThread().getId());

       // drain();

        Long unusedDemand = unusedDemand();
        System.out.println("Unused demand "+ unusedDemand + " merger " + System.identityHashCode(this)
                + " thread " + Thread.currentThread().getId()
                +" queue " + queue.isEmpty() );
        //System.out.println("On complete for Merger "+ System.identityHashCode(this) + " unused demand " + unusedDemand);
        if(unusedDemand>0) {
            System.out.println("Returning unused demand.. " + unusedDemand);
            onFail.accept(unusedDemand);
        }
        complete = true;
        completionHandler.run();

    }
}
