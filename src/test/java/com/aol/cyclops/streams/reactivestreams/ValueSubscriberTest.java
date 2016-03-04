package com.aol.cyclops.streams.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;

public class ValueSubscriberTest {

    @Test
    public void maybeTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        ReactiveSeq.of(1,2,3)
                    .subscribe(sub);
        
        Maybe<Integer> maybe = sub.toMaybe();
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
