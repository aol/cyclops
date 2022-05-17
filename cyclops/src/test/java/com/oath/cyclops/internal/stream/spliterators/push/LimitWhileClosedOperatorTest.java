package com.oath.cyclops.internal.stream.spliterators.push;

import com.oath.cyclops.SpoutsFixtures;
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

public class LimitWhileClosedOperatorTest {
    Executor ex = Executors.newFixedThreadPool(10);

    @Test
    public void takeWhile5() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .takeWhileInclusive(i -> true)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of(1,2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhile1() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .takeWhileInclusive(i -> i<1)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of(1)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhile0() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .takeWhileInclusive(i -> i<0)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(data.get(),equalTo(Vector.of(1)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhile0_transforms1() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        AtomicInteger peeks = new AtomicInteger(0);
        Spouts.of(1,2,3,4,5)
            .peek(System.out::println)
            .peek(p->peeks.incrementAndGet())
            .takeWhileInclusive(i -> i<0)
            .forEach(n->{
                assertFalse(complete.get());
                data.updateAndGet(s->s.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertThat(peeks.get(),equalTo(1));
        assertThat(data.get(),equalTo(Vector.of(1)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhileStages() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s = Spouts.of(1, 2, 3, 4, 5)
            .takeWhileInclusive(i -> true)
            .forEach(2, n -> {
                assertFalse(complete.get());
                data.updateAndGet(sq -> sq.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });



        assertThat(data.get(),equalTo(Vector.of(1,2)));
        assertThat(complete.get(),equalTo(false));
        assertNull(error.get());

        s.request(10l);
        assertThat(data.get(),equalTo(Vector.of(1,2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhileStages3() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s = Spouts.of(1, 2, 3, 4, 5)
            .takeWhileInclusive(i -> i<3)
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

        assertThat(data.get(),equalTo(Vector.of(1,2)));
        assertThat(complete.get(),equalTo(false));
        assertNull(error.get());

        s.request(10l);
        assertThat(data.get(),equalTo(Vector.of(1,2,3)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }

    @Test
    public void predicateError(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1,2,3,4,5)
            .takeWhileInclusive(i ->{ throw new RuntimeException();})
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
    public void takeWhile5Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        SpoutsFixtures.reactive(ReactiveSeq.of(1,2,3,4,5),ex)
            .takeWhileInclusive(i -> true)
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
        assertThat(data.get(),equalTo(Vector.of(1,2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhile1Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ex)
            .takeWhileInclusive(i -> i<1)
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

        assertThat(data.get(),equalTo(Vector.of(1)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void takeWhile0Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ex)
            .takeWhileInclusive(i -> i<0)
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

        assertThat(data.get(),equalTo(Vector.of(1)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());

    }


    @Test
    public void takeWhileStagesAsync() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s =  Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ex)
            .takeWhileInclusive(i -> true)
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
        assertThat(data.get(),equalTo(Vector.of(1,2,3,4,5)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());



    }
    @Test
    public void takeWhileStages3Async() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription s = Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ex)
            .takeWhileInclusive(i -> i<3)
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

        assertThat(data.get(),equalTo(Vector.of(1,2,3)));
        assertThat(complete.get(),equalTo(true));
        assertNull(error.get());


    }

    @Test
    public void predicateErrorAsync(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ex)
            .takeWhileInclusive(i ->{ throw new RuntimeException();})
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
