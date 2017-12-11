package com.oath.cyclops.internal.stream.spliterators.push;


import com.oath.cyclops.async.adapters.Queue;
import cyclops.collections.mutable.ListX;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class LazyConcat<IN> {

    final AtomicLong produced = new AtomicLong(0);
    final AtomicLong requested = new AtomicLong(0);
    boolean processAll = false;

    final StreamSubscription sub;
    final AtomicReference<Subscription> active = new AtomicReference<>(null);
    final AtomicReference<Subscription>  next = new AtomicReference<>(null);


    final Publisher<Operator<IN>> operators;
    AtomicBoolean finished = new AtomicBoolean(false);
    final Consumer<? super IN> onNext;
    final Consumer<? super Throwable> onError;
    final Runnable onComplete;
    final AtomicLong queued = new AtomicLong(0);
    AtomicInteger wip = new AtomicInteger(0);

    ManyToOneConcurrentArrayQueue<Operator<IN>> ops = new ManyToOneConcurrentArrayQueue<>(1024);
    Subscription operatorsSub;

    public LazyConcat(StreamSubscription sub,

                      ListX<Operator<IN>> operators, Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        this.sub = sub;
        this.operators = operators;
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;

        operatorsSubscription();
        operatorsSub.request(1);
    }




    public void request(long n) {

        if(!sub.isOpen){
            return;
        }
        if (processAll) {
            return;
        }
        if(wip.compareAndSet(0,1)){

            //if processAll the demand is self-sustaining, passed on in onComplete
            if (n == Long.MAX_VALUE || sub.requested.get() == Long.MAX_VALUE) {
                processAll = true;

                Subscription sa = null;
                Subscription nextLocal = null;

                do {
                    sa = active.get();
                    if (sa != null) {
                        sa.request(Long.MAX_VALUE);
                    }
                    nextLocal = next.get();
                    if (nextLocal != null) {
                        nextLocal.request(Long.MAX_VALUE);

                    }
                }while(sa==null && nextLocal==null && !complete);
                return;
            }
            requested.accumulateAndGet(n,(a,b)->a+b);

            Subscription local = active.get();
            if (decrementAndCheckActive()) {
                addMissingRequests();
            }
            if(local!=null) {
                local.request(n);
            }

        }else{

            queued.accumulateAndGet(n,(a,b)->a+b);

            if(incrementAndCheckInactive()){

                addMissingRequests();
            }

        }



    }

    private boolean decrementAndCheckActive() {
        return wip.decrementAndGet()!=0;
    }

    //transfer demand from previous to next
    public void addMissingRequests(){


        int missed=1;
        long toRequest=0L;


        do {
            long localQueued = queued.getAndSet(0l);
            Subscription localSub = next.getAndSet(null);
            long missedOutput = produced.get();



            Subscription localActive = active.get();
            long reqs = requested.get() + localQueued;

            if(reqs<0 || toRequest<0) {
                processAll=true;
                if(localSub!=null)
                    localSub.request(Long.MAX_VALUE);
                if(localActive!=null)
                    localActive.request(Long.MAX_VALUE);
                return;
            }
            requested.set(reqs);
            if(localSub!=null){
                active.set(localSub);
                toRequest +=reqs;
            }else if(localQueued !=0 && localActive!=null) {
                toRequest += reqs;
            }
            missed = wip.accumulateAndGet(missed,(a,b)->a-b);

        }while(missed!=0);

        if(toRequest>0) {

            active.get().request(toRequest);
        }

    }
    public void emitted(long n) {
        if (processAll) {
            return;
        }
        if (wip.compareAndSet(0, 1)) {
            requested.accumulateAndGet(n,(a,b)->a-b);
            if (decrementAndCheckActive()) {
                addMissingRequests();
            }

        }else {
            produced.accumulateAndGet(n, (a, b) -> a + b);
            if (incrementAndCheckInactive()) {
                addMissingRequests();
            }
        }
    }

    public void onError(Throwable t){
        emitted(1);
        onError.accept(t);

    }

    public void onNext(IN e){

        emitted(1);
        if(!sub.isOpen){
            return;
        }

        try {

            onNext.accept(e);


        } catch (Throwable t) {
            onError.accept(t);
        }

    }

    public void onComplete(){

        running =false;
        handleMainPublisher();

    }

    volatile boolean complete = false;


    private void operatorsSubscription() {
        operators.subscribe(new Subscriber<Operator<IN>>() {
            @Override
            public void onSubscribe(Subscription s) {
                operatorsSub = s;
            }

            @Override
            public void onNext(Operator<IN> inOperator) {

                ops.add((Operator<IN>)nilsafeIn(inOperator));
                handleMainPublisher();

            }

            @Override
            public void onError(Throwable t) {

                onError.accept(t);
            }

            @Override
            public void onComplete() {

                complete =true;
                handleMainPublisher();
            }
        });
    }
    private Object nilsafeIn(Object o){
        if(o==null)
            return Queue.NILL;
        return o;
    }
    private <T> T nilsafeOut(Object o){
        if(Queue.NILL==o){
            return null;
        }
        return (T)o;
    }
    volatile boolean running = false;
    AtomicInteger mainActive = new AtomicInteger(0);
    int consumed =0;
    int limit =1;
    void handleMainPublisher() {
        if (mainActive.getAndIncrement()==0) {
            do {
                if (!sub.isOpen) {
                    return;
                }
                if (!running) {
                    Object next = ops.poll();
                    if (complete && next==null) {
                        onComplete.run();
                        break;
                    }else if(next!=null) {
                        operatorsSub.request(1l);
                        Operator<IN> pub = (Operator<IN>) nilsafeOut(next);
                        running = true;
                        nextSub(pub);
                    }
                }
            } while (mainActive.decrementAndGet()!=0) ;

        }
    }

    public void nextSub(Operator<IN> op){

        Subscription local = op.subscribe(this::onNext ,this::onError,this::onComplete);
        if(processAll){

            local.request(Long.MAX_VALUE);
            active.set(local);
            return;
        }
        if (wip.compareAndSet(0, 1)) {
            active.set(local);
            long r = requested.get();
            if (decrementAndCheckActive()) {

                this.addMissingRequests();
            }

            if(r>0) {
                local.request(r);
            }
            return;
        }
        next.set(local);
        if(incrementAndCheckInactive()){
            this.addMissingRequests();
            return;
        }
        return;
    }

    private boolean incrementAndCheckInactive() {
        return wip.getAndIncrement()==0;
    }


    public void cancel() {
        Subscription local = active.get();
        if(local!=null)
            local.cancel();
        local = next.get();
        if(local!=null)
            local.cancel();
    }
}
