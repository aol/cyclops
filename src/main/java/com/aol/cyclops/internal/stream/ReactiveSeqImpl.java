package com.aol.cyclops.internal.stream;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import com.aol.cyclops.types.stream.future.FutureOperations;
import com.aol.cyclops.util.stream.AsStreamable;
import com.aol.cyclops.util.stream.StreamUtils;
import com.aol.cyclops.util.stream.Streamable;

public class ReactiveSeqImpl<T> implements Unwrapable, ReactiveSeq<T>, Iterable<T>{
	private final Stream<T> stream;
	private final Optional<ReversableSpliterator> reversable;
	
	public ReactiveSeqImpl(Stream<T> stream){
		
		this.stream = Seq.seq(stream);
		this.reversable = Optional.empty();
		
		
	}
	
	public ReactiveSeqImpl(Stream<T> stream,ReversableSpliterator rev){
		this.stream = Seq.seq(stream);
		this.reversable = Optional.of(rev);
		
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
	 */
	@Override
	public <T> ReactiveSeq<T> unit(T unit) {
		return ReactiveSeq.of(unit);
	}

	

	public HotStream<T> schedule(String cron,ScheduledExecutorService ex){
		return StreamUtils.schedule(this, cron, ex);
		
	}
	public HotStream<T> scheduleFixedDelay(long delay,ScheduledExecutorService ex){
		return StreamUtils.scheduleFixedDelay(this, delay, ex);
	}
	public HotStream<T> scheduleFixedRate(long rate,ScheduledExecutorService ex){
		return StreamUtils.scheduleFixedRate(this, rate, ex);
		
	}
	
	@Deprecated
	public final <R> R unwrap(){
		return (R)this;
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
	public final <T1> ReactiveSeq<T1> flatten(){
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
	public final ReactiveSeq<T> cycle(int times) {
		return StreamUtils.reactiveSeq(StreamUtils.cycle(times,AsStreamable.fromStream(stream)),reversable);
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
	public final ReactiveSeq<T> cycle() {
		return StreamUtils.reactiveSeq(StreamUtils.cycle(stream),reversable);
	}
	/**
	 * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe.
	 * <pre>
	 * {@code 
	 *  Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	 * 
	 * }
	 * </pre>
	 * 
	 * @return duplicated stream
	 */
	public final Tuple2<ReactiveSeq<T>,ReactiveSeq<T>> duplicateSequence(){
		Tuple2<Stream<T>,Stream<T>> tuple = StreamUtils.duplicate(stream);
		return tuple.map1(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
			  	.map2(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())));
	}
	/**
	 * Triplicates a Stream
	 * Buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe.
	 * <pre>
	 * {@code 
	 * 	Tuple3<ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>,ReactiveSeq<Tuple3<T1,T2,T3>>> Tuple3 = sequence.triplicate();
	
	 * }
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	public final Tuple3<ReactiveSeq<T>,ReactiveSeq<T>,ReactiveSeq<T>> triplicate(){
		
		Tuple3<Stream<T>,Stream<T>,Stream<T>> tuple = StreamUtils.triplicate(stream);
		return tuple.map1(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
					.map2(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
					.map3(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())));
		
		
	}
	
	
	/**
	 * Makes four copies of a Stream
	 * Buffers intermediate values, leaders may change positions so a limit
	 * can be safely applied to the leading stream. Not thread-safe. 
	 * 
	 * <pre>
	 * {@code
	 * 
	 * 		Tuple4<ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>,ReactiveSeq<Tuple4<T1,T2,T3,T4>>> quad = sequence.quadruplicate();
	 * }
	 * </pre>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final Tuple4<ReactiveSeq<T>,ReactiveSeq<T>,ReactiveSeq<T>,ReactiveSeq<T>> quadruplicate(){
		Tuple4<Stream<T>,Stream<T>,Stream<T>,Stream<T>> tuple = StreamUtils.quadruplicate(stream);
		return tuple.map1(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
				  	.map2(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
					.map3(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
					.map4(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())));
	}
	/**
	 * Split a Stream at it's head (similar to headAndTail)
	 * <pre>
	 * {@code 
	 * ReactiveSeq.of(1,2,3).splitAtHead()
	 * 
	 *  //Optional[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 * 
	*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Tuple2<Optional<T>,ReactiveSeq<T>> splitSequenceAtHead(){
		Tuple2<ReactiveSeq<T>,ReactiveSeq<T>> Tuple2 = splitAt(1);
		return new Tuple2(Tuple2.v1.toOptional()
							.flatMap( l-> l.size()>0 ? Optional.of(l.get(0)) : Optional.empty()  )
							,Tuple2.v2);
	} 
	/**
	 * Split at supplied location 
	 * <pre>
	 * {@code 
	 * ReactiveSeq.of(1,2,3).splitAt(1)
	 * 
	 *  //SequenceM[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 */
	public final Tuple2<ReactiveSeq<T>,ReactiveSeq<T>> splitAt(int where){
		return StreamUtils.splitAt(stream, where)
				   .map1(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
				   .map2(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())));
		
	}
	/**
	 * Split stream at point where predicate no longer holds
	 * <pre>
	 * {@code
	 *   ReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
	 *   
	 *   //SequenceM[1,2,3] SequenceM[4,5,6]
	 * }
	 * </pre>
	 */
	public final Tuple2<ReactiveSeq<T>,ReactiveSeq<T>> splitBy(Predicate<T> splitter){
		return StreamUtils.splitBy(stream, splitter)
				   .map1(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
				   .map2(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())));
	}
	/**
	 * Partition a Stream into two one a per element basis, based on predicate's boolean value
	 * <pre>
	 * {@code 
	 *  ReactiveSeq.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0) 
	 *  
	 *  //SequenceM[1,3,5], SequenceM[2,4,6]
	 * }
	 *
	 * </pre>
	 */
	public final Tuple2<ReactiveSeq<T>,ReactiveSeq<T>> partitionSequence(Predicate<T> splitter){
		return StreamUtils.partition(stream, splitter)
				 		.map1(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())))
				 		 .map2(s->StreamUtils.reactiveSeq(s,reversable.map(r->r.copy())));
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
	public final ReactiveSeq<T> cycle(Monoid<T> m, int times) {
		return StreamUtils.reactiveSeq(StreamUtils.cycle(times,Streamable.of(m.reduce(stream))),reversable);
		
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
	public final <R> ReactiveSeq<R> cycle(Class<R> monadC, int times) {
		return (ReactiveSeqImpl)cycle(times).map(r -> new ComprehenderSelector().selectComprehender(monadC).of(r));	
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
	public final ReactiveSeq<T> cycleWhile(Predicate<? super T> predicate) {
	
		return StreamUtils.reactiveSeq(StreamUtils.cycle(stream),reversable).limitWhile(predicate);
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
	public final ReactiveSeq<T> cycleUntil(Predicate<? super T> predicate) {
		return StreamUtils.reactiveSeq(StreamUtils.cycle(stream),reversable).limitWhile(predicate.negate());
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
	public final <S> ReactiveSeq<Tuple2<T,S>> zip(Stream<S> second){

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
	public final <S,U> ReactiveSeq<Tuple3<T,S,U>> zip3(Stream<? extends S> second,Stream<? extends U> third){
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
	public final <T2,T3,T4> ReactiveSeq<Tuple4<T,T2,T3,T4>> zip4(Stream<T2> second,Stream<T3> third,Stream<T4> fourth){
		return zip3(second,third).zipStream(fourth).map(t ->  new Tuple4(t.v1().v1(), t.v1().v2(),t.v1().v3(),t.v2()));
		
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
	public final <S, R> ReactiveSeq<R> zipSequence(ReactiveSeq<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return StreamUtils.reactiveSeq(StreamUtils.zipSequence(stream,second, zipper),Optional.empty());
		
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
	@Override
	public final <S, R> ReactiveSeq<R> zipAnyM(AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return zipSequence(second.stream(), zipper);
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
	public final <S, R> ReactiveSeq<R> zipStream(BaseStream<? extends S,? extends BaseStream<? extends S,?>> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return StreamUtils.reactiveSeq(StreamUtils.zipStream(stream,second, zipper),Optional.empty());
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
	public final ReactiveSeq<ListX<T>> sliding(int windowSize) {
		return StreamUtils.reactiveSeq(StreamUtils.sliding(stream,windowSize),reversable);
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
	public final ReactiveSeq<ListX<T>> sliding(int windowSize,int increment) {
		return StreamUtils.reactiveSeq(StreamUtils.sliding(stream,windowSize,increment),reversable);
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
	public final ReactiveSeq<ListX<T>> grouped(int groupSize) {
		return StreamUtils.reactiveSeq(StreamUtils.batchBySize(stream,groupSize),reversable);
	}
	@Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(
            BiPredicate<ListX<? super T>, ? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.groupedStatefullyWhile(stream, predicate), this.reversable);
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
	public final <K> MapX<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
		return MapX.fromMap(collect(Collectors.groupingBy(classifier)));
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
	public final ReactiveSeq<T> distinct() {
		return StreamUtils.reactiveSeq(stream.distinct(),reversable);
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
	public final ReactiveSeq<T> scanLeft(Monoid<T> monoid) {
		return StreamUtils.reactiveSeq(StreamUtils.scanLeft(stream,monoid),reversable);
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
	public final <U> ReactiveSeq<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
		
		return StreamUtils.reactiveSeq(Seq.seq(stream).scanLeft(seed, function),reversable);
		
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
	public final ReactiveSeq<T> scanRight(Monoid<T> monoid) {
		return StreamUtils.reactiveSeq(reverse().scanLeft(monoid.zero(), (u, t) -> monoid.combiner().apply(t, u)),reversable);
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
	public final<U> ReactiveSeq<U> scanRight(U identity,BiFunction<? super T, U, U>  combiner){
		
		return StreamUtils.reactiveSeq(StreamUtils.scanRight(stream,identity,combiner),reversable);
	}
	

	
	/**
	 * <pre>
	 * {@code assertThat(ReactiveSeq.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
	 * </pre>
	 * 
	 */
	public final ReactiveSeq<T> sorted() {
		return StreamUtils.reactiveSeq(stream.sorted(),reversable);
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
	public final ReactiveSeq<T> sorted(Comparator<? super T> c) {
		return StreamUtils.reactiveSeq(stream.sorted(c),reversable);
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
	public final ReactiveSeq<T> skip(long num) {
		return StreamUtils.reactiveSeq( stream.skip(num),reversable);
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
	public final ReactiveSeq<T> skipWhile(Predicate<? super T> p) {
		return StreamUtils.reactiveSeq(StreamUtils.skipWhile(stream, p),reversable);
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
	public final ReactiveSeq<T> skipUntil(Predicate<? super T> p) {
		return StreamUtils.reactiveSeq(StreamUtils.skipUntil(stream,p),reversable);
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
	public final ReactiveSeq<T> limit(long num) {
		return StreamUtils.reactiveSeq(stream.limit(num),reversable);
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
	public final ReactiveSeq<T> limitWhile(Predicate<? super T> p) {
		return StreamUtils.reactiveSeq(StreamUtils.limitWhile(stream,p),reversable);
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
	public final ReactiveSeq<T> limitUntil(Predicate<? super T> p) {
		return StreamUtils.reactiveSeq(StreamUtils.limitUntil(stream,p),reversable);
	}
	/**
	 * @return does nothing - returns this
	 * 	
	 */
	public final ReactiveSeq<T> parallel(){
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
	 *  assertTrue(ReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	 * }
	 * </pre>
	 * 
	 */
	public  boolean xMatch(int num, Predicate<? super T> c) {
		return StreamUtils.xMatch(stream,num,c);
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
	public final  <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function){
		
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
	public final  <C extends Comparable<? super C>> Optional<T> maxBy(Function<? super T,? extends C> f){
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
	 *  ReactiveSeq<String> helloWorld = ReactiveSeq.of("hello","world","last");
		HeadAndTail<String> headAndTail = helloWorld.headAndTail();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		ReactiveSeq<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
	 * }
	 * </pre>
	 * @return
	 */
	public final  HeadAndTail<T> headAndTail(){
		return StreamUtils.headAndTail(stream);
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
	public final  <R> R mapReduce(Reducer<R> reducer){
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
	 * assertThat(ReactiveSeq.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
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
	public final ListX<T> reduce(Stream<? extends Monoid<T>> reducers){
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
	public final ListX<T> reduce(Iterable<? extends Monoid<T>> reducers){
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
	 * assertTrue(ReactiveSeq.of("a", "b", "c").foldLeft("", String::concat).equals("abc"));
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
	public final <T> T foldLeftMapToType(Reducer<T> reducer){
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
	 *  assertTrue(ReactiveSeq.of("a","b","c").foldRight("", String::concat).equals("cba"));
	 * }
	 * </pre>
	 * 
	 * @param identity
	 * @param accumulator
	 * @return
	 */
	public final<U> U foldRight(U seed, BiFunction<? super T, U, U> function){
		 return Seq.seq(stream).foldRight(seed, function);
	 }
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final <T> T foldRightMapToType(Reducer<T> reducer){
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
		return stream.collect(Collectors.toSet());
	}
	/**
	 * @return this monad converted to a list
	 */
	public final List<T> toList(){
	
		return stream.collect(Collectors.toList());
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
	public final ReactiveSeq<T> stream(){
		 return this;
			
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
	public final boolean startsWithIterable(Iterable<T> iterable){
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
	@Override
	public AnyMSeq<T> anyM(){
		return AnyM.fromStream(stream);

	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#map(java.util.function.Function)
	 */
	public final  <R> ReactiveSeq<R> map(Function<? super T,? extends R> fn){
		return new ReactiveSeqImpl(stream.map(fn));
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#peek(java.util.function.Consumer)
	 */
	public final   ReactiveSeq<T>  peek(Consumer<? super T> c) {
		return new ReactiveSeqImpl(stream.peek(c));
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
	public final <R> ReactiveSeq<R> flatMap(Function<? super T,? extends Stream<? extends R>> fn) {
		return StreamUtils.reactiveSeq(stream.flatMap(fn),reversable);
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
	@Override
	public final <R> ReactiveSeq<R> flatMapAnyM(Function<? super T,AnyM<? extends R>> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapAnyM(stream,fn),reversable);
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
	public final <R> ReactiveSeq<R> flatMapIterable(Function<? super T,? extends Iterable<? extends R>> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapIterable(stream,fn),Optional.empty());
		
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
	public final <R> ReactiveSeq<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapStream(stream,fn),reversable);

	}
	/**
	 * flatMap to optional - will result in null values being removed
	 * <pre>
	 * {@code 
	 * 	assertThat(ReactiveSeq.of(1,2,3,null).flatMapOptional(Optional::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	 * }
	 * </pre>
	 * @param fn
	 * @return
	 */
	public final <R> ReactiveSeq<R> flatMapOptional(Function<? super T,Optional<? extends R>> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapOptional(stream,fn),reversable);
	
	}
	/**
	 * flatMap to CompletableFuture - will block until Future complete, although (for non-blocking behaviour use AnyM 
	 *       wrapping CompletableFuture and flatMap to Stream there)
	 *       
	 *  <pre>
	 *  {@code
	 *  	assertThat(ReactiveSeq.of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	 *  }
	 *  </pre>
	 *       
	 * @param fn
	 * @return
	 */
	public final <R> ReactiveSeq<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapCompletableFuture(stream,fn),reversable);
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
	public final  ReactiveSeq<Character> flatMapCharSequence(Function<? super T,CharSequence> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapCharSequence(stream, fn),reversable);
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
	public final  ReactiveSeq<String> flatMapFile(Function<? super T,File> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapFile(stream, fn),reversable);
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
	public final  ReactiveSeq<String> flatMapURL(Function<? super T, URL> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapURL(stream, fn),reversable);
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
	public final ReactiveSeq<String> flatMapBufferedReader(Function<? super T,BufferedReader> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.flatMapBufferedReader(stream, fn),reversable);
	}
	public final   ReactiveSeq<T>  filter(Predicate<? super T> fn){
		return StreamUtils.reactiveSeq(stream.filter(fn),reversable);
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
	
	public ReactiveSeq<T> sequential() {
		return StreamUtils.reactiveSeq(stream.sequential(),reversable);
	}
	
	
	public ReactiveSeq<T> unordered() {
		return StreamUtils.reactiveSeq(stream.unordered(),reversable);
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
	 * // (1, 0, 2, 0, 3, 0, 4) ReactiveSeq.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	public  ReactiveSeq<T> intersperse(T value) {
	
		return StreamUtils.reactiveSeq(stream.flatMap(t -> Stream.of(value,t)).skip(1l),reversable);
	}
	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) ReactiveSeq.of(1, "a", 2, "b",3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <U> ReactiveSeq<U> ofType(Class<U> type) {
		return StreamUtils.reactiveSeq(StreamUtils.ofType(stream, type),reversable);
	}
	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException ReactiveSeq.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	public <U> ReactiveSeq<U> cast(Class<U> type) {
		return StreamUtils.reactiveSeq(StreamUtils.cast(stream, type),reversable);
	}
	/**
	 * Lazily converts this SequenceM into a Collection. This does not trigger the Stream. E.g.
	 * Collection is not thread safe on the first iteration.
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = ReactiveSeq.of(1,2,3,4,5)
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
	public CollectionX<T> toLazyCollection(){
		return StreamUtils.toLazyCollection(stream);
	}
	/**
	 * Lazily converts this SequenceM into a Collection. This does not trigger the Stream. E.g.
	 * 
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = ReactiveSeq.of(1,2,3,4,5)
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
	public CollectionX<T> toConcurrentLazyCollection(){
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
	public ReactiveSeq<T> reverse(){
		if(reversable.isPresent()){
			reversable.ifPresent(r->r.invert());
			return this;
		}
		return StreamUtils.reactiveSeq(StreamUtils.reverse(stream),reversable);
	}
	
	
	@Override
	public ReactiveSeq<T> onClose(Runnable closeHandler) {
		
		return this;
	}
	@Override
	public void close() {
		
		
	}

	public ReactiveSeq<T> shuffle() {
		return StreamUtils.reactiveSeq(StreamUtils.shuffle(stream).stream(),reversable);
	
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
	public ReactiveSeq<T> appendStream(Stream<T> stream) {
		return StreamUtils.reactiveSeq(StreamUtils.appendStream(this.stream,stream),Optional.empty());
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
	public ReactiveSeq<T> prependStream(Stream<T> stream) {
		
		return StreamUtils.reactiveSeq(StreamUtils.prependStream(this.stream,stream),Optional.empty());
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
	public ReactiveSeq<T> append(T... values) {
		return StreamUtils.reactiveSeq(StreamUtils.append(stream, values),Optional.empty());
		
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
	public ReactiveSeq<T> prepend(T... values) {
		return StreamUtils.reactiveSeq(StreamUtils.prepend(stream, values),Optional.empty());
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
	public ReactiveSeq<T> insertAt(int pos, T... values) {
		return StreamUtils.reactiveSeq(StreamUtils.insertAt(stream, pos, values),Optional.empty());
		
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
	public ReactiveSeq<T> deleteBetween(int start,int end) {
		return StreamUtils.reactiveSeq(StreamUtils.deleteBetween(stream, start, end),Optional.empty());
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
	public ReactiveSeq<T> insertStreamAt(int pos, Stream<T> stream) {
		
		return StreamUtils.reactiveSeq(StreamUtils.insertStreamAt(this.stream, pos,stream),Optional.empty());
		
	}

	@Override
	public FutureOperations<T> futureOperations(Executor exec) {
		return StreamUtils.futureOperations(stream,exec);
	}


	@Override
	public boolean endsWithIterable(Iterable<T> iterable) {
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
	public void subscribe(Subscriber<? super T> sub) {
		Iterator<T> it = stream.iterator();
		sub.onSubscribe(new Subscription(){
			
				volatile boolean running = true;
				boolean active = false;
				final LinkedList<Long> requests = new LinkedList<Long>();
				@Override
				public void request(long n) {
				    if(!running)
				        return;
				    if(n<1){
	                     sub.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
	                 }
				    requests.push(n);
				    if(active)
				        return;
				    active=true;//assume single thread calls to request
				    while(requests.size()>0){
				        
				        long num = requests.pop();
    					for(int i=0;i<num && running;i++){
    						boolean progressing = false;
    						boolean progressed = false;
    						try{
    							
    							if(it.hasNext()){
    								progressing= true;
    								sub.onNext(it.next());
    								progressed=true;
    							}
    							else{
    								try{
    									sub.onComplete();
    									
    								}finally{
    									running=false;
    									break;
    								}
    							}
    						}catch(Throwable t){
    							sub.onError(t);
    							if(progressing && !progressed)
    								break;
    							
    						}
    						
    					}
				    }
				    active=false;
				}
				@Override
				public void cancel() {
					running = false;
					
				}
				
			});
		
	}


	@Override
	public <U> ReactiveSeq<Tuple2<T, U>> zip(Seq<U> other) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).zip(other),reversable);
	}


	


	


	@Override
	public ReactiveSeq<T> onEmpty(T value) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).onEmpty(value),Optional.empty());
	}


	@Override
	public ReactiveSeq<T> onEmptyGet(Supplier<T> supplier) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).onEmptyGet(supplier),Optional.empty());
	}


	@Override
	public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(Supplier<X> supplier) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).onEmptyThrow(supplier),Optional.empty());
	}


	@Override
	public ReactiveSeq<T> concat(Stream<T> other) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).concat(other),Optional.empty());
	}


	@Override
	public ReactiveSeq<T> concat(T other) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).concat(other),Optional.empty());
	}


	@Override
	public ReactiveSeq<T> concat(T... other) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).concat(other),Optional.empty());
	}


	@Override
	public <U> ReactiveSeq<T> distinct(
			Function<? super T, ? extends U> keyExtractor) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).distinct(keyExtractor),reversable);
	}


	@Override
	public <U, R> ReactiveSeq<R> zip(Seq<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).zip(other, zipper),Optional.empty());
	}


	@Override
	public ReactiveSeq<T> shuffle(Random random) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).shuffle(random),reversable);
	}


	@Override
	public ReactiveSeq<T> slice(long from, long to) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).slice(from,to),reversable);
	}
	


	@Override
	public <U extends Comparable<? super U>> ReactiveSeq<T> sorted(
			Function<? super T, ? extends U> function) {
		return StreamUtils.reactiveSeq(Seq.seq(stream).sorted(function),reversable);
	}


	@Override
	public <U> ReactiveSeq<Tuple2<T, U>> zipStream(Stream<U> other) {
		return StreamUtils.reactiveSeq(StreamUtils.zipStream(stream, other,Tuple::tuple),Optional.empty());
	}

	@Override
	public ReactiveSeq<T> xPer(int x, long time, TimeUnit t) {
		return StreamUtils.reactiveSeq(StreamUtils.xPer(stream, x,time,t),reversable);
	}

	@Override
	public ReactiveSeq<T> onePer(long time, TimeUnit t) {
		return StreamUtils.reactiveSeq(StreamUtils.onePer(stream,time,t),reversable);
	}

	@Override
	public ReactiveSeq<T> debounce(long time, TimeUnit t) {
		return StreamUtils.reactiveSeq(StreamUtils.debounce(stream, time,t),reversable);
	}

	@Override
	public ReactiveSeq<ListX<T>> groupedBySizeAndTime(int size, long time, TimeUnit t) {
		return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(stream, size, time,t),reversable);
	}

	@Override
	public ReactiveSeq<ListX<T>> groupedByTime(long time, TimeUnit t) {
		return StreamUtils.reactiveSeq(StreamUtils.batchByTime(stream, time,t),reversable);
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
	public ReactiveSeq<T> skip(long time, TimeUnit unit) {
		return StreamUtils.reactiveSeq(StreamUtils.skip(stream,time,unit),
				this.reversable);
	}

	@Override
	public ReactiveSeq<T> limit(long time, TimeUnit unit) {
		return StreamUtils.reactiveSeq(StreamUtils.limit(stream,time,unit),
				this.reversable);
	}

	
	

	@Override
	public ReactiveSeq<T> fixedDelay(long l, TimeUnit unit) {
		return StreamUtils.reactiveSeq(StreamUtils.fixedDelay(stream,l,unit),
				this.reversable);
	}

	@Override
	public ReactiveSeq<T> jitter(long l) {
		return StreamUtils.reactiveSeq(StreamUtils.jitter(stream,l),
				this.reversable);
	}

	
	@Override
	public ReactiveSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
		return StreamUtils.reactiveSeq(StreamUtils.batchUntil(stream,predicate), this.reversable);
	}

	@Override
	public ReactiveSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
		return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream,predicate), this.reversable);
	}

	@Override
	public<C extends Collection<? super T>>  ReactiveSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
		return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream,predicate,factory), this.reversable);
	}
	@Override
	public<C extends Collection<? super T>>  ReactiveSeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
		return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream,predicate.negate(),factory), this.reversable);
	}
	@Override
	public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size,
			long time, TimeUnit unit, Supplier<C> factory) {
		return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(stream, size,time, unit, factory),this.reversable);

	}

	@Override
	public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(long time,
			TimeUnit unit, Supplier<C> factory) {
		return StreamUtils.reactiveSeq(StreamUtils.batchByTime(stream, time, unit, factory),this.reversable);
	}
	@Override
	public <C extends Collection<? super T>> ReactiveSeq<C> grouped(int size,
			Supplier<C> factory) {
		return StreamUtils.reactiveSeq(StreamUtils.batchBySize(stream, size,factory),this.reversable);

	}

	@Override
	public ReactiveSeq<T> skipLast(int num) {
		return StreamUtils.reactiveSeq(StreamUtils.skipLast(stream,num),this.reversable);
	}

	@Override
	public ReactiveSeq<T> limitLast(int num) {
		return StreamUtils.reactiveSeq(StreamUtils.limitLast(stream,num),this.reversable);
	}

	@Override
	public ReactiveSeq<T> recover(Function<Throwable, ? extends T> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.recover(stream,fn),this.reversable);
	}

	@Override
	public <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass,
			Function<EX, ? extends T> fn) {
		return StreamUtils.reactiveSeq(StreamUtils.recover(stream,exceptionClass,fn),this.reversable);
	}
	
	/** 
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	  *<pre>
	 * {@code 
	 * ReactiveSeq.of(1,2)
						.forEach3(a->IntStream.range(10,13),
						.a->b->Stream.of(""+(a+b),"hello world"),
									a->b->c->c+":"a+":"+b);
									
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre> 
	 * @param stream1 Nested Stream to iterate over
	 * @param stream2 Nested Stream to iterate over
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	public <R1,R2,R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1,?>> stream1, 
											Function<? super T,Function<? super R1,? extends BaseStream<R2,?>>> stream2,
												Function<? super T,Function<? super R1,Function<? super R2,? extends R>>> yieldingFunction ){
		return For.stream(this)
				  .stream(u->stream1.apply(u))
				  .stream(u->r1->stream2.apply(u).apply(r1))
				  .yield(yieldingFunction).unwrap();
			
	}
	
	


	
	
	/**
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	 * 
	 * @param stream1 Nested Stream to iterate over
	 * @param stream2 Nested Stream to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	public <R1,R2,R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1,?>> stream1, 
			Function<? super T,Function<? super R1,? extends BaseStream<R2,?>>> stream2,
					Function<? super T,Function<? super R1,Function<? super R2,Boolean>>> filterFunction,
			Function<? super T,Function<? super R1,Function<? super R2,? extends R>>> yieldingFunction ){
		
		 return For.stream(this)
				  .stream(u->stream1.apply(u))
				  .stream(u->r1->stream2.apply(u).apply(r1))
				  .filter(filterFunction)
				  .yield(yieldingFunction).unwrap();
			
	}
	
	

	
	
	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * ReactiveSeq.of(1,2,3)
						.forEach2(a->IntStream.range(10,13),
									a->b->a+b);
									
	 * 
	 *  /SequenceM[11,14,12,15,13,16]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1 Nested Stream to iterate over
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	public <R1,R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1,?>> stream1, 
											Function<? super T,Function<? super R1,? extends R>> yieldingFunction ){
		 return For.stream(this)
				  .stream(u->stream1.apply(u))
				  .yield(yieldingFunction).unwrap();
			
	}

	
	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * ReactiveSeq.of(1,2,3)
						.forEach2(a->IntStream.range(10,13),
						            a->b-> a<3 && b>10,
									a->b->a+b);
									
	 * 
	 *  //SequenceM[14,15]
	 * }
	 * </pre>
	 * @param stream1 Nested Stream to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	public <R1,R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1,?>> stream1, 
			Function<? super T, Function<? super R1, Boolean>> filterFunction,
					Function<? super T,Function<? super R1,? extends R>> yieldingFunction ){
		 return For.stream(this)
				  .stream(u->stream1.apply(u))
				  .filter(filterFunction)
				  .yield(yieldingFunction).unwrap();
			
	}
	
	public <X extends Throwable> Subscription forEachX(long numberOfElements,Consumer<? super T> consumer){
		return StreamUtils.forEachX(this, numberOfElements, consumer);
	}
	public <X extends Throwable> Subscription forEachXWithError(long numberOfElements,Consumer<? super T> consumer,Consumer<? super Throwable> consumerError){
		return StreamUtils.forEachXWithError(this,numberOfElements,consumer,consumerError);
	}
	public <X extends Throwable> Subscription forEachXEvents(long numberOfElements,Consumer<? super T> consumer,Consumer<? super Throwable> consumerError, Runnable onComplete){
		return StreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
	}
	
	public <X extends Throwable> void forEachWithError(Consumer<? super T> consumerElement,
			Consumer<? super Throwable> consumerError){
			StreamUtils.forEachWithError(this, consumerElement, consumerError);
	}
	public <X extends Throwable> void forEachEvent(Consumer<? super T> consumerElement,
			Consumer<? super Throwable> consumerError,
			Runnable onComplete){
		StreamUtils.forEachEvent(this, consumerElement, consumerError, onComplete);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.SequenceM#primedHotStream(java.util.concurrent.Executor)
	 */
	@Override
	public HotStream<T> primedHotStream(Executor e) {
		return StreamUtils.primedHotStream(this, e);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.SequenceM#pausableHotStream(java.util.concurrent.Executor)
	 */
	@Override
	public PausableHotStream<T> pausableHotStream(Executor e) {
		return StreamUtils.pausableHotStream(this, e);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.SequenceM#pausablePrimedHotStream(java.util.concurrent.Executor)
	 */
	@Override
	public PausableHotStream<T> primedPausableHotStream(Executor e) {
		return StreamUtils.primedPausableHotStream(this, e);
	}
	
	
 	/*
    /*
	 * @see org.jooq.lambda.Seq#format()
	 */
	@Override
	public String format() {
		return Seq.seq(this.stream).format();
	}

	@Override
	public Collectable<T> collectable(){
		return Seq.seq(stream);
	}
	@Override
	public <T> ReactiveSeq<T> unitIterator(Iterator<T> it){
		return ReactiveSeq.fromIterator(it);
	}

	@Override
	public ReactiveSeq<T> append(T value) {
		if(value instanceof Stream){
			return appendStream((Stream<T>)value);
		}
		return append((T[])new Object[]{value});
	}
	@Override
	public ReactiveSeq<T> prepend(T value) {
		if(value instanceof Stream){
			return prependStream((Stream<T>)value);
		}
		return prepend((T[])new Object[]{value});
	}
	
	
}
