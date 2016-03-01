package com.aol.cyclops.types.stream.reactive;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.util.ExceptionSoftener;

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
 * @param <T>
 */
public class SeqSubscriber<T> implements Subscriber<T>, 
                                            Supplier<T>,
                                             ConvertableSequence<T>{
        
        
        private final Object UNSET = new Object();
        private final AtomicReference lastValue = new AtomicReference(UNSET);
        private final AtomicReference lastError = new AtomicReference(UNSET);
        private final Runnable onComplete;
        private volatile boolean complete = false;
        private volatile Subscription s;
        private final Runnable requestOne = ()->this.s.request(1l);
       
        protected SeqSubscriber(){
            this.onComplete = ()->{};
        }
        private SeqSubscriber(Runnable onComplete) {
            super();
            this.onComplete = onComplete;
        }
       
        public static <T> SeqSubscriber<T> subscriber(Runnable onComplete){
            return new SeqSubscriber<>(onComplete);
        }
        public static <T> SeqSubscriber<T> subscriber(){
           
            return new SeqSubscriber<>(()->{});
        }
        
        
        @Override
        public void onSubscribe(Subscription s) {
            Objects.requireNonNull(s);
            if(this.s==null){
                 this.s =s;
                 s.request(1); 
            }
            else
                s.cancel();
           
        }

        @Override
        public void onNext(T t) {
            Objects.requireNonNull(t);
            lastValue.set(t);
        }

        @Override
        public void onError(Throwable t) {
            Objects.requireNonNull(t);
            System.out.println("Setting error "  +t);
            lastError.set(t);
        }

        @Override
        public void onComplete() {
           complete  =true;
           this.onComplete.run();
            
        }
        public T get(){
            
            while(lastValue.get()==UNSET && lastError.get()==UNSET)
                LockSupport.parkNanos(1000000l);
            if(lastError.get()!=UNSET){
                Throwable toThrow = (Throwable)lastError.get();
                reset();
                throw ExceptionSoftener.throwSoftenedException(toThrow);
            }
            T result = (T)lastValue.get();
            reset();
            requestOne.run();
            return result;
            
        }
        
        private void reset(){
            lastValue.set(UNSET);
            lastError.set(UNSET);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>(){

                @Override
                public boolean hasNext() {
                    return !complete;
                }

                @Override
                public T next() {
                   return get();
                }
                
            };
        }
        
        

}
