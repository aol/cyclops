package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.util.ExceptionSoftener;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class OperatorToIterable<T,R>  implements Iterable<T> {

    Operator<T> source;
    final Consumer<? super Throwable> defaultErrorHandler;

    public OperatorToIterable(Operator<T> source, Consumer<? super Throwable> defaultErrorHandler){
       this.source= source;
       this.defaultErrorHandler = defaultErrorHandler;

    }

    public Iterator<T> iterator(){
        return new Iterator<T>() {
            Object UNSET = new Object();
            AtomicReference value = new AtomicReference<>(UNSET);
            AtomicReference error = new AtomicReference<>(UNSET);
            AtomicBoolean done = new AtomicBoolean(false);
            boolean requested = false;
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
               source.subscribeAll(action,defaultErrorHandler,()->{});
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
                    while(awaiting){

                        LockSupport.parkNanos(0l);
                    }

                }
                return (!done.get() || value.get()!=UNSET || error.get()!=UNSET) ;
            }

            @Override
            public T next() {
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
