package com.aol.cyclops2.types.foldable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.MapX;
import com.aol.cyclops2.types.stream.HotStream;

/**
 * Represents a type that may be reducable (foldable) to a singleUnsafe value or toX
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) in this Folds
 */
public interface Folds<T> {


    ReactiveSeq<T> stream();

    default Optional<T> max(Comparator<? super T> comparator){
        return stream().sorted(comparator)
                       .get(0l)
                       .toOptional();
    }
    default Optional<T> min(Comparator<? super T> comparator){
        return stream().sorted(comparator.reversed())
                .get(0l)
                .toOptional();
    }
    default int sumInt(ToIntFunction<T> fn){
        return stream().mapToInt(fn).sum();
    }
    default double sumDouble(ToDoubleFunction<T> fn){
        return stream().mapToDouble(fn).sum();
    }
    default double sumLong(ToLongFunction<T> fn){
        return stream().mapToLong(fn).sum();
    }

    /**
     * True if predicate matches all elements when Monad converted to a Stream
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     *
     * @param c
     *            Predicate to check if all match
     */

    default boolean allMatch(final Predicate<? super T> c) {
        return !stream().filterNot(c)
                        .findFirst()
                        .isPresent();
    }

    /**
     * True if a singleUnsafe element matches when Monad converted to a Stream
     *
     * <pre>
     * {@code
     *     ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3))
     *     //true
     * }
     * </pre>
     *
     * @param c
     *            Predicate to check if any match
     */

    default boolean anyMatch(final Predicate<? super T> c) {
        return stream().filter(c).findFirst().isPresent();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#noneMatch(java.util.function.Predicate)
     */

    default boolean noneMatch(final Predicate<? super T> c) {
        return !stream().filter(c)
                        .findFirst()
                         .isPresent();
    }

    default <R, A> R collect(final Collector<? super T, A, R> collector) {

        return stream().collect(collector);
    }
    default long count() {
        return stream().count();
    }

    /**
     * Attempt toNePsted transform this Sequence to the same type as the supplied Monoid
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
    default <R> R mapReduce(final Reducer<R> reducer) {
        return stream().mapReduce(reducer);
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
     * 		if("one".equals(s))
     * 			return 1;
     * 		if("two".equals(s))
     * 			return 2;
     * 		if("three".equals(s))
     * 			return 3;
     * 		if("four".equals(s))
     * 			return 4;
     * 		return -1;
     * 	   }
     *  }
     * </pre>
     * 
     * @param mapper
     *            Function to transform Monad type
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return stream().mapReduce(mapper, reducer);
    }

    /**
     * Reduce this Folds to a singleUnsafe value, using the supplied Monoid. For example
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
    default T reduce(final Monoid<T> reducer) {
        return reduce(reducer.zero(),reducer);
    }

    /**
     * An equivalent function to {@link java.util.stream.Stream#reduce(BinaryOperator)}
     *  
     *  <pre> {@code
     *  
     *       ReactiveSeq.of(1,2,3,4,5).transform(it -> it*100).reduce(
     * (acc,next) -> acc+next)
     *        //Optional[1500]
     *  }
     *  </pre>
     * @param accumulator Combiner function
     * @return
     */
    default Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return stream().reduce(accumulator);
    }

