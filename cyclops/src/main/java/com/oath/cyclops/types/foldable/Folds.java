package com.oath.cyclops.types.foldable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.types.stream.HotStream;
import cyclops.control.Option;
import cyclops.data.*;
import cyclops.data.HashMap;
import cyclops.data.HashSet;
import cyclops.data.TreeSet;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.Seq;

/**
 * Represents a type that may be reducable (foldable) to a single value or toX
 *
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) in this Folds
 */
public interface Folds<T> extends Iterable<T>  {


    ReactiveSeq<T> stream();

    default <R> R[] toArray(IntFunction<R[]> generator){
        return stream().toArray(generator);
    }

    default  Object[] toArray(){
        return stream().toArray();
    }



    default <R> R iterableTo(Function<? super Iterable<? super T>,? extends R> fn){
        return fn.apply(this);
    }
    default BankersQueue<T> bankersQueue(){
        return BankersQueue.fromIterable(this);
    }
    default TreeSet<T> treeSet(Comparator<? super T> comp){
        return TreeSet.fromIterable(this,comp);
    }
    default HashSet<T> hashSet(){
        return HashSet.fromIterable(this);
    }
    default Vector<T> vector(){
        return Vector.fromIterable(this);
    }
    default LazySeq<T> lazySeq(){
        return LazySeq.fromIterable(this);
    }
    default Seq<T> seq(){
        return Seq.fromIterable(this);
    }
    /**
     * Collect the collectable into a {@link Map}.
     */
    default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper){
        return stream().collect(Collectors.toMap(keyMapper,valueMapper));
    }

    /**
     * Collect the collectable into a {@link Map} with the given keys and the self element as value.
     */
    default <K> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper){
        return stream().collect(Collectors.toMap(keyMapper,i->i));
    }
    default <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory){

       return stream().collect(Collectors.toCollection(collectionFactory));
    }
    default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream().collect(supplier,accumulator,combiner);
    }

    default List<T> toList(){

        return  stream().collect(Collectors.toList());
    }
    /**
     * Collect the collectable into a {@link LinkedHashSet}.
     */
    default Set<T> toSet(){
        return  stream().collect(Collectors.toSet());
    }
    default <R1, R2, A1, A2> Tuple2<R1, R2> collect(Collector<? super T, A1, R1> c1, Collector<? super T, A2, R2> c2) {
        return stream().collect(Collector.of(() -> Tuple.tuple(c1.supplier().get(),c2.supplier().get()),
                (t2, next) -> {
                    c1.accumulator().accept(t2._1(), next);
                    c2.accumulator().accept(t2._2(), next);
                },(t2, t2b) -> Tuple.tuple(c1.combiner().apply(t2._1(), t2b._1()),c2.combiner().apply(t2._2(), t2b._2())),
                t2 -> Tuple.tuple(c1.finisher().apply(t2._1()),c2.finisher().apply(t2._2()))));
    }
    default <R1, R2, R3, A1, A2, A3> Tuple3<R1, R2, R3> collect(Collector<? super T, A1, R1> c1, Collector<? super T, A2, R2> c2, Collector<? super T, A3, R3> c3) {
        return stream().collect(Collector.of(() -> Tuple.tuple(c1.supplier().get(),c2.supplier().get(),c3.supplier().get()),
                (t3, next) -> {
                    c1.accumulator().accept(t3._1(), next);
                    c2.accumulator().accept(t3._2(), next);
                    c3.accumulator().accept(t3._3(), next);
                },(t3, t3b) -> Tuple.tuple(c1.combiner().apply(t3._1(), t3b._1()),c2.combiner().apply(t3._2(), t3b._2()),c3.combiner().apply(t3._3(), t3b._3())),
                t3 -> Tuple.tuple(c1.finisher().apply(t3._1()),c2.finisher().apply(t3._2()),c3.finisher().apply(t3._3()))));
    }

    default long countDistinct(){
        return stream().distinct().count();

    }
    default <U> Option<T> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator){
        return foldLeft(BinaryOperator.maxBy(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return comparator.compare(function.apply(o1),function.apply(o2));
            }
        }));
    }
    default <U extends Comparable<? super U>> Option<T> maxBy(Function<? super T, ? extends U> function){
        return maxBy(function, Comparator.naturalOrder());
    }
    default <U extends Comparable<? super U>> Option<T> minBy(Function<? super T, ? extends U> function){
        return minBy(function, Comparator.naturalOrder());
    }
    default <U extends Comparable<? super U>> Option<T> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator){
        return foldLeft(BinaryOperator.minBy(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return comparator.compare(function.apply(o1),function.apply(o2));
            }
        }));
    }
    default Option<T> mode(){
        Map<T,Integer> map = stream().collect(Collectors.toMap(k->k, v->1,(a, b)->a+b));

        return ReactiveSeq.fromIterable(map.entrySet())
                .maxBy(k -> k.getValue())
                .map(t -> t.getKey());
    }
    default ReactiveSeq<Tuple2<T,Integer>> occurances(){

        return ReactiveSeq.deferFromStream(() -> {
            Map<T, Integer> map = stream().collect(Collectors.toMap(k -> k, v -> 1, (a, b) -> a + b));
            return map.entrySet().stream();
        }).map(e->Tuple.tuple(e.getKey(),e.getValue()));
    }

    default double mean(ToDoubleFunction<T> fn){
        return stream().collect(Collectors.<T>averagingDouble(fn));
    }
    default T median(){
        return atPercentile(50.0);
    }


    default Seq<Tuple2<T,BigDecimal>> withPercentiles(){

        Seq<T> list = stream().toSeq();

        int precision = new Double(Math.log10(list.size())).intValue();


        return list.zipWithIndex().map(t -> t.map2(idx -> {
            double d = (idx / new Double(list.size()));
            return new BigDecimal((d*100),new MathContext(precision));
        }));

    }
    /*
        Value at percentile denoted by a double value between 0 and 100
        Assumes the data is already sorted
     */
    default T atPercentile(double percentile){
        List<T> list = stream().collect(Collectors.toList());
        Long pos = Math.round(((list.size()-1) * (percentile/100)));
        return list.get(pos.intValue());
    }


    default double variance(ToDoubleFunction<T> fn){
        Seq<T> list = stream().toSeq();
        double avg = list.collect(Collectors.<T>averagingDouble(fn));
        return (list.map(t -> fn.applyAsDouble(t))
                .map(t -> t - avg)
                .map(t -> t * t)
                .sumDouble(i -> i))/(list.size()-1);

    }
    default double populationVariance(ToDoubleFunction<T> fn){
        Seq<T> list = stream().toSeq();
        double avg = list.collect(Collectors.<T>averagingDouble(fn));
        return (list.map(t -> fn.applyAsDouble(t))
                .map(t -> t - avg)
                .map(t -> t * t)
                .sumDouble(i -> i)/(list.size()));

    }

    default double stdDeviation(ToDoubleFunction<T> fn){
        Seq<T> list = stream().toSeq();
        double avg = list.collect(Collectors.<T>averagingDouble(fn));
        return Math.sqrt( list.mapToDouble(fn)
                .map(i->i-avg)
                .map(i->i*i)
                .average()
                .getAsDouble());
    }

    default LongSummaryStatistics longStats(ToLongFunction<T> fn){
        return stream().collect(Collectors.summarizingLong(fn));
    }
    default IntSummaryStatistics intStats(ToIntFunction<T> fn){
        return stream().collect(Collectors.summarizingInt(fn));
    }
    default DoubleSummaryStatistics doubleStats(ToDoubleFunction<T> fn){
        return stream().collect(Collectors.summarizingDouble(fn));
    }

    default Optional<T> max(Comparator<? super T> comparator){
        return stream().sorted(comparator.reversed())
                       .elementAt(0l)
                       .toOptional();
    }
    default Optional<T> min(Comparator<? super T> comparator){
        return stream().sorted(comparator)
                .elementAt(0l)
                .toOptional();
    }
    default int sumInt(ToIntFunction<T> fn){
        return stream().mapToInt(fn).sum();
    }
    default double sumDouble(ToDoubleFunction<T> fn){
        return stream().mapToDouble(fn).sum();
    }
    default long sumLong(ToLongFunction<T> fn){
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
     * True if a single element matches when Monad converted to a Stream
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
    default <R> R mapReduce(final Reducer<R,T> reducer) {
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
     * Reduce this Folds to a single value, using the supplied Monoid. For example
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
     *       ReactiveSeq.of(1,2,3,4,5).map(it -> it*100).reduce(
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

    default Option<T> foldLeft(final BinaryOperator<T> accumulator) {
        return Option.fromOptional(stream().reduce(accumulator));
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
    default Seq<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
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
    default <R> R foldRightMapToType(final Reducer<R,T> reducer) {
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
     *  assertEquals(asList(2, 4), map1.getValue(0));
     *  assertEquals(asList(1, 3), map1.getValue(1));
     *  assertEquals(2, map1.size());
     *
     * }
     *
     * </pre>
     */
    default <K> HashMap<K, Vector<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return stream().groupBy(classifier);
    }

    /**
     * @return First matching element in sequential order
     *
     * <pre>
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
    default Optional<T> findFirst() {
        return stream().findFirst();
    }

    /**
     * @return first matching element, but order is not guaranteed
     *
     *         <pre>
     * {@code
     * ReactiveSeq.of(1,2,3,4,5).filter(it -> it <3).findAny().getValue();
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
     *                  .map(u->throw new RuntimeException())
     *                  .recover(e->"hello")
     *                  .firstValue(),equalTo("hello"));
     * }
     * </pre>
     *
     * @return first value in this Stream
     * @param alt
     */
    default T firstValue(T alt) {
        return stream().firstValue(alt);
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
     * @param alt
     */
    default T singleOrElse(T alt) {
        return stream().singleOrElse(alt);

    }

    default Maybe<T> single(final Predicate<? super T> predicate) {
        return stream().single(predicate);
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
     *  assertThat(ReactiveSeq.of(1,2,3,4,5).elementAt(2).getValue(),equalTo(3));
     * }
     * </pre>
     *
     * @param index
     *            to extract element from
     * @return elementAt index
     */
    default Maybe<T> elementAt(final long index) {
        return stream().elementAt(index);
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
     * @return Connectable HotStream of emitted from scheduled Stream
     */
    default HotStream<T> schedule(final String cron, final ScheduledExecutorService ex) {
        return stream().schedule(cron, ex);
    }

    default Maybe<Long> indexOf(Predicate<? super T> pred){
      return stream().zipWithIndex()
                     .filter(p->pred.test(p._1()))
                     .takeOne()
                     .map(v->v._2());
    }
    default Maybe<Long> lastIndexOf(Predicate<? super T> pred){
      return stream().zipWithIndex()
                     .filter(p->pred.test(p._1()))
                     .takeRight(1)
                     .takeOne()
                     .map(v->v._2());
    }
    default Maybe<Long> indexOfSlice(Iterable<? extends T> slice){
        LazySeq<? extends T> ls = LazySeq.fromIterable(slice);
        Predicate<? super Seq<? super T>> pred = in -> in.equals(ls);
        return stream().sliding(ls.size(),1).indexOf(pred);
    }
    default Maybe<Long> lastIndexOfSlice(Iterable<? extends T> slice){
        LazySeq<? extends T> ls = LazySeq.fromIterable(slice);
        Predicate<? super Seq<? super T>> pred = in -> in.equals(ls);
        return stream().sliding(ls.size(),1).lastIndexOf(pred);
    }
    /**
     *
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
