package com.aol.cyclops.javaslang.sequence;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collector;

import org.reactivestreams.Publisher;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.javaslang.FromJDK;
import com.aol.cyclops.javaslang.Javaslang;
import com.aol.cyclops.javaslang.ToStream;
import com.aol.cyclops.javaslang.streams.JavaslangHotStream;
import com.aol.cyclops.javaslang.streams.StreamUtils;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.reactivestreams.CyclopsSubscriber;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsLoader;
import com.aol.cyclops.sequence.streamable.AsStreamable;
import com.aol.cyclops.sequence.streamable.Streamable;

import javaslang.Function;
import javaslang.BiFunction;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.control.Option;

public interface Sequence<T> extends Stream<T>,Iterable<T>, Publisher<T>{

	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * <pre>
	 * {@code 
	 *  Sequence.of(Arrays.asList(1,2)).flatten();
	 *  
	 *  //stream of (1,  2);		
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
	default <T1> Sequence<T1> flatten(){
		return fromStream(StreamUtils.flatten(this));	
	}
	
	/**
	 * Type safe unwrap 
	 * <pre>
	 * {@code 
	 * Optional<List<String>> stream = Sequence.of("hello","world")
												.toOptional();
												
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	 * }
	 * 
	 * </pre>
	 * @return
	 */
	default Option<List<T>> toOptionList(){
		return Option.of(toList());
	}
	/**
	 * <pre>
	 * {@code 
	 * CompletableFuture<List<String>> cf = Sequence.of("hello","world")
											.toCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	 * }
	 * </pre>
	 * @return
	 */
	default CompletableFuture<List<T>> toCompletableFuture(){
		return CompletableFuture.completedFuture(toList());
	}
	
	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * <pre>
	 * {@code 
	 * 		assertThat(Sequence.of(1,2,2)
								.cycle(3)
								.collect(Collectors.toList()),
								equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));

	 * 
	 * }
	 * </pre>
	 * @param times
	 *            Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	 default Sequence<T> cycle(int times){
		 return fromStream(StreamUtils.cycle(times));
	 }
	/**
	 * Convert to a Stream with the values infinitely cycled
	 * 
	 * <pre>
	 * {@code 
	 *   assertEquals(asList(1, 1, 1, 1, 1,1),Sequence.of(1).cycle().limit(6).toList());
	 *   }
	 * </pre>
	 * 
	 * @return Stream with values repeated
	 */
	default Sequence<T> cycle(){
		return fromStream(StreamUtils.cycle(this));
	}
	/**
	 * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe.
	 * <pre>
	 * {@code 
	 *  Tuple2<Sequence<Integer>, Sequence<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	 * 
	 * }
	 * </pre>
	 * 
	 * @return duplicated stream
	 */
	default Tuple2<Sequence<T>,Sequence<T>> duplicateSequence(){
		return StreamUtils.duplicate(this).map(s->fromStream(s), s->fromStream(s));
	}
	
