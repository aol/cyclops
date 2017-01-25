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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class PublisherFlatMapOperatorSync<T,R> extends BaseOperator<T,R> implements Printable {


    int maxCapacity=256;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;;

    public PublisherFlatMapOperatorSync(Operator<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper){
        super(source);
        this.mapper = mapper;




    }

    AtomicInteger active = new AtomicInteger(0);


    @Override
    public StreamSubscription subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onComplete) {

        final AtomicBoolean activePush= new AtomicBoolean(false);
        final AtomicBoolean working = new AtomicBoolean(false);
        final AtomicReference<Subscription> activeSub = new AtomicReference<>(null);
        final StreamSubscription[] s = {null};

        final AtomicBoolean completed = new AtomicBoolean(false);
        final AtomicBoolean completeRecieved = new AtomicBoolean(false);
        final AtomicInteger demanded = new AtomicInteger(0);
        StreamSubscription res = new StreamSubscription() {

            @Override
            public void request(long n) {
                if (n <= 0)
                    onError.accept(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                super.request(n);

                if(activeSub.get()==null) {

                    s[0].request(1l);
                }
                else {

                    Subscription sub = activeSub.get();
                    if(requested.get()>0 && working.compareAndSet(false,true)) {
                        System.out.println("Request attempting demand signal..");

                        if(activeSub.compareAndSet(sub,sub)){
                            System.out.println("Request: Inner Signal demand for 1. inner requests " + demanded.incrementAndGet()
                                    + "  current demand " + requested.get() + " working? " + working.get() +  " sub " + activeSub.get());
                            sub.request(1l);
                        }



                    }

                }



            }

            @Override
            public void cancel() {
                s[0].cancel();
                super.cancel();
            }
        };
        final AtomicReference<Runnable> completer = new AtomicReference<>(()->{

        });

        final AtomicInteger pushed = new AtomicInteger(0);
        s[0] = source.subscribe(e -> {
                    System.out.println("E is" + e);
                    try {
                        Publisher<? extends R> next = mapper.apply(e);

                        next.subscribe(new Subscriber<R>() {

                            @Override
                            public void onSubscribe(Subscription s) {


                                active.incrementAndGet();
                                activeSub.set(s);
                                working.set(false);
                                if(res.isActive()) {

                                    if(working.compareAndSet(false,true)) {
                                        System.out.println("On Subscribe: Inner signal demand for 1. inner requests "
                                                + demanded.incrementAndGet() + "working? " + working.get() + " current demand" +
                                                res.requested.get()
                                                +   " sub " + activeSub.get() );
                                        s.request(1l);
                                    }

                                }

                            }

                            @Override
                            public void onNext(R r) {

                                System.out.println("ON next " + r +  " " + activePush.get());
                                while(!activePush.compareAndSet(false,true)) {
                                }
                                System.out.println("Active Push! " + res.isActive());
                                System.out.println("Pushing " + r + " demand " + res.requested.get() + " " + Thread.currentThread().getId());
                                onNext.accept(r);

                                res.requested.decrementAndGet();
                                System.out.println("DECREMENTING********************** " + res.requested.get());
                                working.set(false);
                                System.out.println("Ending active push " + activePush.get() + "  pushed so far " + pushed.incrementAndGet() +  " demand " + res.requested.get());
                                activePush.set(false);

                                if(res.isActive() && working.compareAndSet(false,true)){
                                    System.out.println("On Next : Inner signal demand for 1 inner requests " + demanded.incrementAndGet() +  " sub " + activeSub.get());
                                    activeSub.get().request(1l);

                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                onError.accept(t);
                                res.requested.decrementAndGet();
                                working.set(false);
                                if(res.isActive() && working.compareAndSet(false,true)){
                                    System.out.println("On Error : Inner signal demand for 1 inner requests " + demanded.incrementAndGet() +  " sub " + activeSub.get());
                                    activeSub.get().request(1l);

                                }
                            }

                            @Override
                            public void onComplete() {
                                working.set(false);

                                System.out.println("Inner OC " + active.get() + " " +  completeRecieved.get() + " demand " + res.requested.get());

                                if(completeRecieved.get()) {
                                    System.out.println("!!!!!!!!!!!!!!!!!ON COMPLETE!!");
                                    onComplete.run();
                                    active.decrementAndGet();
                                    return;

                                }else{
                                    System.out.println("request from outer");

                                    s[0].request(1l);
                                    active.decrementAndGet();

                                }
                                if(completeRecieved.get() && active.get()==0) {

                                    completed.set(true);
                                    System.out.println("!!!!!!!!!!!!!!!!!ON COMPLETE!!");
                                    onComplete.run();

                                    // active.decrementAndGet();

                                }
                                else if(active.get()==0){
                                    System.out.println("*******Active is " + active.get());
                                    completer.set(()->{
                                        if(active.get()==0) {
                                            completed.set(true);
                                            System.out.println("!&&&&&&&&&&&&&&&&!ON COMPLETE!!");
                                            onComplete.run();
                                        }
                                    });
                                }
                            }
                        });




                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                , t -> {
                    onError.accept(t);
                    System.out.println("ON ERROR!!!!! DECREMENTING!");
                    res.requested.decrementAndGet();
                    if (res.isActive()) {
                        s[0].request(1);
                    }
                }, () -> {

                    System.out.println("on complete start! " + active.get());

                    completeRecieved.set(true);
                    completer.get().run();
                    /**
                     if(active.get()==0){
                     System.out.println("----------ON COMPLETE!!");
                     onComplete.run();
                     }
                     **/

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

                    while(active.get()>0 || data.size()>0 || queued.size()>0 || errors.size()>0){
                        //drain, create demand, launch queued publishers
                        Object next = data.poll();
                        if(next!=null)
                            onNext.accept(print(nilsafe(next)));

                        if(errors.size()>0){
                            onError.accept(errors.poll());
                        }
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
