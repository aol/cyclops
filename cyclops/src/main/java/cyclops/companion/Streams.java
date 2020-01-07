package cyclops.companion;

import com.oath.cyclops.internal.stream.*;
import com.oath.cyclops.internal.stream.operators.DebounceOperator;
import com.oath.cyclops.internal.stream.operators.MultiReduceOperator;
import com.oath.cyclops.internal.stream.operators.OnePerOperator;
import com.oath.cyclops.internal.stream.operators.RecoverOperator;
import com.oath.cyclops.internal.stream.spliterators.*;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.stream.Connectable;
import com.oath.cyclops.types.stream.NonPausableConnectable;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.control.Either;

import cyclops.data.Vector;

import cyclops.function.*;

import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.util.box.Mutable;

import com.oath.cyclops.types.stream.PausableConnectable;
import com.oath.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import com.oath.cyclops.types.persistent.PersistentList;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.*;

/**
 * Static utility methods for working with Java  8 Streams
 *
 * @author johnmcclean
 *
 */
@UtilityClass
public class Streams {



    public static <T> ReactiveSeq<ReactiveSeq<T>> combinations(int size,Object[] a){


            final int fromIndex = 0;
            final int toIndex = a.length;

            final Iterator<ReactiveSeq<T>> iter = new Iterator<ReactiveSeq<T>>() {
                private final int[] indices = IntStream.range(fromIndex, fromIndex + size).toArray();

                @Override
                public boolean hasNext() {
                    return indices[0] <= toIndex - size;
                }

                @Override
                public ReactiveSeq<T> next() {
                    final List<T> result = new ArrayList<>(size);

                    for (int idx : indices) {
                        result.add((T) a[idx]);
                    }

                    if (++indices[size - 1] == toIndex) {
                        for (int i = size - 1; i > 0; i--) {
                            if (indices[i] > toIndex - (size - i)) {
                                indices[i - 1]++;

                                for (int j = i; j < size; j++) {
                                    indices[j] = indices[j - 1] + 1;
                                }
                            }
                        }
                    }

                    return ReactiveSeq.fromList(result);
                }
            };

            return ReactiveSeq.fromIterator(iter);

    }
    public static <T> ReactiveSeq<ReactiveSeq<T>> permutations(Object[] a){



        final Iterator<ReactiveSeq<T>> iter = new Iterator<ReactiveSeq<T>>() {
            private final int[] indices = IntStream.range(0, a.length).toArray();
            private Option<ReactiveSeq<T>> next = Maybe.fromEval(Eval.later(this::first)).flatMap(i->i);

            @Override
            public boolean hasNext() {
                return next.isPresent();
            }

            @Override
            public ReactiveSeq<T> next() {
                ReactiveSeq<T> res = next.orElse(null);
                next = Maybe.fromEval(Eval.later(this::genNext)).flatMap(i->i);
                return res;
            }
            private Option<ReactiveSeq<T>> first(){
                return indices.length>0 ? Option.some(ReactiveSeq.fromList(buildList())) : Option.none();
            }
            private Option<ReactiveSeq<T>> genNext() {
               return gen(findNextIndexByOrder());
            }

            private Option<ReactiveSeq<T>> gen(int next) {
                return next<0 ? Option.none() : Option.some(ReactiveSeq.fromList(buildAndSwap(next)));

            }
            private void swapIndices(int i) {
                swap(i++,findMinIndex(i-1));
                for (int j = indices.length - 1 ;i < j;i++,j--) {
                    swap(i,j);
                }
            }

            private int findNextIndexByOrder(){
                int i = indices.length - 2;
                for (;i >= 0 && indices[i] > indices[i + 1];--i) {

                }
                return i;
            }
            private int findMinIndex(int startIdx){
                int idx = startIdx + 1;
                int currentMinValue = indices[idx];
                int minIndex = idx;
                for(;idx<indices.length;idx++){
                    if (indices[startIdx] < indices[idx] && indices[idx] < currentMinValue) {
                        currentMinValue = indices[idx];
                        minIndex = idx;
                    }

                }
                return minIndex;
            }


            private List<T> buildAndSwap(int next) {
                swapIndices(next);
                return buildList();
            }
            private List<T> buildList() {
                final List<T> result = new ArrayList<>(indices.length);
                for (int idx : indices) {
                    result.add((T)a[idx]);
                }
                return result;
            }

            private  void swap(int a, int b) {
                int temp = indices[a];
                indices[a] = indices[b];
                indices[b] = temp;
            }

        };

        return ReactiveSeq.fromIterator(iter);
    }
    /**
     * Perform a For Comprehension over a Stream, accepting 3 generating arrow.
     * This results in a four level nested internal iteration over the provided Publishers.
     *
     *  <pre>
     * {@code
     *
     *   import static cyclops.companion.Streams.forEach4;
     *
          forEach4(IntStream.range(1,10).boxed(),
                   a-> Stream.iterate(a,i->i+1).limit(10),
                  (a,b) -> Stream.<Integer>of(a+b),
                  (a,b,c) -> Stream.<Integer>just(a+b+c),
                  Tuple::tuple)
     *
     * }
     * </pre>
     *
     * @param value1 top level Stream
     * @param value2 Nested Stream
     * @param value3 Nested Stream
     * @param value4 Nested Stream
     * @param yieldingFunction  Generates a result per combination
     * @return Stream with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Stream<R> forEach4(Stream<? extends T1> value1,
                                                                 Function<? super T1, ? extends Stream<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Stream<R2>> value3,
                                                                 Function3<? super T1, ? super R1, ? super R2, ? extends Stream<R3>> value4,
                                                                 Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Stream<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Stream<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Stream<R3> c = value4.apply(in,ina,inb);
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });

    }

    /**
     * Perform a For Comprehension over a Stream, accepting 3 generating function.
     * This results in a four level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     *  import static com.oath.cyclops.reactor.Streames.forEach4;
     *
     *  forEach4(IntStream.range(1,10).boxed(),
                 a-> Stream.iterate(a,i->i+1).limit(10),
                 (a,b) -> Stream.<Integer>just(a+b),
                 (a,b,c) -> Stream.<Integer>just(a+b+c),
                 (a,b,c,d) -> a+b+c+d <100,
                 Tuple::tuple);
     *
     * }
     * </pre>
     *
     * @param value1 top level Stream
     * @param value2 Nested Stream
     * @param value3 Nested Stream
     * @param value4 Nested Stream
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return Stream with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, T3, R1, R2, R3, R> Stream<R> forEach4(Stream<? extends T1> value1,
                                                                 Function<? super T1, ? extends Stream<R1>> value2,
                                                                 BiFunction<? super T1, ? super R1, ? extends Stream<R2>> value3,
                                                                 Function3<? super T1, ? super R1, ? super R2, ? extends Stream<R3>> value4,
                                                                 Function4<? super T1, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                                 Function4<? super T1, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Stream<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Stream<R2> b = value3.apply(in,ina);
                return b.flatMap(inb -> {
                    Stream<R3> c = value4.apply(in,ina,inb);
                    return c.filter(in2->filterFunction.apply(in,ina,inb,in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a For Comprehension over a Stream, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     * import static Streams.forEach3;
     *
     * forEach(IntStream.range(1,10).boxed(),
                a-> Stream.iterate(a,i->i+1).limit(10),
                (a,b) -> Stream.<Integer>of(a+b),
                Tuple::tuple);
     *
     * }
     * </pre>
     *
     *
     * @param value1 top level Stream
     * @param value2 Nested Stream
     * @param value3 Nested Stream
     * @param yieldingFunction Generates a result per combination
     * @return Stream with an element per combination of nested publishers generated by the yielding function
     */
    public static <T1, T2, R1, R2, R> Stream<R> forEach3(Stream<? extends T1> value1,
                                                         Function<? super T1, ? extends Stream<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Stream<R2>> value3,
                                                         Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return value1.flatMap(in -> {

            Stream<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Stream<R2> b = value3.apply(in,ina);
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });


        });


    }

    /**
     * Perform a For Comprehension over a Stream, accepting 2 generating function.
     * This results in a three level nested internal iteration over the provided Publishers.
     * <pre>
     * {@code
     *
     * import static Streams.forEach;
     *
     * forEach(IntStream.range(1,10).boxed(),
               a-> Stream.iterate(a,i->i+1).limit(10),
              (a,b) -> Stream.<Integer>of(a+b),
              (a,b,c) ->a+b+c<10,
              Tuple::tuple)
                .listX();
     * }
     * </pre>
     *
     * @param value1 top level Stream
     * @param value2 Nested publisher
     * @param value3 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T1, T2, R1, R2, R> Stream<R> forEach3(Stream<? extends T1> value1,
                                                         Function<? super T1, ? extends Stream<R1>> value2,
                                                         BiFunction<? super T1, ? super R1, ? extends Stream<R2>> value3,
                                                         Function3<? super T1, ? super R1, ? super R2, Boolean> filterFunction,
                                                         Function3<? super T1, ? super R1, ? super R2, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Stream<R1> a = value2.apply(in);
            return a.flatMap(ina -> {
                Stream<R2> b = value3.apply(in,ina);
                return b.filter(in2->filterFunction.apply(in,ina,in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });



        });
    }

    /**
     * Perform a For Comprehension over a Stream, accepting an additonal generating function.
     * This results in a two level nested internal iteration over the provided Publishers.
     *
     * <pre>
     * {@code
     *
     *  import static Streams.forEach2;
     *  forEach(IntStream.range(1, 10).boxed(),
     *          i -> Stream.range(i, 10), Tuple::tuple)
            .forEach(System.out::println);

    //(1, 1)
    (1, 2)
    (1, 3)
    (1, 4)
    ...
     *
     * }</pre>
     *
     * @param value1 top level Stream
     * @param value2 Nested publisher
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Stream<R> forEach2(Stream<? extends T> value1,
                                               Function<? super T, Stream<R1>> value2,
                                               BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Stream<R1> a = value2.apply(in);
            return a.map(in2 -> yieldingFunction.apply(in,  in2));
        });

    }

    /**
     *
     * <pre>
     * {@code
     *
     *   import static Streams.forEach2;
     *
     *   forEach(IntStream.range(1, 10).boxed(),
     *           i -> Stream.range(i, 10),
     *           (a,b) -> a>2 && b<10,
     *           Tuple::tuple)
           .forEach(System.out::println);

    //(3, 3)
    (3, 4)
    (3, 5)
    (3, 6)
    (3, 7)
    (3, 8)
    (3, 9)
    ...

     *
     * }</pre>
     *
     *
     * @param value1 top level Stream
     * @param value2 Nested publisher
     * @param filterFunction A filtering function, keeps values where the predicate holds
     * @param yieldingFunction Generates a result per combination
     * @return
     */
    public static <T, R1, R> Stream<R> forEach2(Stream<? extends T> value1,
                                               Function<? super T, ? extends Stream<R1>> value2,
                                               BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                               BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {


        return value1.flatMap(in -> {

            Stream<R1> a = value2.apply(in);
            return a.filter(in2->filterFunction.apply(in,in2))
                    .map(in2 -> yieldingFunction.apply(in,  in2));
        });
    }


    /**
     * Create an Optional containing a List materialized from a Stream
     *
     * <pre>
     * {@code
     *   Optional<Seq<Integer>> opt = Streams.streamToOptional(Stream.of(1,2,3));
     *
     *   //Optional[[1,2,3]]
     *
     * }
     * </pre>
     *
     *
     * @param stream To convert into an Optional
     * @return Optional with a List of values
     */
    public final static <T> Optional<Seq<T>> streamToOptional(final Stream<T> stream) {

        final List<T> collected = stream.collect(java.util.stream.Collectors.toList());
        if (collected.size() == 0)
            return Optional.empty();
        return Optional.of(Seq.fromIterable(collected));
    }

    /**
     * Convert an Optional to a Stream
     *
     * <pre>
     * {@code
     *     Stream<Integer> stream = Streams.optionalToStream(Optional.of(1));
     *     //Stream[1]
     *
     *     Stream<Integer> zero = Streams.optionalToStream(Optional.zero());
     *     //Stream[]
     * }
     * </pre>
     *
     * @param optional Optional to convert to a Stream
     * @return Stream with a single value (if present) created from an Optional
     */
    public final static <T> Stream<T> optionalToStream(final Optional<T> optional) {
        if (optional.isPresent())
            return Stream.of(optional.get());
        return Stream.of();
    }

    /**
     * Create a CompletableFuture containing a List materialized from a Stream
     *
     * @param stream To convert into an Optional
     * @return CompletableFuture with a List of values
     */
    public final static <T> CompletableFuture<List<T>> streamToCompletableFuture(final Stream<T> stream) {
        return CompletableFuture.completedFuture(stream.collect(Collectors.toList()));

    }

    /**
     * Convert a CompletableFuture to a Stream
     *
     * @param future CompletableFuture to convert
     * @return  Stream with a single value created from a CompletableFuture
     */
    public final static <T> Stream<T> completableFutureToStream(final CompletableFuture<T> future) {
        return Stream.of(future.join());

    }

    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     *
     * <pre>
     * @{code
     *     Subscription next = Streams.forEach(Stream.of(1,2,3,4),2,System.out::println);
     *
     *     System.out.println("First batch processed!");
     *
     *     next.request(2);
     *
     *      System.out.println("Second batch processed!");
     *
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     3
     *     4
     *     Second batch processed!
     * }
     * </pre>
     *
     * @param stream - the Stream to consume data from
     * @param x To consume from the Stream at this time
     * @param consumerElement To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Subscription forEach(final Stream<T> stream, final long x, final Consumer<? super T> consumerElement) {
        val t2 = FutureStreamUtils.forEachX(stream, x, consumerElement);
        t2._2().run();
        return t2._1().join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * <pre>
     * @{code
     *     Subscription next = Streams.forEach(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue),System.out::println, e->e.printStackTrace());
     *
     *     System.out.println("First batch processed!");
     *
     *     next.request(2);
     *
     *      System.out.println("Second batch processed!");
     *
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *
     *     RuntimeException Stack Trace on System.err
     *
     *     4
     *     Second batch processed!
     * }
     * </pre>
     *
     * @param stream - the Stream to consume data from
     * @param x To consume from the Stream at this time
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Subscription forEach(final Stream<T> stream, final long x,
                                                                final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        val t2 = FutureStreamUtils.forEachXWithError(stream, x, consumerElement, consumerError);
        t2._2().run();
        return t2._1().join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     *
     * <pre>
     * @{code
     *     Subscription next = Streams.forEach(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue) ,System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
     *
     *     System.out.println("First batch processed!");
     *
     *     next.request(2);
     *
     *      System.out.println("Second batch processed!");
     *
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *
     *     RuntimeException Stack Trace on System.err
     *
     *     4
     *     Second batch processed!
     *     The take!
     * }
     * </pre>
     * @param stream - the Stream to consume data from
     * @param x To consume from the Stream at this time
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> Subscription forEach(final Stream<T> stream, final long x,
                                                                final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        val t2 = FutureStreamUtils.forEachXEvents(stream, x, consumerElement, consumerError, onComplete);
        t2._2().run();
        return t2._1().join();
    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,
     * <pre>
     * @{code
     *     Subscription next = Streams.forEach(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue),System.out::println, e->e.printStackTrace());
     *
     *     System.out.println("processed!");
     *
     *
     *
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *     4
     *     processed!
     *
     * }
     * </pre>
     * @param stream - the Stream to consume data from
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     */
    public static <T, X extends Throwable> void forEach(final Stream<T> stream, final Consumer<? super T> consumerElement,
                                                        final Consumer<? super Throwable> consumerError) {


        val t2 = FutureStreamUtils.forEachWithError(stream, consumerElement, consumerError);
        t2._2().run();

    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     *
     * <pre>
     * @{code
     *     Subscription next = Streams.forEach(Stream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::getValue),System.out::println, e->e.printStackTrace(),()->System.out.println("the take!"));
     *
     *     System.out.println("processed!");
     *
     *
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *      4
     *     processed!
     *
     *
     * }
     * </pre>
     * @param stream - the Stream to consume data from
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    public static <T, X extends Throwable> void forEach(final Stream<T> stream, final Consumer<? super T> consumerElement,
                                                        final Consumer<? super Throwable> consumerError, final Runnable onComplete) {

        val t2 = FutureStreamUtils.forEachEvent(stream, consumerElement, consumerError, onComplete);
        t2._2().run();

    }

    /**
     * Execute this Stream on a schedule
     *
     * <pre>
     * {@code
     *  //run at 8PM every night
     * Streams.schedule(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            ,"0 20 * * *",Executors.newScheduledThreadPool(1)));
     * }
     * </pre>
     *
     * Connect to the Scheduled Stream
     *
     * <pre>
     * {@code
     * Connectable<Data> dataStream = Streams.schedule(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            							  .map(this::processJob)
     *            							  ,"0 20 * * *",Executors.newScheduledThreadPool(1)));
     *
     *
     * data.connect().forEach(this::logToDB);
     * }
     * </pre>
     *
     *
     * @param stream the stream to schedule element processing on
     * @param cron Expression that determines when each job will run
     * @param ex ScheduledExecutorService
     * @return Connectable Connectable of emitted from scheduled Stream
     */
    public static <T> Connectable<T> schedule(final Stream<T> stream, final String cron, final ScheduledExecutorService ex) {
        return new NonPausableConnectable<>(
                                          stream).schedule(cron, ex);
    }

    /**
     * Execute this Stream on a schedule
     *
     * <pre>
     * {@code
     *  //run every 60 seconds after last job completes
     *  Streams.scheduleFixedDelay(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            ,60_000,Executors.newScheduledThreadPool(1)));
     * }
     * </pre>
     *
     * Connect to the Scheduled Stream
     *
     * <pre>
     * {@code
     * Connectable<Data> dataStream = Streams.scheduleFixedDelay(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            							  .map(this::processJob)
     *            							  ,60_000,Executors.newScheduledThreadPool(1)));
     *
     *
     * data.connect().forEach(this::logToDB);
     * }
     * </pre>
     *
     *
     * @param stream the stream to schedule element processing on
     * @param delay Between last element completes passing through the Stream until the next one starts
     * @param ex ScheduledExecutorService
     * @return Connectable Connectable of emitted from scheduled Stream
     */
    public static <T> Connectable<T> scheduleFixedDelay(final Stream<T> stream, final long delay, final ScheduledExecutorService ex) {
        return new NonPausableConnectable<>(
                                          stream).scheduleFixedDelay(delay, ex);
    }

    /**
     * Execute this Stream on a schedule
     *
     * <pre>
     * {@code
     *  //run every 60 seconds
     *  Streams.scheduleFixedRate(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob),
     *            60_000,Executors.newScheduledThreadPool(1)));
     * }
     * </pre>
     *
     * Connect to the Scheduled Stream
     *
     * <pre>
     * {@code
     * Connectable<Data> dataStream = Streams.scheduleFixedRate(Stream.generate(()->"next job:"+formatDate(new Date()))
     *            							  .map(this::processJob)
     *            							  ,60_000,Executors.newScheduledThreadPool(1)));
     *
     *
     * data.connect().forEach(this::logToDB);
     * }
     * </pre>
     * @param stream the stream to schedule element processing on
     * @param rate Time in millis between job runs
     * @param ex ScheduledExecutorService
     * @return Connectable Connectable of emitted from scheduled Stream
     */
    public static <T> Connectable<T> scheduleFixedRate(final Stream<T> stream, final long rate, final ScheduledExecutorService ex) {
        return new NonPausableConnectable<>(
                                          stream).scheduleFixedRate(rate, ex);
    }

    /**
     * Split at supplied location
     * <pre>
     * {@code
     * ReactiveSeq.of(1,2,3).splitAt(1)
     *
     *  //Stream[1], Stream[2,3]
     * }
     *
     * </pre>
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> splitAt(final Stream<T> stream, final int where) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicate(stream);
        return Tuple.tuple(
                          Tuple2._1().limit(where), Tuple2._2().skip(where));
    }

    /**
     * Split stream at point where predicate no longer holds
     * <pre>
     * {@code
     *   ReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
     *
     *   //Stream[1,2,3] Stream[4,5,6]
     * }
     * </pre>
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> splitBy(final Stream<T> stream, final Predicate<T> splitter) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicate(stream);
        return Tuple.tuple(
                          takeWhile(Tuple2._1(), splitter), dropWhile(Tuple2._2(), splitter));
    }

    /**
     * Partition a Stream into two one a per element basis, based on predicate's boolean value
     * <pre>
     * {@code
     *  ReactiveSeq.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
     *
     *  //Stream[1,3,5], Stream[2,4,6]
     * }
     *
     * </pre>
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> partition(final Stream<T> stream, final Predicate<? super T> splitter) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicate(stream);
        return Tuple.tuple(
                          Tuple2._1().filter(splitter), Tuple2._2().filter(splitter.negate()));
    }

    /**
     * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code
     *  Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
    	 assertTrue(copies._1.anyMatch(i->i==2));
    	 assertTrue(copies._2.anyMatch(i->i==2));
     *
     * }
     * </pre>
     *
     * @return duplicated stream
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> duplicate(final Stream<T> stream) {

        final Tuple2<Iterator<T>, Iterator<T>> Tuple2 = Streams.toBufferingDuplicator(stream.iterator());
        return Tuple.tuple(
                          Streams.stream(Tuple2._1()), Streams.stream(Tuple2._2()));
    }
    /**
     * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code
     *  Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
    assertTrue(copies._1.anyMatch(i->i==2));
    assertTrue(copies._2.anyMatch(i->i==2));
     *
     * }
     * </pre>
     *
     * @return duplicated stream
     */
    public final static <T> Tuple2<Stream<T>, Stream<T>> duplicate(final Stream<T> stream,Supplier<Deque<T>> bufferFactory) {

        final Tuple2<Iterator<T>, Iterator<T>> Tuple2 = Streams.toBufferingDuplicator(stream.iterator(),bufferFactory);
        return Tuple.tuple(
                Streams.stream(Tuple2._1()), Streams.stream(Tuple2._2()));
    }

    private final static <T> Tuple2<Stream<T>, Stream<T>> duplicatePos(final Stream<T> stream, final int pos) {

        final Tuple2<Iterator<T>, Iterator<T>> Tuple2 = Streams.toBufferingDuplicator(stream.iterator(), pos);
        return Tuple.tuple(
                          Streams.stream(Tuple2._1()), Streams.stream(Tuple2._2()));
    }

    /**
     * Triplicates a Stream
     * Buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code
     * 	Tuple3<ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>> Tuple3 = sequence.triplicate();

     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public final static <T> Tuple3<Stream<T>, Stream<T>, Stream<T>> triplicate(final Stream<T> stream) {

        final Stream<Stream<T>> its = Streams.toBufferingCopier(stream.iterator(), 3)
                                                 .stream()
                                                 .map(it -> Streams.stream(it));
        final Iterator<Stream<T>> it = its.iterator();
        return new Tuple3(
                          it.next(), it.next(), it.next());

    }
    /**
     * Triplicates a Stream
     * Buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code
     * 	Tuple3<ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>> Tuple3 = sequence.triplicate();

     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public final static <T> Tuple3<Stream<T>, Stream<T>, Stream<T>> triplicate(final Stream<T> stream, Supplier<Deque<T>> bufferFactory) {

        final Stream<Stream<T>> its = Streams.toBufferingCopier(stream.iterator(), 3,bufferFactory)
                .stream()
                .map(it -> Streams.stream(it));
        final Iterator<Stream<T>> it = its.iterator();
        return new Tuple3(
                it.next(), it.next(), it.next());

    }

    /**
     * Makes four copies of a Stream
     * Buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     *
     * <pre>
     * {@code
     *
     * 		Tuple4<ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();

     * }
     * </pre>
     * @return
     */
    @SuppressWarnings("unchecked")
    public final static <T> Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> quadruplicate(final Stream<T> stream) {
        final Stream<Stream<T>> its = Streams.toBufferingCopier(stream.iterator(), 4)
                                                 .stream()
                                                 .map(it -> Streams.stream(it));
        final Iterator<Stream<T>> it = its.iterator();
        return new Tuple4(
                          it.next(), it.next(), it.next(), it.next());
    }
    /**
     * Makes four copies of a Stream
     * Buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     *
     * <pre>
     * {@code
     *
     * 		Tuple4<ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();

     * }
     * </pre>
     * @return
     */
    @SuppressWarnings("unchecked")
    public final static <T> Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> quadruplicate(final Stream<T> stream, Supplier<Deque<T>> bufferFactory) {
        final Stream<Stream<T>> its = Streams.toBufferingCopier(stream.iterator(), 4,bufferFactory)
                .stream()
                .map(it -> Streams.stream(it));
        final Iterator<Stream<T>> it = its.iterator();
        return new Tuple4(
                it.next(), it.next(), it.next(), it.next());
    }

    /**
     * Append Stream to this Stream
     *
     * <pre>
     * {@code
     * List<String> result = 	of(1,2,3).appendStream(of(100,200,300))
    									.map(it ->it+"!!")
    									.collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     *
     * @param stream1 to append to
     * @param append to append with
     * @return Stream with Stream appended
     */
    public static final <T> Stream<T> appendStream(final Stream<T> stream1, final Stream<T> append) {
        return Stream.concat(stream1, append);
    }

    /**
     * Prepend Stream to this Stream
     *
     * <pre>
     * {@code
     * List<String> result = of(1,2,3).prependStream(of(100,200,300))
    			.map(it ->it+"!!").collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     *
     * }
     * </pre>
     *
     * @param stream1 to Prepend to
     * @param prepend to Prepend with
     * @return Stream with Stream prepended
     */
    public static final <T> Stream<T> prependStream(final Stream<T> stream1, final Stream<T> prepend) {

        return Stream.concat(prepend, stream1);
    }

    /**
     * Append values to the take of this Stream
     * <pre>
     * {@code
     * List<String> result = 	of(1,2,3).append(100,200,300)
    									.map(it ->it+"!!")
    									.collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     * @param values to append
     * @return Stream with appended values
     */
    public static final <T> Stream<T> append(final Stream<T> stream, final T... values) {
        return appendStream(stream, Stream.of(values));
    }

    /**
     * Prepend given values to the skip of the Stream
     * <pre>
     * {@code
     * List<String> result = 	of(1,2,3).prependAll(100,200,300)
    			.map(it ->it+"!!").collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * @param values to prependAll
     * @return Stream with values prepended
     */
    public static final <T> Stream<T> prepend(final Stream<T> stream, final T... values) {
        return appendStream(Stream.of(values), stream);
    }

    /**
     * Insert data into a stream at given position
     * <pre>
     * {@code
     * List<String> result = 	of(1,2,3).insertAt(1,100,200,300)
    			.map(it ->it+"!!").collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     *
     * }
     * </pre>
     * @param pos to insert data at
     * @param values to insert
     * @return Stream with new data inserted
     */
    public static final <T> Stream<T> insertAt(final Stream<T> stream, final int pos, final T... values) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicatePos(stream, pos);
        return appendStream(append(Tuple2._1().limit(pos), values), Tuple2._2().skip(pos));
    }

    /**
     * Delete elements between given indexes in a Stream
     * <pre>
     * {@code
     * List<String> result = 	Streams.deleteBetween(Stream.of(1,2,3,4,5,6),2,4)
    										.map(it ->it+"!!")
    										.collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
     * }
     * </pre>
     * @param start index
     * @param end index
     * @return Stream with elements removed
     */
    public static final <T> Stream<T> deleteBetween(final Stream<T> stream, final int start, final int end) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicatePos(stream, start);
        return appendStream(Tuple2._1().limit(start), Tuple2._2().skip(end));
    }

    /**
     * Insert a Stream into the middle of this stream at the specified position
     * <pre>
     * {@code
     * List<String> result = 	Streams.insertAt(Stream.of(1,2,3),1,of(100,200,300))
    										.map(it ->it+"!!")
    										.collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     * }
     * </pre>
     * @param stream1 to insert in
     * @param pos to insert Stream at
     * @param insert to insert
     * @return newly conjoined Stream
     */
    public static final <T> Stream<T> insertStreamAt(final Stream<T> stream1, final int pos, final Stream<T> insert) {
        final Tuple2<Stream<T>, Stream<T>> Tuple2 = duplicatePos(stream1, pos);

        return appendStream(appendStream(Tuple2._1().limit(pos), insert), Tuple2._2().skip(pos));
    }



    /**
     * skip elements in Stream until Predicate holds true
     * 	<pre>
     * {@code  Streams.dropUntil(Stream.of(4,3,6,7),i->i==6).collect(CyclopsCollectors.toList())
     *  // [6,7]
     *  }</pre>

     * @param stream Stream to skip elements from
     * @param predicate to applyHKT
     * @return Stream with elements skipped
     */
    public static <U> Stream<U> dropUntil(final Stream<U> stream, final Predicate<? super U> predicate) {
        return dropWhile(stream, predicate.negate());
    }

    public static <U> Stream<U> dropRight(final Stream<U> stream, final int num) {
        return StreamSupport.stream(SkipLastSpliterator.dropRight(stream.spliterator(),num),stream.isParallel());
    }

    public static <U> Stream<U> takeRight(final Stream<U> stream, final int num) {
        return StreamSupport.stream(LimitLastSpliterator.takeRight(stream.spliterator(), num),stream.isParallel());
    }

    public static <T> Stream<T> recover(final Stream<T> stream, final Function<Throwable, ? extends T> fn) {
        return new RecoverOperator<>(
                                     stream, Throwable.class).recover(fn);
    }

    public static <T, EX extends Throwable> Stream<T> recover(final Stream<T> stream, final Class<EX> type, final Function<EX, ? extends T> fn) {
        return new RecoverOperator<T>(
                                      stream, (Class) type).recover((Function) fn);
    }

    /**
     * skip elements in a Stream while Predicate holds true
     *
     * <pre>
     *
     * {@code  Streams.dropWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(CyclopsCollectors.toList())
     *  // [6,7]
     *  }</pre>
     * @param stream
     * @param predicate
     * @return
     */
    public static <U> Stream<U> dropWhile(final Stream<U> stream, final Predicate<? super U> predicate) {
        return StreamSupport.stream(new SkipWhileSpliterator<U>(stream.spliterator(),predicate), stream.isParallel());
    }

    public static <U> Stream<U> take(final Stream<U> stream, final long time, final TimeUnit unit) {
        return StreamSupport.stream(
                new LimitWhileTimeSpliterator<U>(stream.spliterator(),time,unit),stream.isParallel());
    }

    public static <U> Stream<U> drop(final Stream<U> stream, final long time, final TimeUnit unit) {
        return StreamSupport.stream(new SkipWhileTimeSpliterator<U>(stream.spliterator(),time,unit),stream.isParallel());
    }

    public static <T> Stream<T> combine(final Stream<T> stream, final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        final Iterator<T> it = stream.iterator();
        final Object UNSET = new Object();
        return Streams.stream(new Iterator<ReactiveSeq<T>>() {
            T current = (T) UNSET;

            @Override
            public boolean hasNext() {
                return it.hasNext() || current != UNSET;
            }

            @Override
            public ReactiveSeq<T> next() {
                while (it.hasNext()) {
                    final T next = it.next();

                    if (current == UNSET) {
                        current = next;

                    } else if (predicate.test(current, next)) {
                        current = op.apply(current, next);

                    } else {
                        final T result = current;
                        current = (T) UNSET;
                        return ReactiveSeq.of(result, next);
                    }
                }
                if (it.hasNext())
                    return ReactiveSeq.empty();
                final T result = current;
                current = (T) UNSET;
                return ReactiveSeq.of(result);
            }

        })
                          .flatMap(Function.identity());
    }
    public static <T> Iterable<Iterable<T>> combineI(final Iterable<T> stream, final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        final Object UNSET = new Object();
        return ()-> new Iterator<Iterable<T>>() {
            T current = (T) UNSET;
            final Iterator<T> it = stream.iterator();
            @Override
            public boolean hasNext() {
                return it.hasNext() || current != UNSET;
            }

            @Override
            public ReactiveSeq<T> next() {
                while (it.hasNext()) {
                    final T next = it.next();

                    if (current == UNSET) {
                        current = next;

                    } else if (predicate.test(current, next)) {
                        current = op.apply(current, next);

                    } else {
                        final T result = current;
                        current = (T) UNSET;
                        return ReactiveSeq.of(result, next);
                    }
                }
                if (it.hasNext())
                    return ReactiveSeq.empty();
                final T result = current;
                current = (T) UNSET;
                return ReactiveSeq.of(result);
            }

        };
    }

    /**
     * Take elements from a stream while the predicates hold
     * <pre>
     * {@code Streams.takeWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(CyclopsCollectors.toList());
     * //[4,3]
     * }
     * </pre>
     * @param stream
     * @param predicate
     * @return
     */
    public static <U> Stream<U> takeWhile(final Stream<U> stream, final Predicate<? super U> predicate) {
        return StreamSupport.stream(new LimitWhileSpliterator<>(stream.spliterator(), predicate),stream.isParallel());
    }

    /**
     * Take elements from a Stream until the predicate holds
     *  <pre>
     * {@code Streams.takeUntil(Stream.of(4,3,6,7),i->i==6).collect(CyclopsCollectors.toList());
     * //[4,3]
     * }
     * </pre>
     * @param stream
     * @param predicate
     * @return
     */
    public static <U> Stream<U> takeUntil(final Stream<U> stream, final Predicate<? super U> predicate) {
        return takeWhile(stream, predicate.negate());

    }

    /**
     * Reverse a Stream
     *
     * <pre>
     * {@code
     * assertThat(Streams.reverse(Stream.of(1,2,3)).collect(CyclopsCollectors.toList())
    			,equalTo(Arrays.asList(3,2,1)));
     * }
     * </pre>
     *
     * @param stream Stream to reverse
     * @return Reversed stream
     */
    public static <U> Stream<U> reverse(final Stream<U> stream) {
        return ReactiveSeq.of(1).flatMap(i->reversedStream(stream.collect(java.util.stream.Collectors.toList())));

    }

    /**
     * Create a reversed Stream from a List
     * <pre>
     * {@code
     * Streams.reversedStream(asList(1,2,3))
    			.map(i->i*100)
    			.forEach(System.out::println);


    	assertThat(Streams.reversedStream(Arrays.asList(1,2,3)).collect(CyclopsCollectors.toList())
    			,equalTo(Arrays.asList(3,2,1)));
     *
     * }
     * </pre>
     *
     * @param list List to create a reversed Stream from
     * @return Reversed Stream
     */
    public static <U> Stream<U> reversedStream(final List<U> list) {
        return new ReversedIterator<>(
                list).stream();

    }
    /**
     * Create a new Stream that infiniteable cycles the provided Stream
     *
     * <pre>
     * {@code
     * assertThat(Streams.cycle(Stream.of(1,2,3))
     * 						.limit(6)
     * 						.collect(CyclopsCollectors.toList()),
     * 								equalTo(Arrays.asList(1,2,3,1,2,3)));
    	}
     * </pre>
     * @param s Stream to cycle
     * @return New cycling stream
     */
    public static <U> Stream<U> cycle(final Stream<U> s) {
        return cycle(Streamable.fromStream(s));
    }

    /**
     * Create a Stream that infiniteable cycles the provided Streamable
     * @param s Streamable to cycle
     * @return New cycling stream
     */
    public static <U> Stream<U> cycle(final Streamable<U> s) {
        return Stream.iterate(s.stream(), s1 -> s.stream())
                     .flatMap(Function.identity());
    }

    /**
     * Create a Stream that finitely cycles the provided Streamable, provided number of times
     *
     * <pre>
     * {@code
     * assertThat(Streams.cycle(3,Streamable.of(1,2,2))
    							.collect(CyclopsCollectors.toList()),
    								equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
     * }
     * </pre>
     * @param s Streamable to cycle
     * @return New cycling stream
     */
    public static <U> Stream<U> cycle(final long times, final Streamable<U> s) {
        return Stream.iterate(s.stream(), s1 -> s.stream())
                     .limit(times)
                     .flatMap(Function.identity());
    }

    /**
     * Create a stream from an iterable
     * <pre>
     * {@code
     * 	assertThat(Streams.stream(Arrays.asList(1,2,3))
     * 								.collect(CyclopsCollectors.toList()),
     * 									equalTo(Arrays.asList(1,2,3)));

     *
     * }
     * </pre>
     * @param it Iterable to convert to a Stream
     * @return Stream from iterable
     */
    public static <U> Stream<U> stream(final Iterable<U> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    public static <U> Stream<U> stream(final Spliterator<U> it) {
        return StreamSupport.stream(it, false);
    }

    /**
     * Create a stream from an iterator
     * <pre>
     * {@code
     * 	assertThat(Streams.stream(Arrays.asList(1,2,3).iterator())
     * 							.collect(CyclopsCollectors.toList()),
     * 								equalTo(Arrays.asList(1,2,3)));

     * }
     * </pre>
     * @param it Iterator to convert to a Stream
     * @return Stream from iterator
     */
    public static <U> Stream<U> stream(final Iterator<U> it) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
    }

    /**
     * Concat an Object and a Stream
     * If the Object is a Stream, Streamable or Iterable will be converted (or left) in Stream form and concatonated
     * Otherwise a new Stream.of(o) is created
     *
     * @param o Object to concat
     * @param stream  Stream to concat
     * @return Concatonated Stream
     */
    @Deprecated
    public static <U> Stream<U> concat(final Object o, final Stream<U> stream) {
        Stream<U> first = null;
        if (o instanceof Stream) {
            first = (Stream) o;
        } else if (o instanceof Iterable) {
            first = stream((Iterable) o);
        } else if (o instanceof Streamable) {
            first = ((Streamable) o).stream();
        } else {
            first = Stream.of((U) o);
        }
        return Stream.concat(first, stream);

    }

    /**
     * Create a stream from a transform
     * <pre>
     * {@code
     * 	Map<String,String> transform = new HashMap<>();
    	transform.put("hello","world");
    	assertThat(Streams.stream(transform).collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));

     * }</pre>
     *
     *
     * @param it Iterator to convert to a Stream
     * @return Stream from a transform
     */
    public final static <K, V> Stream<Map.Entry<K, V>> stream(final Map<K, V> it) {
        return it.entrySet()
                 .stream();
    }



    public final static <T> T firstValue(final Stream<T> stream) {
        return stream.findAny()
                     .get();
    }

    /**
     * Simultaneously reduce a stream with multiple reducers
     *
     * <pre>{@code
     *
     *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
    	Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
    	val result = Streams.reduce(Stream.of(1,2,3,4),Arrays.asList(sum,mult));


    	assertThat(result,equalTo(Arrays.asList(10,24)));
    	}</pre>
     *
     * @param stream Stream to reduce
     * @param reducers Reducers to reduce Stream
     * @return Reduced Stream values as List entries
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <R> Seq<R> reduce(final Stream<R> stream, final Iterable<? extends Monoid<R>> reducers) {
        return Seq.fromIterable(new MultiReduceOperator<R>(
                                                             stream).reduce(reducers));

    }

    /**
     * Simultanously reduce a stream with multiple reducers
     *
     * <pre>
     * {@code
     *  Monoid<String> concat = Monoid.of("",(a,b)->a+b);
    	Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
    	assertThat(Streams.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join))
    	                 ,equalTo(Arrays.asList("helloworldwoo!",",hello,world,woo!")));
     * }
     * </pre>
     *
     *  @param stream Stream to reduce
     * @param reducers Reducers to reduce Stream
     * @return Reduced Stream values as List entries
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <R> Seq<R> reduce(final Stream<R> stream, final Stream<? extends Monoid<R>> reducers) {
        return reduce(stream, Seq.fromIterable((List) reducers.collect(java.util.stream.Collectors.toList())));

    }

    /**
     * Repeat in a Stream while specified predicate holds
     * <pre>
     * {@code
     *  int count =0;
     *
    	assertThat(Streams.cycleWhile(Stream.of(1,2,2)
    										,next -> count++<6 )
    										.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    public final static <T> Stream<T> cycleWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return Streams.takeWhile(Streams.cycle(stream), predicate);
    }

    /**
     * Repeat in a Stream until specified predicate holds
     *
     * <pre>
     * {@code
     * 	count =0;
    	assertThat(Streams.cycleUntil(Stream.of(1,2,2,3)
    										,next -> count++>10 )
    										.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));

     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    public final static <T> Stream<T> cycleUntil(final Stream<T> stream, final Predicate<? super T> predicate) {
        return Streams.takeUntil(Streams.cycle(stream), predicate);
    }

    /**
     * Generic zip function. E.g. Zipping a Stream and a Sequence
     *
     * <pre>
     * {@code
     * Stream<List<Integer>> zipped = Streams.zip(Stream.of(1,2,3)
    											,ReactiveSeq.of(2,3,4),
    												(a,b) -> Arrays.asList(a,b));


    	List<Integer> zip = zipped.collect(CyclopsCollectors.toList()).getValue(1);
    	assertThat(zip.getValue(0),equalTo(2));
    	assertThat(zip.getValue(1),equalTo(3));
     * }
     * </pre>
     * @param second
     *            Monad to zip with
     * @param zipper
     *            Zipping function
     * @return Stream zipping two Monads
    */
    public final static <T, S, R> Stream<R> zipSequence(final Stream<T> stream, final Stream<? extends S> second,
            final BiFunction<? super T, ? super S, ? extends R> zipper) {
        final Iterator<T> left = stream.iterator();
        final Iterator<? extends S> right = second.iterator();
        return Streams.stream(new Iterator<R>() {

            @Override
            public boolean hasNext() {
                return left.hasNext() && right.hasNext();
            }

            @Override
            public R next() {
                return zipper.apply(left.next(), right.next());
            }

        });

    }



    /**
     * Zip this Monad with a Stream
     *
       <pre>
       {@code
       Stream<List<Integer>> zipped = Streams.zipStream(Stream.of(1,2,3)
    											,Stream.of(2,3,4),
    												(a,b) -> Arrays.asList(a,b));


    	List<Integer> zip = zipped.collect(CyclopsCollectors.toList()).getValue(1);
    	assertThat(zip.getValue(0),equalTo(2));
    	assertThat(zip.getValue(1),equalTo(3));
       }
       </pre>
     *
     * @param second
     *            Stream to zip with
     * @param zipper
     *            Zip funciton
     * @return This monad zipped with a Stream
     */
    public final static <T, S, R> Stream<R> zipStream(final Stream<T> stream,
            final BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second, final BiFunction<? super T, ? super S, ? extends R> zipper) {
        final Iterator<T> left = stream.iterator();
        final Iterator<? extends S> right = second.iterator();
        return Streams.stream(new Iterator<R>() {

            @Override
            public boolean hasNext() {
                return left.hasNext() && right.hasNext();
            }

            @Override
            public R next() {
                return zipper.apply(left.next(), right.next());
            }

        });

    }

    /**
     * Create a sliding view over this Stream
     * <pre>
     * {@code
     * List<List<Integer>> list = Streams.sliding(Stream.of(1,2,3,4,5,6)
    											,2,1)
    								.collect(CyclopsCollectors.toList());


    	assertThat(list.getValue(0),hasItems(1,2));
    	assertThat(list.getValue(1),hasItems(2,3));
     * }
     * </pre>
     * @param windowSize
     *            Size of sliding window
     * @return Stream with sliding view
     */
    public final static <T> Stream<Seq<T>> sliding(final Stream<T> stream, final int windowSize, final int increment) {
        return StreamSupport.stream(new SlidingSpliterator<>(stream.spliterator(),Function.identity(),
                windowSize,increment),stream.isParallel());
    }

    /**
     * Create a sliding view over this Stream
     * <pre>
     * {@code
     * List<List<Integer>> list = Streams.sliding(Stream.of(1,2,3,4,5,6)
    											,2,1)
    								.collect(CyclopsCollectors.toList());


    	assertThat(list.getValue(0),hasItems(1,2));
    	assertThat(list.getValue(1),hasItems(2,3));
     * }
     * </pre>
     * @param windowSize
     *            Size of sliding window
     * @return Stream with sliding view over monad
     */
    public final static <T> Stream<Streamable<T>> window(final Stream<T> stream, final int windowSize, final int increment) {
        final Iterator<T> it = stream.iterator();
        final Mutable<PersistentList<T>> list = Mutable.of(Seq.empty());
        return Streams.stream(new Iterator<Streamable<T>>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Streamable<T> next() {
                for (int i = 0; i < increment && list.get()
                                                     .size() > 0; i++)
                    list.mutate(var -> var.removeAt(0));
                for (; list.get()
                           .size() < windowSize
                        && it.hasNext();) {
                    if (it.hasNext()) {
                        list.mutate(var -> var.insertAt(Math.max(0, var.size()), it.next()));
                    }

                }
                return Streamable.fromIterable(list.get());
            }

        });
    }

    /**
     * Create a sliding view over this Stream
     * <pre>
     * {@code
     * List<List<Integer>> list = Streams.sliding(Stream.of(1,2,3,4,5,6)
    											,2)
    								.collect(CyclopsCollectors.toList());


    	assertThat(list.getValue(0),hasItems(1,2));
    	assertThat(list.getValue(1),hasItems(2,3));
     * }
     * </pre>
     *
     * @param stream Stream to create sliding view on
     * @param windowSize size of window
     * @return
     */
    public final static <T> Stream<Seq<T>> sliding(final Stream<T> stream, final int windowSize) {
        return sliding(stream, windowSize, 1);
    }


    /**
     * Group elements in a Stream by size
     *
       <pre>
       {@code
     *  List<List<Integer>> list = Streams.grouped(Stream.of(1,2,3,4,5,6)
                                                        ,3)
                                                    .collect(CyclopsCollectors.toList());


        assertThat(list.getValue(0),hasItems(1,2,3));
        assertThat(list.getValue(1),hasItems(4,5,6));
        }
     * </pre>
     * @param stream Stream to group
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    public final static <T> Stream<Vector<T>> grouped(final Stream<T> stream, final int groupSize) {
        return StreamSupport.stream(new GroupingSpliterator<>(stream.spliterator(),()->Vector.empty(),
                c->Vector.fromIterable(c),groupSize),stream.isParallel());


    }


    /**
     *
     *
       <pre>
       {@code
     *  List<SetX<Integer>> list = Streams.grouped(Stream.of(1,2,3,4,5,6)
                                                        ,3,()->SetX.zero())
                                                    .collect(CyclopsCollectors.toList());


        assertThat(list.getValue(0),hasItems(1,2,3));
        assertThat(list.getValue(1),hasItems(4,5,6));
        }
     * </pre>
     *
     * @param stream Stream to group
     * @param groupSize Size of each Group
     * @param factory Supplier for creating Collections for holding grouping
     * @return  Stream with elements grouped by size
     */
    public final static <T, C extends PersistentCollection<? super T>> Stream<C> grouped(final Stream<T> stream, final int groupSize,
            final Supplier<C> factory) {
        return StreamSupport.stream(new GroupingSpliterator<>(stream.spliterator(),factory,
                Function.identity(),groupSize),stream.isParallel());


    }

    public final static <T> Streamable<T> shuffle(final Stream<T> stream) {
        final List<T> list = stream.collect(java.util.stream.Collectors.toList());
        Collections.shuffle(list);
        return Streamable.fromIterable(list);
    }

    public final static <T> Streamable<T> toLazyStreamable(final Stream<T> stream) {
        return Streamable.fromStream(stream);
    }


    public final static <U, T> Stream<U> scanRight(final Stream<T> stream, final U identity,
            final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return ReactiveSeq.fromStream(stream)
                  .scanRight(identity, combiner);
    }

    /**
     * Scan left using supplied Monoid
     *
     * <pre>
     * {@code
     *
     * 	assertEquals(asList("", "a", "ab", "abc"),
     * 					Streams.scanLeft(Stream.of("a", "b", "c"),Reducers.toString(""))
     * 			.collect(CyclopsCollectors.toList());
     *
     *         }
     * </pre>
     *
     * @param monoid
     * @return
     */
    public final static <T> Stream<T> scanLeft(final Stream<T> stream, final Monoid<T> monoid) {

        final Iterator<T> it = stream.iterator();
        return Streams.stream(new Iterator<T>() {
            boolean init = false;
            T next = monoid.zero();

            @Override
            public boolean hasNext() {
                if (!init)
                    return true;
                return it.hasNext();
            }

            @Override
            public T next() {
                if (!init) {
                    init = true;
                    return monoid.zero();
                }
                return next = monoid
                                    .apply(next, it.next());

            }

        });

    }

    /**
     * Check that there are specified number of matches of predicate in the Stream
     *
     * <pre>
     * {@code
     *  assertTrue(Streams.xMatch(Stream.of(1,2,3,5,6,7),3, i->i>4));
     * }
     * </pre>
     *
     */
    public static <T> boolean xMatch(final Stream<T> stream, final int num, final Predicate<? super T> c) {

        return stream.filter(t -> c.test(t))
                     .collect(java.util.stream.Collectors.counting()) == num;
    }

    /**
     * <pre>
     * {@code
     * assertThat(Streams.noneMatch(of(1,2,3,4,5),it-> it==5000),equalTo(true));
     * }
     * </pre>
     *
     */
    public final static <T> boolean noneMatch(final Stream<T> stream, final Predicate<? super T> c) {
        return stream.allMatch(c.negate());
    }

    public final static <T> String join(final Stream<T> stream) {
        return stream.map(t -> t.toString())
                     .collect(java.util.stream.Collectors.joining());
    }

    public final static <T> String join(final Stream<T> stream, final String sep) {
        return stream.map(t -> t!=null ? t.toString() : "null")
                     .collect(java.util.stream.Collectors.joining(sep));
    }

    public final static <T> String join(final Stream<T> stream, final String sep, final String start, final String end) {
        return stream.map(t -> t!=null ? t.toString() : "null")
                     .collect(java.util.stream.Collectors.joining(sep, start, end));
    }

    public final static <T, C extends Comparable<? super C>> Optional<T> minBy(final Stream<T> stream, final Function<? super T, ? extends C> f) {
        final Optional<Tuple2<C, T>> o = stream.map(in -> new Tuple2<C, T>(
                                                                           f.apply(in), in))
                                               .min(Comparator.comparing(n -> n._1(), Comparator.naturalOrder()));
        return o.map(p -> p._2());
    }

    public final static <T> Optional<T> min(final Stream<T> stream, final Comparator<? super T> comparator) {
        return stream.collect(java.util.stream.Collectors.minBy(comparator));
    }

    public final static <T, C extends Comparable<? super C>> Optional<T> maxBy(final Stream<T> stream, final Function<? super T, ? extends C> f) {
        final Optional<Tuple2<C, T>> o = stream.map(in -> new Tuple2<C, T>(
                                                                           f.apply(in), in))
                                               .max(Comparator.comparing(n -> n._1(), Comparator.naturalOrder()));
        return o.map(p -> p._2());
    }

    public final static <T> Optional<T> max(final Stream<T> stream, final Comparator<? super T> comparator) {
        return stream.collect(java.util.stream.Collectors.maxBy(comparator));
    }

    /**
     * Attempt to transform this Stream to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     *
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <T, R> R foldMap(final Stream<T> stream, final Reducer<R,T> reducer) {
        return reducer.foldMap(stream);
    }

    /**
     *  Attempt to transform this Stream to the same type as the supplied Monoid, using supplied function
     *  Then use Monoid to reduce values
     *
     * @param mapper Function to transform Monad type
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <T, R> R foldMap(final Stream<T> stream, final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reducer.foldLeft(stream.map(mapper));
    }

    /**
     *
     *
     * @param reducer Use supplied Monoid to reduce values starting via foldLeft
     * @return Reduced result
     */
    public final static <T> T foldLeft(final Stream<T> stream, final Monoid<T> reducer) {
        return reducer.foldLeft(stream);
    }

    /**
     *  Attempt to transform this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     *
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <R,T> R foldLeftMapToType(final Stream<T> stream, final Reducer<R,T> reducer) {
        return reducer.foldMap(stream);
    }

    /**
     *
     *
     * @param reducer Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    public final static <T> T foldRight(final Stream<T> stream, final Monoid<T> reducer) {
        return reducer.foldLeft(Streams.reverse(stream));
    }

    /**
     *  Attempt to transform this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     *
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    public final static <R,T> R foldRightMapToType(final Stream<T> stream, final Reducer<R,T> reducer) {
        return reducer.foldMap(Streams.reverse(stream));
    }

    /**
     * @return Underlying monad converted to a Streamable instance
     */
    public final static <T> Streamable<T> toStreamable(final Stream<T> stream) {
        return Streamable.fromStream(stream);
    }

    /**
     * @return This monad converted to a set
     */
    public final static <T> Set<T> toSet(final Stream<T> stream) {
        return stream.collect(java.util.stream.Collectors.toSet());
    }

    /**
     * @return this monad converted to a list
     */
    public final static <T> List<T> toList(final Stream<T> stream) {

        return stream.collect(java.util.stream.Collectors.toList());
    }

    /**
     *
     * <pre>{@code
     * assertTrue(Streams.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3)));
     * }</pre>
     *
     * @param iterable
     * @return True if Monad starts with Iterable sequence of data
     */
    public final static <T> boolean startsWith(final Stream<T> stream, final Iterable<T> iterable) {
        return startsWith(stream, iterable.iterator());

    }

    private static <T> Tuple2<Integer,Iterator<T>> findSize(Iterable<T> iterable){
        if(iterable instanceof Collection) {
            Collection<T> col = (Collection<T>) iterable;
            return Tuple.tuple(col.size(),col.iterator());
        }
        int size=0;
        final Iterator<T> it = iterable.iterator();
        final List<T> compare1 = new ArrayList<>();
        while (it.hasNext()) {
            compare1.add(it.next());
            size++;
        }
        return Tuple.tuple(size,compare1.iterator());
    }
    public final static <T> boolean endsWith(final Stream<T> stream, final Iterable<T> iterable) {
        Tuple2<Integer,Iterator<T>> sizeAndIterator = findSize(iterable);

        final Deque<T> list = new ArrayDeque<T>(sizeAndIterator._1());
        stream.forEach(v -> {
            list.add(v);
            if (list.size() > sizeAndIterator._1())
                list.remove();
        });
        return startsWith(list.stream(), sizeAndIterator._2());

    }

    public final static <T> boolean startsWith(final Stream<T> stream, final Stream<T> stream2) {
        return startsWith(stream, stream2.iterator());
    }

    /**
     * 	<pre>
     * {@code
     * 		 assertTrue(Streams.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3).iterator()))
     * }</pre>

     * @param iterator
     * @return True if Monad starts with Iterators sequence of data
     */
    public final static <T> boolean startsWith(final Stream<T> stream, final Iterator<T> iterator) {
        final Iterator<T> it = stream.iterator();
        while (iterator.hasNext()) {
            if (!it.hasNext())
                return false;
            if (!Objects.equals(it.next(), iterator.next()))
                return false;
        }
        return true;

    }
    public static <T> ReactiveSeq<T> oneShotStream(final Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        return new OneShotStreamX<T>(new IteratableSpliterator<T>(iterable), Optional.empty());

    }
    public static <T> ReactiveSeq<T> oneShotStream(Stream<T> stream){
        return new OneShotStreamX<T>(stream,Optional.empty());
    }
    public static <T> OneShotStreamX<T> oneShotStream(Spliterator<T> stream,final Optional<ReversableSpliterator> rev){
        return new OneShotStreamX<T>(stream,rev);
    }

    public final static <T> ReactiveSeq<T> reactiveSeq(final Stream<? super T> stream, final Optional<ReversableSpliterator> rev) {
        if (stream instanceof ReactiveSeq)
            return (ReactiveSeq) stream;

          //  return new StreamX<T>((Stream<T>)
            //                              stream, rev);
        return oneShotStream((Stream<T>)stream);
    }
    public final static <T> ReactiveSeq<T> reactiveSeq(final Iterable<T> iterable){
        return ReactiveSeq.fromIterable(iterable);
    }
    public final static <T> ReactiveSeq<T> reactiveSeq(final Stream<T> stream){
        return ReactiveSeq.fromStream(stream);
    }

    public final static <T> ReactiveSeq<T> reactiveSeq(final Spliterator<? super T> stream, final Optional<ReversableSpliterator> rev) {

        return new StreamX<T>((Spliterator<T>)
                stream, rev);

    }

    /**
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
     *
     * <pre>
     * {@code
     * assertThat(Arrays.asList(1, 0, 2, 0, 3, 0, 4),
     * 			equalTo( Streams.intersperse(Stream.of(1, 2, 3, 4),0));
     * }
     * </pre>
     */
    public static <T> Stream<T> intersperse(final Stream<T> stream, final T value) {
        return stream.flatMap(t -> Stream.of(value, t))
                     .skip(1);
    }

    /**
     * Keep only those elements in a stream that are of a given type.
     *
     *
     * assertThat(Arrays.asList(1, 2, 3),
     *      equalTo( Streams.ofType(Stream.of(1, "a", 2, "b", 3,Integer.class));
     *
     */
    @SuppressWarnings("unchecked")
    public static <T, U> Stream<U> ofType(final Stream<T> stream, final Class<? extends U> type) {
        return stream.filter(type::isInstance)
                     .map(t -> (U) t);
    }

    /**
     * Cast all elements in a stream to a given type, possibly throwing a
     * {@link ClassCastException}.
     *
     * <pre>
     * {@code
     * Streams.cast(Stream.of(1, "a", 2, "b", 3),Integer.class)
     *  // throws ClassCastException
     *  }
     */
    public static <T, U> Stream<U> cast(final Stream<T> stream, final Class<? extends U> type) {
        return stream.map(type::cast);
    }



    public final static <T> Stream<T> narrow(Stream<? extends T> stream){
        return (Stream<T>)stream;
    }


    /**
     * flatMap operation that allows a Collection to be returned
     * <pre>
     * {@code
     * 	assertThat(Streams.flatMapCollection(Stream.of(20),i->Arrays.asList(1,2,i))
     * 								.collect(CyclopsCollectors.toList()),
     * 								equalTo(Arrays.asList(1,2,20)));

     * }
     * </pre>
     *
     */
    public final static <T, R> Stream<R> concatMapterable(final Stream<T> stream, final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return stream.flatMap(fn.andThen(c -> stream(c)));

    }

    /**
     * <pre>
     * {@code
     * 	assertThat(Streams.flatMapStream(Stream.of(1,2,3),
     * 							i->Stream.of(i)).collect(CyclopsCollectors.toList()),
     * 							equalTo(Arrays.asList(1,2,3)));

     *
     * }
     * </pre>
     *
     */
    public final static <T, R> Stream<R> flatMapStream(final Stream<T> stream, final Function<? super T, ? extends BaseStream<? extends R, ?>> fn) {
        return stream.flatMap(fn.andThen(bs -> {

            if (bs instanceof Stream)
                return (Stream<R>) bs;
            else
                return Streams.stream(bs.iterator());

        }));
    }

    /**
     * cross type flatMap, removes null entries
     * <pre>
     * {@code
     * 	 assertThat(Streams.flatMapOptional(Stream.of(1,2,3,null),
     * 										Optional::ofNullable)
     * 										.collect(CyclopsCollectors.toList()),
     * 										equalTo(Arrays.asList(1,2,3)));

     * }
     * </pre>
     */
    public final static <T, R> Stream<R> flatMapOptional(final Stream<T> stream, final Function<? super T, Optional<? extends R>> fn) {
        return stream.flatMap(in -> Streams.optionalToStream(fn.apply(in)));

    }

    public final static <T> Stream<T> flatten(final Stream<Stream<T>> stream) {
       return stream.flatMap(Function.identity());
    }

    /**
     *<pre>
     * {@code
     * 	assertThat(Streams.flatMapCompletableFuture(Stream.of(1,2,3),
     * 								i->CompletableFuture.completedFuture(i+2))
     * 								.collect(CyclopsCollectors.toList()),
     * 								equalTo(Arrays.asList(3,4,5)));

     * }
     *</pre>
     */
    public final static <T, R> Stream<R> flatMapCompletableFuture(final Stream<T> stream,
            final Function<? super T, CompletableFuture<? extends R>> fn) {
        return stream.flatMap(in -> Streams.completableFutureToStream(fn.apply(in)));

    }

    /**
     * Perform a flatMap operation where the result will be a flattened stream of Characters
     * from the CharSequence returned by the supplied function.
     *
     * <pre>
     * {@code
     *   List<Character> result = Streams.liftAndBindCharSequence(Stream.of("input.file"),
    								.i->"hello world")
    								.toList();

    	assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
     * }
     * </pre>
     *
     * @param fn
     * @return
     *///rename -flatMapCharSequence
    public final static <T> Stream<Character> flatMapCharSequence(final Stream<T> stream, final Function<? super T, CharSequence> fn) {
        return stream.flatMap(fn.andThen(CharSequence::chars)
                                .andThen(s->s.mapToObj(i->Character.toChars(i)[0])));
    }

    /**
     *  Perform a flatMap operation where the result will be a flattened stream of Strings
     * from the text loaded from the supplied files.
     *
     * <pre>
     * {@code
     *
    	List<String> result = Streams.liftAndBindFile(Stream.of("input.file")
    							.map(getClass().getClassLoader()::getResource)
    							.peek(System.out::println)
    							.map(URL::getFile)
    							,File::new)
    							.toList();

    	assertThat(result,equalTo(Arrays.asList("hello","world")));
     *
     * }
     *
     * </pre>
     *
     * @param fn
     * @return
     */
    public final static <T> Stream<String> flatMapFile(final Stream<T> stream, final Function<? super T, File> fn) {
        return stream.flatMap(fn.andThen(f->ExceptionSoftener.softenSupplier(()->Files.lines(Paths.get(f.getAbsolutePath()) ) ).get()));
    }

    /**
     *  Perform a flatMap operation where the result will be a flattened stream of Strings
     * from the text loaded from the supplied URLs
     *
     * <pre>
     * {@code
     * List<String> result = Streams.liftAndBindURL(Stream.of("input.file")
    							,getClass().getClassLoader()::getResource)
    							.collect(CyclopsCollectors.toList();

    	assertThat(result,equalTo(Arrays.asList("hello","world")));
     *
     * }
     * </pre>
     *
     * @param fn
     * @return
     */
    public final static <T> Stream<String> flatMapURL(final Stream<T> stream, final Function<? super T, URL> fn) {
        return stream.flatMap(fn.andThen(url -> ExceptionSoftener.softenSupplier(() -> {
            final BufferedReader in = new BufferedReader(
                                                         new InputStreamReader(
                                                                               url.openStream()));

            return in.lines();
        })
                                                                 .get()));

    }

    /**
      *  Perform a flatMap operation where the result will be a flattened stream of Strings
     * from the text loaded from the supplied BufferedReaders
     *
     * <pre>
     * List<String> result = Streams.liftAndBindBufferedReader(Stream.of("input.file")
    							.map(getClass().getClassLoader()::getResourceAsStream)
    							.map(InputStreamReader::new)
    							,BufferedReader::new)
    							.collect(CyclopsCollectors.toList();

    	assertThat(result,equalTo(Arrays.asList("hello","world")));
     *
     * </pre>
     *
     *
     * @param fn
     * @return
     */
    public final static <T> Stream<String> flatMapBufferedReader(final Stream<T> stream, final Function<? super T, BufferedReader> fn) {
        return stream.flatMap(fn.andThen(in -> ExceptionSoftener.softenSupplier(() -> {


            return in.lines();
        })
                                         .get()));
    }

    public static final <A> Tuple2<Iterable<A>, Iterable<A>> toBufferingDuplicator(final Iterable<A> it,Supplier<Deque<A>> bufferFactory) {
        return Tuple.tuple(()-> toBufferingDuplicator(it.iterator(), Long.MAX_VALUE,bufferFactory)._1(),
                ()-> toBufferingDuplicator(it.iterator(), Long.MAX_VALUE,bufferFactory)._2());
    }
    public static final <A> Tuple2<Iterable<A>, Iterable<A>> toBufferingDuplicator(final Iterable<A> it) {
        return Tuple.tuple(()-> toBufferingDuplicator(it.iterator(), Long.MAX_VALUE)._1(),
                            ()-> toBufferingDuplicator(it.iterator(), Long.MAX_VALUE)._2());
    }
    public static final <A> Tuple2<Iterator<A>, Iterator<A>> toBufferingDuplicator(final Iterator<A> iterator) {
        return toBufferingDuplicator(iterator, Long.MAX_VALUE);
    }

    public static final <A> Tuple2<Iterator<A>, Iterator<A>> toBufferingDuplicator(final Iterator<A> iterator, final long pos) {
        final LinkedList<A> bufferTo = new LinkedList<A>();
        final LinkedList<A> bufferFrom = new LinkedList<A>();
        return Tuple.tuple(
                          new DuplicatingIterator(
                                                  bufferTo, bufferFrom, iterator, Long.MAX_VALUE, 0),
                          new DuplicatingIterator(
                                                  bufferFrom, bufferTo, iterator, pos, 0));
    }
    public static final <A> Tuple2<Iterator<A>, Iterator<A>> toBufferingDuplicator(final Iterator<A> iterator, Supplier<Deque<A>> bufferFactory) {
        return toBufferingDuplicator(iterator, Long.MAX_VALUE,bufferFactory);
    }

    public static final <A> Tuple2<Iterator<A>, Iterator<A>> toBufferingDuplicator(final Iterator<A> iterator, final long pos,Supplier<Deque<A>> bufferFactory) {
        final Deque<A> bufferTo = bufferFactory.get();
        final Deque<A> bufferFrom = bufferFactory.get();
        return Tuple.tuple(
                new DuplicatingIterator(
                        bufferTo, bufferFrom, iterator, Long.MAX_VALUE, 0),
                new DuplicatingIterator(
                        bufferFrom, bufferTo, iterator, pos, 0));
    }


    public static final <A> Seq<Iterable<A>> toBufferingCopier(final Iterable<A> it, final int copies) {

        return  Seq.range(0,copies)
                .zipWithIndex()
                .map(t->()-> toBufferingCopier(it.iterator(),copies).getOrElseGet(t._2().intValue(),()->Arrays.<A>asList().iterator()));
    }
    public static final <A> Seq<Iterable<A>> toBufferingCopier(final Iterable<A> it, final int copies,Supplier<Deque<A>> bufferSupplier) {

        return  Seq.range(0,copies)
                .zipWithIndex()
                .map(t->() ->  toBufferingCopier(it.iterator(),copies,bufferSupplier).getOrElseGet(t._2().intValue(),
                        ()->Arrays.<A>asList().iterator()));
    }

    public static final <A> Seq<Iterator<A>> toBufferingCopier(final Iterator<A> iterator, final int copies) {
        final List<Iterator<A>> result = new ArrayList<>();

        ArrayList<Deque<A>> localBuffers = new ArrayList<>(copies);
        for(int i=0;i<copies;i++) {
            final Deque<A> buffer = new LinkedList<A>();
            localBuffers.add(buffer);
            result.add(new CopyingIterator(localBuffers,
                    iterator,  buffer));
        }


        return Seq.fromIterable(result);
    }
    public static final <A> Seq<Iterator<A>> toBufferingCopier(final Iterator<A> iterator, final int copies, Supplier<Deque<A>> bufferSupplier) {
        final List<Iterator<A>> result = new ArrayList<>();

        ArrayList<Deque<A>> localBuffers = new ArrayList<>(copies);
        for(int i=0;i<copies;i++) {
            final Deque<A> buffer = bufferSupplier.get();
            localBuffers.add(buffer);
            result.add(new CopyingIterator(localBuffers,
                    iterator,  buffer));
        }


        return Seq.fromIterable(result);
    }

    @AllArgsConstructor
    static class DuplicatingIterator<T> implements Iterator<T> {

        Deque<T> bufferTo;
        Deque<T> bufferFrom;
        Iterator<T> it;
        long otherLimit = Long.MAX_VALUE;
        long counter = 0;

        @Override
        public boolean hasNext() {
            if (bufferFrom.size() > 0 || it.hasNext())
                return true;
            return false;
        }

        @Override
        public T next() {
            try {
                if (bufferFrom.size() > 0)
                    return bufferFrom.poll();
                else {
                    final T next = it.next();
                    if (counter < otherLimit)
                        bufferTo.add(next);
                    return next;
                }
            } finally {
                counter++;
            }
        }

    }

    static class CopyingIterator<T> implements Iterator<T> {

        ArrayList<Deque<T>> localBuffers;
        Deque<T> buffer;
        Iterator<T> it;



        @Override
        public boolean hasNext() {

            return buffer.size() > 0 || it.hasNext();

        }

        @Override
        public T next() {
            if (buffer.size() > 0)
                return buffer.poll();

            return handleLeader(); //exceed buffer, now leading

        }

        private void offer(T value){
            for(Deque<T> next : localBuffers ){
                if(next!=buffer)
                    next.add(value);
            }
        }
        private T handleLeader() {
            final T next = it.next();
            offer(next);
            return next;
        }

        public CopyingIterator(ArrayList<Deque<T>> localBuffers,final Iterator<T> it, final Deque<T> buffer) {

            this.it = it;
            this.buffer = buffer;
            this.localBuffers = localBuffers;
        }
    }

    /**
      * Projects an immutable toX of this stream. Initial iteration over the toX is not thread safe
      * (can't be performed by multiple threads concurrently) subsequent iterations are.
      *
      * @return An immutable toX of this stream.
      */
    public static final <A> Collection<A> toLazyCollection(final Stream<A> stream) {
        return SeqUtils.toLazyCollection(stream.iterator());
    }

    public static final <A> Collection<A> toLazyCollection(final Iterator<A> iterator) {
        return SeqUtils.toLazyCollection(iterator);
    }

    /**
     * Lazily constructs a Collection from specified Stream. Collections iterator may be safely used
     * concurrently by multiple threads.
    * @param stream
    * @return
    */
    public static final <A> Collection<A> toConcurrentLazyCollection(final Stream<A> stream) {
        return SeqUtils.toConcurrentLazyCollection(stream.iterator());
    }

    public static final <A> Collection<A> toConcurrentLazyCollection(final Iterator<A> iterator) {
        return SeqUtils.toConcurrentLazyCollection(iterator);
    }

    public final static <T> Stream<Streamable<T>> windowByTime(final Stream<T> stream, final long time, final TimeUnit t) {
        final Iterator<T> it = stream.iterator();
        final long toRun = t.toNanos(time);
        return Streams.stream(new Iterator<Streamable<T>>() {
            long start = System.nanoTime();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Streamable<T> next() {

                final List<T> list = new ArrayList<>();

                while (System.nanoTime() - start < toRun && it.hasNext()) {
                    list.add(it.next());
                }
                if (list.size() == 0 && it.hasNext()) //time unit may be too small
                    list.add(it.next());
                start = System.nanoTime();

                return Streamable.fromIterable(list);
            }

        });
    }

    public final static <T> Stream<Seq<T>> groupedByTime(final Stream<T> stream, final long time, final TimeUnit t) {
        return StreamSupport.stream(new GroupedByTimeSpliterator(stream.spliterator(),()->Seq.fromIterable(new ArrayList<>()),
                Function.identity(),time,t),stream.isParallel());
    }
    @Deprecated
    public final static <T> Stream<Seq<T>> batchByTime(final Stream<T> stream, final long time, final TimeUnit t) {
        return groupedByTime(stream,time,t);
    }
    public final static <T, C extends Collection<? super T>> Stream<C> groupedByTime(final Stream<T> stream, final long time, final TimeUnit t,
            final Supplier<C> factory) {
        return StreamSupport.stream(new GroupedByTimeSpliterator(stream.spliterator(),factory,
                Function.identity(),time,t),stream.isParallel());

    }
    @Deprecated
    public final static <T, C extends Collection<? super T>> Stream<C> batchByTime(final Stream<T> stream, final long time, final TimeUnit t,
            final Supplier<C> factory) {
        return groupedByTime(stream,time,t,factory);
    }

    private static final Object UNSET = new Object();

    /**
     * Group data in a Stream using knowledge of the current batch and the next entry to determing grouping limits
     *
     * @see Traversable#groupedUntil(BiPredicate)
     *
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @return Stream grouped into Lists determined by predicate
     */
    public final static <T> Stream<Seq<T>> groupedStatefullyUntil(final Stream<T> stream,
            final BiPredicate<Seq<? super T>, ? super T> predicate) {
        return StreamSupport.stream(new GroupedStatefullySpliterator<>(stream.spliterator(),()->Seq.of(),Function.identity(),predicate.negate()),stream.isParallel());
    }

    /**
     * Group a Stream while the supplied predicate holds
     *
     * @see ReactiveSeq#groupedWhile(Predicate)
     *
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @return Stream grouped into Lists determined by predicate
     */
    public final static <T> Stream<Seq<T>> groupedWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return StreamSupport.stream(new GroupedWhileSpliterator<>(stream.spliterator(),()->Seq.of(),Function.identity(),predicate.negate()),stream.isParallel());

    }
    @Deprecated
    public final static <T> Stream<Seq<T>> batchWhile(final Stream<T> stream, final Predicate<? super T> predicate) {
        return groupedWhile(stream,predicate);
    }
    /**
     * Group a Stream while the supplied predicate holds
     *
     * @see ReactiveSeq#groupedWhile(Predicate, Supplier)
     *
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @param factory Supplier to create toX for groupings
     * @return Stream grouped into Collections determined by predicate
     */
    public final static <T, C extends PersistentCollection<? super T>> Stream<C> groupedWhile(final Stream<T> stream, final Predicate<? super T> predicate,
                                                                                                         final Supplier<C> factory) {
        return StreamSupport.stream(new GroupedWhileSpliterator<>(stream.spliterator(),factory,Function.identity(),predicate.negate()),stream.isParallel());

    }
    @Deprecated
    public final static <T, C extends PersistentCollection<? super T>> Stream<C> batchWhile(final Stream<T> stream, final Predicate<? super T> predicate,
            final Supplier<C> factory) {
        return groupedWhile(stream,predicate,factory);
    }
    /**
     * Group a Stream until the supplied predicate holds
     *
     * @see ReactiveSeq#groupedUntil(Predicate)
     *
     * @param stream Stream to group
     * @param predicate Predicate to determine grouping
     * @return Stream grouped into Lists determined by predicate
     */
    public final static <T> Stream<Seq<T>> groupedUntil(final Stream<T> stream, final Predicate<? super T> predicate) {
        return groupedWhile(stream, predicate.negate());
    }
    @Deprecated
    public final static <T> Stream<Seq<T>> batchUntil(final Stream<T> stream, final Predicate<? super T> predicate) {
        return groupedUntil(stream, predicate);
    }
    /**
     * Group a Stream by size and time constraints
     *
     * @see ReactiveSeq#groupedBySizeAndTime(int, long, TimeUnit)
     *
     * @param stream Stream to group
     * @param size Max group size
     * @param time Max group time
     * @param t Time unit for max group time
     * @return Stream grouped by time and size
     */
    public final static <T> Stream<Seq<T>> groupedBySizeAndTime(final Stream<T> stream, final int size, final long time, final TimeUnit t) {
        return StreamSupport.stream(new GroupedByTimeAndSizeSpliterator(stream.spliterator(),()->Seq.fromIterable(new ArrayList<>(size)),
                Function.identity(),size,time,t),stream.isParallel());
    }


    /**
     * Group a Stream by size and time constraints
     *
     * @see ReactiveSeq#groupedBySizeAndTime(int, long, TimeUnit, Supplier)
     *
     * @param stream Stream to group
     * @param size Max group size
     * @param time Max group time
     * @param t Time unit for max group time
     * @param factory Supplier to create toX for groupings
     * @return Stream grouped by time and size
     */
    public final static <T, C extends Collection<? super T>> Stream<C> groupedBySizeAndTime(final Stream<T> stream, final int size, final long time,
            final TimeUnit t, final Supplier<C> factory) {
        return StreamSupport.stream(new GroupedByTimeAndSizeSpliterator(stream.spliterator(),factory,
                Function.identity(),size,time,t),stream.isParallel());

    }


    /**
     * Allow one element through per time period, drop all other elements in
     * that time period
     *
     * @see ReactiveSeq#debounce(long, TimeUnit)
     *
     * @param stream Stream to debounce
     * @param time Time to applyHKT debouncing over
     * @param t Time unit for debounce period
     * @return Stream with debouncing applied
     */
    public final static <T> Stream<T> debounce(final Stream<T> stream, final long time, final TimeUnit t) {
        return new DebounceOperator<>(
                                      stream).debounce(time, t);
    }

    /**
     *  emit one element per time period
     *
     * @see ReactiveSeq#onePer(long, TimeUnit)
     *
     * @param stream Stream to emit one element per time period from
     * @param time  Time period
     * @param t Time Pure
     * @return Stream with slowed emission
     */
    public final static <T> Stream<T> onePer(final Stream<T> stream, final long time, final TimeUnit t) {
        return new OnePerOperator<>(
                                    stream).onePer(time, t);
    }

    /**
     *  Introduce a random jitter / time delay between the emission of elements
     *
     *  @see ReactiveSeq#jitter(long)
     *
     * @param stream  Stream to introduce jitter to
     * @param jitterInNanos Max jitter period - random number less than this is used for each jitter
     * @return Jittered Stream
     */
    public final static <T> Stream<T> jitter(final Stream<T> stream, final long jitterInNanos) {
        final Iterator<T> it = stream.iterator();
        final Random r = new Random();
        return Streams.stream(new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T nextValue = it.next();
                try {
                    final long elapsedNanos = (long) (jitterInNanos * r.nextDouble());
                    final long millis = elapsedNanos / 1000000;
                    final int nanos = (int) (elapsedNanos - millis * 1000000);
                    Thread.sleep(Math.max(0, millis), Math.max(0, nanos));

                } catch (final InterruptedException e) {
                    throw ExceptionSoftener.throwSoftenedException(e);
                }
                return nextValue;
            }

        });
    }

    public final static <T> Stream<T> fixedDelay(final Stream<T> stream, final long time, final TimeUnit unit) {
        final Iterator<T> it = stream.iterator();

        return Streams.stream(new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T nextValue = it.next();
                try {
                    final long elapsedNanos = unit.toNanos(time);
                    final long millis = elapsedNanos / 1000000;
                    final int nanos = (int) (elapsedNanos - millis * 1000000);
                    Thread.sleep(Math.max(0, millis), Math.max(0, nanos));

                } catch (final InterruptedException e) {
                    throw ExceptionSoftener.throwSoftenedException(e);

                }
                return nextValue;
            }

        });
    }

    public final static <T> Stream<T> xPer(final Stream<T> stream, final int x, final long time, final TimeUnit t) {
        final Iterator<T> it = stream.iterator();
        final long next = t.toNanos(time);
        return Streams.stream(new Iterator<T>() {
            volatile long last = -1;
            volatile int count = 0;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T nextValue = it.next();
                if (++count < x)
                    return nextValue;
                count = 0;
                LockSupport.parkNanos(next - (System.nanoTime() - last));

                last = System.nanoTime();
                return nextValue;
            }

        });
    }

    public final static <T> Connectable<T> hotStream(final Stream<T> stream, final Executor exec) {
        return new NonPausableConnectable<>(
                                          stream).init(exec);
    }

    public final static <T> Connectable<T> primedHotStream(final Stream<T> stream, final Executor exec) {
        return new NonPausableConnectable<>(
                                          stream).paused(exec);
    }

    public final static <T> PausableConnectable<T> pausableHotStream(final Stream<T> stream, final Executor exec) {
        return new PausableConnectableImpl<>(
                                           stream).init(exec);
    }

    public final static <T> PausableConnectable<T> primedPausableHotStream(final Stream<T> stream, final Executor exec) {
        return new PausableConnectableImpl<>(
                                           stream).paused(exec);
    }



    public static  <T,R> Stream<R> tailRec(T initial, Function<? super T, ? extends Stream<? extends Either<T, R>>> fn) {
        return ReactiveSeq.tailRec(initial,fn.andThen(ReactiveSeq::fromStream)).stream();
    }
}
