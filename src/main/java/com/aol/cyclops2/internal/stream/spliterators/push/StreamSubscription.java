package com.aol.cyclops2.internal.stream.spliterators.push;

import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 *
 */
public class StreamSubscription implements Subscription {
    public volatile boolean isOpen = true;
    AtomicLong requested= new AtomicLong(0);
    public boolean isActive(){
        return isOpen && requested.get()>0;
    }

    AtomicBoolean active = new AtomicBoolean(false);
    public void singleActiveRequest(long n, BooleanSupplier work){


        this.requestInternal(n);
        while(requested.get()!=0) {

                if (active.compareAndSet(false, true)) {

                    if(work.getAsBoolean()){
                        return;
                    }
                    active.set(false);
                }else{
                    return;
                }
            }




    }
    private void requestInternal(long n) {

        if(requested.get()==Long.MAX_VALUE)
            return;
        if(n==Long.MAX_VALUE)
            requested.set(n);
        requested.accumulateAndGet(n,(a,b)->a+b);
    }

    @Override
    public void request(long n) {
        requestInternal(n);
    }

    @Override
    public void cancel() {
        isOpen = false;
    }
}
