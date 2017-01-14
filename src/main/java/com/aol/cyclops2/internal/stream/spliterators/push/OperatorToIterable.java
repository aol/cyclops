package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.util.ExceptionSoftener;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
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
            AtomicReference<T> value = new AtomicReference<>(null);
            AtomicReference<Throwable> error = new AtomicReference<>(null);
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

            @Override
            public boolean hasNext() {
                if (!requested) {
                    awaiting = true;
                    sub.request(1l);
                    requested = true;

                }
                return !done.get();
            }

            @Override
            public T next() {
                if (!requested) {
                    sub.request(1l);

                }
                requested = false;
                while(awaiting){
                    LockSupport.parkNanos(0l);
                }
                if (error.get() != null) {
                    Throwable t = error.get();
                    error.set(null);
                    defaultErrorHandler.accept(t);
                }
                T result = value.get();
                value.set(null);
                return result;
            }
        };
    }


}
