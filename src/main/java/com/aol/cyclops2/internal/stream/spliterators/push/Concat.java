package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 01/02/2017.
 */
public class Concat<IN> {

    final AtomicLong produced = new AtomicLong(0);
    final AtomicLong requested = new AtomicLong(0);
    boolean processAll = false;
    final AtomicInteger status = new AtomicInteger(0);
    final StreamSubscription sub;
    final AtomicReference<Subscription> active = new AtomicReference<>(null);
    final AtomicReference<Subscription>  next = new AtomicReference<>(null);
    int index =0;

    final ListX<Operator<IN>> operators;
    boolean finished =false;
    final Consumer<? super IN> onNext;
    final Consumer<? super Throwable> onError;
    final Runnable onComplete;

    public Concat(StreamSubscription sub,

                            ListX<Operator<IN>> operators, Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        this.sub = sub;
        this.operators = operators;
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.subscribeNext();
        active.set(next.get());
        next.set(null);
    }

    public void request(long n) {
        if(!sub.isOpen){
            return;
        }
        if (processAll) {
            return;
        }
        //if processAll the demand is self-sustaining, passed on in onComplete
        if(n==Long.MAX_VALUE || sub.requested.get()==Long.MAX_VALUE) {
            processAll = true;
            active.get().request(Long.MAX_VALUE);
            if(next.get()!=null)
                next.get().request(Long.MAX_VALUE);
            return;
        }
        //request to currently active
        requested.accumulateAndGet(n,(a,b)->a+b);
        active.get().request(n);

        //transfer demand if changed over
        if (next.get()!=null && status.compareAndSet(0, 1)) {
            addMissingRequests();
            status.set(0);
        }



    }

    //transfer demand from previous to next
    public void addMissingRequests(){

        long prod = 0;
        long reqs = 0;

        Subscription nextLocal;
        do {
            long toRequest = 0;
            nextLocal = next.get();
            do {
                reqs = requested.getAndSet(0);
                prod = produced.getAndSet(0);

                if (reqs - prod > 0) {
                    toRequest += toRequest + (reqs - prod);
                    nextLocal.request(toRequest);
                }
            } while (reqs - prod > 0);
            requested.accumulateAndGet(toRequest,(a,b)->a+b);
        }while(!next.compareAndSet(nextLocal,nextLocal));
        active.set(next.get());
        next.set(null);

    }
    public void onError(Throwable t){
        t.printStackTrace();
        produced.incrementAndGet();
        onError.accept(t);

    }

    public void onNext(IN e){
            if(!sub.isOpen){
                return;
            }

            try {
                System.out.println("on next " +e);
                produced.incrementAndGet();
                onNext.accept(e);


            } catch (Throwable t) {
                onError.accept(t);
            }

    }


    public void onComplete(){
        //check complete
        if(index==operators.size()){
            if(!finished) {
                finished = true;
                onComplete.run();
            }
        }
        index++; //next index
        int localIndex = index;
        if(subscribeNext()) //next subscription
            return;
        System.out.println("demand " + sub.requested.get() + " " + active);
        if(this.processAll) { //if processAll process self-sustains
            if(next.get()!=null)
                next.get().request(Long.MAX_VALUE);
            active.set(next.get());
            next.set(null);
        }
        else{//otherwise transfer demand from previous to next
            if (status.compareAndSet(0, 1)) {
                addMissingRequests();
                status.set(0);
            }



        }
    }
    public boolean subscribeNext(){
        if(index==operators.size()){
            if(!finished) {
                finished = true;
                onComplete.run();
            }
            return true;
        }

        next.set(operators.get(index).subscribe(this::onNext ,this::onError,this::onComplete));
        return false;
    }

    public void unusedDemand(){
        produced.getAndSet(0);
    }

    public void cancel() {
        active.get().cancel();
        if(next.get()!=null)
            next.get().cancel();
    }
}
