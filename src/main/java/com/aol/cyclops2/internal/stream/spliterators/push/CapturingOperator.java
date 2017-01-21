package com.aol.cyclops2.internal.stream.spliterators.push;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Subscription;

public class CapturingOperator<T> implements Operator<T> {


    
    @Getter
    Consumer<? super T> action;
    @Getter
    Consumer<? super Throwable> error;
    @Getter
    Runnable onComplete;
    final Subscription s;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    public CapturingOperator(Subscription s){
        this.s = s;
    }

    public CapturingOperator(){
        this.subscription = new StreamSubscription();
        this.s=new Subscription() {
            @Override
            public void request(long n) {

            }

            @Override
            public void cancel() {

            }
        };
    }

    StreamSubscription subscription = new StreamSubscription(){
        @Override
        public void request(long n) {
             s.request(n);
        }

        @Override
        public void cancel() {
            s.cancel();
            super.cancel();
        }
    };





    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        this.action=onNext;
        this.error = onError;
        this.onComplete = onComplete;
        this.initialized.set(true);
        return subscription;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        this.action=onNext;
        this.error = onError;
        this.onComplete = onComplete;
        this.initialized.set(true);
    }

    public boolean isInitialized() {
        return initialized.get();
    }
}
