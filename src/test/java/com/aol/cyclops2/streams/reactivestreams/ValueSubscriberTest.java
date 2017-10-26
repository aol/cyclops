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
import cyclops.reactive.ReactiveSeq;
import com.aol.cyclops2.types.reactive.ValueSubscriber;

public class ValueSubscriberTest {

    Executor ex = Executors.newFixedThreadPool(5);
    @Test
    public void maybeTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        ReactiveSeq.of(1,2,3)
                    .subscribe(sub);

        Maybe<Integer> maybe = sub.toMaybe();
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void maybeFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Maybe<Integer> maybe = Maybe.fromPublisher(stream);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void FutureFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Future<Integer> maybe = Future.fromPublisher(stream);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void FutureAsyncFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Future<Integer> maybe = Future.fromPublisher(stream,ex);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }

    @Test
    public void evalFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Eval<Integer> maybe = Eval.fromPublisher(stream);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void xorFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Either<Throwable,Integer> maybe = Either.fromPublisher(stream);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void iorFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Ior<Throwable,Integer> maybe = Ior.fromPublisher(stream);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void tryFromPublisherTest(){

        ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Try<Integer,Throwable> maybe = Try.fromPublisher(stream);
        assertThat(maybe.toOptional().get(),equalTo(1));

    }
    @Test
    public void maybePublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Maybe.of(1)
             .subscribe(sub);

        Maybe<Integer> maybe = sub.toMaybe();
        assertThat(maybe.toOptional().get(),equalTo(1));
    }
    @Test
    public void maybeNonePublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Maybe.<Integer>nothing()
             .subscribe(sub);

        Maybe<Integer> maybe = sub.toMaybe();
        assertFalse(maybe.isPresent());
    }
    @Test
    public void xorPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Either.right(1)
             .subscribe(sub);

        Either<Throwable,Integer> maybe = sub.toXor();
        assertThat(maybe.toOptional().get(),equalTo(1));
    }
    @Test
    public void xorPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Either.<Integer,Integer>left(1)
             .subscribe(sub);

        Either<Throwable,Integer> xor = sub.toXor();
        assertThat(xor.swap().orElse(null),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void xorSecondryPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Either.right(1)
             .subscribe(sub);

        Either<Integer,Throwable> maybe = sub.toXor().swap();
        assertThat(maybe.swap().orElse(null),equalTo(1));
    }
    @Test
    public void xorSecondaryPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Either.<Integer,Integer>left(1)
             .subscribe(sub);

        Either<Integer,Throwable> xor = sub.toXor().swap();
        assertThat(xor.orElse(null),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void iorPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.right(1)
             .subscribe(sub);

        Ior<Throwable,Integer> maybe = sub.toIor();
        assertThat(maybe.toOptional().get(),equalTo(1));
    }
    @Test
    public void iorPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.<Integer,Integer>left(1)
             .subscribe(sub);

        Ior<Throwable,Integer> xor = sub.toIor();
        assertThat(xor.swap().orElse(null),instanceOf(NoSuchElementException.class));
    }
    @Test
    public void iorSecondryPublisherTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.right(1)
             .subscribe(sub);

        Ior<Integer,Throwable> maybe = sub.toIor().swap();
        assertThat(maybe.swap().orElse(null),equalTo(1));
    }
    @Test
    public void iorSecondaryPublisherErrorTest(){
        ValueSubscriber<Integer> sub = ValueSubscriber.subscriber();
        Ior.<Integer,Integer>left(1)
             .subscribe(sub);

        Ior<Integer,Throwable> xor = sub.toIor().swap();
        assertThat(xor.orElse(null),instanceOf(NoSuchElementException.class));
    }

}
