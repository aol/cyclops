package com.aol.cyclops2.internal.stream.spliterators.push;

import cyclops.collections.ListX;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class Concat<IN> {

    final AtomicLong produced = new AtomicLong(0);
    final AtomicLong requested = new AtomicLong(0);
    boolean processAll = false;

    final StreamSubscription sub;
    final AtomicReference<Subscription> active = new AtomicReference<>(null);
    final AtomicReference<Subscription>  next = new AtomicReference<>(null);
    int index =0;

    final ListX<Operator<IN>> operators;
    boolean finished =false;
    final Consumer<? super IN> onNext;
    final Consumer<? super Throwable> onError;
    final Runnable onComplete;
    final AtomicLong queued = new AtomicLong(0);
    AtomicInteger wip = new AtomicInteger(0);


    public Concat(StreamSubscription sub,

                   ListX<Operator<IN>> operators, Consumer<? super IN> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        this.sub = sub;
        this.operators = operators;
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.subscribeNext();

    }


    public void request(long n) {
        System.out.println("Concat request " + n);
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
                active.get().request(Long.MAX_VALUE);
                Subscription nextLocal = next.get();
                if(nextLocal!=null) {
                    nextLocal.request(Long.MAX_VALUE);

                }
                return;
            }
            requested.accumulateAndGet(n,(a,b)->a+b);
            System.out.println("Requested now " + requested.get());
            Subscription local = active.get();
            if (decrementAndCheckActive()) {
                addMissingRequests();
            }
            System.out.println("Requesting from " + local);
            local.request(n);

        }else{

            queued.accumulateAndGet(n,(a,b)->a+b);
            System.out.println("Enqueuing " + queued.get() + " wip is " + wip.get());
            if(incrementAndCheckInactive()){
                System.out.println("Transfer demand..");
                addMissingRequests();
            }
            System.out.println("WIP is "  + wip.get());
        }



    }

    private boolean decrementAndCheckActive() {
        return wip.decrementAndGet()!=0;
    }

    //transfer demand from previous to next
    public void addMissingRequests(){

        System.out.println("ADD MISSING REQS Transfering demand");
        int missed=1;
        long toRequest=0L;


        do {
            long localQueued = queued.getAndSet(0l);
            Subscription localSub = next.getAndSet(null);
            long missedOutput = produced.get();



            Subscription localActive = active.get();
            long reqs = requested.get() + localQueued;
            System.out.println("Local queued " + localQueued + " missed " + missedOutput + " reqs " + reqs + " localSub "+ localSub);
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
        System.out.println("************************************************Missing reqs " + toRequest + " " + active.get());
        if(toRequest>0)
            active.get().request(toRequest);

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
        System.out.println("On next..");
        emitted(1);
        if(!sub.isOpen){
            return;
        }

        try {
            System.out.println("demand before dec " + sub.requested.get() + " t " + Thread.currentThread().getId());
            System.out.println("demand onnext " + sub.requested.decrementAndGet() + " " + active + " t " + Thread.currentThread().getId());
            System.out.println("on next " +e + " reqs " + requested.get() + " prod " + produced.get() + " t " + Thread.currentThread().getId());

            onNext.accept(e);


        } catch (Throwable t) {
            onError.accept(t);
        }

    }


    public void onComplete(){
        System.out.println("On complete!");
        //check complete
        if(index==operators.size()){
            if(!finished) {
                finished = true;
                onComplete.run();
            }
        }


        index++; //next index
        int localIndex = index;
        subscribeNext(); //next subscription





    }



    public boolean subscribeNext(){
        if(index==operators.size()){
            if(!finished) {
                finished = true;
                onComplete.run();
            }
            return true;
        }
        Subscription local = operators.get(index).subscribe(this::onNext ,this::onError,this::onComplete);
        if(processAll){
            local.request(Long.MAX_VALUE);
            active.set(local);
            return false;
        }
        if (wip.compareAndSet(0, 1)) {
            System.out.println("Subscribe next is active..");
            active.set(local);

            long r = requested.get();

            if (decrementAndCheckActive()) {
                System.out.println("Subscribe next transferring demand..");
                this.addMissingRequests();
            }
            System.out.println("Sub next Wip is " + wip.get() + "  r " + r);
            if(r>0)
                local.request(r);
            return false;
        }
        System.out.println("Enqueing next sub " + local);
        next.set(local);
        if(incrementAndCheckInactive()){
            this.addMissingRequests();
            return false;
        }


        return false;
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