	/**
	 * Triplicates a Stream
	 * Buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe.
	 * <pre>
	 * {@code 
	 * 	Tuple3<Sequence<Tuple3<T1,T2,T3>>,Sequence<Tuple3<T1,T2,T3>>,Sequence<Tuple3<T1,T2,T3>>> Tuple3 = sequence.triplicate();
	
	 * }
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	 default Tuple3<Sequence<T>,Sequence<T>,Sequence<T>> triplicate(){
		return StreamUtils.triplicate(this).map(s->fromStream(s), s->fromStream(s), s->fromStream(s));
	}
	/**
	 * Makes four copies of a Stream
	 * Buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe. 
	 * 
	 * <pre>
	 * {@code
	 * 
	 * 		Tuple4<Sequence<Tuple4<T1,T2,T3,T4>>,Sequence<Tuple4<T1,T2,T3,T4>>,Sequence<Tuple4<T1,T2,T3,T4>>,Sequence<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();

	 * }
	 * </pre>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default Tuple4<Sequence<T>,Sequence<T>,Sequence<T>,Sequence<T>> quadruplicate(){
		return StreamUtils.quadruplicate(this).map(s->fromStream(s), s->fromStream(s), s->fromStream(s));
	}
	
	/**
	 * Split at supplied location 
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3).splitAt(1)
	 * 
	 *  //Sequence[1], Sequence[2,3]
	 * }
	 * 
	 * </pre>
	 */
	default Tuple2<Sequence<T>,Sequence<T>> splitSequenceAt(int where){
		return StreamUtils.splitAt(this, where).map(s->fromStream(s), s->fromStream(s));
	}
	/**
	 * Split stream at point where predicate no longer holds
	 * <pre>
	 * {@code
	 *   Sequence.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
	 *   
	 *   //Sequence[1,2,3] Sequence[4,5,6]
	 * }
	 * </pre>
	 */
	default Tuple2<Sequence<T>,Sequence<T>> splitBy(Predicate<T> splitter){
		return StreamUtils.splitBy(this, splitter).map(s->fromStream(s), s->fromStream(s));
	}
	/**
	 * Partition a Stream into two one a per element basis, based on predicate's boolean value
	 * <pre>
	 * {@code 
	 *  Sequence.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
	 *  
	 *  //Sequence[1,3,5], Sequence[2,4,6]
	 * }
	 *
	 * </pre>
	 */
	default Tuple2<Sequence<T>,Sequence<T>> partitionSequence(Predicate<T> splitter){
		return StreamUtils.partition(this, splitter).map(s->fromStream(s), s->fromStream(s));
	}
	
	
	
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
	default Sequence<T> cycle(Monoid<T> m, int times){
		return fromStream(StreamUtils.cycle(this, m, times));
	}

	
	
	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * <pre>
	 * {@code
	 * count =0;
		assertThat(Sequence.of(1,2,2)
							.cycleWhile(next -> count++<6)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	default Sequence<T> cycleWhile(Predicate<? super T> predicate){
		return fromStream(StreamUtils.cycleWhile(this, predicate));
	}

	/**
	 * Repeat in a Stream until specified predicate holds
	 * <pre>
	 * {@code 
	 * 	count =0;
		assertThat(Sequence.of(1,2,2)
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
	default Sequence<T> cycleUntil(Predicate<? super T> predicate){
		return fromStream(StreamUtils.cycleUntil(this, predicate));
	}
	
	
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
	default <S,U> Sequence<Tuple3<T,S,U>> zip3(Stream<? extends S> second,Stream<? extends U> third){
		return fromStream(StreamUtils.zip3(second,third));
	}
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
	 default <T2,T3,T4> Sequence<Tuple4<T,T2,T3,T4>> zip4(Stream<T2> second,Stream<T3> third,Stream<T4> fourth){
		 return fromStream(StreamUtils.zip4(second,third,fourth));
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
	 @Override
	default Sequence<Tuple2<T,Long>> zipWithIndex(){
		 
		return fromStream(Seq.super.zipWithIndex());
	}
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
	default <S, R> Sequence<R> zipSequence(Sequence<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return fromStream(StreamUtils.zipSequence(this, second, zipper));
	}
	/**
	 * Zip this Sequence against any monad type.
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
	default <S, R> Sequence<R> zipAnyM(AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return fromStream(StreamUtils.zipAnyM(this, second, zipper));
	}

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
	 default <S, R> Sequence<R> zipStream(BaseStream<? extends S,? extends BaseStream<? extends S,?>> second,
			BiFunction<? super T, ? super S, ? extends R> zipper){
		 return fromStream(StreamUtils.zipStream(this, second, zipper));
	 }

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
	 * @return Sequence with sliding view
	 */
	default Sequence<List<T>> slidingSequence(int windowSize){
		return fromStream(StreamUtils.sliding(this, windowSize));
	}
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
	 * @return Sequence with sliding view
	 */
	default Sequence<List<T>> slidingSequence(int windowSize,int increment){
		return fromStream(StreamUtils.sliding(this, windowSize, increment));
	}

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
	default Sequence<List<T>> groupedSequence(int groupSize){
		return fromStream(StreamUtils.batchBySize(this, groupSize));
	}
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
	
	default <K> java.util.Map<K, java.util.List<T>> groupBy(Function<? super T, ? extends K> classifier){
		return ToStream.toSequenceM(this).groupBy(a->classifier.apply(a));
	}

	/*
	 * Return the distinct Stream of elements
	 * 
	 * <pre>
	 * {@code List<Integer> list =  Sequence.of(1,2,2,2,5,6)
	 *           	 						 .distinct()
	 *				 						 .collect(Collectors.toList()); 
	 * }
	 *</pre>
	 */
	 default Sequence<T> distinct(){
		 return fromSequenceM(ToStream.toSequenceM(this).distinct());
	 }

	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),Sequence.of("a", "b", "c")
	 * 													.scanLeft(Reducers.toString("")).toList());
	 *         
	 *         }
	 * </pre>
	 * 
	 * @param monoid
	 * @return
	 */
	default Sequence<T> scanLeft(Monoid<T> monoid){
		return fromStream(StreamUtils.scanLeft(this, monoid));
	}
	/**
	 * Scan left
	 * <pre>
	 * {@code 
	 *  assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
	 * }
	 * </pre>
	 */
	default <U> Sequence<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return fromSequenceM(ToStream.toSequenceM(this).scanLeft(seed,(a,b)->function.apply(a,b)));
	}
	
	/**
	 * Scan right
	 * <pre>
	 * {@code 
	 * assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
	 * }
	 * </pre>
	 */
	default Sequence<T> scanRight(Monoid<T> monoid){
		return fromStream(StreamUtils.scanRight(this, monoid));
	}
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
	default <U> Sequence<U> scanRight(U identity,BiFunction<? super T, U, U>  combiner){
		return fromSequenceM(ToStream.toSequenceM(this).scanRight(identity,(a,b)->function.apply(a,b)));
	}
	

	
	/**
	 * <pre>
	 * {@code assertThat(Sequence.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
	 * </pre>
	 * 
	 */
	 default Sequence<T> sort(){
		 return fromStream(Stream.super.sort());
	 }

	/**
	 *<pre>
	 * {@code 
	 * 	assertThat(Sequence.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	 * }
	 * </pre>
	 * @param c
	 *            Compartor to sort with
	 * @return Sorted Monad
	 */
	default Sequence<T> sort(Comparator<? super T> c){
		 return fromStream(Stream.super.sort());
	}

	/**
	 * <pre>
	 * {@code assertThat(Sequence.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
	 * </pre>
	 * 
	
	 * 
	 * @param num
	 *            Number of elemenets to skip
	 * @return Monad converted to Stream with specified number of elements
	 *         skipped
	 */
	default Sequence<T> skip(long num){
		return fromStream(Stream.super.drop(num)));
	}
	/**
	 * 
	 * 
	 * <pre>
	 * {@code
	 * assertThat(Sequence.of(4,3,6,7).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	 * }
	 * </pre>
	 * 
	 * @param p
	 *            Predicate to skip while true
	 * @return Monad converted to Stream with elements skipped while predicate
	 *         holds
	 */
	 default Sequence<T> skipWhile(Predicate<? super T> p){
		 return fromStream(StreamUtils.skipWhile(this, p));
	 }

	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(Sequence.of(4,3,6,7).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
	 * </pre>
	 * 
	 * 
	 * @param p
	 *            Predicate to skip until true
	 * @return Monad converted to Stream with elements skipped until predicate
	 *         holds
	 */
	default Sequence<T> skipUntil(Predicate<? super T> p){
		return fromStream(StreamUtils.skipUntil(this,p));
	}

	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(Sequence.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
	 * </pre>
	 * 
	 * @param num
	 *            Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	default Sequence<T> limit(long num){
		
	}

	/**
	 *
	 * 
	 * <pre>
	 * {@code assertThat(Sequence.of(4,3,6,7).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
	 * </pre>
	 * 
	 * @param p
	 *            Limit while predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	default Sequence<T> limitWhile(Predicate<? super T> p){
		return fromStream(StreamUtils.limitWhile(this, p));
	}
	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(Sequence.of(4,3,6,7).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
	 * </pre>
	 * 
	 * @param p
	 *            Limit until predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	default Sequence<T> limitUntil(Predicate<? super T> p){
		return fromStream(StreamUtils.limitUntil(this, p));
	}
	
	
	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	 * }
	 * </pre>
	 * @param c Predicate to check if all match
	 */
	default boolean  allMatch(Predicate<? super T> c){
		return ToStream.toSequenceM(this).allMatch(c);
	}
	/**
	 * True if a single element matches when Monad converted to a Stream
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	 * }
	 * </pre>
	 * @param c Predicate to check if any match
	 */
	default boolean  anyMatch(Predicate<? super T> c){
		return ToStream.toSequenceM(this).allMatch(c);
	}
	/**
	 * Check that there are specified number of matches of predicate in the Stream
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(Sequence.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	 * }
	 * </pre>
	 * 
	 */
	default boolean xMatch(int num, Predicate<? super T> c){
		return ToStream.toSequenceM(this).xMatch(num,c);
	}
	/* 
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
	 * 
	 * }
	 * </pre>
	 */
	default boolean  noneMatch(Predicate<? super T> c){
		return ToStream.toSequenceM(this).noneMatch(c);
	}
	/**
	 * <pre>
	 * {@code
	 *  assertEquals("123".length(),Sequence.of(1, 2, 3).join().length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	default String join(){
		return ToStream.toSequenceM(this).join();
			
	 }
	/**
	 * <pre>
	 * {@code
	 * assertEquals("1, 2, 3".length(), Sequence.of(1, 2, 3).join(", ").length());
	 * }
	 * </pre>
	 * @return Stream as concatenated String
	 */
	default String join(String sep){
		return ToStream.toSequenceM(this).join(sep);
	}
	/**
	 * <pre>
	 * {@code 
	 * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	 * }
	 * </pre> 
	 *  @return Stream as concatenated String
	 */
	default String join(String sep,String start, String end){
		return ToStream.toSequenceM(this).join(sep,start,end);
	}
	
	
	/**
	 * Extract the minimum as determined by supplied function
	 * 
	 */
	default <C extends Comparable<? super C>> Option<T> minBy(Function<? super T,? extends C> f){
		
		return Stream.super.minBy(f);
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	default Option<T> min(Comparator<? super T> comparator){
		return FromJDK.option(ToStream.toSequenceM(this).min(comparator));
	}
	/**
	 * Extract the maximum as determined by the supplied function
	 * 
	 */
	default <C extends Comparable<C>> Option<T> maxBy(Function<T,C> f){
		return FromJDK.option(ToStream.toSequenceM(this).maxBy(f));
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	default Option<T> max(Comparator<? super T> comparator){
		return FromJDK.option(ToStream.toSequenceM(this).max(comparator));
	}
	
	
	
	
	/**
	 * @return First matching element in sequential order
	 * <pre>
	 * {@code
	 * Sequence.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
	 * 
	 * //3
	 * }
	 * </pre>
	 * (deterministic)
	 * 
	 */
	default Option<T>  findFirst(){
		return FromJDK.option(ToStream.toSequenceM(this).findFirst());
	}
	
	
	/**
	 * Attempt to map this Sequence to the same type as the supplied Monoid (Reducer)
	 * Then use Monoid to reduce values
	 * <pre>
	 * {@code 
	 * Sequence.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
	 * 
	 * //4
	 * }
	 * </pre>
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Monoid<R> reducer){
		return ToStream.toSequenceM(this).mapReduce(reducer);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
	 *  Then use Monoid to reduce values
	 *  
	 *  <pre>
	 *  {@code
	 *  Sequence.of("one","two","three","four")
	 *           .mapReduce(this::toInt,Reducers.toTotalInt());
	 *  
	 *  //10
	 *  
	 *  int toInt(String s){
		if("one".equals(s))
			return 1;
		if("two".equals(s))
			return 2;
		if("three".equals(s))
			return 3;
		if("four".equals(s))
			return 4;
		return -1;
	   }
	 *  }
	 *  </pre>
	 *  
	 * @param mapper Function to map Monad type
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Function<? super T,? extends R> mapper, Monoid<R> reducer){
		return ToStream.toSequenceM(this).mapReduce(mapper,reducer);
	}
	

	/**
	 * Apply multiple collectors Simulataneously to this Monad
	 * 
	 * <pre>{@code
	  	List result =Sequence.of(1,2,3).collect(Stream.of(Collectors.toList(),
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
	default java.util.List collectStream(Stream<Collector> collectors){
		 return ToStream.toSequenceM(this).collectStream(ToStream.toStream(collectors));
	 }
	/**
	 *  Apply multiple Collectors, simultaneously to a Stream
	 * <pre>
	 * {@code 
	 * List result = Sequence.of(1,2,3).collect(
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
	default <R> java.util.List<R> collectIterable(Iterable<Collector> collectors){
		 return ToStream.toSequenceM(this).collectIterable(collectors);
	}
	
	/**
	 * <pre>
	 * {@code 
	 * Sequence.of("hello","2","world","4").reduce(Reducers.toString(","));
	 * 
	 * //hello,2,world,4
	 * }</pre>
	 * 
	 * @param reducer Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	default T reduce(Monoid<T> reducer){
		
		return ToStream.toSequenceM(this).reduce(reducer);
	}
	/* 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
	 * }
	 * </pre>
	 * 
	 */
	 default Option<T> reduce(BinaryOperator<T> accumulator){
		 return FromJDK.option(ToStream.toSequenceM(this).reduce(accumulator));
	 }
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	default T reduce(T identity, BinaryOperator<T> accumulator){
		return ToStream.toSequenceM(this).reduce(identity,accumulator);
	}
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	default <U> U reduce(U identity,
             BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner){
		return ToStream.toSequenceM(this).reduce(identity,accumulator,combiner);
	}
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {@code 
	 * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	   Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	   List<Integer> result = Sequence.of(1,2,3,4)
						.reduce(Arrays.asList(sum,mult).stream() );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param reducers
	 * @return
	 */
	default  List<T> reduce(Stream<Monoid<T>> reducers){
		return StreamUtils.reduce(this, reducers);
		
	}
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {@code 
	 * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		List<Integer> result = Sequence.of(1,2,3,4))
										.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	 * 
	 * }
	 * 
	 * @param reducers
	 * @return
	 */
	 default List<T> reduce(Iterable<Monoid<T>> reducers){
		 return StreamUtils.reduce(this, reducers);
	 }
	
	/**
	 * 
	 *  
		<pre>
		{@code
		Sequence.of("a","b","c").foldLeft(Reducers.toString(""));
       
        // "abc"
        }
        </pre>
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	default T foldLeft(Monoid<T> reducer){
		return StreamUtils.foldLeft(this, reducer);
	}
	/**
	 * foldLeft : immutable reduction from left to right
	 * <pre>
	 * {@code 
	 * 
	 * assertTrue(Sequence.of("a", "b", "c").foldLeft("", String::concat).equals("abc"));
	 * }
	 * </pre>
	 */
	default T foldLeft(T identity,  BinaryOperator<T> accumulator){
		return StreamUtils.foldLeft(this, identity,accumulator);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * <pre>
		{@code
		Sequence.of(1,2,3).foldLeftMapToType(Reducers.toString(""));
       
        // "123"
        }
        </pre>
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldLeftMapToType(Monoid<T> reducer){
		return StreamUtils.foldLeftMapToType(this, reducer);
	}
	/**
	 * 
	 * <pre>
		{@code
		Sequence.of("a","b","c").foldRight(Reducers.toString(""));
       
        // "cab"
        }
        </pre>
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	default T foldRight(Monoid<T> reducer){
		return StreamUtils.foldRight(this, reducer);
	}
	
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * <pre>
		{@code
		Sequence.of(1,2,3).foldRightMapToType(Reducers.toString(""));
       
        // "321"
        }
        </pre>
	 * 
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldRightMapToType(Monoid<T> reducer){
		return StreamUtils.foldRightMapToType(this, reducer);
	}
	/**
	 * <pre>
	 * {@code 
	 * 	Streamable<Integer> repeat = Sequence.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toStreamable();
		
		repeat.sequenceM().toList(); //Arrays.asList(2,4,6,8,10,12));
		repeat.sequenceM().toList() //Arrays.asList(2,4,6,8,10,12));
	 * 
	 * }
	 * 
	 * @return Lazily Convert to a repeatable Streamable
	 * 
	 */
	default Streamable<T> toStreamable(){
		return AsStreamable.fromIterable(this);
	}
	/**
	 * @return This Stream converted to a set
	 */
	default Set<T> toSet(){
		return Stream.super.toSet();
	}
	/**
	 * @return this Stream converted to a list
	 */
	default List<T> toList(){
		return Stream.super.toList();
	}
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#toCollection(java.util.function.Supplier)
	 */
	default <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory){
		return ToStream.toSequenceM(this).toCollection(collectionFactory);
	}
	
	
	
	
	/**
	 * 	<pre>{@code assertTrue(Sequence.of(1,2,3,4).startsWith(Arrays.asList(1,2,3).iterator())) }</pre>

	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	default boolean startsWith(Iterator<T> iterator){
		return ToStream.toSequenceM(this).startsWith(iterator);
	}
	
	/**
	 * @return this Sequence converted to AnyM format
	 */
	default AnyM<T> anyM(){
		return Javaslang.anyM(this);
	}
	
	default <R> Sequence<R> map(Function<? super T,? extends R> fn){
		return fromStream(Stream.super.map(fn));
	}
	
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#peek(java.util.function.Consumer)
	 */
	default Sequence<T>  peek(Consumer<? super T> c){
		return fromStream(Stream.super.peek(c));
	}
	
	/**
	 * flatMap operation
	 * <pre>
	 * {@code
	 * 	assertThat(Sequence.of(1,2)
	 * 						.flatMap(i -> asList(i, -i).stream())
	 * 						.toList(),equalTo(asList(1, -1, 2, -2)));		
 
	 * }
	 * </pre>
	 * @param fn to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	default <R> Sequence<R> flatMap(Function<? super T,? extends Stream<? extends R>> fn){
		return fromStream(Stream.super.flatMap(fn));
	}
	/**
	 * Allows flatMap return type to be any Monad type
	 * <pre>
	 * {@code 
	 * 	assertThat(Sequence.of(1,2,3)).flatMapAnyM(i-> anyM(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));

	 * }</pre>
	 * 
	 * 
	 * @param fn to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	 */
	default <R> Sequence<R> flatMapAnyM(Function<? super T,AnyM<? extends R>> fn){
		return fromSequenceM(ToStream.toSequenceM(this).flatMapAnyM(fn));
	}
	/**
	 * FlatMap where the result is a Collection, flattens the resultant collections into the
	 * host Sequence
	 * <pre>
	 * {@code 
	 * 	Sequence.of(1,2)
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
	default <R> Sequence<R> flatMapCollection(Function<? super T,Collection<? extends R>> fn){
		return flatMap(t->Stream.ofAll(fn.apply(t)));
	}
	/**
	 * flatMap operation
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(Sequence.of(1,2,3)
	 *                      .flatMapStream(i->IntStream.of(i))
	 *                      .toList(),equalTo(Arrays.asList(1,2,3)));

	 * }
	 * </pre>
	 * 
	 * @param fn to be applied
	 * @return new stage in Sequence with flatMap operation to be lazily applied
	*/
	default <R> Sequence<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn){
		return flatMap(t->Stream.ofAll(()->fn.apply(t).iterator()));
	}
	/**
	 * flatMap to optional - will result in null values being removed
	 * <pre>
	 * {@code 
	 * 	assertThat(Sequence.of(1,2,3,null)
	 *                      .flatMapOptional(Optional::ofNullable)
			      			.collect(Collectors.toList()),
			      			equalTo(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * @param fn
	 * @return
	 */
	 default <R> Sequence<R> flatMapOption(Function<? super T,Option<? extends R>> fn){
		 return flatMap(t->fn.apply(t).toStream());
	 }
	/**
	 * flatMap to CompletableFuture - will block until Future complete, although (for non-blocking behaviour use AnyM 
	 *       wrapping CompletableFuture and flatMap to Stream there)
	 *       
	 *  <pre>
	 *  {@code
	 *  	assertThat(Sequence.of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	 *  }
	 *  </pre>
	 *       
	 * @param fn
	 * @return
	 */
	<R> Sequence<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn);

	/**
	 * Perform a flatMap operation where the result will be a flattened stream of Characters
	 * from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {@code 
	 *   List<Character> result = Sequence.of("input.file")
									.flatMapCharSequence(i->"hello world")
									.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	Sequence<Character> flatMapCharSequence(Function<? super T,CharSequence> fn);
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied files.
	 * 
	 * <pre>
	 * {@code
	 * 
		List<String> result = Sequence.of("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.flatMapFile(File::new)
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
	Sequence<String> flatMapFile(Function<? super T,File> fn);
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied URLs 
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = Sequence.of("input.file")
								.flatMapURL(getClass().getClassLoader()::getResource)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	Sequence<String> flatMapURL(Function<? super T, URL> fn) ;
	/**
	  *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied BufferedReaders
	 * 
	 * <pre>
	 * List<String> result = Sequence.of("input.file")
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
	Sequence<String> flatMapBufferedReader(Function<? super T,BufferedReader> fn);

	/* (non-Javadoc)
	 * @see java.util.stream.Stream#filter(java.util.function.Predicate)
	 */
	Sequence<T>  filter(Predicate<? super T> fn);

	
	

	
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#sequential()
	 */
	Sequence<T> sequential() ;
	
	
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#unordered()
	 */
	Sequence<T> unordered();
	
	
	
	
	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) Sequence.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	Sequence<T> intersperse(T value);
	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) Sequence.of(1, "a", 2, "b",3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	<U> Sequence<U> ofType(Class<U> type);
	
	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException Sequence.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	<U> Sequence<U> cast(Class<U> type);
	
	/**
	 * Lazily converts this Sequence into a Collection. This does not trigger the Stream. E.g.
	 * Collection is not thread safe on the first iteration.
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = Sequence.of(1,2,3,4,5)
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
	Collection<T> toLazyCollection();
	/**
	 * Lazily converts this Sequence into a Collection. This does not trigger the Stream. E.g.
	 * 
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = Sequence.of(1,2,3,4,5)
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
	 Collection<T> toConcurrentLazyCollection();
	
	
	/**
	 * <pre>
	 * {@code 
	 * Streamable<Integer> repeat = Sequence.of(1,2,3,4,5,6)
												.map(i->i+2)
												.toConcurrentLazyStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	 * }
	 * </pre>
	 * @return Streamable that replay this Sequence, populated lazily and can be populated
	 * across threads
	 */
	public Streamable<T> toConcurrentLazyStreamable();
	
	/* 
	 * Potentially efficient Sequence reversal. Is efficient if
	 * 
	 * - Sequence created via a range
	 * - Sequence created via a List
	 * - Sequence created via an Array / var args
	 * 
	 * Otherwise Sequence collected into a Collection prior to reversal
	 * 
	 * <pre>
	 * {@code
	 *  assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
	 *  }
	 * </pre>
	 */
	public Sequence<T> reverse();
	
	
	
	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#shuffle()
	 */
	public Sequence<T> shuffle();
	/**
	 * Append Stream to this Sequence
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	Sequence.of(1,2,3)
	 *                                  .appendStream(Sequence.of(100,200,300))
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	 * }
	 * </pre>
	 * 
	 * @param stream to append
	 * @return Sequence with Stream appended
	 */
	public Sequence<T> appendStream(Stream<T> stream);
	/**
	 * Prepend Stream to this Sequence
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = Sequence.of(1,2,3)
	 * 								  .prependStream(of(100,200,300))
									  .map(it ->it+"!!")
									  .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param stream to Prepend
	 * @return Sequence with Stream prepended
	 */
	Sequence<T> prependStream(Stream<T> stream);
	/**
	 * Append values to the end of this Sequence
	 * <pre>
	 * {@code 
	 * List<String> result = Sequence.of(1,2,3)
	 * 								   .append(100,200,300)
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	 * }
	 * </pre>
	 * @param values to append
	 * @return Sequence with appended values
	 */
	Sequence<T> append(T... values);
	/**
	 * Prepend given values to the start of the Stream
	 * <pre>
	 * {@code 
	 * List<String> result = 	Sequence.of(1,2,3)
	 * 									 .prepend(100,200,300)
										 .map(it ->it+"!!")
										 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * }
	 * @param values to prepend
	 * @return Sequence with values prepended
	 */
	 Sequence<T> prepend(T... values) ;
	/**
	 * Insert data into a stream at given position
	 * <pre>
	 * {@code 
	 * List<String> result = 	Sequence.of(1,2,3)
	 * 									 .insertAt(1,100,200,300)
										 .map(it ->it+"!!")
										 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
	 * 
	 * }
	 * </pre>
	 * @param pos to insert data at
	 * @param values to insert
	 * @return Stream with new data inserted
	 */
	Sequence<T> insertAt(int pos, T... values);
	/**
	 * Delete elements between given indexes in a Stream
	 * <pre>
	 * {@code 
	 * List<String> result = 	Sequence.of(1,2,3,4,5,6)
	 * 									 .deleteBetween(2,4)
										 .map(it ->it+"!!")
										 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
	 * }
	 * </pre>
	 * @param start index
	 * @param end index
	 * @return Stream with elements removed
	 */
	Sequence<T> deleteBetween(int start,int end);
	/**
	 * Insert a Stream into the middle of this stream at the specified position
	 * <pre>
	 * {@code 
	 * List<String> result = 	Sequence.of(1,2,3)
	 * 									 .insertStreamAt(1,of(100,200,300))
										 .map(it ->it+"!!")
										 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
	 * }
	 * </pre>
	 * @param pos to insert Stream at
	 * @param stream to insert
	 * @return newly conjoined Sequence
	 */
	Sequence<T> insertStreamAt(int pos, Stream<T> stream);
	
	/**
	 * Access asynchronous terminal operations (each returns a Future)
	 * 
	 * @param exec Executor to use for Stream execution
	 * @return Async Future Terminal Operations
	 */
	FutureOperations<T> futureOperations(Executor exec);
	
	/**
	 * <pre>
	 * {@code
	 *  assertTrue(Sequence.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	 * 
	 * }
	 * 
	 * @param iterable Values to check 
	 * @return true if Sequence ends with values in the supplied iterable
	 */
	boolean endsWith(Iterable<T> iterable);
	/**
	 * <pre>
	 * {@code
	 * assertTrue(Sequence.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6))); 
	 * }
	 * </pre>
	 * 
	 * @param stream Values to check 
	 * @return true if Sequence endswith values in the supplied Stream
	 */
	boolean endsWith(Stream<T> stream);
	/**
	 * Skip all elements until specified time period has passed
	 * <pre>
	 * {@code 
	 * List<Integer> result = Sequence.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		//[4,5,6]
	 * 
	 * }
	 * </pre>
	 * 
	 * @param time Length of time
	 * @param unit Time unit
	 * @return Sequence that skips all elements until time period has elapsed
	 */
	Sequence<T> skip(long time, final TimeUnit unit);
	/**
	 * Return all elements until specified time period has elapsed
	 * <pre>
	 * {@code 
	 * List<Integer> result = Sequence.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		//[1,2,3,4]
	 * }
	 * </pre>
	 * @param time Length of time
	 * @param unit Time unit
	 * @return Sequence that returns all elements until time period has elapsed
	 */
	Sequence<T> limit(long time, final TimeUnit unit);
	/**
	 * assertThat(Sequence.of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	 * 
	 * @param num
	 * @return
	 */
	Sequence<T> skipLast(int num);
	/**
	 * Limit results to the last x elements in a Sequence
	 * <pre>
	 * {@code 
	 * 	assertThat(Sequence.of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	 * 
	 * }
	 * 
	 * @param num of elements to return (last elements)
	 * @return Sequence limited to last num elements
	 */
	Sequence<T> limitLast(int num);

	/**
	 * Turns this Sequence into a HotStream, a connectable Stream, being executed on a thread on the 
	 * supplied executor, that is producing data
	 * <pre>
	 * {@code 
	 *  HotStream<Integer> ints = Sequence.range(0,Integer.MAX_VALUE)
											.hotStream(exec)
											
		
		ints.connect().forEach(System.out::println);									
	 *  //print out all the ints
	 *  //multiple consumers are possible, so other Streams can connect on different Threads
	 *  
	 * }
	 * </pre>
	 * @param e Executor to execute this Sequence on
	 * @return a Connectable HotStream
	 */
	HotStream<T> hotStream(Executor e);
	
	/**
	 * <pre>
	 * {@code 
	 * 	assertThat(Sequence.of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(),equalTo("hello"));
	 * }
	 * </pre>
	 * @return first value in this Stream
	 */
	T firstValue();
	
	/**
	 * <pre>
	 * {@code 
	 *    
	 *    //1
	 *    Sequence.of(1).single(); 
	 *    
	 *    //UnsupportedOperationException
	 *    Sequence.of().single();
	 *     
	 *     //UnsupportedOperationException
	 *    Sequence.of(1,2,3).single();
	 * }
	 * </pre>
	 * 
	 * @return a single value or an UnsupportedOperationException if 0/1 values in this Stream
	 */
	default T single(){
		Iterator<T> it = iterator();
		if(it.hasNext()){
			T result = it.next();
			if(!it.hasNext())
				return result;
		}
		throw new UnsupportedOperationException("single only works for Streams with a single value");
		
	}

	/**
	 * <pre>
	 * {@code 
	 *    
	 *    //Optional[1]
	 *    Sequence.of(1).singleOptional(); 
	 *    
	 *    //Optional.empty
	 *    Sequence.of().singleOpional();
	 *     
	 *     //Optional.empty
	 *    Sequence.of(1,2,3).singleOptional();
	 * }
	 * </pre>
	 * 
	 * @return An Optional with single value if this Stream has exactly one element, otherwise Optional Empty
	 */
	default Option<T> singleOptional(){
		Iterator<T> it = iterator();
		if(it.hasNext()){
			T result = it.next();
			if(!it.hasNext())
				return Option.of(result);
		}
		return Option.none();
		
	}

	/**
	 * Return the elementAt index or Optional.empty
	 * <pre>
	 * {@code
	 * 	assertThat(Sequence.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
	 * }
	 * </pre>
	 * @param index to extract element from
	 * @return elementAt index
	 */
	default Option<T> elementAt(long index){
		return this.zipWithIndex()
				   .filter(t->t.v2==index)
				   .findFirst()
				   .map(t->t.v1());
	}
	/**
	 * Gets the element at index, and returns a Tuple containing the element (it must be present)
	 * and a lazy copy of the Sequence for further processing.
	 * 
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3,4,5).get(2).v1
	 * //3
	 * }
	 * </pre>
	 * 
	 * @param index to extract element from
	 * @return Element and Sequence
	 */
	default Tuple2<T,Sequence<T>> get(long index){
		 Tuple2<Sequence<T>, Sequence<T>> tuple = this.duplicateSequence();
		 return tuple.map1(s->s.zipWithIndex()
				   .filter(t->t.v2==index)
				   .findFirst()
				   .map(t->t.v1()).get());
	}
	
	/**
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3,4,5)
				 .elapsed()
				 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @return Sequence that adds the time between elements in millis to each element
	 */
	default Sequence<Tuple2<T,Long>> elapsed(){
		AtomicLong last = new AtomicLong(System.currentTimeMillis());
		
		return zip(Sequence.generate( ()->{
		long now =System.currentTimeMillis();
		
		long result =  now-last.get();
		last.set(now);
		return result;
		} ));
	}
	/**
	 * <pre>
	 * {@code
	 *    Sequence.of(1,2,3,4,5)
				   .timestamp()
				   .forEach(System.out::println)
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @return Sequence that adds a timestamp to each element
	 */
	default Sequence<Tuple2<T,Long>> timestamp(){
		return zip(Sequence.generate( ()->System.currentTimeMillis()));
	}
	

	
	/**
	 * Create a subscriber that can listen to Reactive Streams (simple-react, RxJava
	 * AkkaStreams, Kontraktor, QuadarStreams etc)
	 * 
	 * <pre>
	 * {@code
	 * CyclopsSubscriber<Integer> sub = Sequence.subscriber();
		Sequence.of(1,2,3).subscribe(sub);
		sub.sequenceM().forEach(System.out::println);
		
		  1 
		  2
		  3
	 * }
	 * 
	 *</pre>
	 * 
	 * @return A reactive-streams Subscriber
	 */
	public static <T> CyclopsSubscriber<T> subscriber(){
		return ReactiveStreamsLoader.subscriber.get().subscribe();
	}
	
	
	/**
	 * Create an efficiently reversable Sequence from the provided elements
	 * @param elements To Construct sequence from
	 * @return
	 */
	public static <T> Sequence<T> of(T... elements){
		ReversingArraySpliterator array = new ReversingArraySpliterator<T>(elements,false,0);
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(array, false),array);
		
	}
	/**
	 * Construct a Reveresed Sequence from the provided elements
	 * Can be reversed (again) efficiently
	 * @param elements To Construct sequence from
	 * @return
	 */
	public static <T> Sequence<T> reversedOf(T... elements){
		ReversingArraySpliterator array = new ReversingArraySpliterator<T>(elements, false,0).invert();
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(array, false),array);
		
	}
	/**
	 * Construct a Reveresed Sequence from the provided elements
	 * Can be reversed (again) efficiently
	 * @param elements To Construct sequence from
	 * @return
	 */
	public static <T> Sequence<T> reversedListOf(List<T> elements){
		Objects.requireNonNull(elements);
		ReversingListSpliterator list = new ReversingListSpliterator<T>(elements, false).invert();
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(list, false),list);

	}
	/**
	 * Create an efficiently reversable Sequence that produces the integers between start 
	 * and end
	 * @param start Number of range to start from
	 * @param end Number for range to end at
	 * @return Range Sequence
	 */
	public static Sequence<Integer> range(int start, int end){
		ReversingRangeIntSpliterator range = new ReversingRangeIntSpliterator(start, end, false);
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(range, false),range);

	}
	/**
	 * Create an efficiently reversable Sequence that produces the integers between start 
	 * and end
	 * @param start Number of range to start from
	 * @param end Number for range to end at
	 * @return Range Sequence
	 */
	public static Sequence<Long> rangeLong(long start, long end){
		ReversingRangeLongSpliterator range = new ReversingRangeLongSpliterator(start, end, false);
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(range, false),range);

	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static <T> Sequence<T> fromStream(Stream<T> stream){
		Objects.requireNonNull(stream);
		if(stream instanceof Sequence)
			return (Sequence)stream;
		return SequenceFactory.instance.sequenceM(stream,null);
	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static Sequence<Integer> fromIntStream(IntStream stream){
		Objects.requireNonNull(stream);
		return SequenceFactory.instance.sequenceM(Seq.seq(stream),null);
	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static Sequence<Long> fromLongStream(LongStream stream){
		Objects.requireNonNull(stream);
		return SequenceFactory.instance.sequenceM(Seq.seq(stream),null);
	}
	/**
	 * Construct a Sequence from a Stream
	 * @param stream Stream to construct Sequence from
	 * @return
	 */
	public static Sequence<Double> fromDoubleStream(DoubleStream stream){
		Objects.requireNonNull(stream);
		return SequenceFactory.instance.sequenceM(Seq.seq(stream),null);
	}
	/**
	 * Construct a Sequence from a List (prefer this method if the source is a list,
	 * as it allows more efficient Stream reversal).
	 * @param iterable  to construct Sequence from
	 * @return Sequence
	 */
	public static <T> Sequence<T> fromList(List<T> list){
		Objects.requireNonNull(list);
		ReversingListSpliterator array = new ReversingListSpliterator<T>(list,false);
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(array, false),array);
	}
	
	/**
	 * Construct a Sequence from an Iterable
	 * @param iterable  to construct Sequence from
	 * @return Sequence
	 */
	public static <T> Sequence<T> fromIterable(Iterable<T> iterable){
		Objects.requireNonNull(iterable);
		return SequenceFactory.instance.sequenceM(StreamSupport.stream(iterable.spliterator(),false),null);
	}
	/**
	 * Construct a Sequence from an Iterator
	 * @param iterator  to construct Sequence from
	 * @return Sequence
	 */
	public static <T> Sequence<T> fromIterator(Iterator<T> iterator){
		Objects.requireNonNull(iterator);
		return fromIterable( ()-> iterator);
	}
    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> Sequence<T> iterate(final T seed, final UnaryOperator<T> f) {
        return SequenceFactory.instance.sequenceM(Stream.iterate(seed, f),null);
    }

   
    /**
     * @see Stream#generate(Supplier)
     */
    static <T> Sequence<T> generate(Supplier<T> s) {
        return SequenceFactory.instance.sequenceM(Stream.generate(s),null);
    }
	/**
	 * Unzip a zipped Stream 
	 * 
	 * <pre>
	 * {@code 
	 *  unzip(Sequence.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c")))
	 *  
	 *  // Sequence[1,2,3], Sequence[a,b,c]
	 * }
	 * 
	 * </pre>
	 * 
	 */
	public static <T,U> Tuple2<Sequence<T>,Sequence<U>> unzip(Sequence<Tuple2<T,U>> sequence){
		Tuple2<Sequence<Tuple2<T,U>>,Sequence<Tuple2<T,U>>> tuple2 = sequence.duplicateSequence();
		return new Tuple2(tuple2.v1.map(Tuple2::v1),tuple2.v2.map(Tuple2::v2));
	}
	/**
	 * Unzip a zipped Stream into 3
	 * <pre>
	 * {@code 
	 *    unzip3(Sequence.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
	 * }
	 * // Sequence[1,2,3], Sequence[a,b,c], Sequence[2l,3l,4l]
	 * </pre>
	 */
	public static <T1,T2,T3> Tuple3<Sequence<T1>,Sequence<T2>,Sequence<T3>> unzip3(Sequence<Tuple3<T1,T2,T3>> sequence){
		Tuple3<Sequence<Tuple3<T1,T2,T3>>,Sequence<Tuple3<T1,T2,T3>>,Sequence<Tuple3<T1,T2,T3>>> tuple3 = sequence.triplicate();
		return new Tuple3(tuple3.v1.map(Tuple3::v1),tuple3.v2.map(Tuple3::v2),tuple3.v3.map(Tuple3::v3));
	}
	/**
	 * Unzip a zipped Stream into 4
	 * 
	 * <pre>
	 * {@code 
	 * unzip4(Sequence.of(new Tuple4(1, "a", 2l,'z'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
						"c", 4l,'x')));
		}
		// Sequence[1,2,3], Sequence[a,b,c], Sequence[2l,3l,4l], Sequence[z,y,x]
	 * </pre>
	 */
	public static <T1,T2,T3,T4> Tuple4<Sequence<T1>,Sequence<T2>,Sequence<T3>,Sequence<T4>> unzip4(Sequence<Tuple4<T1,T2,T3,T4>> sequence){
		Tuple4<Sequence<Tuple4<T1,T2,T3,T4>>,Sequence<Tuple4<T1,T2,T3,T4>>,Sequence<Tuple4<T1,T2,T3,T4>>,Sequence<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();
		return new Tuple4(quad.v1.map(Tuple4::v1),quad.v2.map(Tuple4::v2),quad.v3.map(Tuple4::v3),quad.v4.map(Tuple4::v4));
	}
	
	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#crossJoin(java.util.stream.Stream)
	 */
	<U> Sequence<Tuple2<T, U>> crossJoin(Stream<U> other);

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#innerJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	default <U> Sequence<Tuple2<T, U>> innerJoin(Stream<U> other,
			BiPredicate<T, U> predicate){
		 Streamable<U> s = Streamable.fromIterable(Sequence.fromStream(other).toLazyCollection());

	        return flatMap(t -> s.stream()
	                           .filter(u -> predicate.test(t, u))
	                           .map(u -> Tuple.tuple(t, u)));
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#leftOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	default <U> Sequence<Tuple2<T, U>> leftOuterJoin(Stream<U> other,
			BiPredicate<T, U> predicate)
		{

		 Streamable<U> s =Streamable.fromIterable(Sequence.fromStream(other).toLazyCollection());

	        return flatMap(t -> Seq.seq(s.stream())
	                           .filter(u -> predicate.test(t, u))
	                           .onEmpty(null)
	                           .map(u -> Tuple.tuple(t, u)));
	    
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#rightOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	<U> Sequence<Tuple2<T, U>> rightOuterJoin(Stream<U> other,
			BiPredicate<T, U> predicate);
	/** If this Sequence is empty replace it with a another Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(4,5,6)
							.onEmptySwitch(()->Sequence.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
	 * }
	 * </pre>
	 * @param switchTo Supplier that will generate the alternative Stream
	 * @return Sequence that will switch to an alternative Stream if empty
	 */
	default Sequence<T> onEmptySwitch(Supplier<Stream<T>> switchTo){
		AtomicBoolean called=  new AtomicBoolean(false);
		return Sequence.fromStream(onEmptyGet((Supplier)()->{
				called.set(true); 
				return switchTo.get();
		}).flatMap(s->{
			if(called.get())
				return (Stream)s;
			return Stream.of(s);
		}));
	}
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#onEmpty(java.lang.Object)
	 */
	Sequence<T> onEmpty(T value);

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#onEmptyGet(java.util.function.Supplier)
	 */
	Sequence<T> onEmptyGet(Supplier<T> supplier);

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#onEmptyThrow(java.util.function.Supplier)
	 */
	default <X extends Throwable> Sequence<T> onEmptyThrow(Supplier<X> supplier){
		return Sequence.fromStream(Seq.super.onEmptyThrow(supplier));
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#concat(java.util.stream.Stream)
	 */
	Sequence<T> concat(Stream<T> other);

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#concat(java.lang.Object)
	 */
	Sequence<T> concat(T other);

	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#concat(java.lang.Object[])
	 */
	Sequence<T> concat(T... other);

	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#distinct(java.util.function.Function)
	 */
	<U> Sequence<T> distinct(Function<? super T, ? extends U> keyExtractor);

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
	 */
	<U, R> Sequence<R> zip(Seq<U> other, BiFunction<T, U, R> zipper);

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#shuffle(java.util.Random)
	 */
	Sequence<T> shuffle(Random random);

	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	Sequence<T> slice(long from, long to);

	
	/* (non-Javadoc)
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	<U extends Comparable<? super U>> Sequence<T> sorted(
			Function<? super T, ? extends U> function);

	/**
	 * emit x elements per time period 
	 * 
	 * <pre>
	 * {@code 
	 *  SimpleTimer timer = new SimpleTimer();
		assertThat(Sequence.of(1,2,3,4,5,6)
		                    .xPer(6,100000000,TimeUnit.NANOSECONDS)
		                    .collect(Collectors.toList()).size(),is(6));

	 * }
	 * </pre>
	 * @param x number of elements to emit
	 * @param time period
	 * @param t Time unit
	 * @return Sequence that emits x elements per time period
	 */
	Sequence<T> xPer(int x, long time, TimeUnit t);

	/**
	 * emit one element per time period
	 * <pre>
	 * {@code 
	 * Sequence.iterate("", last -> "next")
				.limit(100)
				.batchBySize(10)
				.onePer(1, TimeUnit.MICROSECONDS)
				.peek(batch -> System.out.println("batched : " + batch))
				.flatMap(Collection::stream)
				.peek(individual -> System.out.println("Flattened : "
						+ individual))
				.forEach(a->{});
	 * }
	 * @param time period
	 * @param t Time unit
	 * @return Sequence that emits 1 element per time period
	 */
	Sequence<T> onePer(long time, TimeUnit t);

	/**
	 * Allow one element through per time period, drop all other 
	 * elements in that time period
	 * 
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3,4,5,6)
	 *          .debounce(1000,TimeUnit.SECONDS).toList();
	 *          
	 * // 1 
	 * }</pre>
	 * 
	 * @param time
	 * @param t
	 * @return
	 */
	Sequence<T> debounce(long time, TimeUnit t);

	/**
	 * Batch elements by size into a List
	 * 
	 * <pre>
	 * {@code
	 * Sequence.of(1,2,3,4,5,6)
				.batchBySizeAndTime(3,10,TimeUnit.SECONDS)
				.toList();
			
	 * //[[1,2,3],[4,5,6]] 
	 * }
	 * 
	 * @param size Max size of a batch
	 * @param time (Max) time period to build a single batch in
	 * @param t time unit for batch
	 * @return Sequence batched by size and time
	 */
	Sequence<List<T>> batchBySizeAndTime(int size, long time, TimeUnit t);
	/**
	 *  Batch elements by size into a collection created by the supplied factory 
	 * <pre>
	 * {@code 
	 * List<ArrayList<Integer>> list = of(1,2,3,4,5,6)
					.batchBySizeAndTime(10,1,TimeUnit.MICROSECONDS,()->new ArrayList<>())
					.toList();
	 * }
	 * </pre>
	 * @param size Max size of a batch
	 * @param time (Max) time period to build a single batch in
	 * @param unit time unit for batch
	 * @param factory Collection factory
	 * @return Sequence batched by size and time
	 */
	<C extends Collection<T>> Sequence<C> batchBySizeAndTime(int size,long time, TimeUnit unit, Supplier<C> factory);
	/**
	 * Batch elements in a Stream by time period
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
	 * assertThat(Sequence.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	 * }
	 * </pre>
	 * 
	 * @param time - time period to build a single batch in
	 * @param t  time unit for batch
	 * @return Sequence batched into lists by time period
	 */
	Sequence<List<T>> batchByTime(long time, TimeUnit t);
	/**
	 * Batch elements by time into a collection created by the supplied factory 
	 * 
	 * <pre>
	 * {@code 
	 *   assertThat(Sequence.of(1,1,1,1,1,1)
	 *                       .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
	 *                       .toList()
	 *                       .get(0)
	 *                       .size(),is(1));
	 * }
	 * </pre>
	 * 
	 * @param time - time period to build a single batch in
	 * @param unit time unit for batch
	 * @param factory Collection factory
	 * @return Sequence batched into collection types by time period
	 */
	<C extends Collection<T>> Sequence<C> batchByTime(long time, TimeUnit unit, Supplier<C> factory);
	/**
	 * Batch elements in a Stream by size into Lists
	 * 
	 * <pre>
	 * {@code 
	 *  assertThat(Sequence.of(1,2,3,4,5,6)
	 *                      .batchBySize(3)
	 *                      .collect(Collectors.toList())
	 *                      .size(),is(2));
	 * }
	 * @param size of batch
	 * @return Sequence batched by size into Lists
	 */
	Sequence<List<T>> batchBySize(int size);
	/**
	 * Batch elements in a Stream by size into a collection created by the supplied factory 
	 * <pre>
	 * {@code
	 * assertThat(Sequence.of(1,1,1,1,1,1)
	 * 						.batchBySize(3,()->new TreeSet<>())
	 * 						.toList()
	 * 						.get(0)
	 * 						.size(),is(1));
	 * }
	 * 
	 * @param size batch size
	 * @param supplier Collection factory
	 * @return Sequence batched into collection types by size
	 */
	<C extends Collection<T>>Sequence<C> batchBySize(int size, Supplier<C> supplier);

	/**
	 * emit elements after a fixed delay
	 * <pre>
	 * {@code 
	 * 	SimpleTimer timer = new SimpleTimer();
		assertThat(Sequence.of(1,2,3,4,5,6)
							.fixedDelay(10000,TimeUnit.NANOSECONDS)
							.collect(Collectors.toList())
							.size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
	 * }
	 * </pre>
	 * @param l time length in nanos of the delay
	 * @param unit for the delay
	 * @return Sequence that emits each element after a fixed delay
	 */
	Sequence<T> fixedDelay(long l, TimeUnit unit);

	/**
	 * Introduce a random jitter / time delay between the emission of elements
	 * <pre>
	 * {@code 
	 * SimpleTimer timer = new SimpleTimer();
		assertThat(Sequence.of(1,2,3,4,5,6)
							.jitter(10000)
							.collect(Collectors.toList())
							.size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(20000l));
	 * }
	 * </pre>
	 * @param maxJitterPeriodInNanos - random number less than this is used for each jitter
	 * @return Sequence with a random jitter between element emission
	 */
	Sequence<T> jitter(long maxJitterPeriodInNanos);
	/**
	 * Create a Sequence of Streamables (replayable Streams / Sequences) where each Streamable is populated up to a max size,
	 * or for max period of time
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5,6)
						.windowBySizeAndTime(3,10,TimeUnit.SECONDS)
						.toList()
						.get(0)
						.stream()
						.count(),is(3l));
	 * 
	 * }
	 * @param maxSize of window
	 * @param maxTime of window
	 * @param maxTimeUnit of window
	 * @return Windowed Sequence
	 */
	Sequence<Streamable<T>> windowBySizeAndTime(int maxSize, long maxTime, TimeUnit maxTimeUnit);
	/**
	 * Create a Sequence of Streamables (replayable Streams / Sequences) where each Streamable is populated 
	 * while the supplied predicate holds. When the predicate failsa new window/ Stremable opens
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.forEach(System.out::println);
	 *   
	 *  StreamableImpl(streamable=[1, 2, 3]) 
	 *  StreamableImpl(streamable=[4, 5, 6])
	 * }
	 * </pre>
	 * @param predicate Window while true
	 * @return Sequence windowed while predicate holds
	 */
	Sequence<Streamable<T>> windowWhile(Predicate<T> predicate);
	/**
	 * Create a Sequence of Streamables (replayable Streams / Sequences) where each Streamable is populated 
	 * until the supplied predicate holds. When the predicate failsa new window/ Stremable opens
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3,4,5,6)
				.windowUntil(i->i%3==0)
				.forEach(System.out::println);
	 *   
	 *  StreamableImpl(streamable=[1, 2, 3]) 
	 *  StreamableImpl(streamable=[4, 5, 6])
	 * }
	 * </pre>
	 * @param predicate Window until true
	 * @return Sequence windowed until predicate holds
	 */
	Sequence<Streamable<T>> windowUntil(Predicate<T> predicate);
	/**
	 * Create Sequence of Streamables  (replayable Streams / Sequences) where each Streamable is populated 
	 * while the supplied bipredicate holds. The bipredicate recieves the Streamable from the last window as
	 * well as the current value and can choose to aggregate the current value or create  a new window
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5,6)
				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
				.toList().size(),equalTo(5));
	 * }
	 * </pre>
	 * 
	 * @param predicate Window while true
	 * @return Sequence windowed while predicate holds
	 */
	Sequence<Streamable<T>> windowStatefullyWhile(BiPredicate<Streamable<T>,T> predicate);
	/**
	 * Create Sequence of Streamables  (replayable Streams / Sequences) where each Streamable is populated
	 * within a specified time window
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5, 6)
							.map(n-> n==6? sleep(1) : n)
							.windowByTime(10,TimeUnit.MICROSECONDS)
							.toList()
							.get(0).sequenceM().toList()
							,not(hasItem(6)));
	 * }
	 * </pre>
	 * @param time max time per window 
	 * @param t time unit per window
	 * @return Sequence windowed by time
	 */
	Sequence<Streamable<T>> windowByTime(long time, TimeUnit t);
	/**
	 * Create a Sequence batched by List, where each batch is populated until the predicate holds
	 * <pre>
	 * {@code 
	 *  assertThat(Sequence.of(1,2,3,4,5,6)
				.batchUntil(i->i%3==0)
				.toList()
				.size(),equalTo(2));
	 * }
	 * </pre>
	 * @param predicate Batch until predicate holds, then open next batch
	 * @return Sequence batched into lists determined by the predicate supplied
	 */
	Sequence<List<T>> batchUntil(Predicate<T> predicate);
	/**
	 * Create a Sequence batched by List, where each batch is populated while the predicate holds
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5,6)
				.batchWhile(i->i%3!=0)
				.toList().size(),equalTo(2));
	
	 * }
	 * </pre>
	 * @param predicate Batch while predicate holds, then open next batch
	 * @return Sequence batched into lists determined by the predicate supplied
	 */
	Sequence<List<T>> batchWhile(Predicate<T> predicate);
	/**
	 * Create a Sequence batched by a Collection, where each batch is populated while the predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5,6)
				.batchWhile(i->i%3!=0)
				.toList()
				.size(),equalTo(2));
	 * }
	 * </pre>
	 * @param predicate Batch while predicate holds, then open next batch
	 * @param factory Collection factory
	 * @return Sequence batched into collections determined by the predicate supplied
	 */
	<C extends Collection<T>>  Sequence<C> batchWhile(Predicate<T> predicate, Supplier<C> factory);
	/**
	 * Create a Sequence batched by a Collection, where each batch is populated until the predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4,5,6)
				.batchUntil(i->i%3!=0)
				.toList()
				.size(),equalTo(2));
	 * }
	 * </pre>
	 * 
	 * 
	 * @param predicate Batch until predicate holds, then open next batch
	 * @param factory Collection factory
	 * @return Sequence batched into collections determined by the predicate supplied
	 */
	<C extends Collection<T>>  Sequence<C> batchUntil(Predicate<T> predicate, Supplier<C> factory);

	/**
	 * Recover from an exception with an alternative value
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4)
						   .map(i->i+2)
						   .map(u->{throw new RuntimeException();})
						   .recover(e->"hello")
						   .firstValue(),equalTo("hello"));
	 * }
	 * </pre>
	 * @param fn Function that accepts a Throwable and returns an alternative value
	 * @return Sequence that can recover from an Exception
	 */
	Sequence<T> recover(final Function<Throwable, T> fn);
	/**
	 * Recover from a particular exception type
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Sequence.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(IOException.class,e->"hello")
					.firstValue(),equalTo("hello"));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param exceptionClass Type to recover from
	 * @param fn That accepts an error and returns an alternative value
	 * @return Sequence that can recover from a particular exception
	 */
	<EX extends Throwable> Sequence<T> recover(Class<EX> exceptionClass, final Function<EX, T> fn);
	
	
	/**
	 * Retry a transformation if it fails. Default settings are to retry up to 7 times, with an doubling
	 * backoff period starting @ 2 seconds delay before retry.
	 * 
	 * <pre>
	 * {@code 
	 * given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException(new SocketException("First")),
				new RuntimeException(new IOException("Second"))).willReturn(
				"42");

	
		String result = Sequence.of( 1,  2, 3)
				.retry(serviceMock)
				.firstValue();

		assertThat(result, is("42"));
	 * }
	 * </pre>
	 * @param fn Function to retry if fails
	 * 
	 */
	default <R> Sequence<R> retry(Function<T,R> fn){
		Function<T,R> retry = t-> {
			int count = 7;
			int[] sleep ={2000};
			Throwable exception=null;
			while(count-->0){
				try{
					return fn.apply(t);
				}catch(Throwable e){
					exception = e;
				}
				ExceptionSoftener.softenRunnable(()->Thread.sleep(sleep[0]));
				
				sleep[0]=sleep[0]*2;
			}
			throw ExceptionSoftener.throwSoftenedException(exception);
			
		};
		return map(retry);
	}
	
	/**
	 * Remove all occurances of the specified element from the Sequence
	 * <pre>
	 * {@code
	 * 	Sequence.of(1,2,3,4,5,1,2,3).remove(1)
	 * 
	 *  //Streamable[2,3,4,5,2,3]
	 * }
	 * </pre>
	 * 
	 * @param t element to remove
	 * @return Filtered Stream / Sequence
	 */
	default Sequence<T> remove(T t){
		return this.filter(v->v!=t);
	}
	
	/**
	 * Generate the permutations based on values in the Sequence
	 * Makes use of Streamable to store intermediate stages in a collection 
	 * 
	 * 
	 * @return Permutations from this Sequence
	 */
	default Sequence<Stream<T>> permutations() {
		return fromStream(Stream.super.permutations());
	 }
	
	/**
	 * Return a Stream with elements before the provided start index removed, and elements after the provided
	 * end index removed
	 * 
	 * <pre>
	 * {@code 
	 *   Sequence.of(1,2,3,4,5,6).subStream(1,3);
	 *   
	 *   
	 *   //Sequence[2,3]
	 * }
	 * </pre>
	 * 
	 * @param start index inclusive
	 * @param end index exclusive
	 * @return Sequence between supplied indexes of original Sequence
	 */
	default Sequence<T> subSequence(int start, int end){
		return fromStream(Stream.super.subSequence(start, end));
		
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
	 * {@code 
	 * HotStream<Data> dataStream = SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            							  .map(this::processJob)
	 *            							  .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
	 * 
	 * 
	 * data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * 
	 * 
	 * 
	 * @param cron Expression that determines when each job will run
	 * @param ex ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	JavaslangHotStream<T> schedule(String cron,ScheduledExecutorService ex);
	
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
	 * {@code 
	 * HotStream<Data> dataStream = SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            							  .map(this::processJob)
	 *            							  .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
	 * 
	 * 
	 * data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param delay Between last element completes passing through the Stream until the next one starts
	 * @param ex ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	JavaslangHotStream<T> scheduleFixedDelay(long delay,ScheduledExecutorService ex);
	
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
	 * HotStream<Data> dataStream = SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            							  .map(this::processJob)
	 *            							  .scheduleFixedRate(60_000,Executors.newScheduledThreadPool(1));
	 * 
	 * 
	 * data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * @param rate Time in millis between job runs
	 * @param ex ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	JavaslangHotStream<T> scheduleFixedRate(long rate,ScheduledExecutorService ex);
	
	/**
	 * [equivalent to count]
	 * 
	 * @return size
	 */
	default int size(){
		return this.toList().size();
	}
	/** 
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	  *<pre>
	 * {@code 
	 * Sequence.of(1,2)
						.forEach2(a->IntStream.range(10,13),
						.forEach2(a->b->Stream.of(""+(a+b),"hello world"),
									a->b->c->c+":"a+":"+b);
									
	 * 
	 *  //Sequence[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre> 
	 * @param stream1 Nested Stream to iterate over
	 * @param stream2 Nested Stream to iterate over
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return Sequence with elements generated via nested iteration
	 */
	<R1,R2,R> Sequence<R> forEach3(Function<T,? extends BaseStream<R1,? extends BaseStream<? extends R1,?>>> stream1, 
													BiFunction<T,R1,? extends BaseStream<R2,? extends BaseStream<? extends R2,?>>> stream2,
													Function<T,Function<R1,Function<R2,R>>> yieldingFunction );
	
	


	
	
	/**
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	 * 
	 * @param stream1 Nested Stream to iterate over
	 * @param stream2 Nested Stream to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return Sequence with elements generated via nested iteration
	 */
	<R1,R2,R> Sequence<R> forEach3(Function<T,? extends BaseStream<R1,? extends BaseStream<? extends R1,?>>> stream1, 
													BiFunction<T,R1,? extends BaseStream<R2,? extends BaseStream<? extends R2,?>>> stream2,
															Function<T,Function<R1,Function<R2,Boolean>>> filterFunction,
													Function<T,Function<R1,Function<R2,R>>> yieldingFunction );
	
	

	
	
	
	
	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3)
						.forEach2(a->IntStream.range(10,13),
									a->b->a+b);
									
	 * 
	 *  //Sequence[11,14,12,15,13,16]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1 Nested Stream to iterate over
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return Sequence with elements generated via nested iteration
	 */
	<R1,R> Sequence<R> forEach2(Function<T,? extends BaseStream<R1,? extends BaseStream<? extends R1,?>>> stream1, 
													Function<T,Function<R1,R>> yieldingFunction );

	
	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * Sequence.of(1,2,3)
						.forEach2(a->IntStream.range(10,13),
						            a->b-> a<3 && b>10,
									a->b->a+b);
									
	 * 
	 *  //Sequence[14,15]
	 * }
	 * </pre>
	 * @param stream1 Nested Stream to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return Sequence with elements generated via nested iteration
	 */
	<R1,R> Sequence<R> forEach2(Function<T,? extends BaseStream<R1,? extends BaseStream<? extends R1,?>>> stream1, 
												Function<T, Function<R1, Boolean>> filterFunction,
													Function<T,Function<R1,R>> yieldingFunction );

}
