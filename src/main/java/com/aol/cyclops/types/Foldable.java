package com.aol.cyclops.types;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Validator;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.types.stream.HotStream;

/**
 * Represents a type that may be reducable (foldable) to a single value or collection
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) in this Foldable
 */
public interface Foldable<T> {

    /**
     * @return this Foldable converted to a Stream ({@link com.aol.cyclops.control.ReactiveSeq}) of it's elements
     */
    ReactiveSeq<T> stream();

    /**
     * @return This Foldable as a ReactiveSeq with it's type narrowed to a Foldable
     */
    default Foldable<T> foldable() {
        return stream();
    }

    /**
     * Attempt to map this Sequence to the same type as the supplied Monoid
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
        return foldable().mapReduce(reducer);
    }

    /**
     * Attempt to map this Monad to the same type as the supplied Monoid, using
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
     *            Function to map Monad type
     * @param reducer
     *            Monoid to reduce values
     * @return Reduce result
     */
    default <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return foldable().mapReduce(mapper, reducer);
    }

    /**
     * Reduce this Foldable to a single value, using the supplied Monoid. For example
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
        return foldable().reduce(reducer);
    }

    /**
     * An equivalent function to {@link java.util.stream.Stream#reduce(BinaryOperator)}
     *  
     *  <pre> {@code
     *  
     *       ReactiveSeq.of(1,2,3,4,5).map(it -> it*100).reduce(
     * (acc,next) -> acc+next)
     *        //Optional[1500]
     *  }
     *  </pre>
     * @param accumulator Combiner function
     * @return
     */
    default Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return foldable().reduce(accumulator);
    }

    /**
     *  An equivalent function to {@link java.util.stream.Stream#reduce(Object, BinaryOperator)}
     * @param accumulator Combiner function
     * @return Value produced by applying the current accumulated value and the 
     *          next value to the combiner function as this Foldable is traversed from left to right
     */
    default T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return foldable().reduce(identity, accumulator);
    }

    /**
     * An equivalent function to {@link java.util.stream.Stream#reduce(Object, BinaryOperator)}
     * 
     * @param identity Identity value for the combiner function (leaves the input unchanged)
     * @param accumulator Combiner function
     * @return Value produced by applying the current accumulated value and the 
     *          next value to the combiner function as this Foldable is traversed from left to right
     */
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        final Foldable<T> foldable = foldable();
        return foldable.reduce(identity, accumulator);
    }

    /**
     * An equivalent function to {@link java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)}
     * 
     */
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return foldable().reduce(identity, accumulator, combiner);
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
     *                                    .foldable());
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
        return foldable().reduce(reducers);
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
        return foldable().reduce(reducers);
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
        return foldable().foldRight(reducer);
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
        return foldable().foldRight(identity, accumulator);
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
        return foldable().foldRight(identity, accumulator);
    }

    /**
     * Attempt to map this Monad to the same type as the supplied Monoid (using
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
        return foldable().foldRightMapToType(reducer);
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

        return foldable().join();
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
        return foldable().join(sep);
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
        return foldable().join(sep, start, end);
    }

    /**
     * Write each element within this Foldable in turn to the supplied PrintStream
     * 
     * @param str PrintStream to write to
     */
    default void print(final PrintStream str) {
        foldable().print(str);
    }

    /**
     * Write each element within this Foldable in turn to the supplied PrintWriter
     * 
     * @param writer PrintWriter to write to
     */
    default void print(final PrintWriter writer) {
        foldable().print(writer);
    }

    /**
     *  Print each value in this Foldable to the console in turn (left-to-right)
     */
    default void printOut() {
        foldable().printOut();
    }

    /**
     *  Print each value in this Foldable to the error console in turn (left-to-right)
     */
    default void printErr() {
        foldable().printErr();
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
    default <K> MapX<K, List<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return foldable().groupBy(classifier);
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
        return foldable().findFirst();
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
        return foldable().findAny();
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
        return foldable().startsWithIterable(iterable);
    }

    /**
     * <pre>
     * {@code assertTrue(ReactiveSeq.of(1,2,3,4).startsWith(Stream.of(1,2,3))) }
     * </pre>
     * 
     * @param stream Stream to check if this Foldable has the same elements in the same order, at the start
     * @return True if Monad starts with Iterators sequence of data
     */
    default boolean startsWith(final Stream<T> stream) {
        return foldable().startsWith(stream);
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
        return foldable().endsWithIterable(iterable);
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
        return foldable().endsWith(stream);
    }

    /**
     * Lazily converts this SequenceM into a Collection. This does not trigger
     * the Stream. E.g. Collection is not thread safe on the first iteration.
     * 
     * <pre>
     * {@code 
     *  
     *  Collection<Integer> col = ReactiveSeq.of(1, 2, 3, 4, 5).peek(System.out::println).toLazyCollection();
     * 
     *  col.forEach(System.out::println);
     * }
     * 
     * // Will print out &quot;first!&quot; before anything else
     * </pre>
     * 
     * @return Lazy Collection
     */
    default CollectionX<T> toLazyCollection() {
        return foldable().toLazyCollection();
    }

    /**
     * Lazily converts this SequenceM into a Collection. This does not trigger
     * the Stream. E.g.
     * 
     * <pre>
     * {@code
     *  
     *  Collection<Integer> col = ReactiveSeq.of(1, 2, 3, 4, 5)
     *                                       .peek(System.out::println).toConcurrentLazyCollection();
     * 
     *  col.forEach(System.out::println);
     * }
     * 
     * // Will print out "first!" before anything else
     * </pre>
     * 
     * @return Concurrent Lazy Collection
     */
    default CollectionX<T> toConcurrentLazyCollection() {
        return foldable().toConcurrentLazyCollection();
    }

    /**
     * <pre>
     * {@code
     *  
     *  Streamable<Integer> repeat = ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *                                          .map(i ->; i + 2)
     *                                          .toConcurrentLazyStreamable();
     * 
     *  assertThat(repeat.sequenceM().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     *  assertThat(repeat.sequenceM().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     * }
     * </pre>
     * 
     * @return Streamable that replay this SequenceM, populated lazily and can
     *         be populated across threads
     */
    default Streamable<T> toConcurrentLazyStreamable() {
        return foldable().toConcurrentLazyStreamable();
    }

    /**
     * <pre>
     * {@code 
     *  assertThat(ReactiveSeq.of(1,2,3,4)
     *                  .map(u->throw new RuntimeException())
     *                  .recover(e->"hello")
     *                  .firstValue(),equalTo("hello"));
     * }
     * </pre>
     * 
     * @return first value in this Stream
     */
    default T firstValue() {
        return foldable().firstValue();
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
    default T single() {
        return foldable().single();

    }

    default T single(final Predicate<? super T> predicate) {
        return foldable().single(predicate);
    }

    /**
     * <pre>
     * {@code 
     *    
     *    //Optional[1]
     *    ReactiveSeq.of(1).singleOptional(); 
     *    
     *    //Optional.empty
     *    ReactiveSeq.of().singleOpional();
     *     
     *     //Optional.empty
     *    ReactiveSeq.of(1,2,3).singleOptional();
     * }
     * </pre>
     * 
     * @return An Optional with single value if this Stream has exactly one
     *         element, otherwise Optional Empty
     */
    default Optional<T> singleOptional() {
        return foldable().singleOptional();

    }

    /**
     * Return the elementAt index or Optional.empty
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
    default Optional<T> get(final long index) {
        return foldable().get(index);
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
     * @return Connectable HotStream of output from scheduled Stream
     */
    default HotStream<T> schedule(final String cron, final ScheduledExecutorService ex) {
        return foldable().schedule(cron, ex);
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
     * @return Connectable HotStream of output from scheduled Stream
     */
    default HotStream<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return foldable().scheduleFixedDelay(delay, ex);
    }

    /**
     * Execute this Stream on a schedule
     * 
     * <pre>
     * {@code
     *  //run every 60 seconds
     *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
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
     *  HotStream<Data> dataStream = ReactiveSeq.generate(() -&gt; "next job:" + formatDate(new Date())).map(this::processJob)
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
     * @return Connectable HotStream of output from scheduled Stream
     */
    default HotStream<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return foldable().scheduleFixedRate(rate, ex);
    }

    /**
     * Apply the specified validator to all elements in this foldable, resulting in a failure Stream and success Stream.
     * Each Stream is accessible via the returned Ior (Inclusive Or) {@link com.aol.cyclops.control.Ior}
     * 
     * @param validator {@link com.aol.cyclops.control.Validator} to validate each element with
     * @return Ior with a Failure Stream and / or Success Stream
     */
    default <S, F> Ior<ReactiveSeq<F>, ReactiveSeq<S>> validate(final Validator<T, S, F> validator) {

        final Tuple2<ReactiveSeq<Xor<F, S>>, ReactiveSeq<Xor<F, S>>> xors = stream().<Xor<F, S>> flatMap(s -> validator.accumulate(s)
                                                                                                                       .toXors()
                                                                                                                       .stream())
                                                                                    .duplicateSequence();

        final Predicate<Xor<F, S>> primaryPredicate = e -> e.isPrimary();

        return Ior.both(xors.v1.filter(primaryPredicate.negate())
                               .map(x -> x.secondaryGet()),
                        xors.v2.filter(primaryPredicate)
                               .map(x -> x.get()));
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
        return foldable().xMatch(num, c);
    }
}
