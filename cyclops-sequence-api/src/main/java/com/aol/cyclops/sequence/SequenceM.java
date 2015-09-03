package com.aol.cyclops.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsLoader;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsPublisher;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsSubscriber;
import com.aol.cyclops.sequence.streamable.Streamable;





public interface SequenceM<T> extends Unwrapable, Stream<T>, Seq<T>,Iterable<T>, Publisher<T>{
	
	@Override
	<R> R unwrap();

	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * <pre>
	 * {@code 
	 *  assertThat(SequenceM.<Integer>of(Arrays.asList(1,2)).flatten().toList().size(),equalTo(asList(1,  2).size()));		
	 *  
	 *  // or more advanced example 
	 *  
	 *  AnyM<AnyM<Integer>> applied =anyM(Optional.of(2))
			 							.simpleFilter(anyM(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3)));
	
	     assertThat(applied.toSequence().flatten().toList(),equalTo(Arrays.asList(2)));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @return Flattened / joined one level
	 */
	<T1> SequenceM<T1> flatten();
	
	/**
	 * Type safe unwrap 
	 * <pre>
	 * {@code 
	 * Optional<List<String>> stream = anyM("hello","world")
											.asSequence()
											.unwrapOptional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	 * }
	 * 
	 * </pre>
	 * @return
	 */
	Optional<List<T>> unwrapOptional();
	/**
	 * <pre>
	 * {@code 
	 * CompletableFuture<List<String>> cf = anyM("hello","world")
											.asSequence()
											.unwrapCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	 * }
	 * </pre>
	 * @return
	 */
	CompletableFuture<List<T>> unwrapCompletableFuture();
	
	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * <pre>
	 * {@code 
	 * 		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycle(3).collect(Collectors.toList()),
											equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));

	 * 
	 * }
	 * </pre>
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
	 *   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());
	 *   }
	 * </pre>
	 * 
	 * @return Stream with values repeated
	 */
	SequenceM<T> cycle();
	/**
	 * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe.
	 * <pre>
	 * {@code 
	 *  Tuple2<SequenceM<Integer>, SequenceM<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	 * 
	 * }
	 * </pre>
	 * 
	 * @return duplicated stream
	 */
	Tuple2<SequenceM<T>,SequenceM<T>> duplicateSequence();
	
	/**
	 * Triplicates a Stream
	 * Buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe.
	 * <pre>
	 * {@code 
	 * 	Tuple3<SequenceM<Tuple3<T1,T2,T3>>,SequenceM<Tuple3<T1,T2,T3>>,SequenceM<Tuple3<T1,T2,T3>>> Tuple3 = sequence.triplicate();
	
	 * }
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	 Tuple3<SequenceM<T>,SequenceM<T>,SequenceM<T>> triplicate();
	/**
	 * Makes four copies of a Stream
	 * Buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe. 
	 * 
	 * <pre>
	 * {@code
	 * 
	 * 		Tuple4<SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();

	 * }
	 * </pre>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Tuple4<SequenceM<T>,SequenceM<T>,SequenceM<T>,SequenceM<T>> quadruplicate();
	/**
	 * Split a Stream at it's head (similar to headAndTail)
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
	Tuple2<Optional<T>,SequenceM<T>> splitSequenceAtHead();
	/**
	 * Split at supplied location 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3).splitAt(1)
	 * 
	 *  //SequenceM[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 */
	Tuple2<SequenceM<T>,SequenceM<T>> splitAt(int where);
	/**
	 * Split stream at point where predicate no longer holds
	 * <pre>
	 * {@code
	 *   SequenceM.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
	 *   
	 *   //SequenceM[1,2,3] SequenceM[4,5,6]
	 * }
	 * </pre>
	 */
	Tuple2<SequenceM<T>,SequenceM<T>> splitBy(Predicate<T> splitter);
	/**
	 * Partition a Stream into two one a per element basis, based on predicate's boolean value
	 * <pre>
	 * {@code 
	 *  SequenceM.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
	 *  
	 *  //SequenceM[1,3,5], SequenceM[2,4,6]
	 * }
	 *
	 * </pre>
	 */
	Tuple2<SequenceM<T>,SequenceM<T>> partitionSequence(Predicate<T> splitter);
	
	
	
	/**
	 * Convert to a Stream with the result of a reduction operation repeated
	 * specified times
	 * 
	 * <pre>
	 * {@code 
	 *   		List<Integer> list = AsGenericMonad,asMonad(Stream.of(1,2,2))
	 * 										.cycle(Reducers.toCountInt(),3)
	 * 										.collect(Collectors.toList());
	 * 	//is asList(3,3,3);
	 *   }
	 * </pre>
	 * 
	 * @param m
	 *            Monoid to be used in reduction
	 * @param times
	 *            Number of times value should be repeated
	 * @return Stream with reduced values repeated
	 */
	SequenceM<T> cycle(Monoid<T> m, int times) ;

	
	/**
	 * 
	 * Convert to a Stream, repeating the resulting structure specified times
	 * and lifting all values to the specified Monad type
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Optional&lt;Integer&gt;&gt; list = monad(Stream.of(1, 2)).cycle(Optional.class,
	 * 			2).toList();
	 * 
	 * 	// is asList(Optional.of(1),Optional.of(2),Optional.of(1),Optional.of(2) ));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * 
	 * @param monadC
	 *            class type
	 * @param times
	 * @return
	 */
	<R> SequenceM<R> cycle(Class<R> monadC, int times);
	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * <pre>
	 * {@code
	 * count =0;
		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
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
	 * <pre>
	 * {@code 
	 * 	count =0;
		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycleUntil(next -> count++>6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));

	 * 
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
	 * {@code 
	 * List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();
       // [[1,"a"],[2,"b"]]
		 } 
	 * </pre>
	 * 
	 */
	<U> SequenceM<Tuple2<T, U>> zipStream(Stream<U> other);
	/**
	 * Zip 2 streams into one
	 * 
	 * <pre>
	 * {@code 
	 * List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();
       // [[1,"a"],[2,"b"]]
		 } 
	 * </pre>
	 * 
	 */
	@Override
	<U> SequenceM<Tuple2<T, U>> zip(Seq<U> other);
	/**
	 * zip 3 Streams into one
	 * <pre>
	 * {@code 
	 * List<Tuple3<Integer,Integer,Character>> list =
				of(1,2,3,4,5,6).zip3(of(100,200,300,400),of('a','b','c'))
											.collect(Collectors.toList());
	 * 
	 * //[[1,100,'a'],[2,200,'b'],[3,300,'c']]
	 * }
	 * 
	 *</pre>
	 */
	<S,U> SequenceM<Tuple3<T,S,U>> zip3(Stream<? extends S> second,Stream<? extends U> third);
	/**
	 * zip 4 Streams into 1
	 * 
	 * <pre>
	 * {@code 
	 * List<Tuple4<Integer,Integer,Character,String>> list =
				of(1,2,3,4,5,6).zip4(of(100,200,300,400),of('a','b','c'),of("hello","world"))
												.collect(Collectors.toList());
			
	 * }
	 *  //[[1,100,'a',"hello"],[2,200,'b',"world"]]
	 * </pre>
	 */
	 <T2,T3,T4> SequenceM<Tuple4<T,T2,T3,T4>> zip4(Stream<T2> second,Stream<T3> third,Stream<T4> fourth);
	/** 
	 * Add an index to the current Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
	 * }
	 * </pre>
	 */
	SequenceM<Tuple2<T,Long>> zipWithIndex();
	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asMonad(Stream.of(1, 2, 3)).zip(
	 * 			asMonad(Optional.of(2)), (a, b) -&gt; Arrays.asList(a, b));
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
	<S, R> SequenceM<R> zipSequence(SequenceM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) ;
	/**
	 * Zip this SequenceM against any monad type.
	 * 
	 * <pre>
	 * {@code
	 * Stream<List<Integer>> zipped = anyM(Stream.of(1,2,3))
										.asSequence()
										.zip(anyM(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
	 * }
	 * </pre>
	 * 
	 */
	<S, R> SequenceM<R> zipAnyM(AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) ;

	/**
	 * Zip this Monad with a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asMonad(Stream.of(1, 2, 3)).zip(
	 * 			Stream.of(2, 3, 4), (a, b) -&gt; Arrays.asList(a, b));
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
	 <S, R> SequenceM<R> zipStream(BaseStream<? extends S,? extends BaseStream<? extends S,?>> second,
			BiFunction<? super T, ? super S, ? extends R> zipper);

	/**
	 * Create a sliding view over this Sequence
	 * 
	 * <pre>
	 * {@code 
	 * List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	 * 
	 * }
	 * 
	 * </pre>
	 * @param windowSize
	 *            Size of sliding window
	 * @return SequenceM with sliding view
	 */
	SequenceM<List<T>> sliding(int windowSize);
	/**
	 *  Create a sliding view over this Sequence
	 * <pre>
	 * {@code 
	 * List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.sliding(3,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param windowSize number of elements in each batch
	 * @param increment for each window
	 * @return SequenceM with sliding view
	 */
	SequenceM<List<T>> sliding(int windowSize,int increment);

	/**
	 * Group elements in a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = monad(Stream.of(1, 2, 3, 4, 5, 6)).grouped(3)
	 * 			.collect(Collectors.toList());
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
	SequenceM<List<T>> grouped(int groupSize);
	/**
	 * Use classifier function to group elements in this Sequence into a Map
	 * <pre>
	 * {@code 
	 * Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
		        assertEquals(asList(2, 4), map1.get(0));
		        assertEquals(asList(1, 3), map1.get(1));
		        assertEquals(2, map1.size());
	 * 
	 * }
	 * 
	 * </pre>
	 */
	<K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier);

	/*
	 * Return the distinct Stream of elements
	 * 
	 * <pre>{@code List<Integer> list =
	 *        anyM(Optional.of(Arrays.asList(1,2,2,2,5,6)))
	 *           .<Stream<Integer>,Integer>toSequence() .distinct()
	 *				 .collect(Collectors.toList()); 
	 * }
	 *</pre>
	 */
	 SequenceM<T> distinct();

	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),monad(Stream.of("a", "b", "c")).scanLeft(Reducers.toString("")).toList());
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
	 * <pre>
	 * {@code 
	 *  assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
	 * }
	 * </pre>
	 */
	<U> SequenceM<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
	
	/**
	 * Scan right
	 * <pre>
	 * {@code 
	 * assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
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
            is(asList(0, 3, 5, 6).size()));
	 * 
	 * }
	 * </pre>
	 */
	<U> SequenceM<U> scanRight(U identity,BiFunction<? super T, U, U>  combiner);
	

	
	/**
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
	 * </pre>
	 * 
	 */
	 SequenceM<T> sorted();

	/**
	 *<pre>
	 * {@code 
	 * 	assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	 * }
	 * </pre>
	 * @param c
	 *            Compartor to sort with
	 * @return Sorted Monad
	 */
	SequenceM<T> sorted(Comparator<? super T> c);

	/**
	 * <pre>
	 * {@code assertThat(anyM(Stream.of(4,3,6,7)).asSequence().skip(2).toList(),equalTo(Arrays.asList(6,7))); }
	 * </pre>
	 * 
	
	 * 
	 * @param num
	 *            Number of elemenets to skip
	 * @return Monad converted to Stream with specified number of elements
	 *         skipped
	 */
	SequenceM<T> skip(long num);
	/**
	 * 
	 * 
	 * <pre>
	 * {@code
	 * assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	 * }
	 * </pre>
	 * 
	 * @param p
	 *            Predicate to skip while true
	 * @return Monad converted to Stream with elements skipped while predicate
	 *         holds
	 */
	 SequenceM<T> skipWhile(Predicate<? super T> p);

	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(anyM(Stream.of(4,3,6,7)).asSequence().skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
	 * </pre>
	 * 
	 * 
	 * @param p
	 *            Predicate to skip until true
	 * @return Monad converted to Stream with elements skipped until predicate
	 *         holds
	 */
	SequenceM<T> skipUntil(Predicate<? super T> p);

	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(anyM(Stream.of(4,3,6,7)).asSequence().limit(2).toList(),equalTo(Arrays.asList(4,3)));}
	 * </pre>
	 * 
	 * @param num
	 *            Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	SequenceM<T> limit(long num);

	/**
	 *
	 * 
	 * <pre>
	 * {@code assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
	 * </pre>
	 * 
	 * @param p
	 *            Limit while predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	SequenceM<T> limitWhile(Predicate<? super T> p);
	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(anyM(Stream.of(4,3,6,7)).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
	 * </pre>
	 * 
	 * @param p
	 *            Limit until predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	SequenceM<T> limitUntil(Predicate<? super T> p);
	/**
	 * @return this monad converted to a Parallel Stream, via streamedMonad() wraped in the SequenceM interface
	 */
	SequenceM<T> parallel();
	
	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	 * }
	 * </pre>
	 * @param c Predicate to check if all match
	 */
	 boolean  allMatch(Predicate<? super T> c);
	/**
	 * True if a single element matches when Monad converted to a Stream
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	 * }
	 * </pre>
	 * @param c Predicate to check if any match
	 */
	boolean  anyMatch(Predicate<? super T> c);
	/**
	 * Check that there are specified number of matches of predicate in the Stream
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
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
	 * 
	 * }
	 * </pre>
	 */
	boolean  noneMatch(Predicate<? super T> c);
	/**
	 * <pre>
	 * {@code
	 *  assertEquals("123".length(),of(1, 2, 3).join().length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	 String join();
	/**
	 * <pre>
	 * {@code
	 * assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	 * }
	 * </pre>
	 * @return Stream as concatenated String
	 */
	String join(String sep);
	/**
	 * <pre>
	 * {@code 
	 * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	 * }
	 * </pre> 
	 *  @return Stream as concatenated String
	 */
	String join(String sep,String start, String end);
	
	
	/**
	 * Extract the minimum as determined by supplied function
	 * 
	 */
	<C extends Comparable<C>> Optional<T> minBy(Function<T,C> f);
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	Optional<T> min(Comparator<? super T> comparator);
	/**
	 * Extract the maximum as determined by the supplied function
	 * 
	 */
	<C extends Comparable<C>> Optional<T> maxBy(Function<T,C> f);
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	 Optional<T> max(Comparator<? super T> comparator);
	
	
	
	/**
	 * extract head and tail together, where head is expected to be present
	 * 
	 * <pre>
	 * {@code
	 *  SequenceM<String> helloWorld = SequenceM.of("hello","world","last");
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		SequenceM<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
	 * }
	 * </pre>
	 * @return
	 */
	HeadAndTail<T> headAndTail();
	/**
	 * extract head and tail together, where no head or tail may be present
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM<String> helloWorld = SequenceM.of();
		Optional<HeadAndTail<String>> headAndTail = helloWorld.headAndTailOptional();
		assertTrue(!headAndTail.isPresent());
	 * 
	 * }
	 * </pre>
	 * @return
	 */
	 Optional<HeadAndTail<T>> headAndTailOptional();
	
	/**
	 * @return First matching element in sequential order
	 * 
	 * (deterministic)
	 * 
	 */
	Optional<T>  findFirst() ;
	/**
	 * @return first matching element,  but order is not guaranteed
	 * 
	 * (non-deterministic) 
	 */
	 Optional<T>  findAny();
	
	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	<R> R mapReduce(Monoid<R> reducer);
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
	 *  Then use Monoid to reduce values
	 *  
	 * @param mapper Function to map Monad type
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	<R> R mapReduce(Function<? super T,? extends R> mapper, Monoid<R> reducer);
	

	/**
	 * Apply multiple collectors Simulataneously to this Monad
	 * 
	 * <pre>{@code
	  	List result =SequenceM.of(1,2,3).collect(Stream.of(Collectors.toList(),
	  															Collectors.summingInt(Integer::intValue),
	  															Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
		}</pre>
		
	 * 
	 * @param collectors Stream of Collectors to apply
	 * @return  List of results
	 */
	 List collectStream(Stream<Collector> collectors);
	/**
	 *  Apply multiple Collectors, simultaneously to a Stream
	 * <pre>
	 * {@code 
	 * List result = SequenceM.of(1,2,3).collect(
								Arrays.asList(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
	 * }
	 * </pre>
	 * @param stream Stream to collect
	 * @param collectors Collectors to apply
	 * @return Result as a list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	<R> List<R> collectIterable(Iterable<Collector> collectors);
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	T reduce(Monoid<T> reducer);
	/* 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
	 * }
	 * </pre>
	 * 
	 */
	 Optional<T> reduce(BinaryOperator<T> accumulator);
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	T reduce(T identity, BinaryOperator<T> accumulator);
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	<U> U reduce(U identity,
             BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner);
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * @param reducers
	 * @return
	 */
	 List<T> reduce(Stream<Monoid<T>> reducers);
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * @param reducers
	 * @return
	 */
	 List<T> reduce(Iterable<Monoid<T>> reducers);
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	 T foldLeft(Monoid<T> reducer);
	/**
	 * foldLeft : immutable reduction from left to right
	 * <pre>
	 * {@code 
	 * 
	 * assertTrue(SequenceM.of("a", "b", "c").foldLeft("", String::concat).equals("abc"));
	 * }
	 * </pre>
	 */
	T foldLeft(T identity,  BinaryOperator<T> accumulator);
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	<T> T foldLeftMapToType(Monoid<T> reducer);
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	 T foldRight(Monoid<T> reducer);
	/**
	 * Immutable reduction from right to left
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
	public T foldRight(T identity,  BinaryOperator<T> accumulator);
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public <T> T foldRightMapToType(Monoid<T> reducer);
	/**
	 * @return Underlying monad converted to a Streamable instance
	 */
	public  Streamable<T> toStreamable();
	/**
	 * @return This monad converted to a set
	 */
	public Set<T> toSet();
	/**
	 * @return this monad converted to a list
	 */
	public  List<T> toList();
	public  <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory);
	/**
	 * @return  calls to stream() but more flexible on type for inferencing purposes.
	 */
	public  <T> Stream<T> toStream();
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public Stream<T> stream();
	
	/**
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3)));
	 * }</pre>
	 * 
	 * @param iterable
	 * @return True if Monad starts with Iterable sequence of data
	 */
	boolean startsWith(Iterable<T> iterable);
	/**
	 * 	<pre>{@code assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3).iterator())) }</pre>

	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	boolean startsWith(Iterator<T> iterator);
	
	/**
	 * @return this SequenceM converted to AnyM format
	 */
	public AnyM<T> anyM();
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#map(java.util.function.Function)
	 */
	<R> SequenceM<R> map(Function<? super T,? extends R> fn);
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#peek(java.util.function.Consumer)
	 */
	SequenceM<T>  peek(Consumer<? super T> c) ;
	/**
	 * flatMap operation
	 * <pre>
	 * {@code
	 * 	assertThat(this.<Integer>of(1,2).flatMap(i -> asList(i, -i).stream()).toList(),equalTo(asList(1, -1, 2, -2)));		
 
	 * }
	 * </pre>
	 * @param fn to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	<R> SequenceM<R> flatMap(Function<? super T,? extends Stream<? extends R>> fn);
	/**
	 * Allows flatMap return type to be any Monad type
	 * <pre>
	 * {@code 
	 * 	assertThat(anyM(Seq.of(1,2,3)).asSequence().flatMapAnyM(i-> anyM(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));

	 * }</pre>
	 * 
	 * 
	 * @param fn to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	<R> SequenceM<R> flatMapAnyM(Function<? super T,AnyM<? extends R>> fn);
	/**
	 * Convenience method & performance optimisation
	 * 
	 * flatMapping to a Stream will result in the Stream being converted to a List, if the host Monad
	 * type is not a Stream. (i.e.
	 *  <pre>
	 *  {@code  
	 *   AnyM<Integer> opt = anyM(Optional.of(20));
	 *   Optional<List<Integer>> optionalList = opt.flatMap( i -> anyM(Stream.of(1,2,i))).unwrap();  
	 *   
	 *   //Optional [1,2,20]
	 *  }</pre>
	 *  
	 *  In such cases using Arrays.asList would be more performant
	 *  <pre>
	 *  {@code  
	 *   AnyM<Integer> opt = anyM(Optional.of(20));
	 *   Optional<List<Integer>> optionalList = opt.flatMapCollection( i -> asList(1,2,i))).unwrap();  
	 *   
	 *   //Optional [1,2,20]
	 *  }</pre>
	 * @param fn
	 * @return
	 */
	<R> SequenceM<R> flatMapCollection(Function<? super T,Collection<? extends R>> fn);
	/**
	 * flatMap operation
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(anyM(Stream.of(1,2,3)).asSequence().flatMapStream(i->Stream.of(i)).toList(),equalTo(Arrays.asList(1,2,3)));

	 * }
	 * </pre>
	 * 
	 * @param fn to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	*/
	<R> SequenceM<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn);
	/**
	 * flatMap to optional - will result in null values being removed
	 * <pre>
	 * {@code 
	 * 	assertThat(SequenceM.of(1,2,3,null).flatMapOptional(Optional::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * @param fn
	 * @return
	 */
	 <R> SequenceM<R> flatMapOptional(Function<? super T,Optional<? extends R>> fn) ;
	/**
	 * flatMap to CompletableFuture - will block until Future complete, although (for non-blocking behaviour use AnyM 
	 *       wrapping CompletableFuture and flatMap to Stream there)
	 *       
	 *  <pre>
	 *  {@code
	 *  	assertThat(SequenceM.of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	 *  }
	 *  </pre>
	 *       
	 * @param fn
	 * @return
	 */
	<R> SequenceM<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn);
