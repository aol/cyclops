package cyclops.reactive;

import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.EmitterProcessor;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
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

    @Test
    public void emptyNonEmpty() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Spouts.<Integer>empty().zip(Spouts.of(1,2,3,4,5,6), (a, b) -> a + b)
            .forEach(n->{
                data.set(true);
                values.updateAndGet(v->v.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertFalse(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));
    }
    @Test
    public void emptyNonEmptyIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Subscription sub = Spouts.<Integer>empty().zip(Spouts.of(1,2,3,4,5,6), (a, b) -> a + b)
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

        sub.request(1l);

        assertFalse(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));
    }
    @Test
    public void nonEmptyEmpty() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Spouts.of(1,2,3,4,5,6).zip(Spouts.<Integer>empty(), (a, b) -> a + b)
            .forEach(n->{
                data.set(true);
                values.updateAndGet(v->v.plus(n));
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertFalse(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));
    }
    @Test
    public void nonEmptyEmptyIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Integer>> values = new AtomicReference<Vector<Integer>>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);


        Subscription sub = Spouts.of(1,2,3,4,5,6).zip(Spouts.<Integer>empty(), (a, b) -> a + b)
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

        sub.request(1l);

        assertFalse(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.empty()));
    }
    @Test
    public void pairWise() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Tuple2<Integer,String>>> values = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.of(1).zip(Spouts.of("test"))
            .zip(Spouts.of("test2")).map(t -> Tuple.tuple(t._1()
                    ._1(),
                t._1()
                    ._2() + t._2())).forEach( n -> {
                data.set(true);
                values.updateAndGet(v -> v.plus(n));
            }, e -> {
                error.set(e);
            }, () -> {
                complete.set(true);
            });
        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(Tuple.tuple(1,"testtest2"))));
    }
    @Test
    public void pairWiseIncremental() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicReference<Vector<Tuple2<Integer,String>>> values = new AtomicReference<>(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription sub = Spouts.of(1).zip(Spouts.of("test"))
            .zip(Spouts.of("test2")).map(t -> Tuple.tuple(t._1()
                    ._1(),
                t._1()
                    ._2() + t._2())).forEach(0, n -> {
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

        sub.request(10l);
        assertTrue(data.get());
        assertTrue(complete.get());
        assertNull(error.get());
        assertThat(values.get(),equalTo(Vector.of(Tuple.tuple(1,"testtest2"))));
    }

    @Test
    public void push() {
        AtomicReference<Integer> data = new AtomicReference<>();

        EmitterProcessor<Integer> pushA = EmitterProcessor.create();
        EmitterProcessor<Integer> pushB = EmitterProcessor.create();

        Spouts.from(pushA)
              .zip((t1, t2) -> t1 + t2,pushB)
              .forEachAsync(it -> data.set(it));

        pushA.onNext(1);
        pushB.onNext(2);
        pushB.onNext(3);
        pushB.onNext(4);


        assertThat(data.get(),equalTo(3));


        pushB.onNext(5);
        pushA.onNext(6);


        assertThat(data.get(),equalTo(9));
    }
    @Test
    public void pushFlatMap() {

        ReactiveSeq<Integer> odds = Spouts.of(1, 3, 5, 7, 9);
        ReactiveSeq<Integer> even = Spouts.of(2, 4, 6);

        ReactiveSeq<Vector<Tuple2<Integer,Integer>>> zipped =
                                odds.zip(  (t1, t2) -> Tuple.tuple(t1, t2),even).reduceAll(Vector.empty(),(a, b)->a.plus(b));


        Vector<Tuple2<Integer, Integer>> x = zipped.elementAt(0l).orElse(null);
        System.out.println(x);
        assertThat(x,contains(Tuple.tuple(1, 2),
            Tuple.tuple(3, 4),
            Tuple.tuple(5, 6)));

        ReactiveSeq<Vector<Tuple2<Integer,Integer>>> zipped2 = odds.flatMap(it -> Spouts.of(it)
                                                                                        .zip( (t1, t2) -> Tuple.tuple(t1, t2),even)
        ).reduceAll(Vector.empty(),(a, b)->a.plus(b));

        Vector<Tuple2<Integer, Integer>> x2 = zipped2.elementAt(0l).orElse(null);
        System.out.println("X2 is  " +x2);
        assertThat(x2,contains(Tuple.tuple(1, 2),
            Tuple.tuple(3, 2),
               Tuple.tuple(5, 2),
                   Tuple.tuple(7, 2),
                        Tuple.tuple(9, 2)));
    }



}
