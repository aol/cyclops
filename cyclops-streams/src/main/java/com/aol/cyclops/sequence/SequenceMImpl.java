package com.aol.cyclops.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
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
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.internal.AsGenericMonad;
import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsLoader;
import com.aol.cyclops.sequence.spliterators.ReversableSpliterator;
import com.aol.cyclops.sequence.streamable.AsStreamable;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.streams.HotStreamImpl;
import com.aol.cyclops.streams.StreamUtils;



public class SequenceMImpl<T> implements Unwrapable, SequenceM<T>, Iterable<T>{
	private final Seq<T> stream;
	private final Optional<ReversableSpliterator> reversable;
	
	public SequenceMImpl(Stream<T> stream){
		this.stream = Seq.seq(stream);
		this.reversable = Optional.empty();
		
	}
	
	public SequenceMImpl(Stream<T> stream,ReversableSpliterator rev){
		this.stream = Seq.seq(stream);
		this.reversable = Optional.of(rev);
		
	}
	public HotStream<T> schedule(String cron,ScheduledExecutorService ex){
		return new HotStreamImpl(this).schedule(cron,ex);
	}
	public HotStream<T> schedule(long delay,ScheduledExecutorService ex){
		return new HotStreamImpl(this).schedule(delay,ex);
	}
	
	
	public final <R> R unwrap(){
		return (R)stream;
	}

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
	public final <T1> SequenceM<T1> flatten(){
		return StreamUtils.flatten(stream);
		
	}
	/**
	 * Type safe unwrap
	 * 
	 * <pre>
	 * {@code 
	 * Stream<String> stream = anyM("hello","world").asSequence().unwrapStream();
		assertThat(stream.collect(Collectors.toList()),equalTo(Arrays.asList("hello","world")));
	 * }
	 * </pre>
	 * @return Stream with current wrapped values
	 */
	public final Stream<T> unwrapStream(){
		
		return stream;
		
	}
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
	public final Optional<List<T>> toOptional(){
		return StreamUtils.streamToOptional(stream);
		
	}
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
	public final CompletableFuture<List<T>> toCompletableFuture(){
		return StreamUtils.streamToCompletableFuture(stream);
		
		
	}
	
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
	public final SequenceM<T> cycle(int times) {
		return StreamUtils.sequenceM(StreamUtils.cycle(times,AsStreamable.fromStream(stream)),reversable);
	}
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
	public final SequenceM<T> cycle() {
		return StreamUtils.sequenceM(StreamUtils.cycle(stream),reversable);
	}
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
	public final Tuple2<SequenceM<T>,SequenceM<T>> duplicateSequence(){
		Tuple2<Stream<T>,Stream<T>> tuple = StreamUtils.duplicate(stream);
		return tuple.map1(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
			  	.map2(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())));
	}
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
	public final Tuple3<SequenceM<T>,SequenceM<T>,SequenceM<T>> triplicate(){
		
		Tuple3<Stream<T>,Stream<T>,Stream<T>> tuple = StreamUtils.triplicate(stream);
		return tuple.map1(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
					.map2(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
					.map3(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())));
		
		
	}
	
	
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
	public final Tuple4<SequenceM<T>,SequenceM<T>,SequenceM<T>,SequenceM<T>> quadruplicate(){
		Tuple4<Stream<T>,Stream<T>,Stream<T>,Stream<T>> tuple = StreamUtils.quadruplicate(stream);
		return tuple.map1(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
				  	.map2(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
					.map3(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
					.map4(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())));
	}
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
	public final Tuple2<Optional<T>,SequenceM<T>> splitSequenceAtHead(){
		Tuple2<SequenceM<T>,SequenceM<T>> Tuple2 = splitAt(1);
		return new Tuple2(Tuple2.v1.toOptional()
							.flatMap( l-> l.size()>0 ? Optional.of(l.get(0)) : Optional.empty()  )
							,Tuple2.v2);
	} 
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
	public final Tuple2<SequenceM<T>,SequenceM<T>> splitAt(int where){
		return StreamUtils.splitAt(stream, where)
				   .map1(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
				   .map2(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())));
		
	}
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
	public final Tuple2<SequenceM<T>,SequenceM<T>> splitBy(Predicate<T> splitter){
		return StreamUtils.splitBy(stream, splitter)
				   .map1(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
				   .map2(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())));
	}
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
	public final Tuple2<SequenceM<T>,SequenceM<T>> partitionSequence(Predicate<T> splitter){
		return StreamUtils.partition(stream, splitter)
				 		.map1(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())))
				 		 .map2(s->StreamUtils.sequenceM(s,reversable.map(r->r.copy())));
	}
	
	
	
	/**
	 * Convert to a Stream with the result of a reduction operation repeated
	 * specified times
	 * 
	 * <pre>
	 * {@code 
	 *   		List<Integer> list = AsGenericMonad,asStreamUtils.sequenceM(Stream.of(1,2,2))
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
	public final SequenceM<T> cycle(Monoid<T> m, int times) {
		return StreamUtils.sequenceM(StreamUtils.cycle(times,Streamable.of(m.reduce(stream))),reversable);
		
	}

	
	/**
	 * 
	 * Convert to a Stream, repeating the resulting structure specified times
	 * and lifting all values to the specified Monad type
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Optional&lt;Integer&gt;&gt; list = StreamUtils.sequenceM(Stream.of(1, 2)).cycle(Optional.class,
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
	public final <R> SequenceM<R> cycle(Class<R> monadC, int times) {
		return (SequenceMImpl)cycle(times).map(r -> new ComprehenderSelector().selectComprehender(monadC).of(r));	
	}

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
	public final SequenceM<T> cycleWhile(Predicate<? super T> predicate) {
	
		return StreamUtils.sequenceM(StreamUtils.cycle(stream),reversable).limitWhile(predicate);
	}

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
	public final SequenceM<T> cycleUntil(Predicate<? super T> predicate) {
		return StreamUtils.sequenceM(StreamUtils.cycle(stream),reversable).limitWhile(predicate.negate());
	}
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
	public final <S> SequenceM<Tuple2<T,S>> zip(Stream<? extends S> second){
		return zipStream(second,(a,b)->new Tuple2<>(a,b));
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
	public final <S,U> SequenceM<Tuple3<T,S,U>> zip3(Stream<? extends S> second,Stream<? extends U> third){
		return zip(second).zipStream(third).map(p -> new Tuple3(p.v1().v1(),p.v1().v2(),p.v2()));
		
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
	public final <T2,T3,T4> SequenceM<Tuple4<T,T2,T3,T4>> zip4(Stream<T2> second,Stream<T3> third,Stream<T4> fourth){
		return zip3(second,third).zipStream(fourth).map(t ->  new Tuple4(t.v1().v1(), t.v1().v2(),t.v1().v3(),t.v2()));
		
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
	public final  SequenceM<Tuple2<T,Long>> zipWithIndex(){
		return zipStream(LongStream.iterate(0, i->i+1),(a,b)->new Tuple2<>(a,b));
	}
	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asStreamUtils.sequenceM(Stream.of(1, 2, 3)).zip(
	 * 			asStreamUtils.sequenceM(Optional.of(2)), (a, b) -&gt; Arrays.asList(a, b));
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
	public final <S, R> SequenceM<R> zipSequence(SequenceM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return StreamUtils.sequenceM(StreamUtils.zipSequence(stream,second, zipper),Optional.empty());
		
	}
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
	public final <S, R> SequenceM<R> zip(AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return zipSequence(second.toSequence(), zipper);
	}

	/**
	 * Zip this Monad with a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = asStreamUtils.sequenceM(Stream.of(1, 2, 3)).zip(
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
	public final <S, R> SequenceM<R> zipStream(BaseStream<? extends S,? extends BaseStream<? extends S,?>> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return StreamUtils.sequenceM(StreamUtils.zipStream(stream,second, zipper),Optional.empty());
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
	 * @return SequenceM with sliding view
	 */
	public final SequenceM<List<T>> sliding(int windowSize) {
		return StreamUtils.sequenceM(StreamUtils.sliding(stream,windowSize),reversable);
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
	 * @return SequenceM with sliding view
	 */
	public final SequenceM<List<T>> sliding(int windowSize,int increment) {
		return StreamUtils.sequenceM(StreamUtils.sliding(stream,windowSize,increment),reversable);
	}

	/**
	 * Group elements in a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = StreamUtils.sequenceM(Stream.of(1, 2, 3, 4, 5, 6)).grouped(3)
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
	public final SequenceM<List<T>> grouped(int groupSize) {
		return StreamUtils.sequenceM(StreamUtils.batchBySize(stream,groupSize),reversable);
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
	public final <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
		return collect(Collectors.groupingBy(classifier));
	}

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
	public final SequenceM<T> distinct() {
		return StreamUtils.sequenceM(stream.distinct(),reversable);
	}

	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),StreamUtils.sequenceM(Stream.of("a", "b", "c")).scanLeft(Reducers.toString("")).toList());
	 *         
	 *         }
	 * </pre>
	 * 
	 * @param monoid
	 * @return
	 */
	public final SequenceM<T> scanLeft(Monoid<T> monoid) {
		return StreamUtils.sequenceM(StreamUtils.scanLeft(stream,monoid),reversable);
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
	public final <U> SequenceM<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
		return StreamUtils.sequenceM(stream.scanLeft(seed, function),reversable);
		
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
	public final SequenceM<T> scanRight(Monoid<T> monoid) {
		return StreamUtils.sequenceM(reverse().scanLeft(monoid.zero(), (u, t) -> monoid.combiner().apply(t, u)),reversable);
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
	public final<U> SequenceM<U> scanRight(U identity,BiFunction<? super T, U, U>  combiner){
		
		return StreamUtils.sequenceM(StreamUtils.scanRight(stream,identity,combiner),reversable);
	}
	

	
	/**
	 * <pre>
	 * {@code assertThat(SequenceM.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
	 * </pre>
	 * 
	 */
	public final SequenceM<T> sorted() {
		return StreamUtils.sequenceM(stream.sorted(),reversable);
	}

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
	public final SequenceM<T> sorted(Comparator<? super T> c) {
		return StreamUtils.sequenceM(stream.sorted(c),reversable);
	}

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
	public final SequenceM<T> skip(long num) {
		return StreamUtils.sequenceM( stream.skip(num),reversable);
	}

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
	public final SequenceM<T> skipWhile(Predicate<? super T> p) {
		return StreamUtils.sequenceM(StreamUtils.skipWhile(stream, p),reversable);
	}

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
	public final SequenceM<T> skipUntil(Predicate<? super T> p) {
		return StreamUtils.sequenceM(StreamUtils.skipUntil(stream,p),reversable);
	}

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
	public final SequenceM<T> limit(long num) {
		return StreamUtils.sequenceM(stream.limit(num),reversable);
	}

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
	public final SequenceM<T> limitWhile(Predicate<? super T> p) {
		return StreamUtils.sequenceM(StreamUtils.limitWhile(stream,p),reversable);
	}

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
	public final SequenceM<T> limitUntil(Predicate<? super T> p) {
		return StreamUtils.sequenceM(StreamUtils.limitUntil(stream,p),reversable);
	}
	/**
	 * @return does nothing - returns this
	 * 	
	 */
	public final SequenceM<T> parallel(){
		return this;
	}
	
	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	 * }
	 * </pre>
	 * @param c Predicate to check if all match
	 */
	public final  boolean  allMatch(Predicate<? super T> c) {
		return stream.allMatch(c);
	}
	/**
	 * True if a single element matches when Monad converted to a Stream
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	 * }
	 * </pre>
	 * @param c Predicate to check if any match
	 */
	public final  boolean  anyMatch(Predicate<? super T> c) {
		return stream.anyMatch(c);
	}
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
	public  boolean xMatch(int num, Predicate<? super T> c) {
		return stream.filter(t -> c.test(t)) .collect(Collectors.counting()) == num;
	}
	/* 
	 * <pre>
	 * {@code 
	 * assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
	 * 
	 * }
	 * </pre>
	 */
	public final  boolean  noneMatch(Predicate<? super T> c) {
		return stream.allMatch(c.negate());
	}
	/**
	 * <pre>
	 * {@code
	 *  assertEquals("123".length(),of(1, 2, 3).join().length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	public final  String join(){
		return StreamUtils.join(stream,"");
	}
	/**
	 * <pre>
	 * {@code
	 * assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	 * }
	 * </pre>
	 * @return Stream as concatenated String
	 */
	public final  String join(String sep){
		return StreamUtils.join(stream,sep);
	}
	/**
	 * <pre>
	 * {@code 
	 * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	 * }
	 * </pre> 
	 *  @return Stream as concatenated String
	 */
	public final   String join(String sep,String start, String end){
		return StreamUtils.join(stream, sep,start, end);
	}
	
	
	/**
	 * Extract the minimum as determined by supplied function
	 * 
	 */
	public final  <U extends Comparable<U>> Optional<T> minBy(Function<T, U> function){
		
		return StreamUtils.minBy(stream,function);
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	public final   Optional<T> min(Comparator<? super T> comparator){
		return  StreamUtils.min(stream,comparator);
	}
	/**
	 * Extract the maximum as determined by the supplied function
	 * 
	 */
	public final  <C extends Comparable<C>> Optional<T> maxBy(Function<T,C> f){
		return StreamUtils.maxBy(stream,f);
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	public final  Optional<T> max(Comparator<? super T> comparator){
		return StreamUtils.max(stream,comparator);
	}
	
	
	
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
	public final  HeadAndTail<T> headAndTail(){
		return StreamUtils.headAndTail(stream);
	}
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
	public final  Optional<HeadAndTail<T>> headAndTailOptional(){
		return StreamUtils.headAndTailOptional(stream);
	}
	
	/**
	 * @return First matching element in sequential order
	 * 
	 * (deterministic)
	 * 
	 */
	public final  Optional<T>  findFirst() {
		return stream.findFirst();
	}
	/**
	 * @return first matching element,  but order is not guaranteed
	 * 
	 * (non-deterministic) 
	 */
	public final  Optional<T>  findAny() {
		return stream.findAny();
	}
	
	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final  <R> R mapReduce(Monoid<R> reducer){
		return reducer.mapReduce(stream);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
	 *  Then use Monoid to reduce values
	 *  
	 * @param mapper Function to map Monad type
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final  <R> R mapReduce(Function<? super T,? extends R> mapper, Monoid<R> reducer){
		return reducer.reduce(stream.map(mapper));
	}
	
	/**
	 * Mutable reduction / collection over this Monad converted to a Stream
	 * 
	 * @param collector Collection operation definition
	 * @return Collected result
	 */
	public final <R, A> R collect(Collector<? super T, A, R> collector){
		return stream.collect(collector);
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#collect(java.util.function.Supplier, java.util.function.BiConsumer, java.util.function.BiConsumer)
	 */
	public final  <R> R collect(Supplier<R> supplier,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R, R> combiner){
		return stream.collect(supplier, accumulator, combiner);
	}
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
	public final  List collect(Stream<Collector> collectors){
		return StreamUtils.collect(stream,collectors);
	}
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
	public <R> List<R> collectIterable(Iterable<Collector> collectors){
		return StreamUtils.collect(stream, collectors);
	}
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	public final  T reduce(Monoid<T> reducer){
		
		return reducer.reduce(stream);
	}
	/* 
	 * <pre>
	 * {@code 
	 * assertThat(SequenceM.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
	 * }
	 * </pre>
	 * 
	 */
	public final Optional<T> reduce(BinaryOperator<T> accumulator){
		 return stream.reduce(accumulator);
	 } 
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	public final T reduce(T identity, BinaryOperator<T> accumulator){
		 return stream.reduce(identity, accumulator);
	 }
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	public final <U> U reduce(U identity,
             BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner){
		 return stream.reduce(identity, accumulator, combiner);
	 }
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedStreamUtils.sequenceM() first. I.e. streamedStreamUtils.sequenceM().reduce(reducer)
	 * 
	 * @param reducers
	 * @return
	 */
	public final List<T> reduce(Stream<Monoid<T>> reducers){
		return StreamUtils.reduce(stream, reducers);
	}
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedStreamUtils.sequenceM() first. I.e. streamedStreamUtils.sequenceM().reduce(reducer)
	 * 
	 * @param reducers
	 * @return
	 */
	public final List<T> reduce(Iterable<Monoid<T>> reducers){
		return StreamUtils.reduce(stream, reducers);
	}
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	public final T foldLeft(Monoid<T> reducer){
		return reduce(reducer);
	}
	/**
	 * foldLeft : immutable reduction from left to right
	 * <pre>
	 * {@code 
	 * 
	 * assertTrue(SequenceM.of("a", "b", "c").foldLeft("", String::concat).equals("abc"));
	 * }
	 * </pre>
	 */
	public final T foldLeft(T identity,  BinaryOperator<T> accumulator){
		 return stream.reduce(identity, accumulator);
	 }
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final <T> T foldLeftMapToType(Monoid<T> reducer){
		return reducer.mapReduce(stream);
	}
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	public final T foldRight(Monoid<T> reducer){
		return reducer.reduce(reverse());
	}
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
	public final<U> U foldRight(U seed, BiFunction<? super T, U, U> function){
		 return stream.foldRight(seed, function);
	 }
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final <T> T foldRightMapToType(Monoid<T> reducer){
		return reducer.mapReduce(reverse());
	}
	/**
	 * @return Underlying Stream (lazily) converted to a Streamable instance
	 */
	public final Streamable<T> toStreamable(){
		return  AsStreamable.fromStream(stream());
	}
	/**
	 * @return This monad converted to a set
	 */
	public final Set<T> toSet(){
		return (Set)stream.collect(Collectors.toSet());
	}
	/**
	 * @return this monad converted to a list
	 */
	public final List<T> toList(){
	
		return (List)stream.collect(Collectors.toList());
	}
	public final <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory) {
        return stream.collect(Collectors.toCollection(collectionFactory));
    }
	/**
	 * @return  calls to stream() but more flexible on type for inferencing purposes.
	 */
	public final <T> Stream<T> toStream(){
		return (Stream<T>) stream;
	}
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public final Stream<T> stream(){
		 return stream;
			
	}
	
	/**
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(StreamUtils.sequenceM(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3)));
	 * }</pre>
	 * 
	 * @param iterable
	 * @return True if Monad starts with Iterable sequence of data
	 */
	public final boolean startsWith(Iterable<T> iterable){
		return StreamUtils.startsWith(stream,iterable);
		
	}
	/**
	 * 	<pre>{@code assertTrue(StreamUtils.sequenceM(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3).iterator())) }</pre>

	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	public final boolean startsWith(Iterator<T> iterator){
		return StreamUtils.startsWith(stream,iterator);
		
	}
	
	/**
	 * @return this SequenceM converted to AnyM format
	 */
	public AnyM<T> anyM(){
		return AsGenericMonad.fromStream(stream).anyM();
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#map(java.util.function.Function)
	 */
	public final  <R> SequenceM<R> map(Function<? super T,? extends R> fn){
		return new SequenceMImpl(stream.map(fn));
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#peek(java.util.function.Consumer)
	 */
	public final   SequenceM<T>  peek(Consumer<? super T> c) {
		return new SequenceMImpl(stream.peek(c));
	}
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
	public final <R> SequenceM<R> flatMap(Function<? super T,? extends Stream<? extends R>> fn) {
		return StreamUtils.sequenceM(stream.flatMap(fn),reversable);
	}
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
	public final <R> SequenceM<R> flatMapAnyM(Function<? super T,AnyM<? extends R>> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapAnyM(stream,fn),reversable);
	}
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
	public final <R> SequenceM<R> flatMapCollection(Function<? super T,Collection<? extends R>> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapCollection(stream,fn),Optional.empty());
		
	}
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
	public final <R> SequenceM<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapStream(stream,fn),reversable);

	}
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
	public final <R> SequenceM<R> flatMapOptional(Function<? super T,Optional<? extends R>> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapOptional(stream,fn),reversable);
	
	}
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
	public final <R> SequenceM<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapCompletableFuture(stream,fn),reversable);
	}
	
	
	/**
	 * Perform a flatMap operation where the result will be a flattened stream of Characters
	 * from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {@code 
	 *   List<Character> result = anyM("input.file")
									.asSequence()
									.flatMapCharSequence(i->"hello world")
									.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	public final  SequenceM<Character> flatMapCharSequence(Function<? super T,CharSequence> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapCharSequence(stream, fn),reversable);
	}
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
	public final  SequenceM<String> flatMapFile(Function<? super T,File> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapFile(stream, fn),reversable);
	}
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
	public final  SequenceM<String> flatMapURL(Function<? super T, URL> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapURL(stream, fn),reversable);
	}
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
	public final SequenceM<String> flatMapBufferedReader(Function<? super T,BufferedReader> fn) {
		return StreamUtils.sequenceM(StreamUtils.flatMapBufferedReader(stream, fn),reversable);
	}
	public final   SequenceM<T>  filter(Predicate<? super T> fn){
		return StreamUtils.sequenceM(stream.filter(fn),reversable);
	}
	public void forEach(Consumer<? super T> action) {
		stream.forEach(action);
		
	}
	
	public Iterator<T> iterator() {
		return stream.iterator();
	}
	
	public Spliterator<T> spliterator() {
		return stream.spliterator();
	}
	
	public boolean isParallel() {
		return stream.isParallel();
	}
	
	public SequenceM<T> sequential() {
		return StreamUtils.sequenceM(stream.sequential(),reversable);
	}
	
	
	public SequenceM<T> unordered() {
		return StreamUtils.sequenceM(stream.unordered(),reversable);
	}
	
	
	
	
	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return stream.mapToInt(mapper);
	}
	
	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return stream.mapToLong(mapper);
	}
	
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return stream.mapToDouble(mapper);
	}
	
	public IntStream flatMapToInt(
			Function<? super T, ? extends IntStream> mapper) {
		return stream.flatMapToInt(mapper);
	}
	
	public LongStream flatMapToLong(
			Function<? super T, ? extends LongStream> mapper) {
		return stream.flatMapToLong(mapper);
	}
	
	public DoubleStream flatMapToDouble(
			Function<? super T, ? extends DoubleStream> mapper) {
		return stream.flatMapToDouble(mapper);
	}

	
	public void forEachOrdered(Consumer<? super T> action) {
		stream.forEachOrdered(action);
		
	}
	
	public Object[] toArray() {
		return stream.toArray();
	}
	
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return stream.toArray(generator);
	}
	
	public long count() {
		return stream.count();
	}
	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) SequenceM.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	public  SequenceM<T> intersperse(T value) {
	
		return StreamUtils.sequenceM(stream.flatMap(t -> Stream.of(value,t)).skip(1l),reversable);
	}
	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) SequenceM.of(1, "a", 2, "b",3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <U> SequenceM<U> ofType(Class<U> type) {
		return StreamUtils.sequenceM(StreamUtils.ofType(stream, type),reversable);
	}
	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException SequenceM.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	public <U> SequenceM<U> cast(Class<U> type) {
		return StreamUtils.sequenceM(StreamUtils.cast(stream, type),reversable);
	}
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
	public Collection<T> toLazyCollection(){
		return StreamUtils.toLazyCollection(stream);
	}
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
	public Collection<T> toConcurrentLazyCollection(){
		return StreamUtils.toConcurrentLazyCollection(stream);
	}
	
	/**
	 * @return Streamable that can replay this SequenceM
	 */
	public Streamable<T> toLazyStreamable(){
		return StreamUtils.toLazyStreamable(stream);
	}
	/**
	 * @return Streamable that replay this SequenceM
	 */
	public Streamable<T> toConcurrentLazyStreamable(){
		return StreamUtils.toConcurrentLazyStreamable(stream);
		
	}
	public SequenceM<T> reverse(){
		if(reversable.isPresent()){
			reversable.ifPresent(r->r.invert());
			return this;
		}
		return StreamUtils.sequenceM(StreamUtils.reverse(stream),reversable);
	}
	
	
	@Override
	public SequenceM<T> onClose(Runnable closeHandler) {
		
		return this;
	}
	@Override
	public void close() {
		
		
	}

	public SequenceM<T> shuffle() {
		return StreamUtils.sequenceM(StreamUtils.shuffle(stream).stream(),reversable);
	
	}
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
	public SequenceM<T> appendStream(Stream<T> stream) {
		return StreamUtils.sequenceM(StreamUtils.appendStream(this.stream,stream),Optional.empty());
	}
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
	public SequenceM<T> prependStream(Stream<T> stream) {
		
		return StreamUtils.sequenceM(StreamUtils.prependStream(this.stream,stream),Optional.empty());
	}
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
	public SequenceM<T> append(T... values) {
		return StreamUtils.sequenceM(StreamUtils.append(stream, values),Optional.empty());
		
	}
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
	public SequenceM<T> prepend(T... values) {
		return StreamUtils.sequenceM(StreamUtils.prepend(stream, values),Optional.empty());
	}
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
	public SequenceM<T> insertAt(int pos, T... values) {
		return StreamUtils.sequenceM(StreamUtils.insertAt(stream, pos, values),Optional.empty());
		
	}
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
	public SequenceM<T> deleteBetween(int start,int end) {
		return StreamUtils.sequenceM(StreamUtils.deleteBetween(stream, start, end),Optional.empty());
	}
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
	public SequenceM<T> insertStreamAt(int pos, Stream<T> stream) {
		
		return StreamUtils.sequenceM(StreamUtils.insertStreamAt(this.stream, pos,stream),Optional.empty());
		
	}

	@Override
	public FutureOperations<T> futureOperations(Executor exec) {
		return StreamUtils.futureOperations(stream,exec);
	}


	@Override
	public boolean endsWith(Iterable<T> iterable) {
		return StreamUtils.endsWith(stream, iterable);
	}


	@Override
	public HotStream<T> hotStream(Executor e) {
		return StreamUtils.hotStream(stream, e);
	}


	@Override
	public T firstValue() {
		return StreamUtils.firstValue(stream);
	}


	@Override
	public void subscribe(Subscriber<? super T> s) {
		 ReactiveStreamsLoader.publisher.get().subscribe(stream,s);	
	}


	@Override
	public <U> SequenceM<Tuple2<T, U>> zip(Seq<U> other) {
		return StreamUtils.sequenceM(stream.zip(other),reversable);
	}


	@Override
	public <S, R> SequenceM<R> zipAnyM(AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return StreamUtils.sequenceM(StreamUtils.zipAnyM(stream,second,zipper),reversable);
	}


	


	@Override
	public <U> SequenceM<Tuple2<T, U>> crossJoin(Stream<U> other) {
		return StreamUtils.sequenceM(stream.crossJoin(other),reversable);
	}


	@Override
	public <U> SequenceM<Tuple2<T, U>> innerJoin(Stream<U> other,
			BiPredicate<T, U> predicate) {
		return StreamUtils.sequenceM(stream.innerJoin(other, predicate),reversable);
	}


	@Override
	public <U> SequenceM<Tuple2<T, U>> leftOuterJoin(Stream<U> other,
			BiPredicate<T, U> predicate) {
		return StreamUtils.sequenceM(stream.leftOuterJoin(other, predicate),reversable);
	}


	@Override
	public <U> SequenceM<Tuple2<T, U>> rightOuterJoin(Stream<U> other,
			BiPredicate<T, U> predicate) {
		return StreamUtils.sequenceM(stream.rightOuterJoin(other, predicate),reversable);
	}


	@Override
	public SequenceM<T> onEmpty(T value) {
		return StreamUtils.sequenceM(stream.onEmpty(value),Optional.empty());
	}


	@Override
	public SequenceM<T> onEmptyGet(Supplier<T> supplier) {
		return StreamUtils.sequenceM(stream.onEmptyGet(supplier),Optional.empty());
	}


	@Override
	public <X extends Throwable> SequenceM<T> onEmptyThrow(Supplier<X> supplier) {
		return StreamUtils.sequenceM(stream.onEmptyThrow(supplier),Optional.empty());
	}


	@Override
	public SequenceM<T> concat(Stream<T> other) {
		return StreamUtils.sequenceM(stream.concat(other),Optional.empty());
	}


	@Override
	public SequenceM<T> concat(T other) {
		return StreamUtils.sequenceM(stream.concat(other),Optional.empty());
	}


	@Override
	public SequenceM<T> concat(T... other) {
		return StreamUtils.sequenceM(stream.concat(other),Optional.empty());
	}


	@Override
	public <U> SequenceM<T> distinct(
			Function<? super T, ? extends U> keyExtractor) {
		return StreamUtils.sequenceM(stream.distinct(keyExtractor),reversable);
	}


	@Override
	public <U, R> SequenceM<R> zip(Seq<U> other, BiFunction<T, U, R> zipper) {
		return StreamUtils.sequenceM(stream.zip(other, zipper),Optional.empty());
	}


	@Override
	public SequenceM<T> shuffle(Random random) {
		return StreamUtils.sequenceM(stream.shuffle(random),reversable);
	}


	@Override
	public SequenceM<T> slice(long from, long to) {
		return StreamUtils.sequenceM(stream.slice(from,to),reversable);
	}
	


	@Override
	public <U extends Comparable<? super U>> SequenceM<T> sorted(
			Function<? super T, ? extends U> function) {
		return StreamUtils.sequenceM(stream.sorted(function),reversable);
	}


	@Override
	public <U> SequenceM<Tuple2<T, U>> zipStream(Stream<U> other) {
		return StreamUtils.sequenceM(StreamUtils.zipStream(stream, other,Tuple::tuple),Optional.empty());
	}

	@Override
	public SequenceM<T> xPer(int x, long time, TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.xPer(stream, x,time,t),reversable);
	}

	@Override
	public SequenceM<T> onePer(long time, TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.onePer(stream,time,t),reversable);
	}

	@Override
	public SequenceM<T> debounce(long time, TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.debounce(stream, time,t),reversable);
	}

	@Override
	public SequenceM<List<T>> batchBySizeAndTime(int size, long time, TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.batchBySizeAndTime(stream, size, time,t),reversable);
	}

	@Override
	public SequenceM<List<T>> batchByTime(long time, TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.batchByTime(stream, time,t),reversable);
	}

	@Override
	public T foldRight(T identity, BinaryOperator<T> accumulator) {
		return reverse().foldLeft(identity, accumulator);
	}

	@Override
	public boolean endsWith(Stream<T> iterable) {
		return StreamUtils.endsWith(stream,()->iterable.iterator());
	}

	@Override
	public SequenceM<T> skip(long time, TimeUnit unit) {
		return StreamUtils.sequenceM(StreamUtils.skip(stream,time,unit),
				this.reversable);
	}

	@Override
	public SequenceM<T> limit(long time, TimeUnit unit) {
		return StreamUtils.sequenceM(StreamUtils.limit(stream,time,unit),
				this.reversable);
	}

	@Override
	public SequenceM<List<T>> batchBySize(int size) {
		return StreamUtils.sequenceM(StreamUtils.batchBySize(stream, size),this.reversable);
	}

	

	@Override
	public SequenceM<T> fixedDelay(long l, TimeUnit unit) {
		return StreamUtils.sequenceM(StreamUtils.fixedDelay(stream,l,unit),
				this.reversable);
	}

	@Override
	public SequenceM<T> jitter(long l) {
		return StreamUtils.sequenceM(StreamUtils.jitter(stream,l),
				this.reversable);
	}

	@Override
	public SequenceM<Streamable<T>> windowBySizeAndTime(int size, long time,
			TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.windowBySizeAndTime(stream,size,time,t), this.reversable);
	}

	@Override
	public SequenceM<Streamable<T>> windowWhile(Predicate<T> predicate) {
		return StreamUtils.sequenceM(StreamUtils.windowWhile(stream,predicate), this.reversable);
	}

	@Override
	public SequenceM<Streamable<T>> windowUntil(Predicate<T> predicate) {
		return StreamUtils.sequenceM(StreamUtils.windowWhile(stream,predicate.negate()), this.reversable);
	}

	@Override
	public SequenceM<Streamable<T>> windowStatefullyWhile(
			BiPredicate<Streamable<T>, T> predicate) {
		return StreamUtils.sequenceM(StreamUtils.windowStatefullyWhile(stream, predicate), this.reversable);
	}

	@Override
	public SequenceM<Streamable<T>> windowByTime(long time, TimeUnit t) {
		return StreamUtils.sequenceM(StreamUtils.windowByTime(stream, time,t), this.reversable);
	}

	@Override
	public SequenceM<List<T>> batchUntil(Predicate<T> predicate) {
		return StreamUtils.sequenceM(StreamUtils.batchUntil(stream,predicate), this.reversable);
	}

	@Override
	public SequenceM<List<T>> batchWhile(Predicate<T> predicate) {
		return StreamUtils.sequenceM(StreamUtils.batchWhile(stream,predicate), this.reversable);
	}
	@Override
	public List collectStream(Stream<Collector> collectors) {
		return StreamUtils.collect(stream,collectors);
	}
	@Override
	public<C extends Collection<T>>  SequenceM<C> batchWhile(Predicate<T> predicate, Supplier<C> factory) {
		return StreamUtils.sequenceM(StreamUtils.batchWhile(stream,predicate,factory), this.reversable);
	}
	@Override
	public<C extends Collection<T>>  SequenceM<C> batchUntil(Predicate<T> predicate, Supplier<C> factory) {
		return StreamUtils.sequenceM(StreamUtils.batchWhile(stream,predicate.negate(),factory), this.reversable);
	}
	@Override
	public <C extends Collection<T>> SequenceM<C> batchBySizeAndTime(int size,
			long time, TimeUnit unit, Supplier<C> factory) {
		return StreamUtils.sequenceM(StreamUtils.batchBySizeAndTime(stream, size,time, unit, factory),this.reversable);

	}

	@Override
	public <C extends Collection<T>> SequenceM<C> batchByTime(long time,
			TimeUnit unit, Supplier<C> factory) {
		return StreamUtils.sequenceM(StreamUtils.batchByTime(stream, time, unit, factory),this.reversable);
	}
	@Override
	public <C extends Collection<T>> SequenceM<C> batchBySize(int size,
			Supplier<C> factory) {
		return StreamUtils.sequenceM(StreamUtils.batchBySize(stream, size,factory),this.reversable);

	}

	@Override
	public SequenceM<T> skipLast(int num) {
		return StreamUtils.sequenceM(StreamUtils.skipLast(stream,num),this.reversable);
	}

	@Override
	public SequenceM<T> limitLast(int num) {
		return StreamUtils.sequenceM(StreamUtils.limitLast(stream,num),this.reversable);
	}

	@Override
	public SequenceM<T> recover(Function<Throwable, T> fn) {
		return StreamUtils.sequenceM(StreamUtils.recover(stream,fn),this.reversable);
	}

	@Override
	public <EX extends Throwable> SequenceM<T> recover(Class<EX> exceptionClass,
			Function<EX, T> fn) {
		return StreamUtils.sequenceM(StreamUtils.recover(stream,exceptionClass,fn),this.reversable);
	}
}
