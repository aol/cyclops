package com.aol.cyclops2.types;


import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by johnmcclean on 26/01/2017.
 */
public interface Completable<T> {

    boolean isFailed();

    boolean isDone();
    boolean complete(T complete);
    boolean completeExceptionally(Throwable error);


    static class CompletablePublisher<T> implements Publisher<T>, Completable<T>{
        private Subscriber<? super T> sub;
        private volatile T result;
        private volatile Throwable error;
        private AtomicBoolean isComplete = new AtomicBoolean(false);
        @Override
        public void subscribe(Subscriber<? super T> s) {
            this.sub = s;
        }

        @Override
        public boolean isFailed() {
            return isComplete.get() && error!=null;
        }

        @Override
        public boolean isDone() {
            return isComplete.get();
        }

        @Override
        public boolean complete(T complete) {
            if(isComplete.compareAndSet(false,true)){
                result = complete;
                sub.onNext(complete);
                sub.onComplete();
                return true;
            }else{
                return false;
            }
        }

        @Override
        public boolean completeExceptionally(Throwable error) {
            if(isComplete.compareAndSet(false,true)){
                error= error;
                sub.onError(error);
                return true;
            }else{
                return false;
            }
        }
    }

}

