package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.types.mixins.Printable;
import cyclops.async.Queue;
import cyclops.box.Mutable;
import cyclops.stream.ReactiveSeq;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class PublisherFlatMapOperator<T,R> extends BaseOperator<T,R> implements Printable {


    int maxCapacity=256;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;;

    public PublisherFlatMapOperator(Operator<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper){
        super(source);
        this.mapper = mapper;




    }

    AtomicInteger active = new AtomicInteger(0);


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {
        StreamSubscription[] s = {null};
        boolean[] completeRecieved = {false};
        Runnable[] thunk = {() -> {
            if (completeRecieved[0]) {
                onComplete.run();
            } else {
                s[0].request(1);
            }
        }};


        StreamSubscription res = new StreamSubscription() {
            LongConsumer work = n -> {
                thunk[0].run();
            };

            @Override
            public void request(long n) {
                if (n <= 0)
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));

                this.singleActiveRequest(n, work);

            }

            @Override
            public void cancel() {
                s[0].cancel();
                super.cancel();
            }
        };
        s[0] = source.subscribe(e -> {
                    try {
                        Publisher<? extends R> next = mapper.apply(e);
                        ReactiveSeq<R> seq = ReactiveSeq.fromPublisher(next);
                        Spliterator<R> split = seq.spliterator();

                        thunk[0] = () -> {

                            boolean canAdvance = false;
                            while (res.isActive()) {
                                try {
                                    canAdvance = split.tryAdvance(onNext);
                                } catch (Throwable t) {
                                    onError.accept(t);
                                }
                                if (canAdvance)
                                    res.requested.decrementAndGet();
                                else {
                                    if (completeRecieved[0])
                                        onComplete.run();
                                    break;
                                }


                            }
                            if (!canAdvance && res.isActive())
                                s[0].request(1);


                        };
                        thunk[0].run();


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                , t -> {
                    onError.accept(t);
                    res.requested.decrementAndGet();
                    if (res.isActive()) {
                        s[0].request(1);
                    }
                }, () -> {
                    completeRecieved[0] = true;
                    thunk[0].run();

                });

        return res;
    }

    @Override
    public void subscribeAll(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Deque<Publisher<? extends R>> queued = new ConcurrentLinkedDeque<>();
        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(256);
        ManyToOneConcurrentArrayQueue<Throwable> errors = new ManyToOneConcurrentArrayQueue<>(256);
        AtomicReference<PVector<Subscription>> activeSubs = new AtomicReference<>(TreePVector.empty());
        source.subscribeAll(e-> {
                    try {

                        Publisher<? extends R> next = mapper.apply(e);
                        queued.add(next);
                        StreamSubscription sub = new StreamSubscription();
                        sub.request(maxCapacity);
                        drainAndLaunch(sub,onNext, queued, data, errors);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{

                    while(active.get()>0 || data.size()>0 || queued.size()>0){
                        //drain, create demand, launch queued publishers
                        Object next = data.poll();
                        if(next!=null)
                            onNext.accept(print(nilsafe(next)));


                    }
                    onCompleteDs.run();
                });
    }

    private <T> T nilsafe(Object o){
        if(Queue.NILL==o){
            return null;
        }
        return (T)o;
    }

    private void drainAndLaunch(StreamSubscription maxCapacity,Consumer<? super R> onNext, Deque<Publisher<? extends R>> queued, ManyToOneConcurrentArrayQueue<R> data, ManyToOneConcurrentArrayQueue<Throwable> errors) {

        while(queued.size()>0 && active.get()<10) {
            subscribe(queued, data, errors);
            Object next = data.poll();
            if(next!=null)
                onNext.accept(print(nilsafe(next)));

        }



    }

    private void subscribe(Deque<Publisher<? extends R>> queued,
                           ManyToOneConcurrentArrayQueue<R> data,
                           ManyToOneConcurrentArrayQueue<Throwable> errors){


        Publisher<? extends R> next = queued.poll();
        if(next==null)
            return;
        next.subscribe(new Subscriber<R>() {
            AtomicReference<Subscription> sub;

            private Object nilsafe(Object o){
                if(o==null)
                    return Queue.NILL;
                return o;
            }

            @Override
            public void onSubscribe(Subscription s) {

                this.sub=new AtomicReference<>(s);
                active.incrementAndGet();
                s.request(1l);

            }

            @Override
            public void onNext(R r) {
                //Optimization check if this is the
                //main thread and if so just call onNext
                data.offer((R)nilsafe(r));
                sub.get().request(1l);

            }

            @Override
            public void onError(Throwable t) {
                //Optimization check if this is the
                //main thread and if so just call onError
                errors.offer(t);
                sub.get().request(1l);
            }

            @Override
            public void onComplete() {


                if(queued.size()>0){
                    subscribe(queued,data,errors);
                }
                active.decrementAndGet();
            }
        });
    }

}
