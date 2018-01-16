package com.oath.cyclops.streams;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import cyclops.data.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.companion.Semigroups;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.Topic;
import com.oath.cyclops.async.adapters.Queue;
import cyclops.data.HashMap;
import cyclops.data.LazySeq;

import cyclops.control.Option;
import cyclops.control.Maybe;
import cyclops.control.LazyEither;
import cyclops.data.Vector;
import org.hamcrest.Matchers;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

public class BaseSequentialTest {

    public static final int ITERATIONS = 1;

    protected <U> ReactiveSeq<U> of(U... array) {
        return ReactiveSeq.of(array);
    }


    ReactiveSeq<Integer> empty;
    ReactiveSeq<Integer> nonEmpty;

    @Before
    public void setup() {
        empty = of();
        nonEmpty = of(1);
    }

    Integer value2() {
        return 5;
    }

    @Test
    public void subscribeEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));

    }
    @Test
    public void subscribe() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(1));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(2));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(3l);

        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmpty() throws InterruptedException {
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));

    }
    @Test
    public void subscribeError() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(1));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(2));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3Error() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(3l);

        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmptyOnComplete() throws InterruptedException {
        List result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        s.request(1l);
        assertThat(onComplete.get(), Matchers.equalTo(true));
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(0));

    }
    @Test
    public void subscribeErrorOnComplete() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));

        assertThat(onComplete.get(), Matchers.equalTo(false));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(1));
        assertThat(onComplete.get(), Matchers.equalTo(false));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(2));
        assertThat(onComplete.get(), Matchers.equalTo(false));
        s.request(1l);
        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(), Matchers.equalTo(true));
    }
    @Test
    public void subscribe3ErrorOnComplete() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        assertThat(onComplete.get(), Matchers.equalTo(false));
        s.request(4l);
        assertThat(onComplete.get(), Matchers.equalTo(true));

        assertThat(result.size(), Matchers.equalTo(3));
        assertThat(result,hasItems(1,2,3));

        assertThat(onComplete.get(), Matchers.equalTo(true));
    }
    @Test
    public void duplicateDuplicate() {
        for (int k = 0; k < ITERATIONS; k++) {
            assertThat(of(1, 2, 3).duplicate()
                    ._1().duplicate()._1().duplicate()._1().toList(), equalTo(Arrays.asList(1, 2, 3)));
        }

    }

    @Test
    public void duplicateDuplicateDuplicate() {
        for (int k = 0; k < ITERATIONS; k++) {
            assertThat(of(1, 2, 3).duplicate()
                    ._1().duplicate()._1().duplicate()._1().duplicate()._1().toList(), equalTo(Arrays.asList(1, 2, 3)));
        }

    }

    @Test
    public void skipDuplicateSkip() {
        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).toList(), equalTo(Arrays.asList(3)));
        assertThat(of(1, 2, 3).duplicate()._2().skip(1).duplicate()._2().skip(1).toList(), equalTo(Arrays.asList(3)));
    }

    @Test
    public void skipLimitDuplicateLimitSkip() {
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup = of(1, 2, 3).triplicate();
        Option<Integer> head1 = dup._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.none();
        });
        Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> dup2 = dup._2().skip(1).triplicate();
        Option<Integer> head2 = dup2._1().limit(1).to().option().flatMap(l -> {
            return l.size() > 0 ? l.get(0) : Option.none();
        });
        assertThat(dup2._2().skip(1).toList(), equalTo(Arrays.asList(3)));

        assertThat(of(1, 2, 3).duplicate()._1().skip(1).duplicate()._1().skip(1).toList(), equalTo(Arrays.asList(3)));
    }


    @Test
    public void splitThenSplit() {
        assertThat(of(1, 2, 3).to().option(), equalTo(Option.of(Arrays.asList(1, 2, 3))));
        // System.out.println(of(1, 2, 3).splitAtHead()._2.listX());
        System.out.println("split " + of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
        assertEquals(Option.of(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
    }

    @Test
    public void changesEmpty() {
        assertThat(of().changes()
                .toList(), equalTo(Arrays.asList()));


    }


    @Test
    public void changes() {
        assertThat(of(1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 6).changes()
                .toList(), equalTo(Arrays.asList(1, 2, 3, 4, 5, 6)));


    }


    @Test
    public void publishToAndMerge() {
        for (int k = 0; k < ITERATIONS; k++) {
            System.out.println("Publish toNested and zip iteration " + k);
            com.oath.cyclops.async.adapters.Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                    .build();

            Thread t = new Thread(() -> {


                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Closing! " + queue.size());
                queue.close();


            });
            t.start();


            AtomicBoolean complete = new AtomicBoolean(false);
            AtomicBoolean start = new AtomicBoolean(false);

            List<Integer> list = of(1, 2, 3)
                    .publishTo(queue)
                    .peek(System.out::println)
                    .merge(queue)
                    .toList();
            assertThat(list, hasItems(1, 2, 3));
            assertThat(list.size(), equalTo(6));


            System.gc();
        }

    }

    @Test
    public void publishTest() {
        for (int k = 0; k < ITERATIONS; k++) {
            Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                    .build();

            Thread t = new Thread(() -> {


                try {
                    System.out.println("Sleeping!");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Waking!");
                System.out.println("Closing! " + queue.size());
                queue.close();


            });
            t.start();
            of(1, 2, 3).peek(i -> System.out.println("publishing " + i))
                    .publishTo(queue)
                    .forEach(System.out::println);
            assertThat(queue.stream().collect(Collectors.toList()), equalTo(Arrays.asList(1, 2, 3)));
            t = null;
            System.gc();
        }
    }

    @Test
    public void mergeAdapterTest() {
        for (int k = 0; k < ITERATIONS; k++) {
            Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                    .build();

            Thread t = new Thread(() -> {


                queue.add(1);
                queue.add(2);
                queue.add(3);
                try {
                    System.out.println("Sleeping!");
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Waking!");
                System.out.println("Closing! " + queue.size());
                queue.close();


            });
            t.start();


            assertThat(this.<Integer>of().peek(i -> System.out.println("publishing " + i))
                    .merge(queue).collect(Collectors.toList()), equalTo(Arrays.asList(1, 2, 3)));
            t = null;
            System.gc();
        }
    }

    @Test
    public void mergeAdapterTest1() {
        for (int k = 0; k < ITERATIONS; k++) {
            System.out.println("Test iteration " + k);
            Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                    .build();

            Thread t = new Thread(() -> {


                queue.add(1);
                queue.add(2);
                queue.add(3);
                try {
                    //    System.out.println("Sleeping!");
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //   System.out.println("Closing! " + queue.size());
                queue.close();


            });
            t.start();
            assertThat(this.<Integer>of(10).peek(i -> System.out.println("publishing " + i))
                    .merge(queue).collect(Collectors.toList()), hasItems(10, 1, 2, 3));

            t = null;
            System.gc();
        }
    }

    @Test
    public void parallelFanOut() {

        assertThat(of(1, 2, 3, 4)
                .parallelFanOut(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 2 == 0).map(i -> i * 2),
                        s2 -> s2.filter(i -> i % 2 != 0).map(i -> i * 100))
                .collect(Collectors.toList()), Matchers.equalTo(Arrays.asList(4, 100, 8, 300)));

        assertThat(of(1, 2, 3, 4)
                .parallelFanOutZipIn(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 2 == 0).map(i -> i * 2),
                        s2 -> s2.filter(i -> i % 2 != 0).map(i -> i * 100), (a, b) -> a + b)
                .toList(), Matchers.equalTo(Arrays.asList(104, 308)));

    }

    @Test
    public void mergePTest() {
        for (int i = 0; i < ITERATIONS; i++) {
            List<Integer> list = of(3, 6, 9).mergeP(of(2, 4, 8), of(1, 5, 7)).toList();
            assertThat(list, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertThat(list.size(), Matchers.equalTo(9));
        }
    }

    @Test
    public void triplicateFanOut() {

        for (int k = 0; k < ITERATIONS; k++) {
            System.out.println("******************Triplicate & zip.. " + k);
            System.out.println("******************Triplicate & zip.. " + k);
            System.out.println("******************Triplicate & zip.. " + k);
            System.out.println("******************Triplicate & zip.. " + k);
            List<Integer> res = of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .fanOut(s1 -> s1.peek(System.out::println).filter(i -> i % 3 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 3 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 3 == 2).map(i -> i * 1000))
                    .toList();
            assertThat("List is "  + res,res, Matchers.hasItems(6, 100, 2000, 12, 400, 5000, 18, 700, 8000));
        }

    }

    @Test
    public void fanOut() {
        for (int k = 0; k < ITERATIONS; k++) {
            assertThat(of(1, 2, 3, 4)
                    .fanOut(s1 -> s1.filter(i -> i % 2 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 2 != 0).map(i -> i * 100))
                    .toList(), Matchers.hasItems(4, 100, 8, 300));
            assertThat(of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .fanOut(s1 -> s1.filter(i -> i % 3 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 3 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 3 == 2).map(i -> i * 1000))
                    .toList(), Matchers.hasItems(6, 100, 2000, 12, 400, 5000, 18, 700, 8000));
            assertThat(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
                    .fanOut(s1 -> s1.filter(i -> i % 4 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 4 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 4 == 2).map(i -> i * 1000),
                            s4 -> s4.filter(i -> i % 4 == 3).map(i -> i * 10000))
                    .toList(), Matchers.hasItems(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000));
        }
    }

    @Test
    public void parallelFanOut2() {
        for (int k = 0; k < ITERATIONS; k++) {
            assertThat(of(1, 2, 3, 4)
                    .parallelFanOut(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 2 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 2 != 0).map(i -> i * 100))
                    .toList(), Matchers.equalTo(Arrays.asList(4, 100, 8, 300)));
            assertThat(of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .parallelFanOut(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 3 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 3 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 3 == 2).map(i -> i * 1000))
                    .toList(), Matchers.equalTo(Arrays.asList(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
            assertThat(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
                    .parallelFanOut(ForkJoinPool.commonPool(), s1 -> s1.filter(i -> i % 4 == 0).map(i -> i * 2),
                            s2 -> s2.filter(i -> i % 4 == 1).map(i -> i * 100),
                            s3 -> s3.filter(i -> i % 4 == 2).map(i -> i * 1000),
                            s4 -> s4.filter(i -> i % 4 == 3).map(i -> i * 10000))
                    .toList(), Matchers.equalTo(Arrays.asList(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
        }
    }

    @Test
    public void broadcastTest() {
        Topic<Integer> topic = of(1, 2, 3)
                .broadcast();


        ReactiveSeq<Integer> stream1 = topic.stream();
        ReactiveSeq<Integer> stream2 = topic.stream();
        assertThat(stream1.toList(), Matchers.equalTo(Arrays.asList(1, 2, 3)));
        assertThat(stream2.stream().toList(), Matchers.equalTo(Arrays.asList(1, 2, 3)));

    }


    @Test
    public void ambTest() {
        assertThat(of(1, 2, 3).ambWith(Flux.just(10, 20, 30)).toList(), isOneOf(Arrays.asList(10, 20, 30), Arrays.asList(1, 2, 3)));
    }

    @Test
    public void concatMap() {
        for (int k = 0; k < ITERATIONS; k++) {
            assertThat(of(1, 2, 3)
                    .concatMap(i -> of(10, 20, 30 * i))
                    .toList(), equalTo(Arrays.asList(10, 20, 30, 10, 20, 60, 10, 20, 90)));
        }
    }

    @Test
    public void flatMapStreamFilter() {
        assertThat(of(1, 2, 3, null).flatMap(i -> ReactiveSeq.of(i).filter(Objects::nonNull))
                        .collect(Collectors.toList()),
                Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void concatMapStream() {
        assertThat(of(1, 2, 3, null).concatMap(i -> ReactiveSeq.of(i).filter(Objects::nonNull))
                        .collect(Collectors.toList()),
                Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void concatMapMaybe() {
        assertThat(of(1, 2, 3, null).concatMap(Maybe::ofNullable)
                        .collect(Collectors.toList()),
                Matchers.equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void flatMapStream() {
        for (int i = 0; i < ITERATIONS; i++) {
            assertThat(of(1, 2, 3, null).flatMap(Stream::of)
                            .collect(Collectors.toList()),
                    Matchers.equalTo(Arrays.asList(1, 2, 3, null)));
        }
    }

    @Test
    public void flatMap() {
        assertThat(of(1, 2, 3)
                .flatMap(i -> Stream.of(10, 20, 30 * i))
                .toList(), equalTo(Arrays.asList(10, 20, 30, 10, 20, 60, 10, 20, 90)));
    }

    @Test
    public void flatMapSimple() {
        assertThat(of(1)
                .flatMap(i -> Stream.of(10, 20))
                .toList(), equalTo(Arrays.asList(10, 20)));
    }

    @Test
    public void combine() {
        assertThat(of(1, 2, 3, 4, 5, 6, 7, 8)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .takeOne(), Matchers.equalTo(Maybe.of(6)));
    }

    @Test
    public void combineOneFirstOrError() {
        assertThat(of(1)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .findFirstOrError(), Matchers.equalTo(LazyEither.right(1)));
    }

    @Test
    public void combineOne() {
        assertThat(of(1)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .takeOne(), Matchers.equalTo(Maybe.of(1)));
    }

    @Test
    public void combineTwo() {
        assertThat(of(1, 2)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .takeOne(), Matchers.equalTo(Maybe.of(3)));
    }

    @Test
    public void combineEmpty() {
        assertThat(this.<Integer>of()
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .takeOne(), Matchers.equalTo(Maybe.nothing()));
    }

    @Test
    public void combineTerminate() {
        assertThat(of(1, 2, 3, 4, 5, 6, 7, 8)
                .combine((a, b) -> a < 5, Semigroups.intSum)
                .findFirst(), Matchers.equalTo(Optional.of(6)));
    }

    @Test
    public void dropRight() {
        assertThat(of(1, 2, 3).dropRight(1).toList(), hasItems(1, 2));
    }

    @Test
    public void dropRightNeg() {
        assertThat(of(1, 2, 3).dropRight(1).toList(), not(hasItems(3)));
    }

    @Test
    public void dropRightEmpty() {
        assertThat(of().dropRight(1).toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void dropUntil() {
        assertThat(of(1, 2, 3, 4, 5).dropUntil(p -> p == 2).toList().size(), lessThan(5));
    }

    @Test
    public void dropUntilEmpty() {
        assertThat(of().dropUntil(p -> true).toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void dropWhile() {
        assertThat(of(1, 2, 3, 4, 5).dropWhile(p -> p < 6).toList().size(), lessThan(1));
    }

    @Test
    public void dropWhileEmpty() {
        assertThat(of().dropWhile(p -> true).toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void takeRight() {
        assertThat(of(1, 2, 3).takeRight(1).toList(), hasItems(3));
    }

    @Test
    public void takeRightEmpty() {
        assertThat(of().takeRight(1).toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void takeUntil() {
        assertThat(of(1, 2, 3, 4, 5).takeUntil(p -> p == 2).toList().size(), greaterThan(0));
    }

    @Test
    public void takeUntilEmpty() {
        assertThat(of().takeUntil(p -> true).toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void takeWhile() {
        assertThat(of(1, 2, 3, 4, 5).takeWhile(p -> p < 6).toList().size(), greaterThan(1));
    }

    @Test
    public void takeWhileEmpty() {
        assertThat(of().takeWhile(p -> true).toList(), equalTo(Arrays.asList()));
    }


    @Test
    public void presentConvert(){

        assertTrue(of(1).to().option().isPresent());
        assertTrue(of(1).toList().size()>0);
        assertTrue(of(1).to().seq().size()>0);
        assertTrue(of(1).to().lazySeq().size()>0);
        assertTrue(of(1).to().bankersQueue().size()>0);
        assertTrue(of(1).to().vector().size()>0);
        assertTrue(of(1).to().hashSet().size()>0);
        assertTrue(of(1).toSet().size()>0);
        assertTrue(of(1).to().treeSet(Comparator.naturalOrder()).size()>0);
        assertTrue(of(1).to().streamable().size()>0);
        assertTrue(of(1).to().bag().size()>0);
        assertTrue(of(1).to().hashMap(t->t, t->t).size()>0);


        assertTrue(of(1).toSet().size()>0);
        assertTrue(of(1).toList().size()>0);
        assertTrue(of(1).to().streamable().size()>0);


    }

    @Test
    public void optionalConvert() {
        for (int i = 0; i < 10; i++) {
            assertThat(of(1, 2, 3).to().option(), equalTo(Optional.of(LazySeq.of(1, 2, 3))));
        }
    }
    @Test
    public void presentConvert2(){

        assertTrue(of(1,2).to().option().isPresent());
        assertTrue(of(1,2).toList().size()==2);
        assertTrue(of(1,2).to().seq().size()==2);
        assertTrue(of(1,2).to().lazySeq().size()==2);
        assertTrue(of(1,2).to().bankersQueue().size()==2);
        assertTrue(of(1,2).to().vector().size()==2);
        assertTrue(of(1,2).to().hashSet().size()==2);
        assertTrue(of(1,2).toSet().size()==2);
        assertTrue(of(1,2).to().treeSet(Comparator.naturalOrder()).size()==2);
        assertTrue(of(1,2).to().streamable().size()==2);
        assertTrue(of(1,2).to().bag().size()==2);
        assertTrue(of(1,2).to().hashMap(t->t, t->t).size()==2);


        assertTrue(of(1,2).toSet().size()==2);
        assertTrue(of(1,2).toList().size()==2);
        assertTrue(of(1,2).to().streamable().size()==2);


    }



    private int addOne(Integer i) {
        return i + 1;
    }

    private int add(Integer a, Integer b) {
        return a + b;
    }

    private String concat(String a, String b, String c) {
        return a + b + c;
    }

    private String concat4(String a, String b, String c, String d) {
        return a + b + c + d;
    }

    private String concat5(String a, String b, String c, String d, String e) {
        return a + b + c + d + e;
    }



    @Test
    public void batchBySize() {
        System.out.println("Grouped " + of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList()));
        assertThat(of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList()).size(), is(2));
    }


    @Test
    public void prepend() {
        for(int k=0;k<ITERATIONS;k++){
            List<String> result = of(1, 2, 3).prependAll(100, 200, 300)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!", "1!!", "2!!", "3!!")));
        }
    }

    @Test
    public void append() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of(1, 2, 3).appendAll(100, 200, 300)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));
        }
    }

    @Test
    public void concatStreams() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of(1, 2, 3).appendStream(of(100, 200, 300))
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));
        }
    }

    @Test
    public void prependStreams() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of(1, 2, 3).prependStream(of(100, 200, 300))
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!", "1!!", "2!!", "3!!")));
        }
    }

    @Test
    public void insertAt() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of(1, 2, 3).insertAt(1, 100, 200, 300)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));
        }
    }
    @Test
    public void insertAtEmpty() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of().insertAt(0, 100, 200, 300)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!")));
        }
    }

    @Test
    public void insertAtOutOfRangeEmpty() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of().insertAt(1, 100, 200, 300)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!","200!!","300!!")));
        }
    }
    @Test
    public void insertAtOutOfRangeEmptyValue() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of().insertAt(1, 10)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("10!!")));
        }
    }
    @Test
    public void insertAtSOutOfRangeEmpty() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of().insertStreamAt(1, Stream.of(100, 200, 300))
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!","200!!","300!!")));
        }
    }
    @Test
    public void insertAtIOutOfRangeEmpty() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of().insertAt(1, Arrays.asList(100))
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!")));
        }
    }

    @Test
    public void insertAtStream() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of(1, 2, 3).insertStreamAt(1, of(100, 200, 300))
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));
        }
    }

    @Test
    public void insertAtStreamEmpty() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of().insertStreamAt(0, Stream.of(100, 200, 300))
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!")));
        }
    }

    @Test
    public void insertAtStreamOutOfRangeEmpty() {
        List<String> result = of().insertStreamAt(1, Stream.of(100, 200, 300))
                .map(it -> it + "!!").collect(Collectors.toList());

        assertThat(result, equalTo(Arrays.asList("100!!","200!!","300!!")));
    }

    @Test
    public void deleteBetween() {
        for(int k=0;k<ITERATIONS;k++) {
            List<String> result = of(1, 2, 3, 4, 5, 6).deleteBetween(2, 4)
                    .map(it -> it + "!!").collect(Collectors.toList());

            assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "5!!", "6!!")));
        }
    }

    @Test
    public void zip() {
        List<Tuple2<Integer, Integer>> list =
                of(1, 2, 3, 4, 5, 6).zip(of(100, 200, 300, 400))
                        .peek(it -> System.out.println(it)).collect(Collectors.toList());

        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        assertThat(right, hasItem(100));
        assertThat(right, hasItem(200));
        assertThat(right, hasItem(300));
        assertThat(right, hasItem(400));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        assertThat(asList(1, 2, 3, 4), equalTo(left));


    }

    @Test
    public void zip2of() {
        List<Tuple2<Integer, Integer>> list = of(1, 2, 3, 4, 5, 6).zip(of(100, 200, 300, 400)).peek(it -> System.out.println(it)).collect(Collectors.toList());

        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        assertThat(right, hasItem(100));
        assertThat(right, hasItem(200));
        assertThat(right, hasItem(300));
        assertThat(right, hasItem(400));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));

    }

    @Test
    public void zipInOrder() {

        for (int i = 0; i < ITERATIONS; i++) {
            List<Tuple2<Integer, Integer>> list = of(1, 2, 3, 4, 5, 6).limit(6)
                    .zip(of(100, 200, 300, 400).limit(4))
                    .collect(Collectors.toList());

            assertThat(list.get(0)._1(), is(1));
            assertThat(list.get(0)._2(), is(100));
            assertThat(list.get(1)._1(), is(2));
            assertThat(list.get(1)._2(), is(200));
            assertThat(list.get(2)._1(), is(3));
            assertThat(list.get(2)._2(), is(300));
            assertThat(list.get(3)._1(), is(4));
            assertThat(list.get(3)._2(), is(400));

        }


    }

    @Test
    public void zipEmpty() throws Exception {


        final ReactiveSeq<Integer> zipped = empty.zip(this.<Integer>of(), (a, b) -> a + b);
        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {


        final ReactiveSeq<Integer> zipped = empty.zip(nonEmpty, (a, b) -> a + b);
        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {


        final ReactiveSeq<Integer> zipped = nonEmpty.zip(empty, (a, b) -> a + b);


        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {

        final ReactiveSeq<String> first = of("A", "B", "C");
        final ReactiveSeq<Integer> second = of(1, 2, 3);


        final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


        assertThat(zipped.collect(Collectors.toList()), equalTo(asList("A1", "B2", "C3")));
    }


    @Test
    public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
        final ReactiveSeq<String> first = of("A", "B", "C");
        final ReactiveSeq<Integer> second = of(1, 2, 3, 4);


        final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

        assertThat(zipped.collect(Collectors.toList()), equalTo(asList("A1", "B2", "C3")));
    }

    @Test
    public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
        final ReactiveSeq<String> first = of("A", "B", "C", "D");
        final ReactiveSeq<Integer> second = of(1, 2, 3);
        final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


        assertThat(zipped.collect(Collectors.toList()), equalTo(asList("A1", "B2", "C3")));
    }

    @Test
    public void limitWhileTest() {
        List<Integer> list = of(1, 2, 3, 4, 5, 6).limitWhile(it -> it < 4).peek(it -> System.out.println(it)).collect(Collectors.toList());

        System.out.println("List " + list);
        assertThat(list, hasItem(1));
        assertThat(list, hasItem(2));
        assertThat(list, hasItem(3));


    }


    @Test
    public void testReverse() {
        assertThat(of(1, 2, 3).reverse().toList(), is(asList(3, 2, 1)));
    }

    @Test
    public void testShuffle() {
        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3);

        assertEquals(3, s.get().shuffle().toList().size());
        assertThat(s.get().shuffle().toList(), hasItems(1, 2, 3));


    }

    @Test
    public void testCycle() {

        assertEquals(asList(1, 2, 1, 2, 1, 2), of(1, 2).cycle().limit(6).toList());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle().limit(6).toList());
    }

    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i : of(1, 2, 3)) {
            assertThat(list, hasItem(i));
        }
    }

    @Test
    public void testDuplicate() {
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies = of(1, 2, 3, 4, 5, 6).duplicate();

        assertTrue(copies._1().anyMatch(i -> i == 2));
        assertTrue(copies._2().anyMatch(i -> i == 2));
    }


    @Test
    public void testGroupByEager() {
        HashMap<Integer, Vector<Integer>> map1 = of(1, 2, 3, 4).groupBy(i -> i % 2);
        assertEquals(Option.some(Vector.of(2, 4)), map1.get(0));
        assertEquals(Option.some(Vector.of(1, 3)), map1.get(1));
        assertEquals(2, map1.size());


    }


    @Test
    public void testJoin() {
        assertEquals("123", of(1, 2, 3).join());
        assertEquals("1, 2, 3", of(1, 2, 3).join(", "));
        assertEquals("^1|2|3$", of(1, 2, 3).join("|", "^", "$"));
    }


    @Test
    @Ignore //failing!
    public void testOptional() {
        assertEquals(asList(1), of(Optional.of(1)).toList());
        assertEquals(asList(), of(Optional.empty()).toList());
    }

    @Test
    public void testZipDifferingLength() {
        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

        assertEquals(2, list.size());
        assertTrue(asList(1, 2).contains(list.get(0)._1()));
        assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));


    }

    @Test
    public void zip2() {
        assertThat(of(1, 2).zipWithStream(of('a', 'b')).toList(), equalTo(Arrays.asList(tuple(1, 'a'), tuple(2, 'b'))));
    }

    @Test
    public void testZipWithIndex() {
        System.out.println();
        assertEquals(asList(), of().zipWithIndex().toList());
        assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L)).toList());
        assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());
        assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
        assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L), new Tuple2("c", 2L)), of("a", "b", "c").zipWithIndex().toList());
    }


    @Test
    public void testSkipWhile() {
        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

        assertEquals(asList(1, 2, 3, 4, 5), s.get().skipWhile(i -> false).toList());
        assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i % 3 != 0).toList());
        assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i < 3).toList());
        assertEquals(asList(4, 5), s.get().skipWhile(i -> i < 4).toList());
        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
    }

    @Test
    public void testSkipUntil() {
        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i % 3 == 0).toList());
        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i == 3).toList());
        assertEquals(asList(4, 5), s.get().skipUntil(i -> i == 4).toList());
        assertEquals(asList(1, 2, 3, 4, 5), s.get().skipUntil(i -> true).toList());
    }

    @Test
    public void testSkipUntilWithNulls() {
        Supplier<ReactiveSeq<Integer>> s = () -> ReactiveSeq.of(1, 2, null, 3, 4, 5);

        assertEquals(asList(1, 2, null, 3, 4, 5), s.get().skipUntil(i -> true).toList());
    }

    @Test
    public void testLimitWhile() {
        Supplier<ReactiveSeq<Integer>> s = () -> ReactiveSeq.of(1, 2, 3, 4, 5);

        assertEquals(asList(), s.get().limitWhile(i -> false).toList());
        assertEquals(asList(1, 2), s.get().limitWhile(i -> i % 3 != 0).toList());
        assertEquals(asList(1, 2), s.get().limitWhile(i -> i < 3).toList());
        assertEquals(asList(1, 2, 3), s.get().limitWhile(i -> i < 4).toList());
        assertEquals(asList(1, 2, 3, 4, 5), s.get().limitWhile(i -> true).toList());
    }
    @Test
    public void testLimitWhileClosed() {
        Supplier<ReactiveSeq<Integer>> s = () -> ReactiveSeq.of(1, 2, 3, 4, 5);

        assertEquals(asList(1), s.get().limitWhileClosed(i -> false).toList());
        assertEquals(asList(1, 2, 3), s.get().limitWhileClosed(i -> i % 3 != 0).toList());
        assertEquals(asList(1, 2,3), s.get().limitWhileClosed(i -> i < 3).toList());
        assertEquals(asList(1, 2, 3,4), s.get().limitWhileClosed(i -> i < 4).toList());
        assertEquals(asList(1, 2, 3, 4, 5), s.get().limitWhileClosed(i -> true).toList());
    }


    @Test
    public void testLimitUntil() {
        assertEquals(asList(1, 2, 3, 4, 5), of(1, 2, 3, 4, 5).limitUntil(i -> false).toList());
        assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList());
        assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).limitUntil(i -> i == 3).toList());
        assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5).limitUntil(i -> i == 4).toList());
        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());


        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
    }

    @Test
    public void testLimitUntilClosed() {
      //  assertEquals(asList(1, 2, 3, 4, 5), of(1, 2, 3, 4, 5).limitUntilClosed(i -> false).toList());
        assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5).limitUntilClosed(i -> i % 3 == 0).toList());
        assertEquals(asList(1, 2,3), of(1, 2, 3, 4, 5).limitUntilClosed(i -> i == 3).toList());
        assertEquals(asList(1, 2, 3,4), of(1, 2, 3, 4, 5).limitUntilClosed(i -> i == 4).toList());
        assertEquals(asList(1), of(1, 2, 3, 4, 5).limitUntilClosed(i -> true).toList());


        assertEquals(asList(1), of(1, 2, 3, 4, 5).limitUntilClosed(i -> true).toList());
    }
    @Test
    public void testLimitUntilWithNulls() {


        assertThat(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList(), equalTo(asList(1, 2, null, 3, 4, 5)));
    }

    @Test
    public void testPartition() {
        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0)._1().toList());
        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0)._2().toList());

        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0)._1().toList());
        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0)._2().toList());

        assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3)._1().toList());
        assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3)._2().toList());

        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true)._1().toList());
        assertEquals(asList(), s.get().partition(i -> true)._2().toList());

        assertEquals(asList(), s.get().partition(i -> false)._1().toList());
        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitBy(i -> false)._2().toList());
    }

    @Test
    public void testSplitAt() {
        for (int i = 0; i < ITERATIONS; i++) {
            Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


            assertEquals(asList(4, 5, 6), s.get().splitAt(3)._2().toList());


        }
        for (int i = 0; i < ITERATIONS; i++) {
            Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


            assertEquals(asList(1, 2, 3), s.get().splitAt(3)._1().toList());

        }
        for (int i = 0; i < ITERATIONS; i++) {
            Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


            assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(6)._1().toList());
        }
        for (int i = 0; i < ITERATIONS; i++) {
            Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


            assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(7)._1().toList());
        }
    }

    @Test
    public void skipInvestigate() {
        System.out.println("0" + of(1, 2, 3).skip(0).toList());

        assertThat(of(1, 2, 3).skip(0).toList(), equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void splitAtInvestigate() {


        System.out.println("0" + of(1, 2, 3).splitAt(0)._2().toList());

        assertThat(of(1, 2, 3).splitAt(0)._2().toList(), equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void splitAtHeadInvestigate() {
        System.out.println("0" + of(1, 2, 3).splitAt(0)._2().toList());
        System.out.println(of(1, 2, 3).splitAtHead()._1());
        System.out.println(of(1, 2, 3).splitAtHead()._2().toList());
        System.out.println(of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
        System.out.println(of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
        assertThat(of(1, 2, 3).splitAtHead()._2().toList(), equalTo(Arrays.asList(2, 3)));
    }

    @Test
    public void splitAtHeadImpl2() {
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();

        assertThat(t._1().limit(1).toList(), equalTo(Arrays.asList(1)));
        assertThat(t._2().skip(1).toList(), equalTo(Arrays.asList()));

    }

    @Test
    public void limitReplay() {
        final ReactiveSeq<Integer> t = of(1).map(i -> i).flatMap(i -> Stream.of(i));
        assertThat(t.limit(1).toList(), equalTo(Arrays.asList(1)));
        assertThat(t.limit(1).toList(), equalTo(Arrays.asList(1)));
    }

    @Test
    public void duplicateReplay() {
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t._1().limit(1).toList(), equalTo(Arrays.asList(1)));
        assertThat(t._1().limit(1).toList(), equalTo(Arrays.asList(1)));
    }

    @Test
    public void splitLimit() {
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(), equalTo(Arrays.asList(1)));
        assertThat(t._1().limit(1).toList(), equalTo(Arrays.asList(1)));
        assertThat(t._1().limit(1).toList(), equalTo(Arrays.asList(1)));
    }


    @Test

    public void testMinByMaxBy() {
        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

        assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
        assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
    }

    @Test
    public void testUnzip() {
        Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());
        assertThat(u1._1().toList(), equalTo(asList(1, 2, 3)));
        assertThat(u1._2().toList(), equalTo(asList("a", "b", "c")));


    }


    @Test
    public void testFoldLeft() {
        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
        assertTrue(s.get().foldLeft("", String::concat).contains("c"));

        assertEquals(3, (int) s.get().map(str -> str.length()).foldLeft(0, (u, t) -> u + t));


        assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));
        assertEquals("-a-b-c", s.get().map(str -> new StringBuilder(str)).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString());
    }

    @Test
    public void testFoldRight() {
        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

        assertTrue(s.get().foldRight("", String::concat).equals("cba"));
        assertTrue(s.get().foldRight("", String::concat).contains("b"));
        assertTrue(s.get().foldRight("", String::concat).contains("c"));
        assertEquals(3, (int) s.get().map(str -> str.length()).foldRight(0, (t, u) -> u + t));

    }


    //tests converted from maybe-seq suite
    @Test
    public void flattenEmpty() throws Exception {
        assertTrue(this.<Integer>of().flatMap(i -> asList(i, -i).stream()).toList().isEmpty());
    }

    @Test
    public void flatten() throws Exception {
        assertThat(this.<Integer>of(1, 2).flatMap(i -> asList(i, -i).stream()).toList(), equalTo(asList(1, -1, 2, -2)));
    }


    @Test
    public void flattenEmptyStream() throws Exception {

        assertThat(this.<Integer>of(1, 2, 3, 4, 5, 5, 6, 8, 9, 10).flatMap(this::flatMapFun).limit(10).collect(Collectors.toList()),
                equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0)));
    }

    private Stream<Integer> flatMapFun(int i) {
        if (i <= 0) {
            return Arrays.<Integer>asList().stream();
        }
        switch (i) {
            case 1:
                return asList(2).stream();
            case 2:
                return asList(3, 4).stream();
            case 3:
                return asList(5, 6, 7).stream();
            default:
                return asList(0, 0).stream();
        }
    }


}
