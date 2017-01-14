package com.aol.cyclops2.types.stream.reactive;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

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
 * A reactive-streams Subscriber that can generate various forms of sequences from a publisher
 * 
 * <pre>
 * {@code 
 *    SeqSubscriber<Integer> ints = SeqSubscriber.subscriber();
 *    ReactiveSeq.of(1,2,3)
 *               .publish(ints);
 *    
 *   ListX list = ints.toListX();
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Subscriber type
 */
@AllArgsConstructor//(access=AccessLevel.PRIVATE)
public class ReactiveSubscriber<T> implements Subscriber<T> {


    volatile boolean isOpen;
    AtomicLong requested= new AtomicLong(0);
    private volatile Subscription s = new Subscription(){

        @Override
        public void request(long n) {
            if(requested.get()==Long.MAX_VALUE)
                return;
            if(n==Long.MAX_VALUE)
                requested.set(n);
            requested.accumulateAndGet(n,(a,b)->a+b);
        }

        @Override
        public void cancel() {
            isOpen = false;
        }
    };
    private volatile LazyImmutable<CapturingOperator<T>> action = LazyImmutable.def();
   

    public ReactiveSubscriber() {
    }

    volatile boolean streamCreated=  false;

    public ReactiveSeq<T> stream(){
        streamCreated = true;
        return Spouts.reactiveStream(action.computeIfAbsent(()->new CapturingOperator<T>(s)));
    }
    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);
        if(streamCreated)
            throw new IllegalStateException("Subscription passed after downstream Stream created. Subscribe with this Subscriber first, then extract the Stream");
        if (this.s == null) {
            this.s = s;
            s.request(1);
        } else
            s.cancel();

    }

    @Override
    public void onNext(final T t) {
        Objects.requireNonNull(t);
        val cons = action.get().getAction();
        if(cons!=null) 
              cons.accept(t);

        
    }

    @Override
    public void onError(final Throwable t) {
        Objects.requireNonNull(t);
        val cons = action.get().getError();
        if(cons!=null) 
              cons.accept(t);
        
        
    }

    @Override
    public void onComplete() {

        val run = action.get().getOnComplete();
        if(run!=null)
            run.run();

    }

    

   
}
