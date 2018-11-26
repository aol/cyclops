package com.oath.cyclops.internal.stream.spliterators.push;

import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class SkipWhileClosedOperatorTest {

    Executor ex = Executors.newFixedThreadPool(10);

    @Test
    public void dropWhile5() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .dropWhileInclusive(i -> false)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of(2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhile1() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .dropWhileInclusive(i -> i<5)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of()));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhile0() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .dropWhileInclusive(i -> i<4)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of(5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhile4_transforms5() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        AtomicInteger peeks = new AtomicInteger(0);
        Spouts.of(1,2,3,4,5)
            .peek(System.out::println)
            .peek(p->peeks.incrementAndGet())
            .dropWhileInclusive(i -> i<4)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(peeks.get(),equalTo(5));
        assertThat(data.get(),equalTo(Vector.of(5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhile1_transforms5() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        AtomicInteger peeks = new AtomicInteger(0);
        Spouts.of(1,2,3,4,5)
            .peek(System.out::println)
            .peek(p->peeks.incrementAndGet())
            .dropWhileInclusive(i -> i<1)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(peeks.get(),equalTo(5));
        assertThat(data.get(),equalTo(Vector.of(2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhileStages() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s = Spouts.of(1, 2, 3, 4, 5)
            .dropWhileInclusive(i -> false)
            .forEach(2, n -> {
                assertFalse(complete.get());
                data.updateAndGet(sq -> sq.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });



        assertThat(data.get(),equalTo(Vector.of(2,3)));
        assertThat(complete.get(),equalTo(false));
        assertNull(error.get());

        s.request(10l);
        assertThat(data.get(),equalTo(Vector.of(2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhileStages3() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s = Spouts.of(1, 2, 3, 4, 5)
            .dropWhileInclusive(i -> i<2)
            .forEach(2, n -> {
                assertFalse(complete.get());
                data.updateAndGet(sq -> sq.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        while(data.get().size()<2){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(),equalTo(Vector.of(3,4)));
        assertThat(complete.get(),equalTo(false));
        assertNull(error.get());

        s.request(10l);
        assertThat(data.get(),equalTo(Vector.of(3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }

    @Test
    public void predicateError(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .dropWhileInclusive(i ->{ throw new RuntimeException();})
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of()));
        assertThat(complete.get(),equalTo(true));
        assertThat(error.get(),instanceOf(RuntimeException.class));
    }
    @Test
    public void dropWhile5Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.async(ReactiveSeq.of(1,2,3,4,5),ex)
            .dropWhileInclusive(i -> false)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }
        assertThat(data.get(),equalTo(Vector.of(2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhile1Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.async(ReactiveSeq.of(1,2,3,4,5),ex)
            .dropWhileInclusive(i -> i<4)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(),equalTo(Vector.of(5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void dropWhile0Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.async(ReactiveSeq.of(1,2,3,4,5),ex)
            .dropWhileInclusive(i -> i<0)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(),equalTo(Vector.of(2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }


    @Test
    public void dropWhileStagesAsync() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s =  Spouts.async(ReactiveSeq.of(1,2,3,4,5),ex)
            .dropWhileInclusive(i -> false)
            .forEach(2, n -> {
                assertFalse(complete.get());
                data.updateAndGet(sq -> sq.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });


        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }
        assertThat(data.get(),equalTo(Vector.of(2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());



    }
    @Test
    public void dropWhileStages3Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s = Spouts.async(ReactiveSeq.of(1,2,3,4,5),ex)
            .dropWhileInclusive(i -> i<2)
            .forEach(2, n -> {
                assertFalse(complete.get());
                data.updateAndGet(sq -> sq.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(),equalTo(Vector.of(3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());


    }

    @Test
    public void predicateErrorAsync(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.async(ReactiveSeq.of(1,2,3,4,5),ex)
            .dropWhileInclusive(i ->{ throw new RuntimeException();})
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(),equalTo(Vector.of()));
        assertThat(complete.get(),equalTo(true));
        assertThat(error.get(),instanceOf(RuntimeException.class));
    }

}
