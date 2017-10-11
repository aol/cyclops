package com.aol.cyclops2.control;

import com.aol.cyclops2.types.reactive.AsyncSubscriber;
import com.aol.cyclops2.util.SimpleTimer;
import com.google.common.collect.Lists;
import cyclops.collections.immutable.BagX;
import cyclops.companion.Monoids;
import cyclops.companion.Semigroups;
import cyclops.collections.mutable.ListX;

import com.aol.cyclops2.types.reactive.ReactiveSubscriber;
import cyclops.async.Future;
import cyclops.control.lazy.Eval;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.stream.Streamable;
import cyclops.typeclasses.EnumerationTest.Days;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cyclops.function.Predicates.anyOf;
import static cyclops.function.Predicates.greaterThan;
import static cyclops.function.Predicates.hasItems;
import static cyclops.function.Predicates.in;
import static cyclops.function.Predicates.not;
import static cyclops.stream.ReactiveSeq.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static cyclops.collections.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReactiveSeqTest {
    AtomicBoolean active = new AtomicBoolean(true);

    @Test
    public void testEnums(){

        ReactiveSeq.enums(Days.class)
                   .printOut();
    }
    BiPredicate TRUE = (t, u) -> true;
    @Test
    public void testCrossJoin() {



        // {A} x {B}
        // ---------------------------------------------------------------------
        assertEquals(asList(),
                of().crossJoin(of()).toList());
        assertEquals(asList(),
                of().crossJoin(of(1)).toList());
        assertEquals(asList(),
                of().crossJoin(of(1, 2)).toList());

        assertEquals(asList(),
                of("A").crossJoin(of()).toList());
        assertEquals(asList(
                tuple("A", 1)),
                of("A").crossJoin(of(1)).toList());
        assertEquals(asList(
                tuple("A", 1),
                tuple("A", 2)),
                of("A").crossJoin(of(1, 2)).toList());

        assertEquals(asList(),
                of("A", "B").crossJoin(of()).toList());
        assertEquals(asList(
                tuple("A", 1),
                tuple("B", 1)),
                of("A", "B").crossJoin(of(1)).toList());
        assertEquals(asList(
                tuple("A", 1),
                tuple("A", 2),
                tuple("B", 1),
                tuple("B", 2)),
                of("A", "B").crossJoin(of(1, 2)).toList());

        assertEquals(asList(),
                of("A", "B", "C").crossJoin(of()).toList());
        assertEquals(asList(
                tuple("A", 1),
                tuple("B", 1),
                tuple("C", 1)),
                of("A", "B", "C").crossJoin(of(1)).toList());
        assertEquals(asList(
                tuple("A", 1),
                tuple("A", 2),
                tuple("B", 1),
                tuple("B", 2),
                tuple("C", 1),
                tuple("C", 2)),
                of("A", "B", "C").crossJoin(of(1, 2)).toList());







    }

    @Test
    public void takeOne(){


        assertThat(of()
                        .takeOne().isPresent(),equalTo(false));
        assertThat(of(1,2,3)
                              .takeOne().toOptional().get(),equalTo(1));
        assertThat(ListX.of(1,2,3)
                              .takeOne().toOptional().get(),equalTo(1));

        assertThat(of(1,2,3)
                              .get(0).toOptional().get(),equalTo(1));
        assertThat(ListX.of(1,2,3)
                              .get(0l).toOptional().get(),equalTo(1));
        assertThat(of(1,2,3)
                .get(1).toOptional().get(),equalTo(2));
        assertThat(ListX.of(1,2,3)
                        .get(1l).toOptional().get(),equalTo(2));
    }

    @Test
    public void cycleWhile(){
        of(1).toListX();
        count =0;
        ListX<Integer> b= ReactiveSeq.fromStream(Stream.of(1, 2, 3)).peek(System.out::println)
                .cycleUntil(next->count++==6).toListX();

        System.out.println("B " + b);
        assertEquals(asList(1, 2,3, 1, 2,3),b);
    }
    @Test
    public void combineNoOrderMonoid(){
        assertThat(of(1,2,3)
                .combine(Monoids.intSum,(a, b)->a.equals(b))
                .toListX(),equalTo(ListX.of(1,2,3)));

    }
    @Test
    public void testCombineMonoid(){

        assertThat(of(1,1,2,3)
                .combine(Monoids.intMult,(a, b)->a.equals(b))
                .findFirst().get()
               , equalTo(1));
    }
    @Test
    public void testCombineMonoidTwo(){

        assertThat(of(1,1,2,3)
                        .combine((a, b)->a.equals(b),Monoids.intMult)
                        .findFirst().get()
                , equalTo(1));
    }
    @Test
    public void crossJoinTest(){
        assertThat(of(1, 2)
                   .crossJoin(of('a', 'b'))
                   .toList(),equalTo(ListX.of(tuple(1,'a'),
                                              tuple(1,'b'),
                                              tuple(2,'a'),
                                              tuple(2,'b'))));
    }

    @Test
    public void combinations(){
        SimpleTimer timer = new SimpleTimer();

            Streamable.of(1, 2, 3, 4,5,6)
                    .combinations(3).forEach(s -> System.out.println(s.join(",")));


        long streamTime = timer.getElapsedNanoseconds();
        System.out.println("Streamable took " + streamTime);
        timer = new SimpleTimer();

            of(1, 2, 3, 4, 5, 6)
                    .combinations(3)
                    .forEach(s -> System.out.println(s.join(",")));

        long rsTime = timer.getElapsedNanoseconds();

        System.out.println("RS took " + rsTime);

    }

    @Test
    public void recoverTest(){
        for(int i=0;i<100;i++) {
            Map<String, List<String>> txn = new HashMap<>();
            txn.put("5000", Lists.newArrayList("5001", "5002", "5003", "5004a"));
            txn.put("6000", Lists.newArrayList("6001", "6002a", "6003", "6004"));
            txn.put("7000", Lists.newArrayList("7001", "7002a", "7003", "7004"));
            txn.put("8000", Lists.newArrayList("8001", "8002", "8003a", "8004"));

            List<Long> data = ReactiveSeq.fromIterable(txn.entrySet())
                    .map(e -> e.getValue())
                    .flatMap(v -> v.stream())
                    .map(v -> Long.valueOf(v))
                    .recover(t -> -1L)
                    .toList();

            assertThat(ListX.of(7001l, -1l, 7003l, 7004l, 8001l, 8002l, -1l,
                    8004l, 5001l, 5002l, 5003l, -1l, 6001l, -1l, 6003l, 6004l),
                    equalTo(data));

        }
    }
    @Test
    public void cycleUntil(){
        System.out.println("List " + BagX.of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toListX());
    }

    @Test
    public void multipathsInts() {

        ReactiveSeq<Integer> list = ReactiveSeq.ofInts(1, 2, 3);
        ReactiveSeq<Integer> by10 = list.map(i -> i * 10);
        ReactiveSeq<Integer> plus2 = list.map(i -> i + 2);
        ReactiveSeq<Integer> by10Plus2 = by10.to(mapInts(i -> i + 2));
        assertThat(by10.toListX(), equalTo(Arrays.asList(10, 20, 30)));
        assertThat(plus2.toListX(), equalTo(Arrays.asList(3, 4, 5)));
        assertThat(by10Plus2.toListX(), equalTo(Arrays.asList(12, 22, 32)));
    }

    @Test
    public void multipathsLongs() {

        ReactiveSeq<Long> list = ReactiveSeq.ofLongs(1, 2, 3);
        ReactiveSeq<Long> by10 = list.map(i -> i * 10);
        ReactiveSeq<Long> plus2 = list.map(i -> i + 2);
        ReactiveSeq<Long> by10Plus2 = by10.to(mapLongs(i -> i + 2));
        assertThat(by10.toListX(), equalTo(Arrays.asList(10l, 20l, 30l)));
        assertThat(plus2.toListX(), equalTo(Arrays.asList(3l, 4l, 5l)));
        assertThat(by10Plus2.toListX(), equalTo(Arrays.asList(12l, 22l, 32l)));
    }
    @Test
    public void multipathsDoubles() {

        ReactiveSeq<Double> list = ReactiveSeq.ofDoubles(1, 2, 3);
        ReactiveSeq<Double> by10 = list.map(i -> i * 10);
        ReactiveSeq<Double> plus2 = list.map(i -> i + 2);
        ReactiveSeq<Double> by10Plus2 = by10.to(mapDoubles(i -> i + 2));
        assertThat(by10.toListX(), equalTo(Arrays.asList(10d, 20d, 30d)));
        assertThat(plus2.toListX(), equalTo(Arrays.asList(3d, 4d, 5d)));
        assertThat(by10Plus2.toListX(), equalTo(Arrays.asList(12d, 22d, 32d)));
    }

    @Test
    public void replayable(){

        Stream<Integer> stream = ReactiveSeq.range(0,1000)
                                            .map(i->i*2);

        stream.forEach(System.out::println);
        List<Integer> replayed = stream.collect(Collectors.toList());
        stream.map(i->"hello  " + i)
                .forEach(System.out::println);



    }
    @Test
    public void compareFindFirst(){
        for(int i=0;i<10;i++)
            count += Stream.of(1,2,3)
                            .findFirst().get();
        long time = System.currentTimeMillis();
        for(int i=0;i<50000;i++)
            count += Stream.of(1,2,3)
                    .findFirst().get();

        long stream = System.currentTimeMillis()-time;
        for(int i=0;i<10;i++)
            count += of(1,2,3)
                             .findFirst().get();
        long time2 = System.currentTimeMillis();
        for(int k=0;k<50000;k++)
            count += of(1,2,3)
                                 .findFirst().get();
        long rs = System.currentTimeMillis()-time2;
        System.out.println("Stream " + stream + " rs " + rs + " count " + count);
    }

    int count =0;
    @Test
    public void compareConcat(){
        List<Integer> oneThousand= Stream.iterate(1,i->i+1).limit(1000).collect(Collectors.toList());
        int total = 0;
        for(int x=0;x<10;x++) {


            total += Stream.concat(oneThousand.stream(),oneThousand.stream()).count();

        }
        long time = System.currentTimeMillis();
        for(int k=0;k<5000;k++)
            count += Stream.concat(oneThousand.stream(),oneThousand.stream()).count();
        long stream = System.currentTimeMillis()-time;
        for(int x=0;x<10;x++) {


            total += ReactiveSeq.concat(oneThousand.stream(),oneThousand.stream()).count();

        }
        long time2 = System.currentTimeMillis();
        for(int k=0;k<5000;k++)
            count += ReactiveSeq.concat(oneThousand.stream(),oneThousand.stream()).count();
        long rs = System.currentTimeMillis()-time2;
        System.out.println("Stream " + stream + " rs with construction " + rs + " count " + count);


    }
    @Test
    public void compareFlatMap(){
        for(int k=0;k<10;k++)
            count += Stream.generate(()->1).limit(100).flatMap(i->Stream.iterate(1,x->x+i).limit(500)).count();
        long time = System.currentTimeMillis();
        for(int k=0;k<1000;k++)
            count += Stream.generate(()->1).limit(100).flatMap(i->Stream.iterate(1,x->x+i).limit(500)).count();
        long stream = System.currentTimeMillis()-time;
        for(int k=0;k<10;k++)
            count += ReactiveSeq.generate(()->1).limit(100).flatMap(i->Stream.iterate(1,x->x+i).limit(500)).count();
        long time2 = System.currentTimeMillis();
        for(int k=0;k<1000;k++)
            count += ReactiveSeq.generate(()->1).limit(100).flatMap(i->Stream.iterate(1,x->x+i).limit(500)).count();
        long rs = System.currentTimeMillis()-time2;
        System.out.println("Stream " + stream + " rs with construction " + rs + " count " + count);
    }
    @Test
    public void compareMap(){

        for(int k=0;k<10;k++)
            count +=Stream.generate(()->1).limit(1_000).map(i->i*2).map(i->i*10).map(i->i*100).count();
        long time = System.currentTimeMillis();
        for(int k=0;k<1000;k++)
            count +=Stream.generate(()->1).limit(1_0000).map(i->i*2).map(i->i*10).map(i->i*100).count();
        long stream = System.currentTimeMillis()-time;
        for(int k=0;k<10;k++)
            count +=ReactiveSeq.generate(()->1).limit(1_000).map(i->i*2).map(i->i*10).map(i->i*100).count();
        long time2 = System.currentTimeMillis();
       // ReactiveSeq<Integer> s = ReactiveSeq.generate(()->1).limit(1_0000).transform(i->i*2).transform(i->i*10).transform(i->i*100);
        for(int k=0;k<1000;k++)
            count += ReactiveSeq.generate(()->1).limit(1_0000).map(i->i*2).map(i->i*10).map(i->i*100).count();
        long rs = System.currentTimeMillis()-time2;
        long time3 = System.currentTimeMillis();
        ReactiveSeq<Integer> s = ReactiveSeq.generate(()->1).limit(1_0000).map(i->i*2).map(i->i*10).map(i->i*100);
        for(int k=0;k<1000;k++)
            count += s.count();
        long rsWithout = System.currentTimeMillis()-time3;

        System.out.println("Stream " + stream + " rs with construction " + rs +  " rs without " + rsWithout + " count " + count);
    }
    @Test
    public void mapFuseTest(){
        int total = 0;
        for(int x=0;x<10;x++) {


            total += Stream.concat(IntStream.range(0, 1000).boxed(), IntStream.range(1000, 10000).boxed())
                    .filter(i -> i < 5000)
                    .map(i -> i * 2)
                    .filter(i -> i > 500)
                    .filter(i -> i < 5000)
                    .map(i -> i * 2)
                    .filter(i -> i > 500).collect(Collectors.toList()).size();

        }
        long time = System.currentTimeMillis();
        for(int x=0;x<1000;x++) {


            Stream.iterate(1,i->i+1).limit(10000)
                    .map(i->i*100)
                    .map(i -> i * 2)
                    .map(i->"hello" + i)
                    .map(s->s.length())
                    .map(i -> i * 2)
                    .forEach(i->count++);//collect(CyclopsCollectors.toList()).size();

        }
        System.out.println("Streams " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        ReactiveSeq<Integer> stream = ReactiveSeq.iterate(1, i -> i + 1).limit(10000)
                .map(i -> i * 100)
                .map(i -> i * 2)
                .map(i -> "hello" + i)
                .map(s -> s.length())
                .map(i -> i * 2);
        for(int x=0;x<1000;x++) {

            //total +=
                    stream.forEach(i->count++);//collect(CyclopsCollectors.toList()).size();

        }
        System.out.println("Reactive Seq without construction " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();

        for(int x=0;x<1000;x++) {

            //total +=
            ReactiveSeq.iterate(1, i -> i + 1).limit(10000)
                    .map(i -> i * 100)
                    .map(i -> i * 2)
                    .map(i -> "hello" + i)
                    .map(s -> s.length())
                    .map(i -> i * 2).forEach(i->count++);//collect(CyclopsCollectors.toList()).size();

        }
        System.out.println("Reactive Seq with construction " + (System.currentTimeMillis() - time));

        System.out.println("Total " + total);
    }
    @Test
    public void filterFuseTest(){
        int total = 0;
        for(int x=0;x<10;x++) {


            total += Stream.concat(IntStream.range(0, 1000).boxed(), IntStream.range(1000, 10000).boxed())
                    .filter(i -> i < 5000)
                    .map(i -> i * 2)
                    .filter(i -> i > 500)
                    .filter(i -> i < 5000)
                    .map(i -> i * 2)
                    .filter(i -> i > 500).collect(Collectors.toList()).size();

        }
        long time = System.currentTimeMillis();
        for(int x=0;x<1000;x++) {


           total += Stream.generate(()->1000).limit(1_0000)
                   .map(i->i+1)
                    .filter(i -> i > 5)
                   .filter(i -> i > 6)
                   .filter(i -> i > 7)
                   .filter(i -> i > 8)
                   .filter(i -> i > 9)
                   .filter(i -> i > 10)
                   .filter(i -> i > 10)
                   .collect(Collectors.toList()).size();

        }
        System.out.println("Streams " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        ;
        for(int x=0;x<1000;x++) {

            total += ReactiveSeq.generate(()->1000).limit(1_0000)
                    .map(i->i+1)
                    .filter(i -> i > 5)
                    .filter(i -> i > 6)
                    .filter(i -> i > 7)
                    .filter(i -> i > 8)
                    .filter(i -> i > 9)
                    .filter(i -> i > 10)
                    .filter(i -> i > 10).collect(Collectors.toList()).size();

        }
        System.out.println("Reactive Seq " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        ;
        ReactiveSeq<Integer> stream = ReactiveSeq.generate(() -> 1000).limit(1_0000)
                .map(i->i+1)
                .filter(i -> i > 5)
                .filter(i -> i > 6)
                .filter(i -> i > 7)
                .filter(i -> i > 8)
                .filter(i -> i > 9)
                .filter(i -> i > 10)
                .filter(i -> i > 10);
        for(int x=0;x<5000;x++) {

            total += stream.collect(Collectors.toList()).size();

        }
        System.out.println("Reactive Seq without construction " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        ;
        ReactiveSeq<Integer> stream2 = ReactiveSeq.generate(() -> 1000).limit(1_0000)
                .map(i->i+1)
                .filter(i -> i > 5)
                .filter(i -> i > 6)
                .filter(i -> i > 7)
                .filter(i -> i > 8)
                .filter(i -> i > 9)
                .filter(i -> i > 10)
                .filter(i -> i > 10);
        for(int x=0;x<5000;x++) {

            total += stream2.collect(()->new ArrayList(),List::add,(l1, l2)->l1.addAll(l2)).size();

        }
        System.out.println("Reactive Seq without construction reactiveStream collect" + (System.currentTimeMillis() - time));


        System.out.println("Total " + total);


    }

    @Test
    public void coflatMapTest(){
        ReactiveSeq<ReactiveSeq<Integer>> stream = ReactiveSeq.fromIterable(Arrays.asList(1,2,3))
                                                                .coflatMap(s -> s);

        ReactiveSeq<Integer> stream2 = stream.flatMap(s -> s).map(i -> i * 10);
        ReactiveSeq<Integer> stream3 = stream.flatMap(s -> s).map(i -> i * 100);

        assertThat(stream2.toListX(),equalTo(ListX.of(10,20,30)));
        assertThat(stream3.toListX(),equalTo(ListX.of(100,200,300)));

    }
    @Test
    public void arrayConcat(){
        assertThat(ReactiveSeq.concat(of(1,2,3), of(100,200,300))
                   .map(i->i*1000).toListX(),equalTo(ListX.of(1000,
                2000,
                3000,
                100000,
                200000,
                300000)));

    }
    @Test
    public void lastOneBug(){
        assertThat(of(1, 2, 3, 4)
                .takeRight(1)
                .single()
                .orElse(-1),equalTo(4));
    }

    @Test
    public void testReverseList() {

        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1))
                .reverse().toList(), equalTo(asList(-1, 2, 400,10)));
    }
    @Test
    public void testReverseListLimitFirst() {
        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1)).reverse().limit(2)
                .toList(), equalTo(asList(-1, 2)));

    }

    @Test
    public void testReverseListLimit() {

        assertThat( ReactiveSeq.fromList(Arrays.asList(10,400,2,-1)).limit(2)
                .reverse().toList(), equalTo(asList(400, 10)));
    }
    @Test
    public void testReverseRange() {

        assertThat( ReactiveSeq.range(0,10)
                .reverse().toList(), equalTo(asList(10,9,8,7,6,5,4,3,2,1)));
    }

    @Test
    public void rangeLongMultiReverse(){
        assertThat(ReactiveSeq.rangeLong(0,5).reverse().reverse().count(),equalTo(5l));

    }
    @Test
    public void rangeLong(){
        ReactiveSeq.rangeLong(0,5).reverse().reverse().reverse().printOut();
        of('a').zipS(ReactiveSeq.rangeLong(0,100)).printOut();
    }
    @Test
    public void testParallel(){
        for(int k=0;k<1000;k++) {
            System.out.println("Iteration " + k);
            assertThat(ReactiveSeq.range(0, 1000)
                    .parallel(s -> s.map(i -> i * 2))
                    .count(), equalTo(1000L));
        }
    }
    @Test
    public void testParallel2(){

        for(int k=0;k<1000;k++) {
            System.out.println("Iteration " + k);
            assertThat(ReactiveSeq.range(0, 1000)
                    .parallel(new ForkJoinPool(10),s -> s.map(i -> i * 2))
                    .count(), equalTo(1000L));
        }
    }

    @Test
    public void testLimit(){
        assertThat(ReactiveSeq.fill(1).limit(2).count(),equalTo(2l));
    }

    @Test
    public void fillReplay(){
        ReactiveSeq<Integer> seq = ReactiveSeq.fill(1);
        ReactiveSeq<Integer> seq1 = seq.take(100).map(i->i*2);

        seq.take(100).forEach(System.out::println);
        seq1.forEach(System.err::println);

    }

    @Test
    public void testReplay(){
        Flux<Integer> f1 = Flux.range(0,100);
        Flux<Integer> f2 = f1.map(i->i*2);

        System.out.println(f1.count().block());
        System.out.println(f2.count().block());

        ReactiveSeq<String> stream = of("hello","world");
        ReactiveSeq<String> stream1 = stream.map(str->"hello world " + str);
        stream.forEach(System.out::println);
        stream1.forEach(System.out::println);

        ReactiveSeq<Integer> streama = ReactiveSeq.range(1,100);
        ReactiveSeq<Integer> streamb = streama.map(i->i*2);

        System.out.println(streama.count());
        System.out.println(streama.map(i->i*3).zipWithIndex().count());
        System.out.println(streamb.zipWithIndex().count());
    }
    @Test
    public void replayStream(){

        ReactiveSeq<String> stream = of("hello","world");
        ReactiveSeq<String> stream1 = stream.map(str->"hello world " + str);
        Spliterator<String> sp = stream1.spliterator();

        ReactiveSeq.fromSpliterator(sp).forEach(System.out::println);

        ReactiveSeq.fromSpliterator(sp).forEach(System.err::println);

    }
    @Test
    public void replay(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();
        ReactiveSeq<String> stream1 = stream.map(str->"hello world " + str);
        Spliterator<String> sp = stream1.spliterator();
        pushable.onNext("hello");

        pushable.onComplete();
        ReactiveSeq.fromSpliterator(sp).forEach(System.out::println);
        pushable.onNext("world");

        pushable.onComplete();
        ReactiveSeq.fromSpliterator(sp).forEach(System.err::println);

    }
    @Test
    public void skip(){
        assertThat(of(1,2,3).skip(1).count(),equalTo(2l));
        of(10,1,10,2,10,3).skip(1).printOut();
        of(1,2,3).flatMap(i->Stream.of(10,i)).skip(1).printOut();
    }
    @Test
    public void limit(){
        assertThat(of(1,2,3).limit(2).count(),equalTo(2l));
        of(10,1,10,2,10,3).skip(1).printOut();
        of(1,2,3).flatMap(i->Stream.of(10,i)).skip(1).printOut();
    }
    @Test
    public void multipaths() {

        ReactiveSeq<Integer> list = of(1, 2, 3);
        ReactiveSeq<Integer> by10 = list.map(i -> i * 10);
        ReactiveSeq<Integer> plus2 = list.map(i -> i + 2);
        ReactiveSeq<Integer> by10Plus2 = by10.map(i -> i + 2);
        assertThat(by10.toListX(), equalTo(Arrays.asList(10, 20, 30)));
        assertThat(plus2.toListX(), equalTo(Arrays.asList(3, 4, 5)));
        assertThat(by10Plus2.toListX(), equalTo(Arrays.asList(12, 22, 32)));
    }
