package com.aol.cyclops2.internal.stream.spliterators.push;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

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
                while(!data.offer(r)){
                    Thread.yield();
                }




            }

            @Override
            public void onError(Throwable t) {
                onError(t);
            }

            @Override
            public void onComplete() {
                activeSubs.remove(sub);
                if(queued.size()>0){
                    subscribe(queued,data,activeSubs);
                }
            }
        });
    }
    @Override
    public void subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError, Runnable onCompleteDs) {
        Deque<Publisher<? extends R>> queued = new LinkedList<>();
        ManyToOneConcurrentArrayQueue<R> data = new ManyToOneConcurrentArrayQueue<R>(1000);
        List<Subscription> activeSubs;
        source.subscribe(e-> {
                    try {

                        Publisher<? extends R> next = mapper.apply(e);
                        queued.add(next);
                        //drain queued DATA
                        while(data.size()>0){
                            onNext.accept(data.poll());
                        }
                        //create more demand
                        activeSubs.forEach(s->s.request(Math.min(1l,capacity/activeSubs.size())));

                        //add next publisher to active
                        while( queued.size()>10) {
                            if (data.size() > 0) {
                                onNext.accept(data.poll());
                            }

                            Thread.yield();
                        }
                        subscribe(queued,data);
                        //drain and create demand again

                    } catch (Throwable t) {

                        onError.accept(t);
                    }
                }
                ,onError,()->{
                    while(activeSubs.size()>0 && data.size()>0 && queued.size()>0){
                        //drain, create demand, launch queued publishers
                    }
                    onCompleteDs.run();
                });
    }
}
