package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class StreamSubscription implements Subscription {
    volatile boolean isActive;
    AtomicLong requested= new AtomicLong(0);
    public boolean isActive(){
        return isActive && requested.get()>0;
    }
    @Override
    public void request(long n) {

        if(requested.get()==Long.MAX_VALUE)
            return;
        if(n==Long.MAX_VALUE)
            requested.set(n);
        requested.accumulateAndGet(n,(a,b)->a+b);
    }

    @Override
    public void cancel() {
        isActive = false;
    }
}