/**
    @Test @Ignore
    public void limitPushTest(){
        ReactiveSubscriber<String> pushable = ReactiveSeq.pushable();
        ReactiveSeq<String> reactiveStream = pushable.reactiveStream();
        ReactiveSeq<List<String>> res = reactiveStream.transform(i->i+"-hello").limit(2)
                                               .collectSeq(CyclopsCollectors.toList());
        pushable.onNext("hello1");
        pushable.onNext("hello2");
        pushable.onNext("hello3");


       //LimitSpliterator only supports iteration
        assertThat(res.singleUnsafe().size(),equalTo(3));
    }
 **/

    @Test
    public void forEachWithErrorPush(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();
      //  pushable.onComplete();
        stream.map(i->i+"-hello").limit(2)
                 .forEach(System.out::println, s->System.out.println("Error" + s));

        pushable.onNext("hello1");

        pushable.onError(new RuntimeException());
    }
    @Test @Ignore
    public void block(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();
        new Thread(()->{active.set(false); pushable.onComplete();}).run();
        stream.forEach(System.out::println);
        assertFalse(active.get());
    }
    @Test @Ignore
    public void blockToList(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();
        new Thread(()->{active.set(false); pushable.onComplete();}).run();
        stream.toList();
        assertFalse(active.get());
    }



    @Test @Ignore
    public void limitLast(){
        AsyncSubscriber<String> pushable = Spouts.asyncSubscriber();
        ReactiveSeq<String> stream = pushable.stream();
        pushable.onNext("hello1");

        pushable.onNext("hello2");
        pushable.onNext("hello3");
        pushable.onComplete();
       // reactiveStream.printOut();
        stream.limitLast(2).zipS(Stream.of(1,2)).printOut();
    }

    @Test
    public void testFlatMap(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();


        pushable.onNext("hello");
        pushable.onComplete();
        ReactiveSeq<String> stream = pushable.reactiveStream();
        stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .forEach(System.out::println);
        pushable.onNext("world");
    }
    @Test
    public void testFlatMapOrdering(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        pushable.onComplete();

        pushable.onNext("hello");

        ReactiveSeq<String> stream = pushable.reactiveStream();
        stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .forEach(System.out::println);
        pushable.onNext("world");
    }


    @Test
    public void combineNoOrder(){
        assertThat(of(1,2,3)
                .combine((a, b)->a.equals(b), Semigroups.intSum)
                .toListX(),equalTo(ListX.of(1,2,3)));

    }
    @Test
    public void anyMIteration(){
        Iterator<Integer> it = AnyM.fromStream(of(1,2,3))
                                  .combine((a, b)->a.equals(b), Semigroups.intSum)
                                    .iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());
        }

        assertThat(list,equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void combineNoOrderAnyM(){




        assertThat(AnyM.fromStream(of(1,2,3))
                        .combine((a, b)->a.equals(b), Semigroups.intSum)
                        .toListX(),equalTo(ListX.of(1,2,3)));

    }
    @Test @Ignore
    public void testIterator(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();

        pushable.onNext("hello");
        pushable.onComplete();
        Iterator<Integer> it = stream.map(s->s.length())
                                     .flatMap(s-> IntStream.range(0,s).boxed())
                                     .iterator();
        ListX<Integer> result = ListX.empty();
        while(it.hasNext()){
            result.add(it.next());
        }
       assertThat(result,equalTo(ListX.of(0,1,2,3,4)));
    }
    @Test
    public void testIteratorPull(){

        ReactiveSeq<String> stream = of("hello");


        Iterator<Integer> it = stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .iterator();
        ListX<Integer> result = ListX.empty();
        while(it.hasNext()){
            result.add(it.next());
        }
        assertThat(result,equalTo(ListX.of(0,1,2,3,4)));
    }
    @Test
    public void forEachWithError(){

        ReactiveSubscriber<String> pushable =  Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();

        pushable.onNext("hello");
        pushable.onComplete();
        stream.map(s->s.length())
                .flatMap(s-> IntStream.range(0,s).boxed())
                .forEach(System.out::println,System.err::println);
        pushable.onNext("world");
    }

    @Test @Ignore
    public void zip(){
        Stream<Integer> s = Stream.of(1,2,3);
        Iterator<Integer> it = s.iterator();
        int i = it.next();
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();
        new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pushable.onNext("hello");
            pushable.onNext("hello");
            active.set(false);
            pushable.onComplete();
        }).start();

        assertThat(stream.zipS(Stream.of(1,2)).toList().size(),equalTo(1));
        assertFalse(active.get());
    }

    @Test @Ignore
    public void lazy(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();

        Eval<List<String>> list = stream.peek(System.err::println)
                                        .foldLazy(s->s.collect(Collectors.toList()));

        pushable.onNext("hello");
        pushable.onComplete();
        assertThat(list.get().size(),equalTo(1));

    }
    @Test @Ignore
    public void push(){
        ReactiveSubscriber<String> pushable = Spouts.reactiveSubscriber();
        ReactiveSeq<String> stream = pushable.reactiveStream();
        Executor ex= Executors.newFixedThreadPool(1);
        Future<List<String>> list = stream.peek(System.err::println)
                                           .foldFuture(ex,s->s.collect(Collectors.toList()));

        pushable.onNext("hello");
        pushable.onComplete();
        assertThat(list.orElse(ListX.empty()).size(),equalTo(1));

    }


    @Test
    public void foldInt(){
        Double res= ReactiveSeq.range(1, 1000).mapToInt(i->i).map(i->i*2).filter(i->i<500).average().getAsDouble();
        assertThat(res,equalTo(250d));
    }
    @Test
    public void intOps(){
        assertThat(ReactiveSeq.range(1, 1000).ints(i->i,s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }

    @Test
    public void longs(){
        assertThat(ReactiveSeq.rangeLong(1, 1000).longs(i->i,s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }

    @Test
    public void doubles(){
        assertThat(ReactiveSeq.range(1, 1000).doubles(i->i.doubleValue(),s->s.map(i->i*2).filter(i->i<500))
                             .size(),equalTo(249));
    }
    @Test
    public void ofTestInt(){
        assertThat(ReactiveSeq.ofInts(6)
                             .singleUnsafe(),equalTo(6));
    }
    @Test
    public void ofTestInteger(){
        assertThat(ReactiveSeq.ofInts(new Integer(6))
                             .singleUnsafe(),equalTo(6));
    }
    @Test
    public void ofDouble(){
        assertThat(ReactiveSeq.ofDoubles(6.0)
                             .singleUnsafe(),equalTo(6.0));
    }

    @Test
    public void ofTestObj(){
        assertThat(of("a")
                             .singleUnsafe(),equalTo("a"));
    }
    @Test
    public void intOpsTest(){
        assertThat(ReactiveSeq.ofInts(6)
                             .singleUnsafe(),equalTo(6));
    }
    @Test
    public void coflatMap(){

       assertThat(of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleUnsafe(),equalTo(6));

    }
    @Test
    public void test1() {
        of(1, 2, 3).filter(anyOf(not(in(2, 3, 4)), in(1, 10, 20)));
    }

    @Test
    public void test2() {
        of(1, 2, 3).filter(anyOf(not(in(2, 3, 4)), greaterThan(10)));
    }

    @Test
    public void test3() {
        of(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5)).filter(hasItems(Arrays.asList(2, 3)));
    }

    @Test
    public void test4() {
        of(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5)).filter(not(hasItems(Arrays.asList(2, 3))));
    }

    @Test
    public void test() {

        Predicate<? super Integer> inOne = in(2.4,3,4);
        Predicate<? super Integer> inTwo = in(1,10,20);
        of(1,2,3).filter(anyOf(not(inOne),inTwo));
        of(1,2,3).filter(anyOf(not(in(2.4,3,4)),in(1,10,20)));
    }

    @Test
    public void retryShouldNotThrowNPEIfRetryIsZero() {
        Function<Integer, Integer> fn = i -> 2 * i;

        int result = of(1)
                                .retry(fn, 0, 1, TimeUnit.SECONDS)
                                .firstValue();

        assertEquals(2, result);
    }

    @Test(expected = ArithmeticException.class)
    public void retryShouldExecuteFnEvenIfRetryIsZero() {
        Function<Integer, Integer> fn = i -> i / 0;

        of(1)
                   .retry(fn, 0, 1, TimeUnit.SECONDS)
                   .firstValue();

        fail();
    }

    @Test
    public void retryShouldWaitOnlyAfterFailure() {
        final long[] timings = {System.currentTimeMillis(), Long.MAX_VALUE};
        Function<Integer, Integer> fn = i -> {
            timings[1] = System.currentTimeMillis();
            return 2 * i;
        };

        of(1)
                   .retry(fn, 3, 10000, TimeUnit.MILLISECONDS)
                   .firstValue();

        assertTrue(timings[1] - timings[0] < 5000);
    }
}