    /**
     *  An equivalent function to {@link java.util.stream.Stream#reduce(Object, BinaryOperator)}
     * @param accumulator Combiner function
     * @return Value emitted by applying the current accumulated value and the
     *          next value to the combiner function as this Folds is traversed from left to right
     */
    default T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return stream().reduce(identity, accumulator);
    }

    /**
     * An equivalent function to {@link java.util.stream.Stream#reduce(Object, BinaryOperator)}
     * 
     * @param identity Identity value for the combiner function (leaves the input unchanged)
     * @param accumulator Combiner function
     * @return Value emitted by applying the current accumulated value and the
     *          next value to the combiner function as this Folds is traversed from left to right
     */
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        final Folds<T> foldable = stream();
        return foldable.reduce(identity, accumulator);
    }
    default <U> U foldLeft(final U identity, final BiFunction<U, ? super T, U> accumulator) {
       return reduce(identity,accumulator);
    }
    default <U> U foldLeft(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return reduce(identity,accumulator,combiner);
    }
    default T foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return reduce(identity, accumulator);
    }
    default T foldLeft(final Monoid<T> reducer) {
        return reduce(reducer);
    }
    /**
     * An equivalent function to {@link java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)}
     * 
     */
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return stream().reduce(identity, accumulator, combiner);
    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {@code 
     * 	Monoid<Integer> sum = Monoid.of(0, (a, b) -&gt; a + b);
     * 	Monoid<Integer> mult = Monoid.of(1, (a, b) -&gt; a * b);
     * 	List<Integer> result = ReactiveSeq.of(1, 2, 3, 4)
     *                                    .reduce(Arrays.asList(sum, mult)
     *                                    .reactiveStream());
     * 
     * 	assertThat(result, equalTo(Arrays.asList(10, 24)));
     * 
     * }
     * </pre>
     * 
     * 
     * @param reducers
     * @return List of reduced values
     */
    default ListX<T> reduce(final Stream<? extends Monoid<T>> reducers) {
        return stream().reduce(reducers);
    }

    /**
     * Reduce with multiple reducers in parallel NB if this Monad is an Optional
     * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
     * was one value To reduce over the values on the list, called
     * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {@code 
     * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
     * 		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
     * 		List<Integer> result = ReactiveSeq.of(1,2,3,4))
     * 										.reduce(Arrays.asList(sum,mult) );
     * 				
     * 		 
     * 		assertThat(result,equalTo(Arrays.asList(10,24)));
     * 
     * }
     * </pre>
     * 
     * @param reducers
     * @return
     */
    default ListX<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return stream().reduce(reducers);
    }

    /**
     * 
     * <pre>
     * 		{@code
     * 		ReactiveSeq.of("a","b","c").foldRight(Reducers.toString(""));
     *        
     *         // "cab"
     *         }
     * </pre>
     * 
     * @param reducer
     *            Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    default T foldRight(final Monoid<T> reducer) {
        return stream().foldRight(reducer);
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
     * @param identity  Identity value for the combiner function (leaves the input unchanged)
     * @param accumulator Combining function
     * @return Reduced value
     */
    default T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return stream().foldRight(identity, accumulator);
    }

    /**
     * 
     * Immutable reduction from right to left
     * 
     * @param identity  Identity value for the combiner function (leaves the input unchanged)
     * @param accumulator Combining function
     * @return Reduced value
     */
    default <U> U foldRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> accumulator) {
        return stream().foldRight(identity, accumulator);
    }

    /**
     * Attempt to transform this Monad to the same type as the supplied Monoid (using
     * mapToType on the monoid interface) Then use Monoid to reduce values
     * 
     * <pre>
     * 		{@code
     * 		ReactiveSeq.of(1,2,3).foldRightMapToType(Reducers.toString(""));
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
    default <T> T foldRightMapToType(final Reducer<T> reducer) {
        return stream().foldRightMapToType(reducer);
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
    default String join() {

        return stream().join();
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
    default String join(final String sep) {
        return stream().join(sep);
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
    default String join(final String sep, final String start, final String end) {
        return stream().join(sep, start, end);
    }

    /**
     * Write each element within this Folds in turn to the supplied PrintStream
     *
     * @param str PrintStream to tell to
     */
    default void print(final PrintStream str) {
        stream().print(str);
    }

    /**
     * Write each element within this Folds in turn to the supplied PrintWriter
     *
     * @param writer PrintWriter to tell to
     */
    default void print(final PrintWriter writer) {
        stream().print(writer);
    }

    /**
     *  Print each value in this Folds to the console in turn (left-to-right)
     */
    default void printOut() {
        stream().printOut();
    }

    /**
     *  Print each value in this Folds to the error console in turn (left-to-right)
     */
    default void printErr() {
        stream().printErr();
    }

    /**
     * Use classifier function to group elements in this Sequence into a Map
     * 
     * <pre>
     * {@code
     * 
     *  Map<Integer, List<Integer>> map1 = of(1, 2, 3, 4).groupBy(i -&gt; i % 2);
     *  assertEquals(asList(2, 4), map1.get(0));
     *  assertEquals(asList(1, 3), map1.get(1));
     *  assertEquals(2, map1.size());
     * 
     * }
     * 
     * </pre>
     */
    default <K> MapX<K, ListX<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return stream().groupBy(classifier);
    }

    /**
     * @return First matching element in sequential order
     * 
     * <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
     * 
     * //3
     * }
     * </pre>
     * 
     *         (deterministic)
     * 
     */
    default Optional<T> findFirst() {
        return stream().findFirst();
    }

    /**
     * @return first matching element, but order is not guaranteed
     * 
     *         <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findAny().get();
     * 
     * //3
     * }
     * </pre>
     * 
     * 
     *         (non-deterministic)
     */
    default Optional<T> findAny() {
        return stream().findAny();
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
    default boolean startsWithIterable(final Iterable<T> iterable) {
        return stream().startsWithIterable(iterable);
    }

    /**
     * <pre>
     * {@code assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Stream.of(1,2,3))) }
     * </pre>
     * 
     * @param stream Stream to check if this Folds has the same elements in the same order, at the skip
     * @return True if Monad starts with Iterators sequence of data
     */
    default boolean startsWith(final Stream<T> stream) {
        return stream().startsWith(stream);
    }

    /**
     * <pre>
     * {@code
     *  assertTrue(ReactiveSeq.of(1,2,3,4,5,6)
     *              .endsWith(Arrays.asList(5,6)));
     * 
     * }
     * </pre>
     * 
     * @param iterable Values to check
     * @return true if SequenceM ends with values in the supplied iterable
     */
    default boolean endsWithIterable(final Iterable<T> iterable) {
        return stream().endsWithIterable(iterable);
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
    default boolean endsWith(final Stream<T> stream) {
        return stream().endsWith(stream);
    }




    /**
     * <pre>
     * {@code 
     *  assertThat(ReactiveSeq.of(1,2,3,4)
     *                  .transform(u->throw new RuntimeException())
     *                  .recover(e->"hello")
     *                  .firstValue(),equalTo("hello"));
     * }
     * </pre>
     * 
     * @return first value in this Stream
     */
    default T firstValue() {
        return stream().firstValue();
    }

    /**
     * <pre>
     * {@code 
     *    
     *    //1
     *    ReactiveSeq.of(1).singleUnsafe();
     *    
     *    //UnsupportedOperationException
     *    ReactiveSeq.of().singleUnsafe();
     *     
     *     //UnsupportedOperationException
     *    ReactiveSeq.of(1,2,3).singleUnsafe();
     * }
     * </pre>
     * 
     * @return a singleUnsafe value or an UnsupportedOperationException if 0/1 values
     *         in this Stream
     */
    default T singleUnsafe() {
        return stream().singleUnsafe();

    }

    default Maybe<T> single(final Predicate<? super T> predicate) {
        return stream().single(predicate);
    }

    /**
     * <pre>
     * {@code 
     *    
     *    //Optional[1]
     *    ReactiveSeq.of(1).singleUnsafe();
     *    
     *    //Optional.zero
     *    ReactiveSeq.of().singleOpional();
     *     
     *     //Optional.zero
     *    ReactiveSeq.of(1,2,3).singleUnsafe();
     * }
     * </pre>
     * 
     * @return An Optional with singleUnsafe value if this Stream has exactly one
     *         element, otherwise Optional Empty
     */
    default Maybe<T> single() {
        return stream().single();

    }
    default Maybe<T> takeOne(){
        return stream().takeOne();
    }
    /**
     * Return the elementAt index or Optional.zero
     * 
     * <pre>
     * {@code
     *  assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
     * }
     * </pre>
     * 
     * @param index
     *            to extract element from
     * @return elementAt index
     */
    default Maybe<T> get(final long index) {
        return stream().get(index);
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run at 8PM every night
     *  ReactiveSeq.generate(()->"next job:"+formatDate(new Date()))
     *             .transform(this::processJob)
     *             .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code
     *  
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).transform(this::processJob)
     *          .schedule("0 20 * * *", Executors.newScheduledThreadPool(1));
     * 
     *  data.connect().forEach(this::logToDB);
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
     *             .transform(this::processJob)
     *             .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code 
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -> "next job:" + formatDate(new Date())).transform(this::processJob)
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
     *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
     *            .transform(this::processJob)
     *            .scheduleFixedRate(60_000,Executors.newScheduledThreadPool(1));
     * }
     * </pre>
     * 
     * Connect to the Scheduled Stream
     * 
     * <pre>
     * {@code
     *  
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -&gt; "next job:" + formatDate(new Date())).transform(this::processJob)
     *                                          .scheduleFixedRate(60_000, Executors.newScheduledThreadPool(1));
     * 
     *  data.connect().forEach(this::logToDB);
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



    /**
     * Check that there are specified number of matches of predicate in the
     * Stream
     * 
     * <pre>
     * {@code 
     *  assertTrue(ReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
     * }
     * </pre>
     * 
     */
    default boolean xMatch(final int num, final Predicate<? super T> c) {
        return stream().xMatch(num, c);
    }
}
