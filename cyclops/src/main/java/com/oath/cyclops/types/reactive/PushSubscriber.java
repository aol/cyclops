package com.oath.cyclops.types.reactive;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public interface PushSubscriber<T>{
    public void onNext(T t);


    public void onError(Throwable t);


    public void onComplete();

    boolean isInitialized();

    static <T> PushSubscriber<T> of(Subscriber<T> sub){
        sub.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {

            }

            @Override
            public void cancel() {

            }
        });
        return new PushSubscriber<T>() {
            @Override
            public void onNext(T t) {
                sub.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                sub.onError(t);
            }

            @Override
            public void onComplete() {
                sub.onComplete();
            }

            @Override
            public boolean isInitialized() {
                return true;
            }
        };
    }
    default Subscriber<T> asSubscriber(){

        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(T t) {
                PushSubscriber.this.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                PushSubscriber.this.onError(t);
            }

            @Override
            public void onComplete() {
                PushSubscriber.this.onComplete();
            }
        };
    }
}
