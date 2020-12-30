package com.oath.cyclops.internal.stream.spliterators.push;

import lombok.Getter;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CapturingOperator<T> implements Operator<T> {



    @Getter
    Consumer<? super T> action;
    @Getter
    Consumer<? super Throwable> error;
    @Getter
    Runnable onComplete;
    Subscription s;
    volatile boolean complete;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    public CapturingOperator(Subscription s){
        this.s = s;
        onInit = ()->{};
    }


    final Runnable onInit;
    public CapturingOperator(Runnable onInit){
        this.onInit = onInit;
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
    public CapturingOperator(){
        this.onInit = ()->{};
        this.subscription = new StreamSubscription();
        this.s=new Subscription() {
            @Override
            public void request(long n) {
                if(complete)
                    onComplete.run();
            }

            @Override
            public void cancel() {

            }
        };
    }

    public void setSubscription(Subscription s){
        this.s =s;
    }

    StreamSubscription subscription = new StreamSubscription(){
        @Override
        public void request(long n) {
            if(complete)
                onComplete.run();
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
        onInit.run();
        return subscription;
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        this.action=onNext;
        this.error = onError;
        this.onComplete = onComplete;
        this.initialized.set(true);
        onInit.run();
        subscription.request(Long.MAX_VALUE);
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public void complete() {
        complete = true;
    }
}
