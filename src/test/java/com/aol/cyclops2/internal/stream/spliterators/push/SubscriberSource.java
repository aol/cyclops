package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.internal.stream.spliterators.push.Operator;
import com.aol.cyclops2.internal.stream.spliterators.push.StreamSubscription;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.function.Consumer;

/**
 * Created by johnmcclean on 17/01/2017.
 */
public class SubscriberSource<T> implements Operator<T>, Subscriber<T> {
    Subscription s;
    Consumer<? super T> onNext;
    Consumer<? super Throwable> onError;
    Runnable onComplete;
    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        return new StreamSubscription(){
            @Override
            public void request(long n)
            {   s.request(n);
                super.request(n);
            }

            @Override
            public void cancel() {
                s.cancel();
                super.cancel();
            }
        };
    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

    }

    @Override
    public void onSubscribe(Subscription s) {
        if(this.s==null) {
            this.s = s;
            s.request(1l);
        }
        else{
            s.cancel();
        }

    }

    @Override
    public void onNext(T t) {
        if(t==null)
            throw new NullPointerException();
          this.onNext.accept(t);
    }

    @Override
    public void onError(Throwable t) {
        if(t==null)
            throw new NullPointerException();
          this.onError.accept(t);
    }

    @Override
    public void onComplete() {
        this.onComplete.run();
    }
}
