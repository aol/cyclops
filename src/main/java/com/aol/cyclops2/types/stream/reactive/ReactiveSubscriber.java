package com.aol.cyclops2.types.stream.reactive;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.spliterators.push.StreamSubscription;
import cyclops.box.LazyImmutable;
import cyclops.stream.Spouts;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.internal.stream.spliterators.push.CapturingOperator;

import lombok.AllArgsConstructor;
import lombok.val;
import sun.security.provider.Sun;

/**
 * A Subscriber for Observable type event driven Streams that implement backpressure via the reactive-streams API

 * 
 * @author johnmcclean
 *
 * @param <T> Subscriber type
 */

public class ReactiveSubscriber<T> implements Subscriber<T> {


    volatile boolean isOpen;
    private volatile Subscription s;

    private volatile CapturingOperator<T> action=  null;
   

    public ReactiveSubscriber() {
    }



    volatile boolean streamCreated=  false;
    CapturingOperator<T> getAction(){
        if(action==null)
             action = new CapturingOperator<T>(s);
        return action;
    }

    /**
     * <pre>
     *    {@code
     *           ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();

                Flux.just(1,2,3).subscribe(sub);
                sub.reactiveStream().forEach(System.out::println);

     *          //note JDK Stream based terminal operations may block the current thread
     *          //see ReactiveSeq#collectAll ReactiveSeq#foldAll for non-blocking alternatives
     *    }
     * </pre>
     *
     * @return A push-based asychronous event driven Observable-style Stream that implements Backpressure via the reactive-streams API
     */
    public ReactiveSeq<T> reactiveStream(){
        streamCreated = true;
        if(s==null){
            if(streamCreated)
                throw new IllegalStateException("Stream has been created before a Subscription has been passed to this Subscriber. Subscribe with this Subscriber first, then extract the Stream.");

        }
        return Spouts.reactiveStream(getAction());
    }


    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);
        if(streamCreated)
              throw new IllegalStateException("Subscription passed after downstream Stream created. Subscribe with this Subscriber first, then extract the Stream");

        this.s = s;

    }

    @Override
    public void onNext(final T t) {
        Objects.requireNonNull(t);

        val cons = getAction().getAction();
        if(cons!=null) 
              cons.accept(t);

        
    }

    @Override
    public void onError(final Throwable t) {
        Objects.requireNonNull(t);
        val cons = getAction().getError();
        if(cons!=null) 
              cons.accept(t);
        
        
    }

    @Override
    public void onComplete() {


        val run = getAction().getOnComplete();

        if(run!=null)
            run.run();

    }
    public boolean isInitialized() {
        return getAction().isInitialized();
    }



}
