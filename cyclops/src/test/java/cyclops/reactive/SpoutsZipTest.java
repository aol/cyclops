package cyclops.reactive;

import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class SpoutsZipTest {
    @Test
    public void zip2() {
        ReactiveSeq<Integer> it1 = Spouts.of(1);
        ReactiveSeq<Integer> it2 = Spouts.of(2);



        ReactiveSeq<Seq<Integer>> zipped = it1.zip(it2, Seq::of);


        StepVerifier.create(zipped)
            .consumeNextWith(t -> assertThat(t,contains(1,2)))
            .expectComplete()
            .verify();
    }
    @Test
    public void zip3() {
        ReactiveSeq<Integer> it1 = Spouts.of(1);
        ReactiveSeq<Integer> it2 = Spouts.of(2);
        ReactiveSeq<Integer> it3 = Spouts.of(3);


        ReactiveSeq<Seq<Integer>> zipped = it1.zip3(it2, it3, Seq::of);


        StepVerifier.create(zipped)
            .consumeNextWith(t -> assertThat(t,contains(1,2,3)))
            .expectComplete()
            .verify();
    }
    @Test
    public void zip4() {
        ReactiveSeq<Integer> it1 = Spouts.of(1);
        ReactiveSeq<Integer> it2 = Spouts.of(2);
        ReactiveSeq<Integer> it3 = Spouts.of(3);
        ReactiveSeq<Integer> it4 = Spouts.of(4);


        ReactiveSeq<Seq<Integer>> zipped = it1.zip4(it2, it3, it4, Seq::of);


        StepVerifier.create(zipped)
            .consumeNextWith(t -> assertThat(t,contains(1,2,3,4)))
            .expectComplete()
            .verify();
    }
    @Test
    public void zip2Tuple() {
        ReactiveSeq<Integer> it1 = Spouts.of(1);
        ReactiveSeq<Integer> it2 = Spouts.of(2);


        ReactiveSeq<Tuple2<Integer, Integer>> zipped = it1.zip(it2);


        StepVerifier.create(zipped)
            .consumeNextWith(t -> assertThat(Seq.of(t._1(),t._2()),contains(1,2)))
            .expectComplete()
            .verify();
    }
    @Test
    public void zip3Tuple() {
        ReactiveSeq<Integer> it1 = Spouts.of(1);
        ReactiveSeq<Integer> it2 = Spouts.of(2);
        ReactiveSeq<Integer> it3 = Spouts.of(3);


        ReactiveSeq<Tuple3<Integer, Integer, Integer>> zipped = it1.zip3(it2, it3);


        StepVerifier.create(zipped)
            .consumeNextWith(t -> assertThat(Seq.of(t._1(),t._2(),t._3()),contains(1,2,3)))
            .expectComplete()
            .verify();
    }
    @Test
    public void zip4Tuple() {
        ReactiveSeq<Integer> it1 = Spouts.of(1);
        ReactiveSeq<Integer> it2 = Spouts.of(2);
        ReactiveSeq<Integer> it3 = Spouts.of(3);
        ReactiveSeq<Integer> it4 = Spouts.of(4);


        ReactiveSeq<Tuple4<Integer, Integer, Integer, Integer>> zipped = it1.zip4(it2, it3, it4);


        StepVerifier.create(zipped)
            .consumeNextWith(t -> assertThat(Seq.of(t._1(),t._2(),t._3(),t._4()),contains(1,2,3,4)))
            .expectComplete()
            .verify();
    }

    @Test
    public void zipWithSelf() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);



        Spouts.of(1, 2).zip(Spouts.of(1, 2), (a, b) -> a + b)
            .forEach(n->{
                data.set(true);
                values.updateAndGet(v->v.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2,4)));

    }
    @Test
    public void zipWithSelfIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Subscription sub = Spouts.of(1, 2).zip(Spouts.of(1, 2), (a, b) -> a + b)
            .forEach(0, n -> {
                data.set(true);
                values.updateAndGet(v -> v.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        assertFalse(data.get());
        assertFalse(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));

        sub.request(1);

        assertTrue(data.get());
        assertFalse(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2)));

        sub.request(10);

        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2,4)));

    }

    @Test
    public void zipTwoAndThree(){
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);



        Spouts.of(1, 2).zip(Spouts.of(1, 2, 3), (a, b) -> a + b)
            .forEach(n->{
                data.set(true);
                values.updateAndGet(v->v.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2,4)));

    }
    @Test
    public void zipTwoAndThreeIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Subscription sub = Spouts.of(1, 2).zip(Spouts.of(1, 2,3), (a, b) -> a + b)
            .forEach(0, n -> {
                data.set(true);
                values.updateAndGet(v -> v.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        assertFalse(data.get());
        assertFalse(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));

        sub.request(1);

        assertTrue(data.get());
        assertFalse(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2)));

        sub.request(10);

        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2,4)));

    }
    @Test
    public void zipThreeAndTwo(){
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);



        Spouts.of(1, 2,3).zip(Spouts.of(1, 2), (a, b) -> a + b)
            .forEach(n->{
                data.set(true);
                values.updateAndGet(v->v.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2,4)));

    }
    @Test
    public void zipThreeAndTwoIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Subscription sub = Spouts.of(1, 2, 3).zip(Spouts.of(1, 2), (a, b) -> a + b)
            .forEach(0, n -> {
                data.set(true);
                values.updateAndGet(v -> v.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });

        assertFalse(data.get());
        assertFalse(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));

        sub.request(1);

        assertTrue(data.get());
        assertFalse(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2)));

        sub.request(10);

        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(2,4)));

    }


}
