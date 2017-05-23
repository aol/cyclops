package com.aol.cyclops2.streams.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cyclops.control.*;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import org.junit.Test;

import cyclops.async.Future;
import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;

public class ValueSubscriberTest {

    Executor ex = Executors.newFixedThreadPool(5);
    @Test
    public void maybeTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        ReactiveSeq.of(1,2,3)
                    .subscribe(sub);
        
        Maybe<Integer> maybe = sub.toMaybe();
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void maybeFromPublisherTest(){
        
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Maybe<Integer> maybe = Maybe.fromPublisher(stream);
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void futureWFromPublisherTest(){
     
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Future<Integer> maybe = Future.fromPublisher(stream);
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void futureWAsyncFromPublisherTest(){
      
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Future<Integer> maybe = Future.fromPublisher(stream,ex);
        assertThat(maybe.get(),equalTo(1));
        
    }
 
    @Test
    public void evalFromPublisherTest(){
        
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Eval<Integer> maybe = Eval.fromPublisher(stream);
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void xorFromPublisherTest(){
       
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Xor<Throwable,Integer> maybe = Xor.fromPublisher(stream);
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void iorFromPublisherTest(){
        
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Ior<Throwable,Integer> maybe = Ior.fromPublisher(stream);
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void tryFromPublisherTest(){
       
        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        Try<Integer,Throwable> maybe = Try.fromPublisher(stream);
        assertThat(maybe.get(),equalTo(1));
        
    }
    @Test
    public void maybePublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Maybe.of(1)
             .subscribe(sub);
        
        Maybe<Integer> maybe = sub.toMaybe();
        assertThat(maybe.get(),equalTo(1));
    }
    @Test
    public void maybeNonePublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Maybe.<Integer>none()
             .subscribe(sub);
        
        Maybe<Integer> maybe = sub.toMaybe();
        assertFalse(maybe.isPresent());
    }
    @Test
    public void xorPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Xor.primary(1)
             .subscribe(sub);
        
        Xor<Throwable,Integer> maybe = sub.toXor();
        assertThat(maybe.get(),equalTo(1));
    }
    @Test
    public void xorPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Xor.<Integer,Integer>secondary(1)
             .subscribe(sub);
        
        Xor<Throwable,Integer> xor = sub.toXor();
        assertThat(xor.swap().get(),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void xorSecondryPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Xor.primary(1)
             .subscribe(sub);
        
        Xor<Integer,Throwable> maybe = sub.toXor().swap();
        assertThat(maybe.swap().get(),equalTo(1));
    }
    @Test
    public void xorSecondaryPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Xor.<Integer,Integer>secondary(1)
             .subscribe(sub);
        
        Xor<Integer,Throwable> xor = sub.toXor().swap();
        assertThat(xor.get(),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void iorPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.primary(1)
             .subscribe(sub);
        
        Ior<Throwable,Integer> maybe = sub.toIor();
        assertThat(maybe.get(),equalTo(1));
    }
    @Test
    public void iorPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.<Integer,Integer>secondary(1)
             .subscribe(sub);
        
        Ior<Throwable,Integer> xor = sub.toIor();
        assertThat(xor.swap().get(),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void iorSecondryPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.primary(1)
             .subscribe(sub);
        
        Ior<Integer,Throwable> maybe = sub.toIor().swap();
        assertThat(maybe.swap().get(),equalTo(1));
    }
    @Test
    public void iorSecondaryPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.<Integer,Integer>secondary(1)
             .subscribe(sub);
        
        Ior<Integer,Throwable> xor = sub.toIor().swap();
        assertThat(xor.get(),instanceOf(NoSuchElementException.class));
    }
    
}
