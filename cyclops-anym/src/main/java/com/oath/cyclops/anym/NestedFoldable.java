package com.oath.cyclops.anym;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.*;

import com.oath.cyclops.types.foldable.ConvertableSequence;
import com.oath.cyclops.types.stream.Connectable;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import com.oath.cyclops.types.stream.ToStream;
import org.reactivestreams.Subscription;

@Deprecated //use cyclops-pure Do instead
public interface NestedFoldable<W extends WitnessType<W>,T> extends ToStream<T> {
    public AnyM<W,? extends IterableX<T>> nestedFoldables();




    default <X extends Throwable> AnyM<W,? extends Subscription> forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        return nestedFoldables().map(n->n.forEach(numberOfElements,consumer,consumerError));
    }


    default <X extends Throwable> AnyM<W,? extends Subscription> forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        return nestedFoldables().map(n->n.forEach(numberOfElements,consumer,consumerError,onComplete));
    }



    /**
     *
     * <pre>
     * {@code
     * ReactiveSeq.of("hello","2","world","4").foldMap(Reducers.toCountInt());
     *
     * //4
     * }
     * </pre>
     *
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> AnyM<W,R> foldMap(final Reducer<R,T> reducer) {
        return nestedFoldables().map(s -> s.foldMap(reducer));
    }

    /**
     *
     * <pre>
     *  {@code
     *  ReactiveSeq.of("one","two","three","four")
     *           .foldMap(this::toInt,Reducers.toTotalInt());
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
    default <R> AnyM<W,R> foldMap(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return nestedFoldables().map(s -> s.foldMap(mapper, reducer));
    }

    default AnyM<W,Seq<T>> foldLeft(final Iterable<? extends Monoid<T>> reducer) {
        return nestedFoldables().map(s -> s.foldLeft(reducer));
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
    default <U> AnyM<W,U> foldLeft(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return nestedFoldables().map(s -> s.foldLeft(identity, accumulator,combiner));
    }

    default AnyM<W,Option<T>> foldLeft(final BinaryOperator<T> accumulator) {
        return nestedFoldables().map(s -> s.foldLeft(accumulator));


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
    default <R> AnyM<W,R> foldMapRight(final Reducer<R,T> reducer) {
        return nestedFoldables().map(s -> s.foldMapRight(reducer));
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
    default <K> AnyM<W,cyclops.data.HashMap<K, Vector<T>>> groupBy(final Function<? super T, ? extends K> classifier) {
        return nestedFoldables().map(s -> s.groupBy(classifier));
    }



    default AnyM<W,Option<T>> headOption() {
        return nestedFoldables().map(s -> s.headOption());

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
    default AnyM<W,Boolean> startsWith(final Iterable<T> iterable) {
        return nestedFoldables().map(s -> s.startsWith(iterable));
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
    default AnyM<W,Boolean> endsWith(final Iterable<T> iterable) {
        return nestedFoldables().map(s -> s.endsWith(iterable));
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
    default AnyM<W,T> firstValue(T alt) {
        return nestedFoldables().map(s -> s.firstValue(alt));
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
    default AnyM<W,T> singleOrElse(T alt) {
        return nestedFoldables().map(s -> s.singleOrElse(alt));

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
     *  Connectable<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
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
     * @return Connectable Connectable of emitted from scheduled Stream
     */
    default Connectable<T> schedule(final String cron, final ScheduledExecutorService ex) {
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
     *  Connectable<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
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
     * @return Connectable Connectable of emitted from scheduled Stream
     */
    default Connectable<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
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
     *  Connectable<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).map(this::processJob)
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
     * @return Connectable Connectable of emitted from scheduled Stream
     */
    default Connectable<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return stream().scheduleFixedRate(rate, ex);
    }

}
