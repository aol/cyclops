package com.aol.cyclops2.types.anyM;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.*;
import java.util.stream.Stream;

import com.aol.cyclops2.types.foldable.ConvertableSequence;
import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.Streamable;
import cyclops.monads.transformers.ListT;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.MapX;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.types.stream.ToStream;
import org.reactivestreams.Subscription;

public interface NestedFoldable<W extends WitnessType<W>,T> extends ToStream<T> {
    public AnyM<W,? extends IterableX<T>> nestedFoldables();




    default <X extends Throwable> AnyM<W,? extends Subscription> forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        return nestedFoldables().map(n->n.forEach(numberOfElements,consumer,consumerError));
    }


    default <X extends Throwable> AnyM<W,? extends Subscription> forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        return nestedFoldables().map(n->n.forEach(numberOfElements,consumer,consumerError,onComplete));
    }



    /**
     * Attempt to transform this Sequence to the same type as the supplied Monoid
     * (Reducer) Then use Monoid to reduce values
     *
     * <pre>
     * {@code
     * ReactiveSeq.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
     *
     * //4
     * }
     * </pre>
     *
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> AnyM<W,R> mapReduce(final Reducer<R,T> reducer) {
        return nestedFoldables().map(s -> s.mapReduce(reducer));
    }

    /**
     * Attempt to transform this Monad to the same type as the supplied Monoid, using
     * supplied function Then use Monoid to reduce values
     *
     * <pre>
     *  {@code
     *  ReactiveSeq.of("one","two","three","four")
     *           .mapReduce(this::toInt,Reducers.toTotalInt());
     *
     *  //10
     *
     *  int toInt(String s){
     *      if("one".equals(s))
     *          return 1;
     *      if("two".equals(s))
     *          return 2;
     *      if("three".equals(s))
     *          return 3;
     *      if("four".equals(s))
     *          return 4;
     *      return -1;
     *     }
     *  }
     * </pre>
     *
     * @param mapper
     *            Function to transform Monad type
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> AnyM<W,R> mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return nestedFoldables().map(s -> s.mapReduce(mapper, reducer));
    }

    /**
     * <pre>
     * {@code
     * ReactiveSeq.of("hello","2","world","4").reduce(Reducers.toString(","));
     *
     * //hello,2,world,4
     * }
     * </pre>
     *
     * @param reducer
     *            Use supplied Monoid to reduce values
     * @return reduced values
     */
    default AnyM<W,T> reduce(final Monoid<T> reducer) {
        return nestedFoldables().map(s -> s.reduce(reducer));
    }

    /*
     * <pre> {@code assertThat(ReactiveSeq.of(1,2,3,4,5).map(it -> it*100).reduce(
     * (acc,next) -> acc+next).getValue(),equalTo(1500)); } </pre>
     */
    default AnyM<W,Optional<T>> reduce(final BinaryOperator<T> accumulator) {
        return nestedFoldables().map(s -> s.reduce(accumulator));


    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    default AnyM<W,T> reduce(final T identity, final BinaryOperator<T> accumulator) {
        return nestedFoldables().map(s -> s.reduce(identity, accumulator));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    default <U> AnyM<W,U> reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return nestedFoldables().map(s -> s.reduce(identity, accumulator, combiner));
    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     *
     * <pre>
     * {
     *  {@code
     *  Monoid<Integer> sum = Monoid.of(0, (a, b) -> a + b);
     *  Monoid<Integer> mult = Monoid.of(1, (a, b) -> a * b);
     *  List<Integer> result = ReactiveSeq.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).transformerStream());
     *
     *  assertThat(result, equalTo(Arrays.asList(10, 24)));
     *
     * }
     * </pre>
     *
     *
     * @param reducers
     * @return
     */
    default ListT<W,T> reduce(final Stream<? extends Monoid<T>> reducers) {
        final AnyM<W,ListX<T>> anyM = nestedFoldables().map(s -> s.reduce(reducers));
        return ListT.of(anyM);

    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     *
     * <pre>
     * {@code
     *      Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
     *      Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
     *      List<Integer> result = ReactiveSeq.of(1,2,3,4))
     *                                      .reduce(Arrays.asList(sum,mult) );
     *
     *
     *      assertThat(result,equalTo(Arrays.asList(10,24)));
     *
     * }
     * </pre>
     *
     * @param reducers
     * @return
     */
    default ListT<W,T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return ListT.of(nestedFoldables().map(s -> s.reduce(reducers)));
    }
    default AnyM<W,T> foldLeft(final Monoid<T> reducer) {
        return nestedFoldables().map(s -> s.foldLeft(reducer));
    }
    default AnyM<W,T> foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return nestedFoldables().map(s -> s.foldLeft(identity, accumulator));
    }
    default <U> AnyM<W,U> foldLeft(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        return nestedFoldables().map(s -> s.foldLeft(identity, accumulator));
    }
    default <R> AnyM<W,R> foldLeftMapToType(final Reducer<R,T> reducer) {
        return nestedFoldables().map(s -> s.mapReduce(reducer));
    }
    /**
     *
     * <pre>
     *      {@code
     *      ReactiveSeq.of("a","b","c").foldRight(Reducers.toString(""));
     *
     *         // "cab"
     *         }
     * </pre>
     *
     * @param reducer
     *            Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    default AnyM<W,T> foldRight(final Monoid<T> reducer) {
        return nestedFoldables().map(s -> s.foldRight(reducer));
    }

    /**
     * Immutable reduction from right to left
     *
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of("a","b","c").foldRight("", String::concat).equals("cba"));
     * }
     * </pre>
     *
     * @param identity value that results in the input parameter to the accumulator function being returned.
     *          E.g. for multiplication 1 is the identity value, for addition 0 is the identity value
     * @param accumulator function that combines the accumulated value and the next one
     * @return AnyM containing the results of the nest fold right
     */
    default AnyM<W,T> foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return nestedFoldables().map(s -> s.foldRight(identity, accumulator));
    }

    /**
     * Immutable reduction from right to left
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of("a","b","c").foldRight("", (a,b)->a+b).equals("cba"));
     * }
     * </pre> *
     * @param identity value that results in the input parameter to the accumulator function being returned.
     *          E.g. for multiplication 1 is the identity value, for addition 0 is the identity value
     * @param accumulator function that combines the accumulated value and the next one
     * @return AnyM containing the results of the nest fold right
     */
    default <U> AnyM<W,U> foldRight(final U identity, final BiFunction<? super T, U, U> accumulator) {
        return nestedFoldables().map(s -> s.foldRight(identity, accumulator));
    }

    /**
     * Attempt to transform this Monad to the same type as the supplied Monoid (using
     * mapToType on the monoid interface) Then use Monoid to reduce values
     *
     * <pre>
     *      {@code
     *      ReactiveSeq.of(1,2,3).foldRightMapToType(Reducers.toString(""));
     *
     *         // "321"
     *         }
     * </pre>
     *
     *
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> AnyM<W,R> foldRightMapToType(final Reducer<R,T> reducer) {
        return nestedFoldables().map(s -> s.foldRightMapToType(reducer));
    }

    /**
     * <pre>
     * {@code
     *  assertEquals("123".length(),ReactiveSeq.of(1, 2, 3).join().length());
     * }
     * </pre>
     *
     * @return Stream as concatenated String
     */
    default AnyM<W,String> join() {

        return nestedFoldables().map(s -> s.join());
    }

    /**
     * <pre>
     * {@code
     * assertEquals("1, 2, 3".length(), ReactiveSeq.of(1, 2, 3).join(", ").length());
     * }
     * </pre>
     *
     * @return Stream as concatenated String
     */
    default AnyM<W,String> join(final String sep) {
        return nestedFoldables().map(s -> s.join(sep));
    }

    /**
     * <pre>
     * {@code
     * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
     * }
     * </pre>
     *
     * @return Stream as concatenated String
     */
    default AnyM<W,String> join(final String sep, final String start, final String end) {
        return nestedFoldables().map(s -> s.join(sep, start, end));
    }


    default void print(final PrintStream str) {
        nestedFoldables().peek(s -> s.print(str))
                         .forEach(c -> {
                         });
    }

    default void print(final PrintWriter writer) {
        nestedFoldables().peek(s -> s.print(writer))
                         .forEach(c -> {
                         });
    }

    default void printOut() {
        nestedFoldables().peek(s -> s.printOut())
                         .forEach(c -> {
                         });
    }

    default void printErr() {
        nestedFoldables().peek(s -> s.printErr())
                         .forEach(c -> {
                         });
    }

    /**
     * Use classifier function to group elements in this Sequence into a Map
     *
     * <pre>
     * {@code
     *
     *  Map<Integer, List<Integer>> map1 = of(1, 2, 3, 4).groupBy(i -> i % 2);
     *  assertEquals(asList(2, 4), map1.getValue(0));
     *  assertEquals(asList(1, 3), map1.getValue(1));
     *  assertEquals(2, map1.size());
     *
     * }
     *
     * </pre>
     */
    default <K> AnyM<W,MapX<K, ListX<T>>> groupBy(final Function<? super T, ? extends K> classifier) {
        return nestedFoldables().map(s -> s.groupBy(classifier));
    }


    /**
     * @return First matching element in sequential order
     *
     *         <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findFirst().getValue();
     *
     * //3
     * }
     * </pre>
     *
     *         (deterministic)
     *
     */
    default AnyM<W,Optional<T>> findFirst() {
        return nestedFoldables().map(s -> s.findFirst());

    }

    /**
     *
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Arrays.asList(1,2,3)));
     * }
     * </pre>
     *
     * @param iterable
     * @return True if Monad starts with Iterable sequence of data
     */
    default AnyM<W,Boolean> startsWithIterable(final Iterable<T> iterable) {
        return nestedFoldables().map(s -> s.startsWithIterable(iterable));
    }

    /**
     * <pre>
     * {@code assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Arrays.asList(1,2,3).iterator())) }
     * </pre>
     *
     * @param iterator
     * @return True if Monad starts with Iterators sequence of data
     */
    default AnyM<W,Boolean> startsWith(final Stream<T> stream) {
        final Streamable<T> streamable = Streamable.fromStream(stream);
        return nestedFoldables().map(s -> s.startsWith(streamable.stream()));
    }

    /**
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     *              .endsWith(Arrays.asList(5,6)));
     *
     * }
     *
     * @param iterable Values to check
     * @return true if SequenceM ends with values in the supplied iterable
     */
    default AnyM<W,Boolean> endsWithIterable(final Iterable<T> iterable) {
        return nestedFoldables().map(s -> s.endsWithIterable(iterable));
    }

    /**
     * <pre>
     * {@code
     * assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     *              .endsWith(Stream.of(5,6)));
     * }
     * </pre>
     *
     * @param stream
     *            Values to check
     * @return true if SequenceM endswith values in the supplied Stream
     */
    default AnyM<W,Boolean> endsWith(final Stream<T> stream) {
        return nestedFoldables().map(s -> s.endsWith(stream));
    }
    default <R> AnyM<W,R> toNested(Function<? super ConvertableSequence<T>, ? extends R> fn) {
        return nestedFoldables().map(s -> fn.apply(s.to()));
    }

    /**
     * <pre>
     * {@code
     *  assertThat(ReactiveSeq.of(1,2,3,4)
     *                  .map(u->{throw new RuntimeException();})
     *                  .recover(e->"hello")
     *                  .firstValue(),equalTo("hello"));
     * }
     * </pre>
     *
     * @return first value in this Stream
     */
    default AnyM<W,T> firstValue() {
        return nestedFoldables().map(s -> s.firstValue(null));
    }

    /**
     * <pre>
     * {@code
     *
     *    //1
     *    ReactiveSeq.of(1).single();
     *
     *    //UnsupportedOperationException
     *    ReactiveSeq.of().single();
     *
     *     //UnsupportedOperationException
     *    ReactiveSeq.of(1,2,3).single();
     * }
     * </pre>
     *
     * @return a single value or an UnsupportedOperationException if 0/1 values
     *         in this Stream
     */
    default AnyM<W,T> singleOrElse() {
        return nestedFoldables().map(s -> s.singleOrElse(null));

    }

    default AnyM<W,Maybe<T>> single(final Predicate<? super T> predicate) {
        return nestedFoldables().map(s -> s.single(predicate));
    }

    /**
     * <pre>
     * {@code
     *
     *    //Optional[1]
     *    ReactiveSeq.of(1).single();
     *
     *    //Optional.zero
     *    ReactiveSeq.of().singleOpional();
     *
     *     //Optional.zero
     *    ReactiveSeq.of(1,2,3).single();
     * }
     * </pre>
     *
     * @return An Optional with single value if this Stream has exactly one
     *         element, otherwise Optional Empty
     */
    default AnyM<W,Maybe<T>> single() {
        return nestedFoldables().map(s -> s.single());
    }

    /**
     * Return the elementAt index or Optional.zero
     *
     * <pre>
     * {@code
     *  assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).getValue(),equalTo(3));
     * }
     * </pre>
     *
     * @param index
     *            to extract element from
     * @return elementAt index
     */
    default AnyM<W,Maybe<T>> get(final long index) {
        return nestedFoldables().map(s -> s.elementAt(index));

    }

    /**
     * Execute this Stream on a schedule
     *
     * <pre>
     * {@code
     *  //run at 8PM every night
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *             .map(this::processJob)
     *             .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     *
     * Connect to the Scheduled Stream
     *
     * <pre>
     * {@code
     *
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
     *                                          .schedule("0 20 * * *", Executors.newScheduledThreadPool(1));
     *
     *  data.connect()
     *      .forEach(this::logToDB);
     * }
     * </pre>
     *
     *
     *
     * @param cron
     *            Expression that determines when each job will run
     * @param ex
     *            ScheduledExecutorService
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    default HotStream<T> schedule(final String cron, final ScheduledExecutorService ex) {
        return stream().schedule(cron, ex);
    }

    /**
     * Execute this Stream on a schedule
     *
     * <pre>
     * {@code
     *  //run every 60 seconds after last job completes
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *             .map(this::processJob)
     *             .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     *
     * Connect to the Scheduled Stream
     *
     * <pre>
     * {@code
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
     *                                          .scheduleFixedDelay(60_000, Executors.newScheduledThreadPool(1));
     *
     *  data.connect().forEach(this::logToDB);
     * }
     * </pre>
     *
     *
     * @param delay
     *            Between last element completes passing through the Stream
     *            until the next one starts
     * @param ex
     *            ScheduledExecutorService
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    default HotStream<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return stream().scheduleFixedDelay(delay, ex);
    }

    /**
     * Execute this Stream on a schedule
     *
     * <pre>
     * {@code
     *  //run every 60 seconds
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *            .map(this::processJob)
     *            .scheduleFixedRate(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     *
     * Connect to the Scheduled Stream
     *
     * <pre>
     * {@code
     *
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
     *                                          .scheduleFixedRate(60_000, Executors.newScheduledThreadPool(1));
     *
     *  data.connect()
     *      .forEach(this::logToDB);
     * }
     * </pre>
     *
     * @param rate
     *            Time in millis between job runs
     * @param ex
     *            ScheduledExecutorService
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    default HotStream<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return stream().scheduleFixedRate(rate, ex);
    }

}
