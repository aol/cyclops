package com.oath.cyclops.types.reactive;

import com.oath.cyclops.internal.stream.spliterators.push.CapturingOperator;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.Getter;
import lombok.val;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A Subscriber for Observable type event driven Streams that implement backpressure via the reactive-streams API

 *
 * @author johnmcclean
 *
 * @param <T> Subscriber type
 */

public class ReactiveSubscriber<T> implements Subscriber<T> {


    volatile boolean isOpen;
    @Getter
    private volatile Subscription subscription;

    private volatile CapturingOperator<T> action=  null;






    volatile boolean streamCreated=  false;
    CapturingOperator<T> getAction(){
        if(action==null && subscription!=null)
             action = new CapturingOperator<T>(subscription);
        else if(action==null)
            action = new CapturingOperator<T>();
        return action;
    }

    /**
     * <pre>
     *    {@code
     *           ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();

                Flux.just(1,2,3).forEachAsync(sub);
                sub.stream().forEach(System.out::println);

     *          //note JDK Stream based terminal operations may block the current thread
     *          //see ReactiveSeq#collectStream ReactiveSeq#foldAll for non-blocking alternatives
     *    }
     * </pre>
     *
     * @return A push-based asychronous event driven Observable-style Stream that implements Backpressure via the reactive-streams API
     */
    public ReactiveSeq<T> reactiveStream(){
        streamCreated = true;


        ReactiveSeq<T> result = Spouts.reactiveStream(getAction());
        if(complete)
            return ReactiveSeq.fromIterable(buffer);
        if(error!=null)
            throw ExceptionSoftener.throwSoftenedException(error);
        if(buffer.size()>0){
            return Spouts.concat(Spouts.fromIterable(buffer),result);
        }



        return result;
    }


    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);
        if(streamCreated)
              throw new IllegalStateException("Subscription passed after downstream Stream created. Subscribe with this Subscriber takeOne, then extract the Stream");


        this.subscription = s;
        if(action!=null){
            action.setSubscription(s);
        }


    }

    ArrayList<T> buffer = new ArrayList<>();
    volatile Throwable error =null;
    volatile boolean complete=false;
    @Override
    public void onNext(final T t) {
        Objects.requireNonNull(t);
        if(subscription==null){
            buffer.add(t);
            return;
        }

        val cons = getAction().getAction();
        if(cons!=null)
              cons.accept(t);


    }

    @Override
    public void onError(final Throwable t) {
        Objects.requireNonNull(t);
        if(subscription==null)
            error= t;

        val cons = getAction().getError();
        if(cons!=null)
              cons.accept(t);


    }

    @Override
    public void onComplete() {


        complete=true;
        val run = getAction().getOnComplete();

        if(run!=null)
            run.run();
        else
            getAction().complete();

    }
    public boolean isInitialized() {
        return getAction().isInitialized();
    }



}