//	 <R> SequenceM<R> flatMapLazySeq(Function<? super T,LazySeq<? extends R>> fn);
	/**
	 * Perform a flatMap operation where the result will be a flattened stream of Characters
	 * from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {@code 
	 *   List<Character> result = anyM("input.file")
									.asSequence()
									.liftAndBindCharSequence(i->"hello world")
									.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<Character> liftAndBindCharSequence(Function<? super T,CharSequence> fn);
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied files.
	 * 
	 * <pre>
	 * {@code
	 * 
		List<String> result = anyM("input.file")
								.asSequence()
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.liftAndBindFile(File::new)
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
	SequenceM<String> liftAndBindFile(Function<? super T,File> fn);
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied URLs 
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = anyM("input.file")
								.asSequence()
								.liftAndBindURL(getClass().getClassLoader()::getResource)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<String> liftAndBindURL(Function<? super T, URL> fn) ;
	/**
	  *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied BufferedReaders
	 * 
	 * <pre>
	 * List<String> result = anyM("input.file")
								.asSequence()
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.liftAndBindBufferedReader(BufferedReader::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	SequenceM<String> liftAndBindBufferedReader(Function<? super T,BufferedReader> fn);
	public  SequenceM<T>  filter(Predicate<? super T> fn);
	void forEach(Consumer<? super T> action);
	
	
	Iterator<T> iterator();
	 Spliterator<T> spliterator() ;
	
	boolean isParallel() ;
	
	SequenceM<T> sequential() ;
	
	
	SequenceM<T> unordered();
	
	
	
	
	IntStream mapToInt(ToIntFunction<? super T> mapper);
	
	LongStream mapToLong(ToLongFunction<? super T> mapper);
	
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);
	
	public IntStream flatMapToInt(
			Function<? super T, ? extends IntStream> mapper);
	
	public LongStream flatMapToLong(
			Function<? super T, ? extends LongStream> mapper) ;
	
	public DoubleStream flatMapToDouble(
			Function<? super T, ? extends DoubleStream> mapper) ;

	
	public void forEachOrdered(Consumer<? super T> action);
	
	public Object[] toArray();
	
	public <A> A[] toArray(IntFunction<A[]> generator) ;
	
	public long count();
	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) SequenceM.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	public  SequenceM<T> intersperse(T value) ;
	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) SequenceM.of(1, "a", 2, "b",3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <U> SequenceM<U> ofType(Class<U> type);
	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException SequenceM.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	public <U> SequenceM<U> cast(Class<U> type);
	/**
	 * Lazily converts this SequenceM into a Collection. This does not trigger the Stream. E.g.
	 * Collection is not thread safe on the first iteration.
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = SequenceM.of(1,2,3,4,5)
											.peek(System.out::println)
											.toLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
	 * }
	 * 
	 * //Will print out "first!" before anything else
	 * </pre>
	 * @return
	 */
	public Collection<T> toLazyCollection();
	/**
	 * Lazily converts this SequenceM into a Collection. This does not trigger the Stream. E.g.
	 * 
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = SequenceM.of(1,2,3,4,5)
											.peek(System.out::println)
											.toConcurrentLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
	 * }
	 * 
	 * //Will print out "first!" before anything else
	 * </pre>
	 * @return
	 */
	public Collection<T> toConcurrentLazyCollection();
	
	/**
	 * @return Streamable that can replay this SequenceM
	 */
	public Streamable<T> toLazyStreamable();
	/**
	 * @return Streamable that replay this SequenceM
	 */
	public Streamable<T> toConcurrentLazyStreamable();
	public SequenceM<T> reverse();
	
	@Override
	public SequenceM<T> onClose(Runnable closeHandler) ;
	@Override
	public void close();
	
	public SequenceM<T> shuffle();
	/**
	 * Append Stream to this SequenceM
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	of(1,2,3).appendStream(of(100,200,300))
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	 * }
	 * </pre>
	 * 
	 * @param stream to append
	 * @return SequenceM with Stream appended
	 */
	public SequenceM<T> appendStream(Stream<T> stream);
	/**
	 * Prepend Stream to this SequenceM
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = of(1,2,3).prependStream(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param stream to Prepend
	 * @return SequenceM with Stream prepended
	 */
	SequenceM<T> prependStream(Stream<T> stream);
	/**
	 * Append values to the end of this SequenceM
	 * <pre>
	 * {@code 
	 * List<String> result = 	of(1,2,3).append(100,200,300)
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	 * }
	 * </pre>
	 * @param values to append
	 * @return SequenceM with appended values
	 */
	SequenceM<T> append(T... values);
	/**
	 * Prepend given values to the start of the Stream
	 * <pre>
	 * {@code 
	 * List<String> result = 	of(1,2,3).prepend(100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * }
	 * @param values to prepend
	 * @return SequenceM with values prepended
	 */
	 SequenceM<T> prepend(T... values) ;
	/**
	 * Insert data into a stream at given position
	 * <pre>
	 * {@code 
	 * List<String> result = 	of(1,2,3).insertAt(1,100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
	 * 
	 * }
	 * </pre>
	 * @param pos to insert data at
	 * @param values to insert
	 * @return Stream with new data inserted
	 */
	SequenceM<T> insertAt(int pos, T... values);
	/**
	 * Delete elements between given indexes in a Stream
	 * <pre>
	 * {@code 
	 * List<String> result = 	of(1,2,3,4,5,6).deleteBetween(2,4)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
	 * }
	 * </pre>
	 * @param start index
	 * @param end index
	 * @return Stream with elements removed
	 */
	SequenceM<T> deleteBetween(int start,int end);
	/**
	 * Insert a Stream into the middle of this stream at the specified position
	 * <pre>
	 * {@code 
	 * List<String> result = 	of(1,2,3).insertStreamAt(1,of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
	 * }
	 * </pre>
	 * @param pos to insert Stream at
	 * @param stream to insert
	 * @return newly conjoined SequenceM
	 */
	SequenceM<T> insertStreamAt(int pos, Stream<T> stream);
	FutureOperations<T> futureOperations(Executor exec);
	
	boolean endsWith(Iterable<T> iterable);
