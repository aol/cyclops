package com.oath.cyclops.internal.stream.spliterators.push;

import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.types.futurestream.Continuation;
import com.oath.cyclops.util.ExceptionSoftener;
import lombok.AllArgsConstructor;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class NoBackPressureIterator<T> {

    private final Operator<T> source;

    private volatile Throwable error;


    public NoBackPressureIterator(Operator<T> source){
        this.source = source;



    }



    public Iterator<T> nobackPressure() {
        Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
            .build();

        AtomicBoolean wip = new AtomicBoolean(false);
        Subscription[] sub = {null};

        Continuation cont = new Continuation(() -> {
            if (wip.compareAndSet(false, true)) {
                this.source.subscribeAll(queue::offer,
                    i -> {
                    error=i;
                    queue.close();

                    },
                    () ->
                        queue.close());
            }
            return Continuation.empty();
        });
        queue.addContinuation(cont);


        Iterator<T> it =  queue.stream().iterator();

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return  it.hasNext() || error!=null;
            }

            @Override
            public T next() {
                 if(error != null){
                    throw ExceptionSoftener.throwSoftenedException(error);
                }
                return it.next();
            }
        };
    }
}
