package com.aol.cyclops.types.stream.reactive;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.function.Memoize;

/**
 * A reactive-streams Subscriber that can take 1 value from a reactive-streams publisher and convert
 * it into various forms
 * 
 * <pre>
 * {@code 
 *    ValueSubscriber<Integer> anInt = ValueSubscriber.subscriber();
 *    ReactiveSeq.of(1,2,3)
 *               .publish(anInt);
 *    
 *    Xor<Throwable,Integer> xor = anInt.toXor();
 *    Try<Integer,Throwable> myTry = xor.toTry();
 *    Maybe<Integer> maybe = myTry.toMaybe();
 *    Optional<Integer> maybe = maybe.toOptional();
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class ValueSubscriber<T> implements Subscriber<T>, 
                                            Value<T>{
        
        
        private final Object UNSET = new Object();
        private final AtomicReference firstValue = new AtomicReference(UNSET);
        private final AtomicReference firstError = new AtomicReference(UNSET);
        private final Runnable onComplete;
        
        private volatile Subscription s;
        private final Runnable requestOne = Memoize.memoizeRunnable(()->this.s.request(1l));
        
        private ValueSubscriber(Runnable onComplete) {
            super();
            this.onComplete = onComplete;
        }
       
        public static <T> ValueSubscriber<T> subscriber(Runnable onComplete){
            return new ValueSubscriber<>(onComplete);
        }
        public static <T> ValueSubscriber<T> subscriber(){
            return new ValueSubscriber<>(()->{});
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
            firstValue.compareAndSet((T)UNSET, t);
        }

        @Override
        public void onError(Throwable t) {
            Objects.requireNonNull(t);
            firstError.compareAndSet((T)UNSET, t);
        }

        @Override
        public void onComplete() {
            
           this.onComplete.run();
           firstError.set(new NoSuchElementException("publisher has no elements"));
            
        }
        public void requestOne(){
            
            firstValue.set(UNSET);
            firstError.set(UNSET);
           this.s.request(1);
        }
        public T get(){
        
            while(firstValue.get()==UNSET && firstError.get()==UNSET)
                LockSupport.parkNanos(1000000l);
            if(firstValue.get()==UNSET)
                return null;
          
            return (T)firstValue.get();
        }

        @Override
        public  Xor<Throwable, T> toXor() {
             if(get()==null && firstError.get()!=UNSET){
                 return Xor.secondary((Throwable)firstError.get());
             }
            return Xor.primary(get());
        }
        private T throwingGet(){
           
            while(firstValue.get()==UNSET && firstError.get()==UNSET)
                LockSupport.parkNanos(1000000l);
            if(firstValue.get()==UNSET)
                throw ExceptionSoftener.throwSoftenedException((Throwable)firstError.get());
          
            return (T)firstValue.get();
        }
        @Override
        public <X extends Throwable> Try<T,X> toTry(Class<X>... classes){
            return Try.withCatch( ()->throwingGet(),classes);
        }
       

       

        @Override
        public  Ior<Throwable, T> toIor() {
           get();
           
           Ior<Throwable, T>  secondary=null;
           Ior<Throwable, T>  primary=null;
           
           if(firstError.get()!=UNSET){
               secondary = Ior.<Throwable,T>secondary((Throwable)firstError.get());
           }
           if(firstValue.get()!=UNSET){
               primary= Ior.<Throwable,T>primary((T)firstValue.get());
           }
           if(secondary!=null && primary!=null)
               return Ior.both(secondary, primary);
           if(primary!=null)
               return primary;
           
           return secondary;
               
        }

       
        

}