//	window(Predicate p);

	HotStream<T> hotStream(Executor e);

	T firstValue();
	
//	void publish(Subscriber sub);
	
//	batchBySize();
//	batchByTime();
//	batchBySizeAndTime()
	
	public static <T> Subscriber<T> subscriber(){
		return ReactiveStreamsLoader.subscriber.get().subscribe();
	}
	/**
	 * Construct a Sequence from the provided elements
	 * @param elements To Construct sequence from
	 * @return
	 */
	public static <T> SequenceM<T> of(T... elements){
		return SequenceMFactory.instance.sequenceM(Stream.of(elements),elements);
		
	}
	/**
	 * Construct a Reveresed Sequence from the provided elements
	 * @param elements To Construct sequence from
	 * @return
	 */
	public static <T> SequenceM<T> reversedOf(T... elements){
		return SequenceMFactory.instance.sequenceM(SeqUtils.reversedStream(Arrays.asList(elements)),null);
		
	}
	/**
	 * Construct a Reveresed Sequence from the provided elements
	 * @param elements To Construct sequence from
	 * @return
	 */
	public static <T> SequenceM<T> reversedListOf(List<T> elements){
		return SequenceMFactory.instance.sequenceM(SeqUtils.reversedStream(elements),null);

	}
	public static SequenceM<Integer> range(int start, int end){
		IntStream range = IntStream.range(start, end);
		return SequenceMFactory.instance.sequenceM(Seq.seq(range),null);

	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static <T> SequenceM<T> fromStream(Stream<T> stream){
		if(stream instanceof SequenceM)
			return (SequenceM)stream;
		return SequenceMFactory.instance.sequenceM(stream,null);
	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static SequenceM<Integer> fromIntStream(IntStream stream){
		
		return SequenceMFactory.instance.sequenceM(Seq.seq(stream),null);
	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static SequenceM<Long> fromLongStream(LongStream stream){
		
		return SequenceMFactory.instance.sequenceM(Seq.seq(stream),null);
	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static SequenceM<Double> fromDoubleStream(DoubleStream stream){
		
		return SequenceMFactory.instance.sequenceM(Seq.seq(stream),null);
	}
	/**
	 * Construct a Sequence from a List (prefer this method if the source is a list,
	 * as it allows more efficient Stream reversal).
	 * @param iterable  to construct Sequence from
	 * @return SequenceM
	 */
	public static <T> SequenceM<T> fromList(List<T> list){
		return SequenceMFactory.instance.sequenceM(list.stream(),list);
	}
	
	/**
	 * Construct a Sequence from an Iterable
	 * @param iterable  to construct Sequence from
	 * @return SequenceM
	 */
	public static <T> SequenceM<T> fromIterable(Iterable<T> iterable){
		return SequenceMFactory.instance.sequenceM(StreamSupport.stream(iterable.spliterator(),false),null);
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
	public static <T,U> Tuple2<SequenceM<T>,SequenceM<U>> unzip(SequenceM<Tuple2<T,U>> sequence){
		Tuple2<SequenceM<Tuple2<T,U>>,SequenceM<Tuple2<T,U>>> tuple2 = sequence.duplicateSequence();
		return new Tuple2(tuple2.v1.map(Tuple2::v1),tuple2.v2.map(Tuple2::v2));
	}
	/**
	 * Unzip a zipped Stream into 3
	 * <pre>
	 * {@code 
	 *    unzip3(SequenceM.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
	 * }
	 * // SequenceM[1,2,3], SequenceM[a,b,c], SequenceM[2l,3l,4l]
	 * </pre>
	 */
	public static <T1,T2,T3> Tuple3<SequenceM<T1>,SequenceM<T2>,SequenceM<T3>> unzip3(SequenceM<Tuple3<T1,T2,T3>> sequence){
		Tuple3<SequenceM<Tuple3<T1,T2,T3>>,SequenceM<Tuple3<T1,T2,T3>>,SequenceM<Tuple3<T1,T2,T3>>> tuple3 = sequence.triplicate();
		return new Tuple3(tuple3.v1.map(Tuple3::v1),tuple3.v2.map(Tuple3::v2),tuple3.v3.map(Tuple3::v3));
	}
	/**
	 * Unzip a zipped Stream into 4
	 * 
	 * <pre>
	 * {@code 
	 * unzip4(SequenceM.of(new Tuple4(1, "a", 2l,'z'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
						"c", 4l,'x')));
		}
		// SequenceM[1,2,3], SequenceM[a,b,c], SequenceM[2l,3l,4l], SequenceM[z,y,x]
	 * </pre>
	 */
	public static <T1,T2,T3,T4> Tuple4<SequenceM<T1>,SequenceM<T2>,SequenceM<T3>,SequenceM<T4>> unzip4(SequenceM<Tuple4<T1,T2,T3,T4>> sequence){
		Tuple4<SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>,SequenceM<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();
		return new Tuple4(quad.v1.map(Tuple4::v1),quad.v2.map(Tuple4::v2),quad.v3.map(Tuple4::v3),quad.v4.map(Tuple4::v4));
	}
	
	
	
	<U> SequenceM<Tuple2<T, U>> crossJoin(Stream<U> other) ;
	<U> SequenceM<Tuple2<T, U>> innerJoin(Stream<U> other, BiPredicate<T, U> predicate);
	<U> SequenceM<Tuple2<T, U>> leftOuterJoin(Stream<U> other, BiPredicate<T, U> predicate);
	 <U> SequenceM<Tuple2<T, U>> rightOuterJoin(Stream<U> other, BiPredicate<T, U> predicate);
	 SequenceM<T> onEmpty(T value);
	 SequenceM<T> onEmptyGet(Supplier<T> supplier);
	 <X extends Throwable> SequenceM<T> onEmptyThrow(Supplier<X> supplier);
	 SequenceM<T> concat(Stream<T> other);
	 SequenceM<T> concat(T other);
	    /**
	     * Concatenate two streams.
	     * <p>
	     * <code><pre>
	     * // (1, 2, 3, 4, 5, 6)
	     * Seq.of(1, 2, 3).concat(4, 5, 6)
	     * </pre></code>
	     *
	     * @see #concat(Stream[])
	     */
	   
	   SequenceM<T> concat(T... other);

	    
	    /**
	     * Get a stream of distinct keys.
	     * <p>
	     * <code><pre>
	     * // (1, 2, 3)
	     * Seq.of(1, 1, 2, -2, 3).distinct(Math::abs)
	     * </pre></code>
	     */
	    <U> SequenceM<T> distinct(Function<? super T, ? extends U> keyExtractor);

	  
	    /**
	     * Zip two streams into one using a {@link BiFunction} to produce resulting values.
	     * <p>
	     * <code><pre>
	     * // ("1:a", "2:b", "3:c")
	     * Seq.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -> i + ":" + s)
	     * </pre></code>
	     *
	     * @see #zip(Seq, BiFunction)
	     */
	    <U, R> SequenceM<R> zip(Seq<U> other, BiFunction<T, U, R> zipper);
	    

	    /**
	     * Shuffle a stream using specified source of randomness
	     * <p>
	     * <code><pre>
	     * // e.g. (2, 3, 1)
	     * Seq.of(1, 2, 3).shuffle(new Random())
	     * </pre></code>
	     */
	    SequenceM<T> shuffle(Random random);

	    
	    
	    /**
	     * Returns a limited interval from a given Stream.
	     * <p>
	     * <code><pre>
	     * // (4, 5)
	     * Seq.of(1, 2, 3, 4, 5, 6).slice(3, 5)
	     * </pre></code>
	     *
	     * @see #slice(Stream, long, long)
	     */
	    SequenceM<T> slice(long from, long to);
	    


	    
	    /**
	     * Sort by the results of function.
	     */
	    <U extends Comparable<? super U>> SequenceM<T> sorted(Function<? super T, ? extends U> function);
	 
	 
	    SequenceM<T> xPer(int x, long time, TimeUnit t);
	    SequenceM<T> onePer(long time, TimeUnit t);
	    SequenceM<T> debounce(long time, TimeUnit t);
	    SequenceM<List<T>> batchByTimeAndSize(int size, long time, TimeUnit t);
	    SequenceM<List<T>> batchByTime(long time, TimeUnit t);
	
}
