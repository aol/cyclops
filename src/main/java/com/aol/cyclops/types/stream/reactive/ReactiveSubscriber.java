package com.aol.cyclops.types.stream.reactive;

import java.util.Objects;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.internal.stream.spliterators.PushingSpliterator;

import lombok.AllArgsConstructor;
import lombok.val;

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

    private final PushingSpliterator<T> action = new PushingSpliterator<T>();;
  
   
    private volatile Subscription s;
    
   

    public ReactiveSubscriber() {
    }

    

    public ReactiveSeq<T> stream(){
        return ReactiveSeq.fromSpliterator(action);
    }
    @Override
    public void onSubscribe(final Subscription s) {
        Objects.requireNonNull(s);
        if (this.s == null) {
            this.s = s;
            s.request(1);
        } else
            s.cancel();

    }

    @Override
    public void onNext(final T t) {
        Objects.requireNonNull(t);
        val cons = action.getAction();
        if(cons!=null) 
              cons.accept(t);
        
    }

    @Override
    public void onError(final Throwable t) {
        Objects.requireNonNull(t);
        val cons = action.getError();
        if(cons!=null) 
              cons.accept(t);
        
        
    }

    @Override
    public void onComplete() {
        val run = action.getOnComplete();
        if(run!=null)
            run.run();

    }

    

   
}
