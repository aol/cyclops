package com.aol.cyclops.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.hamcrest.Matcher;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.ExceptionSoftener;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.matcher2.Case;
import com.aol.cyclops.matcher2.CheckValues;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.reactivestreams.CyclopsSubscriber;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsLoader;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsTerminalOperations;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.sequence.traits.ConvertableSequence;
import com.aol.cyclops.sequence.traits.SequenceMCollectable;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.streams.spliterators.ReversingArraySpliterator;
import com.aol.cyclops.streams.spliterators.ReversingListSpliterator;
import com.aol.cyclops.streams.spliterators.ReversingRangeIntSpliterator;
import com.aol.cyclops.streams.spliterators.ReversingRangeLongSpliterator;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableFilterable;
import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicative;


public interface SequenceM<T> extends Unwrapable, Stream<T>, IterableFilterable<T>,Functor<T>, ExtendedTraversable<T>,
												Foldable<T>,JoolWindowing<T>, 
												JoolManipulation<T>,SequenceMCollectable<T>,
												Seq<T>,  Iterable<T>, Publisher<T>,
												ReactiveStreamsTerminalOperations<T>,
												
												ZippingApplicativable<T>, Unit<T>,
												ConvertableSequence<T>{
	@Override
	public <T> SequenceM<T> unitIterator(Iterator<T> it);
	
	@Override
	public <T> SequenceM<T> unit(T unit);

	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> SequenceM<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return SequenceM.fromStream(Seq.zip(this,other,zipper));
	}

	/**
	default <R> ZippingApplicativeBuilder<T,R,SequenceM<R>> applicatives(){
		return new ZippingApplicativeBuilder<T,R,SequenceM<R>> (this);
	}**/
	
	default <R> SequenceM<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (SequenceM<R>)ZippingApplicativable.super.ap1(ap);
	}
	

	@Override
	<R> R unwrap();

	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * <pre>
	 * {@code 
	 *  SequenceM.of(Arrays.asList(1,2)).flatten();
	 *  
	 *  //stream of (1,  2);		
	 *  
	 * 
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @return Flattened / joined one level
	 */
	<T1> SequenceM<T1> flatten();

	

	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * <pre>
	 * {@code 
	 * 		SequenceM.of(1,2,2)
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
	SequenceM<T> cycle(int times);

	/**
	 * Convert to a Stream with the values infinitely cycled
	 * 
	 * <pre>
	 * {@code 
	 *      SequenceM.of(1).cycle().limit(6).toList());
	 *      //List[1, 1, 1, 1, 1,1]
	 *   }
	 * </pre>
	 * 
	 * @return Stream with values repeated
	 */
	SequenceM<T> cycle();

	/**
	 * Duplicate a Stream, buffers intermediate values, leaders may change
	 * positions so a limit can be safely applied to the leading stream. Not
	 * thread-safe.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Tuple2&lt;SequenceM&lt;Integer&gt;, SequenceM&lt;Integer&gt;&gt; copies = of(1, 2, 3, 4, 5, 6).duplicate();
	 * 	assertTrue(copies.v1.anyMatch(i -&gt; i == 2));
	 * 	assertTrue(copies.v2.anyMatch(i -&gt; i == 2));
	 * 
	 * }
	 * </pre>
	 * 
	 * @return duplicated stream
	 */
	Tuple2<SequenceM<T>, SequenceM<T>> duplicateSequence();

	/**
	 * Triplicates a Stream Buffers intermediate values, leaders may change
	 * positions so a limit can be safely applied to the leading stream. Not
	 * thread-safe.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Tuple3&lt;SequenceM&lt;Tuple3&lt;T1, T2, T3&gt;&gt;, SequenceM&lt;Tuple3&lt;T1, T2, T3&gt;&gt;, SequenceM&lt;Tuple3&lt;T1, T2, T3&gt;&gt;&gt; Tuple3 = sequence.triplicate();
	 * 
	 * }
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	Tuple3<SequenceM<T>, SequenceM<T>, SequenceM<T>> triplicate();

	/**
	 * Makes four copies of a Stream Buffers intermediate values, leaders may
	 * change positions so a limit can be safely applied to the leading stream.
	 * Not thread-safe.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Tuple4&lt;SequenceM&lt;Tuple4&lt;T1, T2, T3, T4&gt;&gt;, SequenceM&lt;Tuple4&lt;T1, T2, T3, T4&gt;&gt;, SequenceM&lt;Tuple4&lt;T1, T2, T3, T4&gt;&gt;, SequenceM&lt;Tuple4&lt;T1, T2, T3, T4&gt;&gt;&gt; quad = sequence
	 * 			.quadruplicate();
	 * 
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Tuple4<SequenceM<T>, SequenceM<T>, SequenceM<T>, SequenceM<T>> quadruplicate();

	/**
	 * Split a Stream at it's head (similar to headAndTail)
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3).splitAtHead()
	 * 
	 *  //Optional[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Tuple2<Optional<T>, SequenceM<T>> splitSequenceAtHead();

	/**
	 * Split at supplied location
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3).splitAt(1)
	 * 
	 *  //SequenceM[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 */
	Tuple2<SequenceM<T>, SequenceM<T>> splitAt(int where);

	/**
	 * Split stream at point where predicate no longer holds
	 * 
	 * <pre>
	 * {@code
	 *   SequenceM.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
	 *   
	 *   //SequenceM[1,2,3] SequenceM[4,5,6]
	 * }
	 * </pre>
	 */
	Tuple2<SequenceM<T>, SequenceM<T>> splitBy(Predicate<T> splitter);

	/**
	 * Partition a Stream into two one a per element basis, based on predicate's
	 * boolean value
	 * 
	 * <pre>
	 * {@code 
	 *  SequenceM.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
	 *  
	 *  //SequenceM[1,3,5], SequenceM[2,4,6]
	 * }
	 *
	 * </pre>
	 */
	Tuple2<SequenceM<T>, SequenceM<T>> partitionSequence(Predicate<T> splitter);

	/**
	 * Convert to a Stream with the result of a reduction operation repeated
	 * specified times
	 * 
	 * <pre>
	 * {@code 
	 *   List<Integer> list = SequenceM.of(1,2,2))
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
	SequenceM<T> cycle(Monoid<T> m, int times);

	

	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	MutableInt count = MutableInt.of(0);
	 * 	SequenceM.of(1, 2, 2).cycleWhile(next -&gt; count++ &lt; 6).collect(Collectors.toList());
	 * 
	 * 	// List(1,2,2,1,2,2)
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	SequenceM<T> cycleWhile(Predicate<? super T> predicate);

	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * 	MutableInt count =MutableInt.of(0);
	 * 		SequenceM.of(1,2,2)
	 * 		 		.cycleUntil(next -> count.get()>6)
	 * 		 		.peek(i-> count.mutate(i->i+1))
	 * 		 		.collect(Collectors.toList());
	 * 
	 * 		//List[1,2,2,1,2,2,1]	
	 * }
	 * 
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	SequenceM<T> cycleUntil(Predicate<? super T> predicate);

	/**
	 * Zip 2 streams into one
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Tuple2&lt;Integer, String&gt;&gt; list = of(1, 2).zip(of(&quot;a&quot;, &quot;b&quot;, &quot;c&quot;, &quot;d&quot;)).toList();
	 * 	// [[1,&quot;a&quot;],[2,&quot;b&quot;]]
	 * }
	 * </pre>
	 * 
	 */
	<U> SequenceM<Tuple2<T, U>> zipStream(Stream<U> other);

	/**
	 * Zip 2 streams into one
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Tuple2&lt;Integer, String&gt;&gt; list = of(1, 2).zip(of(&quot;a&quot;, &quot;b&quot;, &quot;c&quot;, &quot;d&quot;)).toList();
	 * 	// [[1,&quot;a&quot;],[2,&quot;b&quot;]]
	 * }
	 * </pre>
	 * 
	 */
	@Override
	default <U> SequenceM<Tuple2<T, U>> zip(Seq<U> other) {
		return fromStream(JoolManipulation.super.zip(other));
	}

	/**
	 * zip 3 Streams into one
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Tuple3&lt;Integer, Integer, Character&gt;&gt; list = of(1, 2, 3, 4, 5, 6).zip3(of(100, 200, 300, 400), of('a', 'b', 'c')).collect(Collectors.toList());
	 * 
	 * 	// [[1,100,'a'],[2,200,'b'],[3,300,'c']]
	 * }
	 * 
	 * </pre>
	 */
	<S, U> SequenceM<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

	/**
	 * zip 4 Streams into 1
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Tuple4&lt;Integer, Integer, Character, String&gt;&gt; list = of(1, 2, 3, 4, 5, 6).zip4(of(100, 200, 300, 400), of('a', 'b', 'c'), of(&quot;hello&quot;, &quot;world&quot;))
	 * 			.collect(Collectors.toList());
	 * 
	 * }
	 * // [[1,100,'a',&quot;hello&quot;],[2,200,'b',&quot;world&quot;]]
	 * </pre>
	 */
	<T2, T3, T4> SequenceM<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third, Stream<T4> fourth);

	/**
	 * Add an index to the current Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
	 * }
	 * </pre>
	 */
	@Override
	default SequenceM<Tuple2<T, Long>> zipWithIndex() {
		return fromStream(JoolManipulation.super.zipWithIndex());
	}

	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asMonad(Stream.of(1, 2, 3)).zip(asMonad(Optional.of(2)), (a, b) -&gt; Arrays.asList(a, b));
	 * 	// [[1,2]]
	 * }
	 * </pre>
	 * 
	 * @param second
	 *            Monad to zip with
	 * @param zipper
	 *            Zipping function
	 * @return Stream zipping two Monads
	 */
	<S, R> SequenceM<R> zipSequence(SequenceM<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper);

	/**
	 * Zip this SequenceM against any monad type.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = anyM(Stream.of(1, 2, 3)).asSequence().zip(anyM(Optional.of(2)), (a, b) -&gt; Arrays.asList(a, b)).toStream();
	 * 
	 * 	List&lt;Integer&gt; zip = zipped.collect(Collectors.toList()).get(0);
	 * 	assertThat(zip.get(0), equalTo(1));
	 * 	assertThat(zip.get(1), equalTo(2));
	 * }
	 * </pre>
	 * 
	 */
	<S, R> SequenceM<R> zipAnyM(AnyM<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper);

	/**
	 * Zip this Monad with a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asMonad(Stream.of(1, 2, 3)).zip(Stream.of(2, 3, 4), (a, b) -&gt; Arrays.asList(a, b));
	 * 
	 * 	// [[1,2][2,3][3,4]]
	 * }
	 * </pre>
	 * 
	 * @param second
	 *            Stream to zip with
	 * @param zipper
	 *            Zip funciton
	 * @return This monad zipped with a Stream
	 */
	<S, R> SequenceM<R> zipStream(BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second, BiFunction<? super T, ? super S, ? extends R> zipper);

	/**
	 * Create a sliding view over this Sequence
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = SequenceM.of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());
	 * 
	 * 	assertThat(list.get(0), hasItems(1, 2));
	 * 	assertThat(list.get(1), hasItems(2, 3));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param windowSize
	 *            Size of sliding window
	 * @return SequenceM with sliding view
	 */
	SequenceM<ListX<T>> sliding(int windowSize);

	/**
	 * Create a sliding view over this Sequence
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = SequenceM.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());
	 * 
	 * 	assertThat(list.get(0), hasItems(1, 2, 3));
	 * 	assertThat(list.get(1), hasItems(3, 4, 5));
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
	SequenceM<ListX<T>> sliding(int windowSize, int increment);

	/**
	 * Group elements in a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = SequenceM.of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
	 * 
	 * 	assertThat(list.get(0), hasItems(1, 2, 3));
	 * 	assertThat(list.get(1), hasItems(4, 5, 6));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param groupSize
	 *            Size of each Group
	 * @return Stream with elements grouped by size
	 */
	SequenceM<ListX<T>> grouped(int groupSize);

	default <K, A, D> SequenceM<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
		return fromStream(JoolManipulation.super.grouped(classifier, downstream));
	}

	public default <K> SequenceM<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
		return fromStream(JoolManipulation.super.grouped(classifier));
	}

	/**
	 * Use classifier function to group elements in this Sequence into a Map
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Map&lt;Integer, List&lt;Integer&gt;&gt; map1 = of(1, 2, 3, 4).groupBy(i -&gt; i % 2);
	 * 	assertEquals(asList(2, 4), map1.get(0));
	 * 	assertEquals(asList(1, 3), map1.get(1));
	 * 	assertEquals(2, map1.size());
	 * 
	 * }
	 * 
	 * </pre>
	 */
	@Override
	default <K> MapX<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
		return MapX.fromMap(JoolManipulation.super.groupBy(classifier));
	}

	/*
	 * Return the distinct Stream of elements
	 * 
	 * <pre> {@code List<Integer> list = SequenceM.of(1,2,2,2,5,6) .distinct()
	 * .collect(Collectors.toList()); }</pre>
	 */
	SequenceM<T> distinct();

	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),SequenceM.of("a", "b", "c")
	 * 													.scanLeft(Reducers.toString("")).toList());
	 *         
	 *         }
	 * </pre>
	 * 
	 * @param monoid
	 * @return
	 */
	SequenceM<T> scanLeft(Monoid<T> monoid);

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
	<U> SequenceM<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);

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
	SequenceM<T> scanRight(Monoid<T> monoid);

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
	<U> SequenceM<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);

	/**
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
	 * </pre>
	 * 
	 */
	SequenceM<T> sorted();

	/**
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	 * }
	 * </pre>
	 * 
	 * @param c
	 *            Compartor to sort with
	 * @return Sorted Stream
	 */
	SequenceM<T> sorted(Comparator<? super T> c);

	/**
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
	 * </pre>
	 * 
	 * 
	 * 
	 * @param num
	 *            Number of elemenets to skip
	 * @return Stream with specified number of elements skipped
	 */
	SequenceM<T> skip(long num);

	/**
	 * 
	 * SkipWhile drops elements from the Stream while the predicate holds, once
	 * the predicte returns true all subsequent elements are included *
	 * 
	 * <pre>
	 * {@code
	 * assertThat(SequenceM.of(4,3,6,7).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	 * }
	 * </pre>
	 * 
	 * @param p
	 *            Predicate to skip while true
	 * @return Stream with elements skipped while predicate holds
	 */
	SequenceM<T> skipWhile(Predicate<? super T> p);

	/**
	 * Drop elements from the Stream until the predicate returns true, after
	 * which all elements are included
	 * 
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
	 * </pre>
	 * 
	 * 
	 * @param p
	 *            Predicate to skip until true
	 * @return Stream with elements skipped until predicate holds
	 */
	SequenceM<T> skipUntil(Predicate<? super T> p);

	default SequenceM<T> skipUntilClosed(Predicate<? super T> p) {
		return fromStream(JoolManipulation.super.skipUntilClosed(p));
	}

	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
	 * </pre>
	 * 
	 * @param num
	 *            Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	SequenceM<T> limit(long num);

	/**
	 * Take elements from the Stream while the predicate holds, once the
	 * predicate returns false all subsequent elements are excluded
	 * 
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
	 * </pre>
	 * 
	 * @param p
	 *            Limit while predicate is true
	 * @return Stream with limited elements
	 */
	SequenceM<T> limitWhile(Predicate<? super T> p);

	/**
	 * Take elements from the Stream until the predicate returns true, after
	 * which all elements are excluded.
	 * 
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
	 * </pre>
	 * 
	 * @param p
	 *            Limit until predicate is true
	 * @return Stream with limited elements
	 */
	SequenceM<T> limitUntil(Predicate<? super T> p);

	/**
	 * @param p
	 * @return
	 */
	@Override
	default SequenceM<T> limitUntilClosed(Predicate<? super T> p) {
		return fromStream(JoolManipulation.super.limitUntilClosed(p));
	}

	/**
	 * @return Does nothing SequenceM is for Sequential Streams
	 * 
	 */
	SequenceM<T> parallel();

	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	 * }
	 * </pre>
	 * 
	 * @param c
	 *            Predicate to check if all match
	 */
	boolean allMatch(Predicate<? super T> c);

	/**
	 * True if a single element matches when Monad converted to a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	 * }
	 * </pre>
	 * 
	 * @param c
	 *            Predicate to check if any match
	 */
	boolean anyMatch(Predicate<? super T> c);

	/**
	 * Check that there are specified number of matches of predicate in the
	 * Stream
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(SequenceM.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	 * }
	 * </pre>
	 * 
	 */
	boolean xMatch(int num, Predicate<? super T> c);

	/*
	 * <pre> {@code assertThat(of(1,2,3,4,5).noneMatch(it->
	 * it==5000),equalTo(true));
	 * 
	 * } </pre>
	 */
	boolean noneMatch(Predicate<? super T> c);

	/**
	 * <pre>
	 * {@code
	 *  assertEquals("123".length(),SequenceM.of(1, 2, 3).join().length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	String join();

	/**
	 * <pre>
	 * {@code
	 * assertEquals("1, 2, 3".length(), SequenceM.of(1, 2, 3).join(", ").length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	String join(String sep);

	/**
	 * <pre>
	 * {@code 
	 * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	String join(String sep, String start, String end);

	/**
	 * Extract the minimum as determined by supplied function
	 * 
	 */
	<C extends Comparable<? super C>> Optional<T> minBy(Function<? super T, ? extends C> f);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	Optional<T> min(Comparator<? super T> comparator);

	/**
	 * Extract the maximum as determined by the supplied function
	 * 
	 */
	<C extends Comparable<? super C>> Optional<T> maxBy(Function<? super T, ? extends C> f);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	Optional<T> max(Comparator<? super T> comparator);

	/**
	 * extract head and tail together, where head is expected to be present
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SequenceM&lt;String&gt; helloWorld = SequenceM.of(&quot;hello&quot;, &quot;world&quot;, &quot;last&quot;);
	 * 	HeadAndTail&lt;String&gt; headAndTail = helloWorld.headAndTail();
	 * 	String head = headAndTail.head();
	 * 	assertThat(head, equalTo(&quot;hello&quot;));
	 * 
	 * 	SequenceM&lt;String&gt; tail = headAndTail.tail();
	 * 	assertThat(tail.headAndTail().head(), equalTo(&quot;world&quot;));
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	HeadAndTail<T> headAndTail();


	
	/**
	 * @return First matching element in sequential order
	 * 
	 *         <pre>
	 * {@code
	 * SequenceM.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
	 * 
	 * //3
	 * }
	 * </pre>
	 * 
	 *         (deterministic)
	 * 
	 */
	Optional<T> findFirst();

	/**
	 * @return first matching element, but order is not guaranteed
	 * 
	 *         <pre>
	 * {@code
	 * SequenceM.of(1,2,3,4,5).filter(it -> it <3).findAny().get();
	 * 
	 * //3
	 * }
	 * </pre>
	 * 
	 * 
	 *         (non-deterministic)
	 */
	Optional<T> findAny();


	
	 /**
	  * Performs a map operation that can call a recursive method without running out of stack space
	  * <pre>
	  * {@code
	  * SequenceM.of(10,20,30,40)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println); 
				 
		Trampoline<Long> fibonacci(int i){
			return fibonacci(i,1,0);
		}
		Trampoline<Long> fibonacci(int n, long a, long b) {
	    	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
		}		 
				 
	  * 55
		6765
		832040
		102334155
	  * 
	  * 
	  * SequenceM.of(10_000,200_000,3_000_000,40_000_000)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
				 
				 
	  * completes successfully
	  * }
	  * 
	 * @param mapper
	 * @return
	 */
	default <R> SequenceM<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		
		 return  map(in-> mapper.apply(in).result());
	 }

	/**
	 * Attempt to map this Sequence to the same type as the supplied Monoid
	 * (Reducer) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
	 * 
	 * //4
	 * }
	 * </pre>
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	<R> R mapReduce(Reducer<R> reducer);

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid, using
	 * supplied function Then use Monoid to reduce values
	 * 
	 * <pre>
	 *  {@code
	 *  SequenceM.of("one","two","three","four")
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
	<R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer);




	/**
	 * <pre>
	 * {@code 
	 * SequenceM.of("hello","2","world","4").reduce(Reducers.toString(","));
	 * 
	 * //hello,2,world,4
	 * }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	T reduce(Monoid<T> reducer);

	/*
	 * <pre> {@code assertThat(SequenceM.of(1,2,3,4,5).map(it -> it*100).reduce(
	 * (acc,next) -> acc+next).get(),equalTo(1500)); } </pre>
	 */
	Optional<T> reduce(BinaryOperator<T> accumulator);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BinaryOperator)
	 */
	T reduce(T identity, BinaryOperator<T> accumulator);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);

	/**
	 * Reduce with multiple reducers in parallel NB if this Monad is an Optional
	 * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
	 * was one value To reduce over the values on the list, called
	 * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Monoid&lt;Integer&gt; sum = Monoid.of(0, (a, b) -&gt; a + b);
	 * 	Monoid&lt;Integer&gt; mult = Monoid.of(1, (a, b) -&gt; a * b);
	 * 	List&lt;Integer&gt; result = SequenceM.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).stream());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(10, 24)));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param reducers
	 * @return
	 */
	ListX<T> reduce(Stream<? extends Monoid<T>> reducers);

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
	 * 		List<Integer> result = SequenceM.of(1,2,3,4))
	 * 										.reduce(Arrays.asList(sum,mult) );
	 * 				
	 * 		 
	 * 		assertThat(result,equalTo(Arrays.asList(10,24)));
	 * 
	 * }
	 * 
	 * @param reducers
	 * @return
	 */
	ListX<T> reduce(Iterable<? extends Monoid<T>> reducers);

	/**
	 * 
	 * 
	 <pre>
	 * 		{@code
	 * 		SequenceM.of("a","b","c").foldLeft(Reducers.toString(""));
	 *        
	 *         // "abc"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	T foldLeft(Monoid<T> reducer);

	/**
	 * foldLeft : immutable reduction from left to right
	 * 
	 * <pre>
	 * {@code 
	 * 
	 * assertTrue(SequenceM.of("a", "b", "c").foldLeft("", String::concat).equals("abc"));
	 * }
	 * </pre>
	 */
	T foldLeft(T identity, BinaryOperator<T> accumulator);

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of(1,2,3).foldLeftMapToType(Reducers.toString(""));
	 *        
	 *         // "123"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	<T> T foldLeftMapToType(Reducer<T> reducer);

	/**
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of("a","b","c").foldRight(Reducers.toString(""));
	 *        
	 *         // "cab"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	T foldRight(Monoid<T> reducer);

	/**
	 * Immutable reduction from right to left
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(SequenceM.of("a","b","c").foldRight("", String::concat).equals("cba"));
	 * }
	 * </pre>
	 * 
	 * @param identity
	 * @param accumulator
	 * @return
	 */
	public T foldRight(T identity, BinaryOperator<T> accumulator);

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of(1,2,3).foldRightMapToType(Reducers.toString(""));
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
	public <T> T foldRightMapToType(Reducer<T> reducer);

	/**
	 * <pre>
	 * {@code 
	 * 	Streamable<Integer> repeat = SequenceM.of(1,2,3,4,5,6)
	 * 												.map(i->i*2)
	 * 												.toStreamable();
	 * 		
	 * 		repeat.sequenceM().toList(); //Arrays.asList(2,4,6,8,10,12));
	 * 		repeat.sequenceM().toList() //Arrays.asList(2,4,6,8,10,12));
	 * 
	 * }
	 * 
	 * @return Lazily Convert to a repeatable Streamable
	 * 
	 */
	public Streamable<T> toStreamable();

	/**
	 * @return This Stream converted to a set
	 */
	public Set<T> toSet();

	/**
	 * @return this Stream converted to a list
	 */
	public List<T> toList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#toCollection(java.util.function.Supplier)
	 */
	public <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory);

	/**
	 * Convert this SequenceM into a Stream
	 * 
	 * @return calls to stream() but more flexible on type for inferencing
	 *         purposes.
	 */
	public <T> Stream<T> toStream();

	/**
	 * Convert this SequenceM into a Stream
	 */
	public SequenceM<T> stream();

	/**
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(SequenceM.of(1,2,3,4).startsWith(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * 
	 * @param iterable
	 * @return True if Monad starts with Iterable sequence of data
	 */
	boolean startsWith(Iterable<T> iterable);

	/**
	 * <pre>
	 * {@code assertTrue(SequenceM.of(1,2,3,4).startsWith(Arrays.asList(1,2,3).iterator())) }
	 * </pre>
	 * 
	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	boolean startsWith(Iterator<T> iterator);

	/**
	 * @return this SequenceM converted to AnyM format
	 */
	public AnyM<T> anyM();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#map(java.util.function.Function)
	 */
	<R> SequenceM<R> map(Function<? super T, ? extends R> fn);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#peek(java.util.function.Consumer)
	 */
	SequenceM<T> peek(Consumer<? super T> c);

	/**
	 * flatMap operation
	 * 
	 * <pre>
	 * {@code
	 * 	assertThat(SequenceM.of(1,2)
	 * 						.flatMap(i -> asList(i, -i).stream())
	 * 						.toList(),equalTo(asList(1, -1, 2, -2)));		
	 *  
	 * }
	 * </pre>
	 * 
	 * @param fn
	 *            to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	<R> SequenceM<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn);

	/**
	 * Allows flatMap return type to be any Monad type
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(1,2,3)).flatMapAnyM(i-> anyM(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param fn
	 *            to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	<R> SequenceM<R> flatMapAnyM(Function<? super T, AnyM<? extends R>> fn);

	/**
	 * FlatMap where the result is a Collection, flattens the resultant
	 * collections into the host SequenceM
	 * 
	 * <pre>
	 * {@code 
	 * 	SequenceM.of(1,2)
	 * 			.flatMap(i -> asList(i, -i))
	 *          .toList();
	 *          
	 *   //1,-1,2,-2       
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	<R> SequenceM<R> flatMapCollection(Function<? super T, Collection<? extends R>> fn);

	/**
	 * flatMap operation
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(1,2,3)
	 *                      .flatMapStream(i->IntStream.of(i))
	 *                      .toList(),equalTo(Arrays.asList(1,2,3)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param fn
	 *            to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	<R> SequenceM<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn);

	/**
	 * flatMap to optional - will result in null values being removed
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(1,2,3,null)
	 *                      .flatMapOptional(Optional::ofNullable)
	 * 			      			.collect(Collectors.toList()),
	 * 			      			equalTo(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	<R> SequenceM<R> flatMapOptional(Function<? super T, Optional<? extends R>> fn);

	/**
	 * flatMap to CompletableFuture - will block until Future complete, although
	 * (for non-blocking behaviour use AnyM wrapping CompletableFuture and
	 * flatMap to Stream there)
	 * 
	 * <pre>
	 *  {@code
	 *  	assertThat(SequenceM.of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
	 * 				  								.collect(Collectors.toList()),
	 * 				  								equalTo(Arrays.asList(3,4,5)));
	 *  }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	<R> SequenceM<R> flatMapCompletableFuture(Function<? super T, CompletableFuture<? extends R>> fn);

	/**
	 * Perform a flatMap operation where the result will be a flattened stream
	 * of Characters from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Character&gt; result = SequenceM.of(&quot;input.file&quot;).flatMapCharSequence(i -&gt; &quot;hello world&quot;).toList();
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList('h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd')));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<Character> flatMapCharSequence(Function<? super T, CharSequence> fn);

	/**
	 * Perform a flatMap operation where the result will be a flattened stream
	 * of Strings from the text loaded from the supplied files.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(&quot;input.file&quot;).map(getClass().getClassLoader()::getResource).peek(System.out::println).map(URL::getFile)
	 * 			.flatMapFile(File::new).toList();
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;hello&quot;, &quot;world&quot;)));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<String> flatMapFile(Function<? super T, File> fn);

	/**
	 * Perform a flatMap operation where the result will be a flattened stream
	 * of Strings from the text loaded from the supplied URLs
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(&quot;input.file&quot;).flatMapURL(getClass().getClassLoader()::getResource).toList();
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;hello&quot;, &quot;world&quot;)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<String> flatMapURL(Function<? super T, URL> fn);

	/**
	 * Perform a flatMap operation where the result will be a flattened stream
	 * of Strings from the text loaded from the supplied BufferedReaders
	 * 
	 * <pre>
	 * List&lt;String&gt; result = SequenceM.of(&quot;input.file&quot;).map(getClass().getClassLoader()::getResourceAsStream).map(InputStreamReader::new)
	 * 		.liftAndBindBufferedReader(BufferedReader::new).toList();
	 * 
	 * assertThat(result, equalTo(Arrays.asList(&quot;hello&quot;, &quot;world&quot;)));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<String> flatMapBufferedReader(Function<? super T, BufferedReader> fn);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#filter(java.util.function.Predicate)
	 */
	SequenceM<T> filter(Predicate<? super T> fn);

	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#sequential()
	 */
	SequenceM<T> sequential() ;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.BaseStream#unordered()
	 */
	SequenceM<T> unordered();

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) SequenceM.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	SequenceM<T> intersperse(T value);

	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) SequenceM.of(1, "a", 2, "b",3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	<U> SequenceM<U> ofType(Class<U> type);

	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException SequenceM.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	<U> SequenceM<U> cast(Class<U> type);

	/**
	 * Lazily converts this SequenceM into a Collection. This does not trigger
	 * the Stream. E.g. Collection is not thread safe on the first iteration.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Collection&lt;Integer&gt; col = SequenceM.of(1, 2, 3, 4, 5).peek(System.out::println).toLazyCollection();
	 * 
	 * 	col.forEach(System.out::println);
	 * }
	 * 
	 * // Will print out &quot;first!&quot; before anything else
	 * </pre>
	 * 
	 * @return
	 */
	CollectionX<T> toLazyCollection();

	/**
	 * Lazily converts this SequenceM into a Collection. This does not trigger
	 * the Stream. E.g.
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Collection&lt;Integer&gt; col = SequenceM.of(1, 2, 3, 4, 5).peek(System.out::println).toConcurrentLazyCollection();
	 * 
	 * 	col.forEach(System.out::println);
	 * }
	 * 
	 * // Will print out &quot;first!&quot; before anything else
	 * </pre>
	 * 
	 * @return
	 */
	CollectionX<T> toConcurrentLazyCollection();

	/**
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Streamable&lt;Integer&gt; repeat = SequenceM.of(1, 2, 3, 4, 5, 6).map(i -&gt; i + 2).toConcurrentLazyStreamable();
	 * 
	 * 	assertThat(repeat.sequenceM().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
	 * 	assertThat(repeat.sequenceM().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
	 * }
	 * </pre>
	 * 
	 * @return Streamable that replay this SequenceM, populated lazily and can
	 *         be populated across threads
	 */
	public Streamable<T> toConcurrentLazyStreamable();

	/*
	 * Potentially efficient Sequence reversal. Is efficient if
	 * 
	 * - Sequence created via a range - Sequence created via a List - Sequence
	 * created via an Array / var args
	 * 
	 * Otherwise Sequence collected into a Collection prior to reversal
	 * 
	 * <pre> {@code assertThat( of(1, 2, 3).reverse().toList(),
	 * equalTo(asList(3, 2, 1))); } </pre>
	 */
	public SequenceM<T> reverse();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.BaseStream#onClose(java.lang.Runnable)
	 */
	@Override
	public SequenceM<T> onClose(Runnable closeHandler);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#shuffle()
	 */
	public SequenceM<T> shuffle();

	/**
	 * Append Stream to this SequenceM
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(1, 2, 3).appendStream(SequenceM.of(100, 200, 300)).map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;, &quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            to append
	 * @return SequenceM with Stream appended
	 */
	public SequenceM<T> appendStream(Stream<T> stream);

	/**
	 * Prepend Stream to this SequenceM
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(1, 2, 3).prependStream(of(100, 200, 300)).map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;, &quot;1!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            to Prepend
	 * @return SequenceM with Stream prepended
	 */
	SequenceM<T> prependStream(Stream<T> stream);

	/**
	 * Append values to the end of this SequenceM
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(1, 2, 3).append(100, 200, 300).map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;, &quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param values
	 *            to append
	 * @return SequenceM with appended values
	 */
	SequenceM<T> append(T... values);

	/**
	 * Prepend given values to the start of the Stream
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	SequenceM.of(1,2,3)
	 * 									 .prepend(100,200,300)
	 * 										 .map(it ->it+"!!")
	 * 										 .collect(Collectors.toList());
	 * 
	 * 			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * }
	 * @param values to prepend
	 * @return SequenceM with values prepended
	 */
	SequenceM<T> prepend(T... values);

	/**
	 * Insert data into a stream at given position
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(1, 2, 3).insertAt(1, 100, 200, 300).map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param pos
	 *            to insert data at
	 * @param values
	 *            to insert
	 * @return Stream with new data inserted
	 */
	SequenceM<T> insertAt(int pos, T... values);

	/**
	 * Delete elements between given indexes in a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(1, 2, 3, 4, 5, 6).deleteBetween(2, 4).map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;2!!&quot;, &quot;5!!&quot;, &quot;6!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param start
	 *            index
	 * @param end
	 *            index
	 * @return Stream with elements removed
	 */
	SequenceM<T> deleteBetween(int start, int end);

	/**
	 * Insert a Stream into the middle of this stream at the specified position
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = SequenceM.of(1, 2, 3).insertStreamAt(1, of(100, 200, 300)).map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param pos
	 *            to insert Stream at
	 * @param stream
	 *            to insert
	 * @return newly conjoined SequenceM
	 */
	SequenceM<T> insertStreamAt(int pos, Stream<T> stream);

	/**
	 * Access asynchronous terminal operations (each returns a Future)
	 * 
	 * @param exec
	 *            Executor to use for Stream execution
	 * @return Async Future Terminal Operations
	 */
	FutureOperations<T> futureOperations(Executor exec);

	/**
	 * <pre>
	 * {@code
	 *  assertTrue(SequenceM.of(1,2,3,4,5,6)
	 * 				.endsWith(Arrays.asList(5,6)));
	 * 
	 * }
	 * 
	 * @param iterable Values to check
	 * @return true if SequenceM ends with values in the supplied iterable
	 */
	boolean endsWith(Iterable<T> iterable);

	/**
	 * <pre>
	 * {@code
	 * assertTrue(SequenceM.of(1,2,3,4,5,6)
	 * 				.endsWith(Stream.of(5,6))); 
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            Values to check
	 * @return true if SequenceM endswith values in the supplied Stream
	 */
	boolean endsWith(Stream<T> stream);

	/**
	 * Skip all elements until specified time period has passed
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Integer&gt; result = SequenceM.of(1, 2, 3, 4, 5, 6).peek(i -&gt; sleep(i * 100)).skip(1000, TimeUnit.MILLISECONDS).toList();
	 * 
	 * 	// [4,5,6]
	 * 
	 * }
	 * </pre>
	 * 
	 * @param time
	 *            Length of time
	 * @param unit
	 *            Time unit
	 * @return SequenceM that skips all elements until time period has elapsed
	 */
	SequenceM<T> skip(long time, final TimeUnit unit);

	/**
	 * Return all elements until specified time period has elapsed
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Integer&gt; result = SequenceM.of(1, 2, 3, 4, 5, 6).peek(i -&gt; sleep(i * 100)).limit(1000, TimeUnit.MILLISECONDS).toList();
	 * 
	 * 	// [1,2,3,4]
	 * }
	 * </pre>
	 * 
	 * @param time
	 *            Length of time
	 * @param unit
	 *            Time unit
	 * @return SequenceM that returns all elements until time period has elapsed
	 */
	SequenceM<T> limit(long time, final TimeUnit unit);

	/**
	 * assertThat(SequenceM.of(1,2,3,4,5) .skipLast(2)
	 * .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	 * 
	 * @param num
	 * @return
	 */
	SequenceM<T> skipLast(int num);

	/**
	 * Limit results to the last x elements in a SequenceM
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(1,2,3,4,5)
	 * 							.limitLast(2)
	 * 							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	 * 
	 * }
	 * 
	 * @param num of elements to return (last elements)
	 * @return SequenceM limited to last num elements
	 */
	SequenceM<T> limitLast(int num);

	/**
	 * Turns this SequenceM into a HotStream, a connectable Stream, being executed on a thread on the 
	 * supplied executor, that is producing data. Note this method creates a HotStream that starts emitting data
	 * immediately. For a hotStream that waits until the first user streams connect @see {@link SequenceM#primedHotStream(Executor)}.
	 * The generated HotStream is not pausable, for a pausable HotStream @see {@link SequenceM#pausableHotStream(Executor)}.
	 * Turns this SequenceM into a HotStream, a connectable Stream, being
	 * executed on a thread on the supplied executor, that is producing data
	 * 
	 * <pre>
	 * {@code 
	 *  HotStream<Integer> ints = SequenceM.range(0,Integer.MAX_VALUE)
	 * 											.hotStream(exec)
	 * 											
	 * 		
	 * 		ints.connect().forEach(System.out::println);									
	 *  //print out all the ints
	 *  //multiple consumers are possible, so other Streams can connect on different Threads
	 *  
	 * }
	 * </pre>
	 * 
	 * @param e
	 *            Executor to execute this SequenceM on
	 * @return a Connectable HotStream
	 */
	HotStream<T> hotStream(Executor e);

	/**
	 * Return a HotStream that will start emitting data when the first connecting Stream connects.
	 * Note this method creates a HotStream that starts emitting data only when the first connecting Stream connects.
	 *  For a hotStream that starts to output data immediately @see {@link SequenceM#hotStream(Executor)}.
	 * The generated HotStream is not pausable, for a pausable HotStream @see {@link SequenceM#primedPausableHotStream(Executor)}.
	 * <pre>
	  * <pre>
	 * {@code 
	 *  HotStream<Integer> ints = SequenceM.range(0,Integer.MAX_VALUE)
											.hotStream(exec)
											
		
		ints.connect().forEach(System.out::println);									
	 *  //print out all the ints - starting when connect is called.
	 *  //multiple consumers are possible, so other Streams can connect on different Threads
	 *  
	 * }
	 * </pre>
	 * @param e
	 * @return
	 */
	HotStream<T> primedHotStream(Executor e);
	/**
	 * Turns this SequenceM into a HotStream, a connectable & pausable Stream, being executed on a thread on the 
	 * supplied executor, that is producing data. Note this method creates a HotStream that starts emitting data
	 * immediately. For a hotStream that waits until the first user streams connect @see {@link SequenceM#primedPausableHotStream(Executor)}.
	 * The generated HotStream is pausable, for a unpausable HotStream (slightly faster execution) @see {@link SequenceM#hotStream(Executor)}.
	 * <pre>
	 * {@code 
	 *  HotStream<Integer> ints = SequenceM.range(0,Integer.MAX_VALUE)
											.hotStream(exec)
											
		
		ints.connect().forEach(System.out::println);
		
		ints.pause(); //on a separate thread pause the generating Stream
											
	 *  //print out all the ints
	 *  //multiple consumers are possible, so other Streams can connect on different Threads
	 *  
	 * }
	 * </pre>
	 * @param e Executor to execute this SequenceM on
	 * @return a Connectable HotStream
	 */
	PausableHotStream<T> pausableHotStream(Executor e);
	/**
	 * Return a pausable HotStream that will start emitting data when the first connecting Stream connects.
	 * Note this method creates a HotStream that starts emitting data only when the first connecting Stream connects.
	 *  For a hotStream that starts to output data immediately @see {@link SequenceM#pausableHotStream(Executor)}.
	 * The generated HotStream is pausable, for a unpausable HotStream @see {@link SequenceM#primedHotStream(Executor)}.
	 * <pre>
	  * <pre>
	 * {@code 
	 *  HotStream<Integer> ints = SequenceM.range(0,Integer.MAX_VALUE)
											.hotStream(exec)
											
		
		ints.connect().forEach(System.out::println);									
	 *  //print out all the ints - starting when connect is called.
	 *  //multiple consumers are possible, so other Streams can connect on different Threads
	 *  
	 * }
	 * </pre>
	 * @param e
	 * @return
	 */
	PausableHotStream<T> primedPausableHotStream(Executor e);
	

	/**
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(1,2,3,4)
	 * 					.map(u->{throw new RuntimeException();})
	 * 					.recover(e->"hello")
	 * 					.firstValue(),equalTo("hello"));
	 * }
	 * </pre>
	 * 
	 * @return first value in this Stream
	 */
	T firstValue();

	/**
	 * <pre>
	 * {@code 
	 *    
	 *    //1
	 *    SequenceM.of(1).single(); 
	 *    
	 *    //UnsupportedOperationException
	 *    SequenceM.of().single();
	 *     
	 *     //UnsupportedOperationException
	 *    SequenceM.of(1,2,3).single();
	 * }
	 * </pre>
	 * 
	 * @return a single value or an UnsupportedOperationException if 0/1 values
	 *         in this Stream
	 */
	default T single() {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			T result = it.next();
			if (!it.hasNext())
				return result;
		}
		throw new UnsupportedOperationException("single only works for Streams with a single value");

	}

	default T single(Predicate<? super T> predicate) {
		return this.filter(predicate).single();

	}

	/**
	 * <pre>
	 * {@code 
	 *    
	 *    //Optional[1]
	 *    SequenceM.of(1).singleOptional(); 
	 *    
	 *    //Optional.empty
	 *    SequenceM.of().singleOpional();
	 *     
	 *     //Optional.empty
	 *    SequenceM.of(1,2,3).singleOptional();
	 * }
	 * </pre>
	 * 
	 * @return An Optional with single value if this Stream has exactly one
	 *         element, otherwise Optional Empty
	 */
	default Optional<T> singleOptional() {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			T result = it.next();
			if (!it.hasNext())
				return Optional.of(result);
		}
		return Optional.empty();

	}

	/**
	 * Return the elementAt index or Optional.empty
	 * 
	 * <pre>
	 * {@code
	 * 	assertThat(SequenceM.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
	 * }
	 * </pre>
	 * 
	 * @param index
	 *            to extract element from
	 * @return elementAt index
	 */
	default Optional<T> get(long index) {
		return this.zipWithIndex().filter(t -> t.v2 == index).findFirst().map(t -> t.v1());
	}

	/**
	 * Gets the element at index, and returns a Tuple containing the element (it
	 * must be present) and a lazy copy of the Sequence for further processing.
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3,4,5).get(2).v1
	 * //3
	 * }
	 * </pre>
	 * 
	 * @param index
	 *            to extract element from
	 * @return Element and Sequence
	 */
	default Tuple2<T, SequenceM<T>> elementAt(long index) {
		Tuple2<SequenceM<T>, SequenceM<T>> tuple = this.duplicateSequence();
		return tuple.map1(s -> s.zipWithIndex().filter(t -> t.v2 == index).findFirst().map(t -> t.v1()).get());
	}

	/**
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3,4,5)
	 * 				 .elapsed()
	 * 				 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @return Sequence that adds the time between elements in millis to each
	 *         element
	 */
	default SequenceM<Tuple2<T, Long>> elapsed() {
		AtomicLong last = new AtomicLong(System.currentTimeMillis());

		return zip(SequenceM.generate(() -> {
			long now = System.currentTimeMillis();

			long result = now - last.get();
			last.set(now);
			return result;
		}));
	}

	/**
	 * <pre>
	 * {@code
	 *    SequenceM.of(1,2,3,4,5)
	 * 				   .timestamp()
	 * 				   .forEach(System.out::println)
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @return Sequence that adds a timestamp to each element
	 */
	default SequenceM<Tuple2<T, Long>> timestamp() {
		return zip(SequenceM.generate(() -> System.currentTimeMillis()));
	}

	/**
	 * Create a subscriber that can listen to Reactive Streams (simple-react,
	 * RxJava AkkaStreams, Kontraktor, QuadarStreams etc)
	 * 
	 * <pre>
	 * {@code
	 * CyclopsSubscriber<Integer> sub = SequenceM.subscriber();
	 * 		SequenceM.of(1,2,3).subscribe(sub);
	 * 		sub.sequenceM().forEach(System.out::println);
	 * 		
	 * 		  1 
	 * 		  2
	 * 		  3
	 * }
	 * 
	 * </pre>
	 * 
	 * @return A reactive-streams Subscriber
	 */
	public static <T> CyclopsSubscriber<T> subscriber() {
		return ReactiveStreamsLoader.subscriber.get().subscribe();
	}

	public static <T> SequenceM<T> empty(){
		return fromStream(Stream.empty());
	}
	

	/**
	 * Create an efficiently reversable Sequence from the provided elements
	 * 
	 * @param elements
	 *            To Construct sequence from
	 * @return
	 */
	@SafeVarargs
	public static <T> SequenceM<T> of(T... elements) {
		ReversingArraySpliterator array = new ReversingArraySpliterator<T>(elements, false, 0);
		return StreamUtils.sequenceM(StreamSupport.stream(array, false),Optional.ofNullable(array));

	}

	/**
	 * Construct a Reveresed Sequence from the provided elements Can be reversed
	 * (again) efficiently
	 * 
	 * @param elements
	 *            To Construct sequence from
	 * @return
	 */
	@SafeVarargs
	public static <T> SequenceM<T> reversedOf(T... elements) {
		ReversingArraySpliterator array = new ReversingArraySpliterator<T>(elements, false, 0).invert();
		return StreamUtils.sequenceM(StreamSupport.stream(array, false),Optional.ofNullable(array));
		

	}

	/**
	 * Construct a Reveresed Sequence from the provided elements Can be reversed
	 * (again) efficiently
	 * 
	 * @param elements
	 *            To Construct sequence from
	 * @return
	 */
	public static <T> SequenceM<T> reversedListOf(List<T> elements) {
		Objects.requireNonNull(elements);
		ReversingListSpliterator list = new ReversingListSpliterator<T>(elements, false).invert();
		return StreamUtils.sequenceM(StreamSupport.stream(list, false),Optional.ofNullable(list));
		

	}

	/**
	 * Create an efficiently reversable Sequence that produces the integers
	 * between start and end
	 * 
	 * @param start
	 *            Number of range to start from
	 * @param end
	 *            Number for range to end at
	 * @return Range SequenceM
	 */
	public static SequenceM<Integer> range(int start, int end) {
		ReversingRangeIntSpliterator range = new ReversingRangeIntSpliterator(start, end, false);
		return StreamUtils.sequenceM(StreamSupport.stream(range, false),Optional.ofNullable(range));

	}

	/**
	 * Create an efficiently reversable Sequence that produces the integers
	 * between start and end
	 * 
	 * @param start
	 *            Number of range to start from
	 * @param end
	 *            Number for range to end at
	 * @return Range SequenceM
	 */
	public static SequenceM<Long> rangeLong(long start, long end) {
		ReversingRangeLongSpliterator range = new ReversingRangeLongSpliterator(start, end, false);
		return StreamUtils.sequenceM(StreamSupport.stream(range, false),Optional.ofNullable(range));

	}

	/**
	 * Construct a Sequence from a Stream
	 * 
	 * @param stream
	 *            Stream to construct Sequence from
	 * @return
	 */
	public static <T> SequenceM<T> fromStream(Stream<T> stream) {
		Objects.requireNonNull(stream);
		if (stream instanceof SequenceM)
			return (SequenceM) stream;
		return StreamUtils.sequenceM(stream,Optional.empty());
	}

	/**
	 * Construct a Sequence from a Stream
	 * 
	 * @param stream
	 *            Stream to construct Sequence from
	 * @return
	 */
	public static SequenceM<Integer> fromIntStream(IntStream stream) {
		Objects.requireNonNull(stream);
		return StreamUtils.sequenceM(stream.boxed(),Optional.empty());
		
	}

	/**
	 * Construct a Sequence from a Stream
	 * 
	 * @param stream
	 *            Stream to construct Sequence from
	 * @return
	 */
	public static SequenceM<Long> fromLongStream(LongStream stream) {
		Objects.requireNonNull(stream);
		return StreamUtils.sequenceM(stream.boxed(),Optional.empty());
	}

	/**
	 * Construct a Sequence from a Stream
	 * 
	 * @param stream
	 *            Stream to construct Sequence from
	 * @return
	 */
	public static SequenceM<Double> fromDoubleStream(DoubleStream stream) {
		Objects.requireNonNull(stream);
		return StreamUtils.sequenceM(stream.boxed(),Optional.empty());
	}

	/**
	 * Construct a Sequence from a List (prefer this method if the source is a
	 * list, as it allows more efficient Stream reversal).
	 * 
	 * @param iterable
	 *            to construct Sequence from
	 * @return SequenceM
	 */
	public static <T> SequenceM<T> fromList(List<T> list) {
		Objects.requireNonNull(list);
		ReversingListSpliterator array = new ReversingListSpliterator<T>(list, false);
		return StreamUtils.sequenceM(StreamSupport.stream(array,false),Optional.ofNullable(array));
	}

	/**
	 * Construct a Sequence from an Iterable
	 * 
	 * @param iterable
	 *            to construct Sequence from
	 * @return SequenceM
	 */
	public static <T> SequenceM<T> fromIterable(Iterable<T> iterable) {
		Objects.requireNonNull(iterable);
		return StreamUtils.sequenceM(StreamSupport.stream(iterable.spliterator(), false),Optional.empty());
	}

	/**
	 * Construct a Sequence from an Iterator
	 * 
	 * @param iterator
	 *            to construct Sequence from
	 * @return SequenceM
	 */
	public static <T> SequenceM<T> fromIterator(Iterator<T> iterator) {
		Objects.requireNonNull(iterator);
		return fromIterable(() -> iterator);
	}

	/**
	 * @see Stream#iterate(Object, UnaryOperator)
	 */
	static <T> SequenceM<T> iterate(final T seed, final UnaryOperator<T> f) {
		return StreamUtils.sequenceM(Stream.iterate(seed, f),Optional.empty());
		
	}

	/**
	 * @see Stream#generate(Supplier)
	 */
	static <T> SequenceM<T> generate(Supplier<T> s) {
		return StreamUtils.sequenceM(Stream.generate(s),Optional.empty());
	
	}

	/**
	 * Unzip a zipped Stream
	 * 
	 * <pre>
	 * {@code 
	 *  unzip(SequenceM.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c")))
	 *  
	 *  // SequenceM[1,2,3], SequenceM[a,b,c]
	 * }
	 * 
	 * </pre>
	 * 
	 */
	public static <T, U> Tuple2<SequenceM<T>, SequenceM<U>> unzip(SequenceM<Tuple2<T, U>> sequence) {
		Tuple2<SequenceM<Tuple2<T, U>>, SequenceM<Tuple2<T, U>>> tuple2 = sequence.duplicateSequence();
		return new Tuple2(tuple2.v1.map(Tuple2::v1), tuple2.v2.map(Tuple2::v2));
	}

	/**
	 * Unzip a zipped Stream into 3
	 * 
	 * <pre>
	 * {@code 
	 *    unzip3(SequenceM.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
	 * }
	 * // SequenceM[1,2,3], SequenceM[a,b,c], SequenceM[2l,3l,4l]
	 * </pre>
	 */
	public static <T1, T2, T3> Tuple3<SequenceM<T1>, SequenceM<T2>, SequenceM<T3>> unzip3(SequenceM<Tuple3<T1, T2, T3>> sequence) {
		Tuple3<SequenceM<Tuple3<T1, T2, T3>>, SequenceM<Tuple3<T1, T2, T3>>, SequenceM<Tuple3<T1, T2, T3>>> tuple3 = sequence.triplicate();
		return new Tuple3(tuple3.v1.map(Tuple3::v1), tuple3.v2.map(Tuple3::v2), tuple3.v3.map(Tuple3::v3));
	}

	/**
	 * Unzip a zipped Stream into 4
	 * 
	 * <pre>
	 * {@code 
	 * unzip4(SequenceM.of(new Tuple4(1, "a", 2l,'z'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
	 * 						"c", 4l,'x')));
	 * 		}
	 * 		// SequenceM[1,2,3], SequenceM[a,b,c], SequenceM[2l,3l,4l], SequenceM[z,y,x]
	 * </pre>
	 */
	public static <T1, T2, T3, T4> Tuple4<SequenceM<T1>, SequenceM<T2>, SequenceM<T3>, SequenceM<T4>> unzip4(SequenceM<Tuple4<T1, T2, T3, T4>> sequence) {
		Tuple4<SequenceM<Tuple4<T1, T2, T3, T4>>, SequenceM<Tuple4<T1, T2, T3, T4>>, SequenceM<Tuple4<T1, T2, T3, T4>>, SequenceM<Tuple4<T1, T2, T3, T4>>> quad = sequence
				.quadruplicate();
		return new Tuple4(quad.v1.map(Tuple4::v1), quad.v2.map(Tuple4::v2), quad.v3.map(Tuple4::v3), quad.v4.map(Tuple4::v4));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#crossJoin(java.util.stream.Stream)
	 */
	default <U> SequenceM<Tuple2<T, U>> crossJoin(Stream<U> other){
		return fromStream(JoolManipulation.super.crossJoin(other));
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#crossJoin(org.jooq.lambda.Seq)
	 */
	default <U> SequenceM<Tuple2<T, U>> crossJoin(Seq<U> other){
		return fromStream(JoolManipulation.super.crossJoin(other));
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#crossJoin(java.lang.Iterable)
	 */
	default <U> SequenceM<Tuple2<T, U>> crossJoin(Iterable<U> other){
		return fromStream(JoolManipulation.super.crossJoin(other));
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#innerJoin(java.util.stream.Stream,
	 * java.util.function.BiPredicate)
	 */

	default <U> SequenceM<Tuple2<T, U>> innerJoin(Stream<U> other, java.util.function.BiPredicate<? super T, ? super U> predicate) {
		Streamable<U> s = Streamable.fromStream(SequenceM.fromStream(other));

		return innerJoin(s, predicate);
	}

	

	default <U> SequenceM<Tuple2<T, U>> innerJoin(Iterable<U> other, java.util.function.BiPredicate<? super T, ? super U> predicate) {
		Streamable<U> s = Streamable.fromIterable(other);
		return innerJoin(s, predicate);
	}

	default <U> SequenceM<Tuple2<T, U>> innerJoin(Seq<U> other, java.util.function.BiPredicate<? super T, ? super U> predicate) {
		Streamable<U> s = Streamable.fromStream(SequenceM.fromStream(other));

		return innerJoin(s, predicate);
	}
	default <U> SequenceM<Tuple2<T, U>> innerJoin(Streamable<U> other, java.util.function.BiPredicate<? super T,? super U> predicate){
		return flatMap(t -> other.stream()
                .filter(u -> predicate.test(t, u))
                .map(u -> Tuple.tuple(t, u)));
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#leftOuterJoin(java.util.stream.Stream,
	 * java.util.function.BiPredicate)
	 */
	default <U> SequenceM<Tuple2<T, U>> leftOuterJoin(Stream<U> other, BiPredicate<? super T, ? super U> predicate) {

		Streamable<U> s = Streamable.fromIterable(SequenceM.fromStream(other).toLazyCollection());

		return leftOuterJoin(s, predicate);

	}

	default <U> SequenceM<Tuple2<T, U>> leftOuterJoin(Seq<U> other, BiPredicate<? super T, ? super U> predicate) {

		Streamable<U> s = Streamable.fromIterable(SequenceM.fromStream(other));

		return leftOuterJoin(s, predicate);

	}

	default <U> SequenceM<Tuple2<T, U>> leftOuterJoin(Iterable<U> other, BiPredicate<? super T, ? super U> predicate) {

		Streamable<U> s = Streamable.fromIterable(other);

		return leftOuterJoin(s, predicate);

	}
	default <U> SequenceM<Tuple2<T, U>> leftOuterJoin(Streamable<U> s, java.util.function.BiPredicate<? super T,? super U> predicate){
		return flatMap(t -> Seq.seq(s.stream())
                .filter(u -> predicate.test(t, u))
                .onEmpty(null)
                .map(u -> Tuple.tuple(t, u)));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#rightOuterJoin(java.util.stream.Stream,
	 * java.util.function.BiPredicate)
	 */
	default <U> SequenceM<Tuple2<T, U>> rightOuterJoin(Stream<U> other, BiPredicate<? super T, ? super U> predicate){
		return fromStream(JoolManipulation.super.rightOuterJoin(other,predicate));
	}

	default <U> SequenceM<Tuple2<T, U>> rightOuterJoin(Iterable<U> other, BiPredicate<? super T, ? super U> predicate){
		return fromStream(JoolManipulation.super.rightOuterJoin(other,predicate));
	}

	default <U> SequenceM<Tuple2<T, U>> rightOuterJoin(Seq<U> other, BiPredicate<? super T, ? super U> predicate){
		return fromStream(JoolManipulation.super.rightOuterJoin(other,predicate));
	}
		

	

	/**
	 * If this SequenceM is empty replace it with a another Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(4,5,6)
	 * 							.onEmptySwitch(()->SequenceM.of(1,2,3))
	 * 							.toList(),
	 * 							equalTo(Arrays.asList(4,5,6)));
	 * }
	 * </pre>
	 * 
	 * @param switchTo
	 *            Supplier that will generate the alternative Stream
	 * @return SequenceM that will switch to an alternative Stream if empty
	 */
	default SequenceM<T> onEmptySwitch(Supplier<Stream<T>> switchTo) {
		AtomicBoolean called = new AtomicBoolean(false);
		return SequenceM.fromStream(onEmptyGet((Supplier) () -> {
			called.set(true);
			return switchTo.get();
		}).flatMap(s -> {
			if (called.get())
				return (Stream) s;
			return Stream.of(s);
		}));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onEmpty(java.lang.Object)
	 */
	SequenceM<T> onEmpty(T value);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onEmptyGet(java.util.function.Supplier)
	 */
	SequenceM<T> onEmptyGet(Supplier<T> supplier);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onEmptyThrow(java.util.function.Supplier)
	 */
	default <X extends Throwable> SequenceM<T> onEmptyThrow(Supplier<X> supplier) {
		return SequenceM.fromStream(JoolManipulation.super.onEmptyThrow(supplier));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#concat(java.util.stream.Stream)
	 */
	SequenceM<T> concat(Stream<T> other);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#concat(java.lang.Object)
	 */
	SequenceM<T> concat(T other);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#concat(java.lang.Object[])
	 */
	SequenceM<T> concat(T... other);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#distinct(java.util.function.Function)
	 */
	<U> SequenceM<T> distinct(Function<? super T, ? extends U> keyExtractor);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#zip(org.jooq.lambda.Seq,
	 * java.util.function.BiFunction)
	 */
	<U, R> SequenceM<R> zip(Seq<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#shuffle(java.util.Random)
	 */
	SequenceM<T> shuffle(Random random);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	SequenceM<T> slice(long from, long to);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	<U extends Comparable<? super U>> SequenceM<T> sorted(Function<? super T, ? extends U> function);

	/**
	 * emit x elements per time period
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SimpleTimer timer = new SimpleTimer();
	 * 	assertThat(SequenceM.of(1, 2, 3, 4, 5, 6).xPer(6, 100000000, TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(), is(6));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param x
	 *            number of elements to emit
	 * @param time
	 *            period
	 * @param t
	 *            Time unit
	 * @return SequenceM that emits x elements per time period
	 */
	SequenceM<T> xPer(int x, long time, TimeUnit t);

	/**
	 * emit one element per time period
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.iterate("", last -> "next")
	 * 				.limit(100)
	 * 				.batchBySize(10)
	 * 				.onePer(1, TimeUnit.MICROSECONDS)
	 * 				.peek(batch -> System.out.println("batched : " + batch))
	 * 				.flatMap(Collection::stream)
	 * 				.peek(individual -> System.out.println("Flattened : "
	 * 						+ individual))
	 * 				.forEach(a->{});
	 * }
	 * @param time period
	 * @param t Time unit
	 * @return SequenceM that emits 1 element per time period
	 */
	SequenceM<T> onePer(long time, TimeUnit t);

	/**
	 * Allow one element through per time period, drop all other elements in
	 * that time period
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3,4,5,6)
	 *          .debounce(1000,TimeUnit.SECONDS).toList();
	 *          
	 * // 1 
	 * }
	 * </pre>
	 * 
	 * @param time
	 * @param t
	 * @return
	 */
	SequenceM<T> debounce(long time, TimeUnit t);

	/**
	 * Batch elements by size into a List
	 * 
	 * <pre>
	 * {@code
	 * SequenceM.of(1,2,3,4,5,6)
	 * 				.batchBySizeAndTime(3,10,TimeUnit.SECONDS)
	 * 				.toList();
	 * 			
	 * //[[1,2,3],[4,5,6]] 
	 * }
	 * 
	 * @param size Max size of a batch
	 * @param time (Max) time period to build a single batch in
	 * @param t time unit for batch
	 * @return SequenceM batched by size and time
	 */
	SequenceM<ListX<T>> batchBySizeAndTime(int size, long time, TimeUnit t);

	/**
	 * Batch elements by size into a collection created by the supplied factory
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;ArrayList&lt;Integer&gt;&gt; list = of(1, 2, 3, 4, 5, 6).batchBySizeAndTime(10, 1, TimeUnit.MICROSECONDS, () -&gt; new ArrayList&lt;&gt;()).toList();
	 * }
	 * </pre>
	 * 
	 * @param size
	 *            Max size of a batch
	 * @param time
	 *            (Max) time period to build a single batch in
	 * @param unit
	 *            time unit for batch
	 * @param factory
	 *            Collection factory
	 * @return SequenceM batched by size and time
	 */
	<C extends Collection<? super T>> SequenceM<C> batchBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory);

	/**
	 * Batch elements in a Stream by time period
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
	 * assertThat(SequenceM.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	 * }
	 * </pre>
	 * 
	 * @param time
	 *            - time period to build a single batch in
	 * @param t
	 *            time unit for batch
	 * @return SequenceM batched into lists by time period
	 */
	SequenceM<ListX<T>> batchByTime(long time, TimeUnit t);

	/**
	 * Batch elements by time into a collection created by the supplied factory
	 * 
	 * <pre>
	 * {@code 
	 *   assertThat(SequenceM.of(1,1,1,1,1,1)
	 *                       .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
	 *                       .toList()
	 *                       .get(0)
	 *                       .size(),is(1));
	 * }
	 * </pre>
	 * 
	 * @param time
	 *            - time period to build a single batch in
	 * @param unit
	 *            time unit for batch
	 * @param factory
	 *            Collection factory
	 * @return SequenceM batched into collection types by time period
	 */
	<C extends Collection<T>> SequenceM<C> batchByTime(long time, TimeUnit unit, Supplier<C> factory);

	/**
	 * Batch elements in a Stream by size into Lists
	 * 
	 * <pre>
	 * {@code 
	 *  assertThat(SequenceM.of(1,2,3,4,5,6)
	 *                      .batchBySize(3)
	 *                      .collect(Collectors.toList())
	 *                      .size(),is(2));
	 * }
	 * @param size of batch
	 * @return SequenceM batched by size into Lists
	 */
	SequenceM<ListX<T>> batchBySize(int size);

	/**
	 * Batch elements in a Stream by size into a collection created by the
	 * supplied factory
	 * 
	 * <pre>
	 * {@code
	 * assertThat(SequenceM.of(1,1,1,1,1,1)
	 * 						.batchBySize(3,()->new TreeSet<>())
	 * 						.toList()
	 * 						.get(0)
	 * 						.size(),is(1));
	 * }
	 * 
	 * @param size batch size
	 * @param supplier Collection factory
	 * @return SequenceM batched into collection types by size
	 */
	<C extends Collection<T>> SequenceM<C> batchBySize(int size, Supplier<C> supplier);

	/**
	 * emit elements after a fixed delay
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SimpleTimer timer = new SimpleTimer();
	 * 	assertThat(SequenceM.of(1, 2, 3, 4, 5, 6).fixedDelay(10000, TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(), is(6));
	 * 	assertThat(timer.getElapsedNanoseconds(), greaterThan(60000l));
	 * }
	 * </pre>
	 * 
	 * @param l
	 *            time length in nanos of the delay
	 * @param unit
	 *            for the delay
	 * @return SequenceM that emits each element after a fixed delay
	 */
	SequenceM<T> fixedDelay(long l, TimeUnit unit);

	/**
	 * Introduce a random jitter / time delay between the emission of elements
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SimpleTimer timer = new SimpleTimer();
	 * 	assertThat(SequenceM.of(1, 2, 3, 4, 5, 6).jitter(10000).collect(Collectors.toList()).size(), is(6));
	 * 	assertThat(timer.getElapsedNanoseconds(), greaterThan(20000l));
	 * }
	 * </pre>
	 * 
	 * @param maxJitterPeriodInNanos
	 *            - random number less than this is used for each jitter
	 * @return Sequence with a random jitter between element emission
	 */
	SequenceM<T> jitter(long maxJitterPeriodInNanos);

	/**
	 * Create a Sequence of Streamables (replayable Streams / Sequences) where
	 * each Streamable is populated up to a max size, or for max period of time
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5,6)
	 * 						.windowBySizeAndTime(3,10,TimeUnit.SECONDS)
	 * 						.toList()
	 * 						.get(0)
	 * 						.stream()
	 * 						.count(),is(3l));
	 * 
	 * }
	 * @param maxSize of window
	 * @param maxTime of window
	 * @param maxTimeUnit of window
	 * @return Windowed SequenceM
	 */
	SequenceM<Streamable<T>> windowBySizeAndTime(int maxSize, long maxTime, TimeUnit maxTimeUnit);

	/**
	 * Create a Sequence of Streamables (replayable Streams / Sequences) where
	 * each Streamable is populated while the supplied predicate holds. When the
	 * predicate failsa new window/ Stremable opens
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3,4,5,6)
	 * 				.windowWhile(i->i%3!=0)
	 * 				.forEach(System.out::println);
	 *   
	 *  StreamableImpl(streamable=[1, 2, 3]) 
	 *  StreamableImpl(streamable=[4, 5, 6])
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            Window while true
	 * @return SequenceM windowed while predicate holds
	 */
	SequenceM<Streamable<T>> windowWhile(Predicate<? super T> predicate);

	/**
	 * Create a Sequence of Streamables (replayable Streams / Sequences) where
	 * each Streamable is populated until the supplied predicate holds. When the
	 * predicate failsa new window/ Stremable opens
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3,4,5,6)
	 * 				.windowUntil(i->i%3==0)
	 * 				.forEach(System.out::println);
	 *   
	 *  StreamableImpl(streamable=[1, 2, 3]) 
	 *  StreamableImpl(streamable=[4, 5, 6])
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            Window until true
	 * @return SequenceM windowed until predicate holds
	 */
	SequenceM<Streamable<T>> windowUntil(Predicate<? super T> predicate);

	/**
	 * Create SequenceM of Streamables (replayable Streams / Sequences) where
	 * each Streamable is populated while the supplied bipredicate holds. The
	 * bipredicate recieves the Streamable from the last window as well as the
	 * current value and can choose to aggregate the current value or create a
	 * new window
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5,6)
	 * 				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
	 * 				.toList().size(),equalTo(5));
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            Window while true
	 * @return SequenceM windowed while predicate holds
	 */
	SequenceM<Streamable<T>> windowStatefullyWhile(BiPredicate<Streamable<? super T>, ? super T> predicate);

	/**
	 * Create SequenceM of Streamables (replayable Streams / Sequences) where
	 * each Streamable is populated within a specified time window
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5, 6)
	 * 							.map(n-> n==6? sleep(1) : n)
	 * 							.windowByTime(10,TimeUnit.MICROSECONDS)
	 * 							.toList()
	 * 							.get(0).sequenceM().toList()
	 * 							,not(hasItem(6)));
	 * }
	 * </pre>
	 * 
	 * @param time
	 *            max time per window
	 * @param t
	 *            time unit per window
	 * @return SequenceM windowed by time
	 */
	SequenceM<Streamable<T>> windowByTime(long time, TimeUnit t);

	/**
	 * Create a SequenceM batched by List, where each batch is populated until
	 * the predicate holds
	 * 
	 * <pre>
	 * {@code 
	 *  assertThat(SequenceM.of(1,2,3,4,5,6)
	 * 				.batchUntil(i->i%3==0)
	 * 				.toList()
	 * 				.size(),equalTo(2));
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            Batch until predicate holds, then open next batch
	 * @return SequenceM batched into lists determined by the predicate supplied
	 */
	SequenceM<ListX<T>> batchUntil(Predicate<? super T> predicate);

	/**
	 * Create a SequenceM batched by List, where each batch is populated while
	 * the predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5,6)
	 * 				.batchWhile(i->i%3!=0)
	 * 				.toList().size(),equalTo(2));
	 * 	
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            Batch while predicate holds, then open next batch
	 * @return SequenceM batched into lists determined by the predicate supplied
	 */
	SequenceM<ListX<T>> batchWhile(Predicate<? super T> predicate);

	/**
	 * Create a SequenceM batched by a Collection, where each batch is populated
	 * while the predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5,6)
	 * 				.batchWhile(i->i%3!=0)
	 * 				.toList()
	 * 				.size(),equalTo(2));
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
	<C extends Collection<? super T>> SequenceM<C> batchWhile(Predicate<? super T> predicate, Supplier<C> factory);

	/**
	 * Create a SequenceM batched by a Collection, where each batch is populated
	 * until the predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5,6)
	 * 				.batchUntil(i->i%3!=0)
	 * 				.toList()
	 * 				.size(),equalTo(2));
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
	<C extends Collection<? super T>> SequenceM<C> batchUntil(Predicate<? super T> predicate, Supplier<C> factory);

	/**
	 * Recover from an exception with an alternative value
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4)
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
	 * @return SequenceM that can recover from an Exception
	 */
	SequenceM<T> recover(final Function<Throwable, ? extends T> fn);

	/**
	 * Recover from a particular exception type
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4)
	 * 					.map(i->i+2)
	 * 					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
	 * 					.recover(IOException.class,e->"hello")
	 * 					.firstValue(),equalTo("hello"));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param exceptionClass
	 *            Type to recover from
	 * @param fn
	 *            That accepts an error and returns an alternative value
	 * @return Sequence that can recover from a particular exception
	 */
	<EX extends Throwable> SequenceM<T> recover(Class<EX> exceptionClass, final Function<EX, ? extends T> fn);

	/**
	 * Retry a transformation if it fails. Default settings are to retry up to 7
	 * times, with an doubling backoff period starting @ 2 seconds delay before
	 * retry.
	 * 
	 * <pre>
	 * {@code 
	 * given(serviceMock.apply(anyInt())).willThrow(
	 * 				new RuntimeException(new SocketException("First")),
	 * 				new RuntimeException(new IOException("Second"))).willReturn(
	 * 				"42");
	 * 
	 * 	
	 * 		String result = SequenceM.of( 1,  2, 3)
	 * 				.retry(serviceMock)
	 * 				.firstValue();
	 * 
	 * 		assertThat(result, is("42"));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 *            Function to retry if fails
	 * 
	 */
	default <R> SequenceM<R> retry(Function<? super T, ? extends R> fn) {
		Function<T, R> retry = t -> {
			int count = 7;
			int[] sleep = { 2000 };
			Throwable exception = null;
			while (count-- > 0) {
				try {
					return fn.apply(t);
				} catch (Throwable e) {
					exception = e;
				}
				ExceptionSoftener.softenRunnable(() -> Thread.sleep(sleep[0]));

				sleep[0] = sleep[0] * 2;
			}
			ExceptionSoftener.throwSoftenedException(exception);
			return null;
		};
		return map(retry);
	}

	/**
	 * Remove all occurances of the specified element from the SequenceM
	 * 
	 * <pre>
	 * {@code
	 * 	SequenceM.of(1,2,3,4,5,1,2,3).remove(1)
	 * 
	 *  //Streamable[2,3,4,5,2,3]
	 * }
	 * </pre>
	 * 
	 * @param t
	 *            element to remove
	 * @return Filtered Stream / SequenceM
	 */
	default SequenceM<T> remove(T t) {
		return this.filter(v -> v != t);
	}

	/**
	 * Generate the permutations based on values in the SequenceM Makes use of
	 * Streamable to store intermediate stages in a collection
	 * 
	 * 
	 * @return Permutations from this SequenceM
	 */
	default SequenceM<SequenceM<T>> permutations() {
		Streamable<Streamable<T>> streamable = Streamable.fromStream(this).permutations();
		return streamable.map(s -> s.sequenceM()).sequenceM();
	}

	/**
	 * Return a Stream with elements before the provided start index removed,
	 * and elements after the provided end index removed
	 * 
	 * <pre>
	 * {@code 
	 *   SequenceM.of(1,2,3,4,5,6).subStream(1,3);
	 *   
	 *   
	 *   //SequenceM[2,3]
	 * }
	 * </pre>
	 * 
	 * @param start
	 *            index inclusive
	 * @param end
	 *            index exclusive
	 * @return Sequence between supplied indexes of original Sequence
	 */
	default SequenceM<T> subStream(int start, int end) {
		return this.limit(end).deleteBetween(0, start);
	}

	/**
	 * <pre>
	 * {@code
	 *   SequenceM.of(1,2,3).combinations(2)
	 *   
	 *   //SequenceM[SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param size
	 *            of combinations
	 * @return All combinations of the elements in this stream of the specified
	 *         size
	 */
	default SequenceM<SequenceM<T>> combinations(int size) {
		Streamable<Streamable<T>> streamable = Streamable.fromStream(this).combinations(size);
		return streamable.map(s -> s.sequenceM()).sequenceM();
	}

	/**
	 * <pre>
	 * {@code
	 *   SequenceM.of(1,2,3).combinations()
	 *   
	 *   //SequenceM[SequenceM[],SequenceM[1],SequenceM[2],SequenceM[3].SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]
	 *   			,SequenceM[1,2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @return All combinations of the elements in this stream
	 */
	default SequenceM<SequenceM<T>> combinations() {
		Streamable<Streamable<T>> streamable = Streamable.fromStream(this).combinations();
		return streamable.map(s -> s.sequenceM()).sequenceM();
	}

	/**
	 * Execute this Stream on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run at 8PM every night
	 *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            .map(this::processJob)
	 *            .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
	 * }
	 * </pre>
	 * 
	 * Connect to the Scheduled Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
	 * 			.schedule(&quot;0 20 * * *&quot;, Executors.newScheduledThreadPool(1));
	 * 
	 * 	data.connect().forEach(this::logToDB);
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
	HotStream<T> schedule(String cron, ScheduledExecutorService ex);

	/**
	 * Execute this Stream on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run every 60 seconds after last job completes
	 *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            .map(this::processJob)
	 *            .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
	 * }
	 * </pre>
	 * 
	 * Connect to the Scheduled Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
	 * 			.scheduleFixedDelay(60_000, Executors.newScheduledThreadPool(1));
	 * 
	 * 	data.connect().forEach(this::logToDB);
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
	HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex);

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
	 * {
	 * 	&#064;code
	 * 	HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
	 * 			.scheduleFixedRate(60_000, Executors.newScheduledThreadPool(1));
	 * 
	 * 	data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * 
	 * @param rate
	 *            Time in millis between job runs
	 * @param ex
	 *            ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex);

	/**
	 * [equivalent to count]
	 * 
	 * @return size
	 */
	default int size() {
		return this.toList().size();
	}

	/**
	 * Perform a three level nested internal iteration over this Stream and the
	 * supplied streams
	 *
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2)
	 * 						.forEach3(a->IntStream.range(10,13),
	 * 						        a->b->Stream.of(""+(a+b),"hello world"),
	 * 									a->b->c->c+":"a+":"+b);
	 * 									
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre>
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param stream2
	 *            Nested Stream to iterate over
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	<R1, R2, R> SequenceM<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
			Function<? super T, Function<? super R1, ? extends BaseStream<R2, ?>>> stream2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);


	/**
	 * Perform a three level nested internal iteration over this Stream and the
	 * supplied streams
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
	 * 						.forEach3(a->IntStream.range(10,13),
	 * 						      a->b->Stream.of(""+(a+b),"hello world"),
	 * 						         a->b->c-> c!=3,
	 * 									a->b->c->c+":"a+":"+b);
	 * 									
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param stream2
	 *            Nested Stream to iterate over
	 * @param filterFunction
	 *            Filter to apply over elements before passing non-filtered
	 *            values to the yielding function
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	<R1, R2, R> SequenceM<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
			Function<? super T, Function<? super R1, ? extends BaseStream<R2, ?>>> stream2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);

	/**
	 * Perform a two level nested internal iteration over this Stream and the
	 * supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
	 * 						.forEach2(a->IntStream.range(10,13),
	 * 									a->b->a+b);
	 * 									
	 * 
	 *  //SequenceM[11,14,12,15,13,16]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	<R1, R> SequenceM<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);

	/**
	 * Perform a two level nested internal iteration over this Stream and the
	 * supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
	 * 						.forEach2(a->IntStream.range(10,13),
	 * 						            a->b-> a<3 && b>10,
	 * 									a->b->a+b);
	 * 									
	 * 
	 *  //SequenceM[14,15]
	 * }
	 * </pre>
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param filterFunction
	 *            Filter to apply over elements before passing non-filtered
	 *            values to the yielding function
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	<R1, R> SequenceM<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1, Function<? super T, Function<? super R1, Boolean>> filterFunction,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);

	@Override
	default long count() {
		return SequenceMCollectable.super.count();
	}
	default <R, A> R collect(Collector<? super T, A, R> collector) {
		return SequenceMCollectable.super.collect(collector);
	}
	@Override
	default Collectable<T> collectable() {
		return (Collectable<T>)this;
	}
	
	@Override
	default <R> SequenceM<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (SequenceM<R>)ZippingApplicativable.super.patternMatch(defaultValue, case1);
	}

	

	


}
