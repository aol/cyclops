package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.control.either.Either5;
import lombok.Getter;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 26/01/2017.
 */
@Getter
public class Merger<T> implements Subscriber<T> {
    private final LongConsumer onFail;
    ManyToManyConcurrentArrayQueue<T> queue = new ManyToManyConcurrentArrayQueue<T>(256);
    volatile boolean complete = false;
    volatile Throwable error;
    private final  StreamSubscription sub;

    private AtomicLong requested = new AtomicLong(0);
    private AtomicLong produced =  new AtomicLong(0);
    private final Consumer<? super T> action;
    public Merger(Operator<T> operator,Consumer<? super T> action,LongConsumer onFail) {
       sub = operator.subscribe(this::onNext,this::onError,this::onComplete);
       this.action = action;
       this.onFail = onFail;
    }

    public void request(long next){
        System.out.println("Merger request.. " + next);
        if(next==Long.MAX_VALUE){
            sub.request(next);
            requested.set(next);
        }
        if(requested.get()!=Long.MAX_VALUE) {
            System.out.println("Signalling demand!! " + next + " "
                    + " Merger " + System.identityHashCode(this) + " "
                    + complete + " unused " + (requested.get()-produced.get())
            + " thread " + Thread.currentThread().getId());
            sub.request(next);
            requested.accumulateAndGet(next, (a, b) -> a + b);
        }

    }
    public void drain(){

        AtomicLong drained = new AtomicLong(0);
        System.out.println("Drain loop starting..");
        int times = 0;
        do{
            times++;
            queue.drain(n -> {
                System.out.println("Draining... " + n + " Merger " + System.identityHashCode(this));
                drained.incrementAndGet();
                action.accept(n);
            });
        }while(produced.get()+drained.get()<requested.get() && !complete && times<3);
        System.out.println("Drain loop complete.. " + drained.get());
    }
    @Override
    public void onSubscribe(Subscription s) {

    }

    @Override
    public void onNext(T t) {
        System.out.println("In merger on next " +  t);
        queue.offer(t);
        produced.incrementAndGet();
        drain();
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("On error for Merger "+ System.identityHashCode(this));
        error = t;
        t.printStackTrace();
        drain();
       // onFail.accept(1l);
    }

    public Long unusedDemand(){
        if(!complete)
            return 0l;
        drain();
        return requested.get()-produced.get();

    }
    @Override
    public void onComplete() {

        System.out.println("On complete for Merger "+ System.identityHashCode(this));
        complete = true;
       // drain();

        Long unusedDemand = unusedDemand();
        if(unusedDemand>0) {
            System.out.println("Returning unused demand.. " + unusedDemand);
            onFail.accept(unusedDemand);
        }
    }
}
