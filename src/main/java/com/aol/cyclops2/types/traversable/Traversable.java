package com.aol.cyclops2.types.traversable;

import com.aol.cyclops2.types.recoverable.OnEmpty;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.functor.FilterableTransformable;
import com.aol.cyclops2.types.functor.TransformerTraversable;
import cyclops.collections.immutable.VectorX;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.*;
import java.util.stream.*;

/**
 * A non-scalar navigatable data type
 * 
 * @author johnmcclean
 *
 * @param <T> The data type of the elements in this Traversable
 */
public interface Traversable<T> extends Publisher<T>,
                                        OnEmpty<T>,
                                        Zippable<T>,
                                        IterableFilterable<T>,
                                        FilterableTransformable<T>,
                                        TransformerTraversable<T>,
                                        Sequential<T>{

    default DoubleStream mapToDouble(ToDoubleFunction<? super T> fn){
        return this.stream().mapToDouble(fn);
    }
    default LongStream mapToLong(ToLongFunction<? super T> fn){
        return this.stream().mapToLong(fn);
    }
    default IntStream mapToInt(ToIntFunction<? super T> fn){
        return this.stream().mapToInt(fn);
    }

    @Override
    default Traversable<T> zip(BinaryOperator<Zippable<T>> combiner, final Zippable<T> app) {
        return traversable().zip(combiner,app);
    }

    @Override
    default <R> Traversable<R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return traversable().zipWith(fn);
    }

    @Override
    default <R> Traversable<R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return traversable().zipWithS(fn);
    }

    @Override
    default <R> Traversable<R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return traversable().zipWithP(fn);
    }

    @Override
    default <T2, R> Traversable<R> zipP(final Publisher<? extends T2> publisher, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return traversable().zipP(publisher,fn);
    }

    @Override
    default <U, R> Traversable<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return traversable().zipS(other,zipper);
    }

    @Override
    default <U> Traversable<Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return traversable().zipP(other);
    }

    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return traversable().zip(other);
    }

    @Override
    default <S, U, R> Traversable<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return traversable().zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> Traversable<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return traversable().zip4(second,third,fourth,fn);
    }

    /**
     * @return This Traversable converted toNested a Stream
     */
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    /**
     * Create an IterableFunctor instance of the same type from an Iterator
     * <pre>
     * {@code
     *       ReactiveSeq<Integer> newSeq = seq.unitIterable(myIterator);
     *
     * }
     * </pre>
     *
     * @param U Iterator toNested create new IterableFunctor from
     * @return New IterableFunctor instance
     */
    <U> Traversable<U> unitIterator(Iterator<U> U);
    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {
        traversable().subscribe(s);
    }

    /**
     * Combine two adjacent elements in a traversable using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbour
     * <pre>
     * {@code 
     *  ReactiveSeq.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .listX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test toNested see if two neighbours should be joined. The takeOne parameter toNested the bi-predicate is the currently
     *                  accumulated result and the second is the next element
     * @param op BinaryOperator toNested combine neighbours
     * @return Combined / Partially Reduced Traversable
     */
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return traversable().combine(predicate, op);
    }

    /**
     * Combine two adjacent elements in a traversable using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbour
     * <pre>
     * {@code
     *  ReactiveSeq.of(1,1,2,3)
                  .combine(Monoids.intMult,(a, b)->a.equals(b))
                  .listX()

     *  //ListX(1)
     * }</pre>
     *
     * Simalar toNested @see {@link Traversable#combine(BiPredicate, BinaryOperator)} but differs in that the takeOne comparison is always toNested the Monoid zero
     * This allows us toNested terminate with just a singleUnsafe value
     *
     * @param predicate Test toNested see if two neighbours should be joined. The takeOne parameter toNested the bi-predicate is the currently
     *                  accumulated result and the second is the next element
     * @param op Monoid toNested combine neighbours
     * @return Combined / Partially Reduced Traversable
     */
    default Traversable<T> combine(final Monoid<T> op,final BiPredicate<? super T, ? super T> predicate) {

        return prepend(op.zero()).traversable()
                                 .combine(predicate, op);
    }

    /**
     * Convert toNested a Stream with the values repeated specified times
     * 
     * <pre>
     * {@code 
     * 		ReactiveSeq.of(1,2,2)
     * 								.cycle(3)
     * 								.collect(CyclopsCollectors.toList());
     * 								
     * 		//List[1,2,2,1,2,2,1,2,2]
     * 
     * }
     * </pre>
     * 
     * @param times
     *            Times values should be repeated within a Stream
     * @return Stream with values repeated
     */
    default Traversable<T> cycle(final long times) {
        return traversable().cycle(times);
    }

    /**
     * Convert toNested a Stream with the result of a reduction operation repeated
     * specified times
     * 
     * <pre>
     * {@code 
     *   List<Integer> list = ReactiveSeq.of(1,2,2))
     *                                 .cycle(Reducers.toCountInt(),3)
     *                                 .collect(CyclopsCollectors.toList());
     *   //List[3,3,3];
     *   }
     * </pre>
     * 
     * @param m
     *            Monoid toNested be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    default Traversable<T> cycle(final Monoid<T> m, final long times) {
        return traversable().cycle(m, times);
    }

    /**
     * Repeat in a Stream while specified predicate holds
     * 
     * <pre>
     * {@code
     *  MutableInt count = MutableInt.of(0);
     *  ReactiveSeq.of(1, 2, 2).cycleWhile(next -> count++ < 6).collect(CyclopsCollectors.toList());
     * 
     *  // List(1,2,2,1,2,2)
     * }
     * </pre>
     * 
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    default Traversable<T> cycleWhile(final Predicate<? super T> predicate) {
        return traversable().cycleWhile(predicate);
    }

    /**
     * Repeat in a Stream until specified predicate holds
     * 
     * <pre>
     * {@code 
     * 	MutableInt count =MutableInt.of(0);
     * 		ReactiveSeq.of(1,2,2)
     * 		 		.cycleUntil(next -> count.get()>6)
     * 		 		.peek(i-> count.mutate(i->i+1))
     * 		 		.collect(CyclopsCollectors.toList());
     * 
     * 		//List[1,2,2,1,2,2,1]	
     * }
     * </pre>
     * 
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    default Traversable<T> cycleUntil(final Predicate<? super T> predicate) {
        return traversable().cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other,final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return traversable().zip(other, zipper);
    }



    /**
     * zip 3 Streams into replaceWith
     * 
     * <pre>
     * {@code
     *  List<Tuple3<Integer, Integer, Character>> list = of(1, 2, 3, 4, 5, 6).zip3(of(100, 200, 300, 400), of('a', 'b', 'c')).collect(CyclopsCollectors.toList());
     * 
     *  // [[1,100,'a'],[2,200,'b'],[3,300,'c']]
     * }
     * 
     * </pre>
     */
    default <S, U> Traversable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return traversable().zip3(second, third);
    }

    /**
     * zip 4 Streams into 1
     * 
     * <pre>
     * {@code
     *  List<Tuple4<Integer, Integer, Character, String>> list = of(1, 2, 3, 4, 5, 6).zip4(of(100, 200, 300, 400), of('a', 'b', 'c'), of("hello", "world"))
     *          .collect(CyclopsCollectors.toList());
     * 
     * }
     * // [[1,100,'a',"hello"],[2,200,'b',"world"]]
     * </pre>
     */
    default <T2, T3, T4> Traversable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {
        return traversable().zip4(second, third, fourth);
    }

    /**
     * Add an index toNested the current Stream
     * 
     * <pre>
     * {@code 
     * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
     * }
     * </pre>
     */
    default Traversable<Tuple2<T, Long>> zipWithIndex() {
        return traversable().zipWithIndex();
    }

    /**
     * Create a sliding view over this Sequence
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(2).collect(CyclopsCollectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2));
     *  assertThat(list.get(1), hasItems(2, 3));
     * 
     * }
     * 
     * </pre>
     * 
     * @param windowSize
     *            Size of sliding window
     * @return SequenceM with sliding view
     */
    default Traversable<VectorX<T>> sliding(final int windowSize) {
        return traversable().sliding(windowSize);
    }

    /**
     * Create a sliding view over this Sequence
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(CyclopsCollectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2, 3));
     *  assertThat(list.get(1), hasItems(3, 4, 5));     
     * 
     * 
     * }
     * 
     * </pre>
     * 
     * @param windowSize
     *            number of elements in each batch
     * @param increment
     *            for each window
     * @return SequenceM with sliding view
     */
    default Traversable<VectorX<T>> sliding(final int windowSize, final int increment) {
        return traversable().sliding(windowSize, increment);
    }

    /**
     * Batch elements in a Stream by size into a toX created by the
     * supplied factory
     * 
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,1,1,1,1,1)
     *                      .batchBySize(3,()->new TreeSet<>())
     *                      .toList()
     *                      .get(0)
     *                      .size(),is(1));
     * }
     * </pre>
     * @param size batch size
     * @param supplier Collection factory
     * @return SequenceM batched into toX types by size
     */
    default <C extends Collection<? super T>> Traversable<C> grouped(final int size, final Supplier<C> supplier) {
        return traversable().grouped(size, supplier);
    }

    /**
     * Create a Traversable batched by List, where each batch is populated until
     * the predicate holds
     * 
     * <pre>
     * {@code 
     *  assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedUntil(i->i%3==0)
     *              .toList()
     *              .size(),equalTo(2));
     * }
     * </pre>
     * 
     * @param predicate
     *            Batch until predicate holds, applyHKT open next batch
     * @return SequenceM batched into lists determined by the predicate supplied
     */
    default Traversable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return traversable().groupedUntil(predicate);
    }

    /**
     * Create Travesable of Lists where
     * each List is populated while the supplied bipredicate holds. The
     * bipredicate recieves the List from the last window as well as the
     * current value and can choose toNested aggregate the current value or create a
     * new window
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedStatefullyUntil((s,i)->s.contains(4) ? true : false)
     *              .toList().size(),equalTo(5));
     * }
     * </pre>
     * 
     * @param predicate
     *            Window while true
     * @return Traversable windowed while predicate holds
     */
    default Traversable<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return traversable().groupedStatefullyUntil(predicate);
    }

    /**
     * Zip (combine) this Zippable with the supplied Stream combining both into a Tuple2
     *
     * @param other Stream toNested combine with
     * @return Combined Zippable
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return traversable().zipS(other);
    }

    /**
     * Create a Traversable batched by List, where each batch is populated while
     * the predicate holds
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedWhile(i->i%3!=0)
     *              .toList().size(),equalTo(2));
     *  
     * }
     * </pre>
     * 
     * @param predicate
     *            Batch while predicate holds, applyHKT open next batch
     * @return SequenceM batched into lists determined by the predicate supplied
     */
    default Traversable<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return traversable().groupedWhile(predicate);
    }

    /**
     * Create a SequenceM batched by a Collection, where each batch is populated
     * while the predicate holds
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .batchWhile(i->i%3!=0)
     *              .toList()
     *              .size(),equalTo(2));
     * }
     * </pre>
     * 
     * @param predicate
     *            Batch while predicate holds, applyHKT open next batch
     * @param factory
     *            Collection factory
     * @return SequenceM batched into collections determined by the predicate
     *         supplied
     */
    default <C extends Collection<? super T>> Traversable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return traversable().groupedWhile(predicate, factory);
    }

    /**
     * Create a ReactiveSeq batched by a Collection, where each batch is populated
     * until the predicate holds
     * 
     * <pre>
     * {@code 
     *  ReactiveSeq.of(1,2,3,4,5,6)
     *             .groupedUntil(i->i%3!=0)
     *             .toList()
     *             
     *  //2
     * }
     * </pre>
     * 
     * 
     * @param predicate
     *            Batch until predicate holds, applyHKT open next batch
     * @param factory
     *            Collection factory
     * @return SequenceM batched into collections determined by the predicate
     *         supplied
     */
    default <C extends Collection<? super T>> Traversable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return traversable().groupedUntil(predicate, factory);
    }

    /**
     * Group elements in a Stream
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).grouped(3).collect(CyclopsCollectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2, 3));
     *  assertThat(list.get(1), hasItems(4, 5, 6));
     * 
     * }
     * </pre>
     * 
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    default Traversable<ListX<T>> grouped(final int groupSize) {
        return traversable().grouped(groupSize);
    }

    /**
     * Group this Traversable by the provided classifying function and collected by the provided Collector
     * 
     * @param classifier Grouping function
     * @param downstream Collector toNested create the grouping toX
     * @return Traversable of grouped data
     */
    default <K, A, D> Traversable<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return traversable().grouped(classifier, downstream);
    }

    /**
     * Group this Traversable by the provided classifying function and collected by the provided Collector
     * 
     * @param classifier Grouping function
     * @return Traversable of grouped data
     */
    default <K> Traversable<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return traversable().grouped(classifier);
    }

    /*
     * Return the distinct Stream of elements
     * 
     * <pre> {@code List<Integer> list = ReactiveSeq.of(1,2,2,2,5,6) .distinct()
     * .collect(CyclopsCollectors.toList()); }</pre>
     */
    default Traversable<T> distinct() {
        return traversable().distinct();
    }

    /**
     * Scan left using supplied Monoid
     * 
     * <pre>
     * {@code  
     * 
     * 	assertEquals(asList("", "a", "ab", "abc"),ReactiveSeq.of("a", "b", "c")
     * 													.scanLeft(Reducers.toString("")).toList());
     *         
     *         }
     * </pre>
     * 
     * @param monoid
     * @return
     */
    default Traversable<T> scanLeft(final Monoid<T> monoid) {
        return traversable().scanLeft(monoid);
    }

    /**
     * Scan left
     * 
     * <pre>
     * {@code 
     *  assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
     *         		is(4));
     * }
     * </pre>
     */
    default <U> Traversable<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return traversable().scanLeft(seed, function);
    }

    /**
     * Scan right
     * 
     * <pre>
     * {@code 
     * assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
     *             is(asList("", "c", "bc", "abc").size()));
     * }
     * </pre>
     */
    default Traversable<T> scanRight(final Monoid<T> monoid) {
        return traversable().scanRight(monoid);
    }

    /**
     * Scan right
     * 
     * <pre>
     * {@code 
     * assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
     *             is(asList(0, 3, 5, 6).size()));
     * 
     * }
     * </pre>
     */
    default <U> Traversable<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return traversable().scanRight(identity, combiner);
    }

    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
     * </pre>
     * 
     */
    default Traversable<T> sorted() {
        return traversable().sorted();
    }

    /**
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
     * }
     * </pre>
     * 
     * @param c
     *            Compartor toNested sort with
     * @return Sorted Stream
     */
    default Traversable<T> sorted(final Comparator<? super T> c) {
        return traversable().sorted(c);
    }

    /**
     * Generate a new Traversable that takes elements from this Traversable as long as the predicate holds
     * 
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).takeWhile(i<3);
     *     //[1,2]
     * }
     * </pre>
     * 
     * @param p Predicate toNested determine when values should be taken
     * @return Traversable generated by application of the predicate toNested the elements in this Traversable in order
     */
    default Traversable<T> takeWhile(final Predicate<? super T> p) {
        return limitWhile(p);
    }

    /**
     * Generate a new Traversable that drops elements from this Traversable as long as the predicate holds
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).dropWhile(i<3);
     *     //[3]
     * }
     * </pre> 
     * @param p Predicate toNested determine when values should be dropped
     * @return Traversable generated by application of the predicate toNested the elements in this Traversable in order
     */
    default Traversable<T> dropWhile(final Predicate<? super T> p) {
        return skipWhile(p);
    }

    /**
     * Generate a new Traversable that takes elements from this Traversable until the predicate holds
      * <pre>
     * {@code 
     *     ListX.of(1,2,3).takeUntil(i<2);
     *     //[1,2]
     * }
     * </pre>
     * 
     * @param p Predicate toNested determine when values should be taken until
     * @return  Traversable generated by application of the predicate toNested the elements in this Traversable in order
     */
    default Traversable<T> takeUntil(final Predicate<? super T> p) {
        return limitUntil(p);
    }

    /**
     * Generate a new Traversable that drops elements from this Traversable until the predicate holds
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).dropUntil(i>2);
     *     //[3]
     * }
     * </pre> 
     * @param p Predicate toNested determine when values should be dropped
     * @return Traversable generated by application of the predicate toNested the elements in this Traversable in order
     */
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return skipUntil(p);
    }

    /**
     * Generate a new Traversable that drops the specified number elements from the take of this Traversable
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).dropRight(2);
     *     //[1]
     * }
     * </pre>
     * @param num Drop this number of elements from the take of this Traversable
     * @return Traversable generated by application of the predicate toNested the elements in this Traversable in order
     */
    default Traversable<T> dropRight(final int num) {
        return skipLast(num);
    }

    /**
     * Generate a new Traversable that takes the specified number elements from the take of this Traversable
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).takeRight(2);
     *     //[2,3]
     * }
     * </pre>
     * @param num Take this number of elements from the take of this Traversable
     * @return Traversable generated by application of the predicate toNested the elements in this Traversable in order
     */
    default Traversable<T> takeRight(final int num) {
        return limitLast(num);
    }
    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).drop(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     * 
     * 
     * 
     * @param num
     *            Number of elemenets toNested drop
     * @return Traversable with specified number of elements skipped
     */
    default Traversable<T> drop(final long num) {
        return traversable().skip(num);
    }
    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     * 
     * 
     * 
     * @param num
     *            Number of elemenets toNested skip
     * @return Stream with specified number of elements skipped
     */
    default Traversable<T> skip(final long num) {
        return traversable().skip(num);
    }

    /**
     * 
     * SkipWhile drops elements from the Stream while the predicate holds, once
     * the predicte returns true all subsequent elements are included *
     * 
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(4,3,6,7).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
     * }
     * </pre>
     * 
     * @param p
     *            Predicate toNested skip while true
     * @return Stream with elements skipped while predicate holds
     */
    default Traversable<T> skipWhile(final Predicate<? super T> p) {
        return traversable().skipWhile(p);
    }

    /**
     * Drop elements from the Stream until the predicate returns true, after
     * which all elements are included
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
     * </pre>
     * 
     * 
     * @param p
     *            Predicate toNested skip until true
     * @return Stream with elements skipped until predicate holds
     */
    default Traversable<T> skipUntil(final Predicate<? super T> p) {
        return traversable().skipUntil(p);
    }
    /**
     * 
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).take(2).toList(),equalTo(Arrays.asList(4,3));}
     * </pre>
     * 
     * @param num
     *            Limit element size toNested num
     * @return Monad converted toNested Stream with elements up toNested num
     */
    default Traversable<T> take(final long num) {
        return traversable().limit(num);
    }

    /**
     * 
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
     * </pre>
     * 
     * @param num
     *            Limit element size toNested num
     * @return Monad converted toNested Stream with elements up toNested num
     */
    default Traversable<T> limit(final long num) {
        return traversable().limit(num);
    }

    /**
     * Take elements from the Stream while the predicate holds, once the
     * predicate returns false all subsequent elements are excluded
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
     * </pre>
     * 
     * @param p
     *            Limit while predicate is true
     * @return Stream with limited elements
     */
    default Traversable<T> limitWhile(final Predicate<? super T> p) {
        return traversable().limitWhile(p);
    }

    /**
     * Take elements from the Stream until the predicate returns true, after
     * which all elements are excluded.
     * 
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
     * </pre>
     * 
     * @param p
     *            Limit until predicate is true
     * @return Stream with limited elements
     */
    default Traversable<T> limitUntil(final Predicate<? super T> p) {
        return traversable().limitUntil(p);
    }

    /**
     * Returns a reactiveStream with a given value interspersed between any two values
     * of this reactiveStream.
     * 
     * 
     * // (1, 0, 2, 0, 3, 0, 4) ReactiveSeq.of(1, 2, 3, 4).intersperse(0)
     * 
     */
    default Traversable<T> intersperse(final T value) {
        return traversable().intersperse(value);
    }

    /**
     * Potentially efficient Stream reversal. Is efficient if
     * 
     * - Sequence created via a range - Sequence created via a List - Sequence
     * created via an Array / var args
     * 
     * Otherwise Sequence collected into a Collection prior toNested reversal
     * 
     * <pre> {@code assertThat( of(1, 2, 3).reverse().toList(),
     * equalTo(asList(3, 2, 1))); } </pre>
     */
    default Traversable<T> reverse() {
        return traversable().reverse();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#shuffle()
     */
    default Traversable<T> shuffle() {
        return traversable().shuffle();
    }



    /**
     * assertThat(ReactiveSeq.of(1,2,3,4,5) .skipLast(2)
     * .collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,3)));
     * 
     * @param num
     * @return
     */
    default Traversable<T> skipLast(final int num) {
        return traversable().skipLast(num);
    }

    /**
     * Limit results toNested the last x elements in a SequenceM
     * 
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(1,2,3,4,5)
     * 							.limitLast(2)
     * 							.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(4,5)));
     * 
     * }
     * </pre>
     * 
     * @param num of elements toNested return (last elements)
     * @return SequenceM limited toNested last num elements
     */
    default Traversable<T> limitLast(final int num) {
        return traversable().limitLast(num);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#onEmpty(java.lang.Object)
     */
    @Override
    default Traversable<T> onEmpty(final T value) {
        return traversable().onEmpty(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default Traversable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return traversable().onEmptyGet(supplier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> Traversable<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return traversable().onEmptyThrow(supplier);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#shuffle(java.util.Random)
     */
    default Traversable<T> shuffle(final Random random) {
        return traversable().shuffle(random);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#slice(long, long)
     */
    default Traversable<T> slice(final long from, final long to) {
        return traversable().slice(from, to);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    default <U extends Comparable<? super U>> Traversable<T> sorted(final Function<? super T, ? extends U> function) {
        return traversable().sorted(function);
    }

    /**
     * @return This Traversable converted toNested a Stream and type narrowed toNested Traversable
     */
    default Traversable<T> traversable() {
        return stream();
    }

    /**
     * Prepend Stream toNested this ReactiveSeq
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3)
     *                                   .prependS(of(100, 200, 300))
     *                                   .map(it -> it + "!!")
     *                                   .collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("100!!", "200!!", "300!!", "1!!", "2!!", "3!!")));
     * }
     * </pre>
     *
     * @param stream
     *            toNested Prepend
     * @return ReactiveSeq with Stream prepended
     */
    default Traversable<T> prependS(Stream<? extends T> stream){
        return traversable().prependS(stream);
    }

    /**
     * Append values toNested the take of this ReactiveSeq
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).append(100, 200, 300).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "3!!", "100!!", "200!!", "300!!")));     * }
     * </pre>
     *
     * @param values
     *            toNested append
     * @return ReactiveSeq with appended values
     */
    default Traversable<T> append(T... values){
        return traversable().append(values);
    }


    default Traversable<T> append(T value){
        return traversable().append(value);
    }


    default Traversable<T> prepend(T value){
        return traversable().prepend(value);
    }

    /**
     * Prepend given values toNested the skip of the Stream
     *
     * <pre>
     * {@code
     * List<String> result = 	ReactiveSeq.of(1,2,3)
     * 									 .prepend(100,200,300)
     * 										 .map(it ->it+"!!")
     * 										 .collect(CyclopsCollectors.toList());
     *
     * 			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * @param values toNested prepend
     * @return ReactiveSeq with values prepended
     */
    default Traversable<T> prepend(T... values){
        return traversable().prepend(values);
    }

    /**
     * Insert data into a reactiveStream at given position
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).insertAt(1, 100, 200, 300).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));     *
     * }
     * </pre>
     *
     * @param pos
     *            toNested insert data at
     * @param values
     *            toNested insert
     * @return Stream with new data inserted
     */
    default Traversable<T> insertAt(int pos, T... values){
        return traversable().insertAt(pos,values);
    }

    /**
     * Delete elements between given indexes in a Stream
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3, 4, 5, 6).deleteBetween(2, 4).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "2!!", "5!!", "6!!")));     * }
     * </pre>
     *
     * @param start
     *            index
     * @param end
     *            index
     * @return Stream with elements removed
     */
    default Traversable<T> deleteBetween(int start, int end){
        return traversable().deleteBetween(start,end);
    }

    /**
     * Insert a Stream into the middle of this reactiveStream at the specified position
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3).insertAtS(1, of(100, 200, 300)).map(it -> it + "!!").collect(CyclopsCollectors.toList());
     *
     *  assertThat(result, equalTo(Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));
     * }
     * </pre>
     *
     * @param pos
     *            toNested insert Stream at
     * @param stream
     *            toNested insert
     * @return newly conjoined Traversable
     */
    default Traversable<T> insertAtS(int pos, Stream<T> stream){
        return traversable().insertAtS(pos,stream);
    }

    /**
     * Recover from an exception with an alternative value
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4)
     * 						   .map(i->i+2)
     * 						   .map(u->{throw new RuntimeException();})
     * 						   .recover(e->"hello")
     * 						   .firstValue(),equalTo("hello"));
     * }
     * </pre>
     *
     * @param fn
     *            Function that accepts a Throwable and returns an alternative
     *            value
     * @return ReactiveSeq that can recover from an Exception
     */
    default Traversable<T> recover(final Function<? super Throwable, ? extends T> fn){
        return traversable().recover(fn);
    }

    /**
     * Recover from a particular exception type
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4)
     * 					.map(i->i+2)
     * 					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
     * 					.recover(IOException.class,e->"hello")
     * 					.firstValue(),equalTo("hello"));
     *
     * }
     * </pre>
     *
     * @param exceptionClass
     *            Type toNested recover from
     * @param fn
     *            That accepts an error and returns an alternative value
     * @return Traversable that can recover from a particular exception
     */
    default <EX extends Throwable> Traversable<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn){
        return traversable().recover(exceptionClass,fn);
    }

}
