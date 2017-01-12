package com.aol.cyclops2.internal.stream.spliterators.push;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by johnmcclean on 12/01/2017.
 */
public class PublisherFlatMapOperator<T,R> extends BaseOperator<T,R> {


    final Function<? super T, ? extends Publisher<? extends R>> mapper;;

    public PublisherFlatMapOperator(Operator<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper){
        super(source);
        this.mapper = mapper;




    }



    private void subscribe(Deque<Publisher<? extends R>> queued,
                           ManyToOneConcurrentArrayQueue<R> data,
                           ManyToOneConcurrentArrayQueue<Throwable> errors,
                           List<Subscription> activeSubs){

        Publisher<? extends R> next = queued.poll();
        next.subscribe(new Subscriber<R>() {
            Subscription sub;
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1l);
                sub=s;
                activeSubs.add(s);
            }

            @Override
            public void onNext(R r) {
                //Optimization check if this is the
                //main thread and if so just call onNext
                data.offer(r);



            }

            @Override
            public void onError(Throwable t) {
                //Optimization check if this is the
                //main thread and if so just call onError
                errors.offer(t);
            }

            @Override
            public void onComplete() {
                activeSubs.remove(sub);
                if(queued.size()>0){
                    subscribe(queued,data,errors,activeSubs);
                }
            }
        });
    }
    @Override
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Deque<Publisher<? extends R>> queued = new LinkedList<>();
        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(256);
        ManyToOneConcurrentArrayQueue<Throwable> errors = new ManyToOneConcurrentArrayQueue<>(256);
        List<Subscription> activeSubs = new ArrayList<>();
        source.subscribe(e-> {
                    try {

                        Publisher<? extends R> next = mapper.apply(e);
                        queued.add(next);
                        drainAndLaunch(onNext, queued, data, errors, activeSubs);

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    while(activeSubs.size()>0 && data.size()>0 && queued.size()>0){
                        //drain, create demand, launch queued publishers
                        drainAndLaunch(onNext, queued, data, errors, activeSubs);
                    }
                    onCompleteDs.run();
                });
    }

    private void drainAndLaunch(Consumer<? super R> onNext, Deque<Publisher<? extends R>> queued, ManyToOneConcurrentArrayQueue<R> data, ManyToOneConcurrentArrayQueue<Throwable> errors, List<Subscription> activeSubs) {
        drainAndCreateDemand(onNext, data, activeSubs);


        //add next publisher to active
        while( queued.size()>10) {
            if (data.size() > 0) {
                onNext.accept(data.poll());
            }

            Thread.yield();
        }
        subscribe(queued,data,errors,activeSubs);
        drainAndCreateDemand(onNext, data, activeSubs);
    }

    private void drainAndCreateDemand(Consumer<? super R> onNext, ManyToOneConcurrentArrayQueue<R> data, List<Subscription> activeSubs) {
        //drain queued DATA
        while(data.size()>0){
            onNext.accept(data.poll());
        }

        int capacity = 256-(data.size()-activeSubs.size());
        //create more demand
        activeSubs.forEach(s->s.request(Math.min(1l,capacity/activeSubs.size())));
    }
}
