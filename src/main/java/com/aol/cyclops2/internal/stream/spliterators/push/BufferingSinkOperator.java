package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.types.reactive.BufferOverflowPolicy;
import cyclops.control.Xor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


public class BufferingSinkOperator<T> implements Operator<T> {
    private final Queue<T> q;

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final Consumer<? super Subscriber<T>> sub;
    private final BufferOverflowPolicy policy;



    public BufferingSinkOperator(Queue<T> q, Consumer<? super Subscriber<T>> sub, BufferOverflowPolicy  policy) {
        this.q = q;
        this.sub = sub;
        this.policy = policy;
    }

    @Override
    public StreamSubscription subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        Subscription[] delegate = {null};
        StreamSubscription ss = new StreamSubscription(){

            @Override
            public void request(long n) {
               super.request(n);
               delegate[0].request(n);
               processQueue(this,onNext);
            }
        };

        sub.accept(new Subscriber<T>() {

            @Override
            public void onSubscribe(Subscription s) {
                delegate[0]=s;
            }

            @Override
            public void onNext(T t) {
               // if(delegate[0]==null)
                //    return;

                if(!q.offer(t)){
                    policy.match(t).map(v->{
                       while(!q.offer(t)){
                           Thread.yield();
                           processQueue(ss,onNext);
                       }
                       return v;
                    });
                }
                processQueue(ss,onNext);

            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
        return ss;
    }
    private void processQueue(StreamSubscription ss,Consumer<? super T> onNext) {

        if(active.compareAndSet(false,true)) {
            while(ss.isActive()) {
                T next = q.poll();
                if (next != null) {
                    onNext.accept(next);
                    ss.requested.decrementAndGet();
                }
                else
                    break;
            }
            active.set(false);
            if (!q.isEmpty() && ss.isActive()) {
                processQueue(ss,onNext);
            }

        }

    }
    private void processQueue(Consumer<? super T> onNext) {

        if(active.compareAndSet(false,true)) {
            while(true) {
                T next = q.poll();
                if (next != null) {
                    onNext.accept(next);

                }
                else
                    break;
            }
            active.set(false);
            if (!q.isEmpty()) {
                processQueue(onNext);
            }

        }

    }

    @Override
    public void subscribeAll(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        sub.accept(new Subscriber<T>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T t) {

                if(!q.offer(t)){
                    policy.match(t).map(v->{
                        while(!q.offer(t)){
                            Thread.yield();
                        }
                        return v;
                    });
                }
                processQueue(onNext);

            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
    }
}
