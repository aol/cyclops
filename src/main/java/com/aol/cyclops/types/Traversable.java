package com.aol.cyclops.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

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
                                        ReactiveStreamsTerminalOperations<T>,
                                        CyclopsCollectable<T>,
                                        IterableFoldable<T>,
                                        IterableFilterable<T>,
                                        FilterableFunctor<T>{


    default Seq<T> seq(){
        return Seq.seq(this);
    }
    /**
     * @return This Traversable converted to a Stream
     */
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> s) {
        traversable().subscribe(s);
    }

    /**
     * Combine two adjacent elements in a traversable using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The output of a combination may in turn be combined
     * with it's neighbour
     * <pre>
     * {@code 
     *  ReactiveSeq.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbours should be joined
     * @param op Reducer to combine neighbours
     * @return Combined / Partially Reduced Traversable
     */
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return traversable().combine(predicate, op);
    }

    /**
     * Convert to a Stream with the values repeated specified times
     * 
     * <pre>
     * {@code 
     * 		ReactiveSeq.of(1,2,2)
     * 								.cycle(3)
     * 								.collect(Collectors.toList());
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
    default Traversable<T> cycle(final int times) {
        return traversable().cycle(times);
    }

    /**
     * Convert to a Stream with the result of a reduction operation repeated
     * specified times
     * 
     * <pre>
     * {@code 
     *   List<Integer> list = ReactiveSeq.of(1,2,2))
     *                                 .cycle(Reducers.toCountInt(),3)
     *                                 .collect(Collectors.toList());
     *   //List[3,3,3];
     *   }
     * </pre>
     * 
     * @param m
     *            Monoid to be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    default Traversable<T> cycle(final Monoid<T> m, final int times) {
        return traversable().cycle(m, times);
    }

    /**
     * Repeat in a Stream while specified predicate holds
     * 
     * <pre>
     * {@code
     *  MutableInt count = MutableInt.of(0);
     *  ReactiveSeq.of(1, 2, 2).cycleWhile(next -> count++ < 6).collect(Collectors.toList());
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
     * 		 		.collect(Collectors.toList());
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
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other,final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return traversable().zip(other, zipper);
    }



    /**
     * zip 3 Streams into one
     * 
     * <pre>
     * {@code
     *  List<Tuple3<Integer, Integer, Character>> list = of(1, 2, 3, 4, 5, 6).zip3(of(100, 200, 300, 400), of('a', 'b', 'c')).collect(Collectors.toList());
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
     *          .collect(Collectors.toList());
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
     * Add an index to the current Stream
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
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());
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
    default Traversable<ListX<T>> sliding(final int windowSize) {
        return traversable().sliding(windowSize);
    }

    /**
     * Create a sliding view over this Sequence
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());
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
    default Traversable<ListX<T>> sliding(final int windowSize, final int increment) {
        return traversable().sliding(windowSize, increment);
    }

    /**
     * Batch elements in a Stream by size into a collection created by the
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
     * @return SequenceM batched into collection types by size
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
     *            Batch until predicate holds, then open next batch
     * @return SequenceM batched into lists determined by the predicate supplied
     */
    default Traversable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return traversable().groupedUntil(predicate);
    }

    /**
     * Create Travesable of Lists where
     * each List is populated while the supplied bipredicate holds. The
     * bipredicate recieves the List from the last window as well as the
     * current value and can choose to aggregate the current value or create a
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
     *            Batch while predicate holds, then open next batch
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
     *            Batch while predicate holds, then open next batch
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
     *            Batch until predicate holds, then open next batch
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
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
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
     * @param downstream Collector to create the grouping collection
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
     * .collect(Collectors.toList()); }</pre>
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
     *            Compartor to sort with
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
     * @param p Predicate to determine when values should be taken
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
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
     * @param p Predicate to determine when values should be dropped
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
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
     * @param p Predicate to determine when values should be taken until
     * @return  Traversable generated by application of the predicate to the elements in this Traversable in order
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
     * @param p Predicate to determine when values should be dropped
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return skipUntil(p);
    }

    /**
     * Generate a new Traversable that drops the specified number elements from the end of this Traversable
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).dropRight(2);
     *     //[1]
     * }
     * </pre>
     * @param num Drop this number of elements from the end of this Traversable
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> dropRight(final int num) {
        return skipLast(num);
    }

    /**
     * Generate a new Traversable that takes the specified number elements from the end of this Traversable
     * <pre>
     * {@code 
     *     ListX.of(1,2,3).takeRight(2);
     *     //[2,3]
     * }
     * </pre>
     * @param num Take this number of elements from the end of this Traversable
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
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
     *            Number of elemenets to drop
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
     *            Number of elemenets to skip
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
     *            Predicate to skip while true
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
     *            Predicate to skip until true
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
     *            Limit element size to num
     * @return Monad converted to Stream with elements up to num
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
     *            Limit element size to num
     * @return Monad converted to Stream with elements up to num
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
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
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
     * Otherwise Sequence collected into a Collection prior to reversal
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
     * .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
     * 
     * @param num
     * @return
     */
    default Traversable<T> skipLast(final int num) {
        return traversable().skipLast(num);
    }

    /**
     * Limit results to the last x elements in a SequenceM
     * 
     * <pre>
     * {@code 
     * 	assertThat(ReactiveSeq.of(1,2,3,4,5)
     * 							.limitLast(2)
     * 							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
     * 
     * }
     * </pre>
     * 
     * @param num of elements to return (last elements)
     * @return SequenceM limited to last num elements
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
     * @return This Traversable converted to a Stream and type narrowed to Traversable
     */
    default Traversable<T> traversable() {
        return stream();
    }

}
