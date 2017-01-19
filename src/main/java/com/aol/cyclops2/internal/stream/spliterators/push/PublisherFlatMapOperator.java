package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.types.mixins.Printable;
import cyclops.async.Queue;
import cyclops.box.Mutable;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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

        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(256);
        ManyToOneConcurrentArrayQueue<Throwable> errors = new ManyToOneConcurrentArrayQueue<>(256);
        Deque<Subscription> activeSubs = new ConcurrentLinkedDeque<>();
        StreamSubscription[] sourceSub = {null};
        AtomicLong activeRequests = new AtomicLong(0);
        StreamSubscription s = new StreamSubscription(){
            LongConsumer work = n-> {

                while((data.size()>0 || activeRequests.get()>0) && isActive()) {

                    Object nextV = data.poll();
                    if(nextV!=null) {
                        onNext.accept(nilsafe(nextV));
                        requested.decrementAndGet();
                        activeRequests.decrementAndGet();
                    }
                    if(activeRequests.get()<100){
                        activeSubs.forEach(sub-> {
                                    if (isActive() && activeRequests.get()<100) {
                                        activeRequests.incrementAndGet();
                                        sub.request(1l);

                                        System.out.println("Making more requests!! " + activeRequests.get());
                                    }

                                }
                        );
                    }

                }
                if(isActive()  && active.get()<10)
                    sourceSub[0].request(1l);
                System.out.println("******Requested "+  n);

            };
            @Override
            public void request(long n) {
                sourceSub[0].request(1l);
                this.singleActiveRequest(n,work);
            }

            @Override
            public void cancel() {
                sourceSub[0].cancel();
                for(Subscription next : activeSubs){
                    next.cancel();
                }
                super.cancel();
            }
        } ;
        sourceSub[0] = source.subscribe(e-> {
                    try {

                        Publisher<? extends R> next = mapper.apply(e);
                        next.subscribe(new Subscriber<R>() {
                            Subscription sub;
                            @Override
                            public void onSubscribe(Subscription s) {
                                activeSubs.offer(s);
                                sub=s;
                                activeRequests.incrementAndGet();
                                s.request(1l);

                            }

                            @Override
                            public void onNext(R r) {
                                System.out.println("On next " + r);
                                data.offer((R)nilsafe(r));
                                if(s.isActive()  && active.get()<10)
                                    sourceSub[0].request(1l);
                                if (s.isActive() && activeRequests.get()<100) {
                                    System.out.println("Requesting..");
                                    activeRequests.incrementAndGet();
                                    sub.request(1l);

                                }

                            }

                            @Override
                            public void onError(Throwable t) {
                                errors.offer(t);
                            }

                            @Override
                            public void onComplete() {
                                activeRequests.decrementAndGet();
                                if(s.isActive()  && active.get()<10)
                                    sourceSub[0].request(1l);
                                else if(s.isActive()){
                                    activeSubs.forEach(sub-> {
                                        if (s.isActive() && activeRequests.get() < 100) {
                                            activeRequests.incrementAndGet();
                                                    sub.request(1l);

                                        }
                                        }
                                    );
                                }
                                System.out.println("Complete! " + activeSubs);
                                activeSubs.remove(sub);
                                System.out.println("After Complete! " + activeSubs);
                            }
                        });


                        while((data.size()>0  || activeRequests.get()>0) && s.isActive()) {

                            Object nextV = data.poll();
                            if(nextV!=null) {
                                onNext.accept(nilsafe(nextV));
                                s.requested.decrementAndGet();
                                activeRequests.decrementAndGet();
                            }
                            if(s.isActive()  && active.get()<10)
                                sourceSub[0].request(1l);
                            if(activeRequests.get()<(100-activeSubs.size())) {
                                activeSubs.forEach(sub -> {
                                            if (s.isActive() && activeRequests.get() < 100) {
                                                activeRequests.incrementAndGet();
                                                sub.request(1l);
                                            }

                                        }
                                );
                            }


                        }


                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    System.out.println("Oncomplete!! " + activeRequests.get());
                    while((data.size()>0 ||   active.get()>0 || activeRequests.get()>0) && s.isActive()) {

                        Object nextV = data.poll();
                        if(nextV!=null) {
                            onNext.accept(nilsafe(nextV));
                            s.requested.decrementAndGet();
                            activeRequests.decrementAndGet();
                        }
                        if(s.isActive()){
                            activeSubs.forEach(sub-> {
                                        if (s.isActive() && activeRequests.get()<100) {
                                            activeRequests.incrementAndGet();
                                            sub.request(1l);

                                        }
                                    }
                            );
                        }
                    }
                    onComplete.run();
                });

        return s;
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
