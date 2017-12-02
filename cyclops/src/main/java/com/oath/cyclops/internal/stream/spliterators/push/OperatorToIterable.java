package com.oath.cyclops.internal.stream.spliterators.push;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class OperatorToIterable<T,R>  implements Iterable<T> {

    Operator<T> source;
    final Consumer<? super Throwable> defaultErrorHandler;
    final boolean async;

    public OperatorToIterable(Operator<T> source, Consumer<? super Throwable> defaultErrorHandler){
       this.source= source;
       this.defaultErrorHandler = defaultErrorHandler;
       async=false;

    }
    public OperatorToIterable(Operator<T> source, Consumer<? super Throwable> defaultErrorHandler,boolean async){
        this.source= source;
        this.defaultErrorHandler = defaultErrorHandler;
        this.async = async;

    }

    public Iterator<T> iterator(){
        return new Iterator<T>() {
            boolean active = false;
            final Object UNSET = new Object();
            final AtomicReference value = new AtomicReference<>(UNSET);
            final AtomicReference error = new AtomicReference<>(UNSET);
            final AtomicBoolean done = new AtomicBoolean(false);
            volatile boolean requested = false;
            volatile  boolean awaiting = false;
            StreamSubscription sub = source.subscribe(e ->{
                value.set(e);
                awaiting = false;
            } , e -> {
                error.set(e);
                awaiting = false;
            }, () -> {

                done.set(true);
                awaiting = false;
            });

            public void forEachRemaining(Consumer<? super T> action) {
               // if(async || active)
                    Iterator.super.forEachRemaining(action);
               // else
                 //     source.subscribeAll(action,defaultErrorHandler,()->{});
            }


            boolean unRead(){
                return (value.get()!=UNSET || error.get()!=UNSET);
            }
            boolean complete(){
                if(done.get() && !unRead())
                    return true;
                return false;
            }
            @Override
            public boolean hasNext() {

                if(complete())
                    return false;
                if (!requested) {
                    awaiting = true;
                    sub.request(1l);
                    requested = true;
                    while(awaiting && !done.get()){

                        LockSupport.parkNanos(0l);
                    }

                }
                return (!done.get() || value.get()!=UNSET || error.get()!=UNSET) ;
            }

            @Override
            public T next() {
                active=true;
                if (!hasNext()) {
                    throw new NoSuchElementException();

                }
                requested = false;
                if (error.get() != UNSET) {
                    Throwable t = (Throwable)error.get();
                    error.set(UNSET);
                    defaultErrorHandler.accept(t);
                }
                T result = (T)value.get();
                value.set(UNSET);
                return result;
            }

        };
    }


}
