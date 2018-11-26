package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.control.Either;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

/**
 * Created by johnmcclean on 12/01/2017.
 *
 */
public class StreamSubscription implements Subscription {
    public volatile boolean isOpen = true;
    protected final AtomicLong requested= new AtomicLong(0);
    public boolean isActive(){
        return isOpen && requested.get()>0;
    }

    public boolean singleActiveRequest(long n, LongConsumer work){
        if(this.requestInternal(n)) {
            work.accept(n);
            return true;
        }

        return false;
    }


    private boolean requestInternal(long n) {

        for (; ; ) {
            long currentRequests = requested.get();
            if (Long.MAX_VALUE==currentRequests) {
                return false;
            }
            long newTotal = currentRequests + n;
            if (requested.compareAndSet(currentRequests, newTotal <0 ? Long.MAX_VALUE : newTotal)) {
                return currentRequests==0;
            }

        }
    }


    @Override
    public void request(long n) {
        requestInternal(n);
    }

    @Override
    public void cancel() {
        isOpen = false;
    }

    public long getRequested() {
        return requested.get();
    }
}
