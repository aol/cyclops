package com.aol.simple.react.stream.traits.future.operators;

import static com.aol.simple.react.stream.traits.LazyFutureStream.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public interface OperationsOnFutures<T> {
	// Handle case where input is a LazyFutureStream (zip, append, concat,
	// prepend etc).
	public LazyFutureStream<T> fromStreamOfFutures(Stream<FastFuture<T>> stream);

	public LazyStreamWrapper<T> getLastActive();

	public LazyFutureStream<T> withLastActive(LazyStreamWrapper<T> active);

	public T safeJoin(FastFuture<T> f);
	
	public Continueable getSubscription();

	/**
	 * Reverse this Stream, by reversing the order in which the underlying Futures will be processed
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1, 2, 3).actOnFutures()
	        						.reverse()
	        						.toList();
	        						
	     //3,2,1   						
	 * }
	 * </pre>
	 * 
	 * 
	 * @return reversed Stream
	 */
	default LazyFutureStream<T> reverse() {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.reverse());
	}
	

	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * <pre>
	 * {@code 
	 * 		assertThat(LazyFutureStream.of(1,2,2)
	 *                              .actOnFutures()
	 * 								.cycle(3)
	 * 								.collect(Collectors.toList()),
	 * 								equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	 * 
	 * 
	 * }
	 * </pre>
	 * 
	 * @param times
	 *            Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	default LazyFutureStream<T> cycle(int times) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.cycle(times));
	}

	/**
	 * Convert to a Stream with the values infinitely cycled
	 * 
	 * <pre>
	 * {@code 
	 *   assertEquals(asList(1, 1, 1, 1, 1,1),LazyFutureStream.of(1)
	 *   												.actOnFutures()
	 *   												.cycle()
	 *   												.limit(6).toList());
	 *   }
	 * </pre>
	 * 
	 * @return Stream with values repeated
	 */
	default LazyFutureStream<T> cycle() {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.cycle());
	}

	/**
	 * Duplicate a Stream, buffers intermediate values, leaders may change
	 * positions so a limit can be safely applied to the leading stream. Not
	 * thread-safe.
	 * 
	 * <pre>
	 * {@code
	 	Tuple2<LazyFutureStream<<Integer>, LazyFutureStream<<Integer>> copies = of(1, 2, 3, 4, 5, 6)
	 * 			actOnFutures().duplicate();
	 * 	assertTrue(copies.v1.anyMatch(i -> i == 2));
	 * 	assertTrue(copies.v2.anyMatch(i -> i == 2));
	 * 
	 * }
	 * </pre>
	 * 
	 * @return duplicated stream
	 */
	default Tuple2<LazyFutureStream<T>, LazyFutureStream<T>> duplicate() {
		return SequenceM
				.fromStream(
						(Stream<FastFuture<T>>) (Stream) this.getLastActive()
								.injectFutures()).duplicateSequence()
				.map1(s -> fromStreamOfFutures(s))
				.map2(s -> fromStreamOfFutures(s));

	}

	/**
	 * Triplicates a Stream. Buffers intermediate values, leaders may change
	 * positions so a limit can be safely applied to the leading stream. Not
	 * thread-safe.
	 * 
	 * <pre>
	 * {@code 
	 *  Tuple3<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().triplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
	 * 
	 * }
	
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	default Tuple3<LazyFutureStream<T>, LazyFutureStream<T>, LazyFutureStream<T>> triplicate() {
		return SequenceM
				.fromStream(
						(Stream<FastFuture<T>>) (Stream) this.getLastActive()
								.injectFutures()).triplicate()
				.map1(s -> fromStreamOfFutures(s))
				.map2(s -> fromStreamOfFutures(s))
				.map3(s -> fromStreamOfFutures(s));
	}

	/**
	 * Makes four copies of a Stream Buffers intermediate values, leaders may
	 * change positions so a limit can be safely applied to the leading stream.
	 * Not thread-safe.
	 * <pre>
	 * {@code 
	 *  Tuple4<LazyFutureStream<Integer>, LazyFutureStream<Integer>, LazyFutureStream<Integer>,LazyFutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().quadruplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
		 assertTrue(copies.v4.anyMatch(i->i==2));
	 * 
	 * }
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default Tuple4<LazyFutureStream<T>, LazyFutureStream<T>, LazyFutureStream<T>, LazyFutureStream<T>> quadruplicate() {
		return SequenceM
				.fromStream(
						(Stream<FastFuture<T>>) (Stream) this.getLastActive()
								.injectFutures()).quadruplicate()
				.map1(s -> fromStreamOfFutures(s))
				.map2(s -> fromStreamOfFutures(s))
				.map3(s -> fromStreamOfFutures(s))
				.map4(s -> fromStreamOfFutures(s));
	}

	/**
	 * Split a Stream at it's head (similar to headAndTail)
	 * 
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1,2,3).actOnFutures().splitAtHead()
	 * 
	 *  //Optional[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default Tuple2<Optional<T>, LazyFutureStream<T>> splitAtHead() {
		return SequenceM
				.<FastFuture<T>> fromStream(
						(Stream<FastFuture<T>>) (Stream) this.getLastActive()
								.injectFutures()).splitSequenceAtHead()
				.map1(o -> o.<T> map(f -> (T) f.join()))
				.map2(s -> fromStreamOfFutures(s));
	}

	/**
	 * Split at supplied location
	 * 
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1,2,3).actOnFutures().splitAt(1)
	 * 
	 *  //SequenceM[1], SequenceM[2,3]
	 * }
	 * 
	 * </pre>
	 */
	default Tuple2<LazyFutureStream<T>, LazyFutureStream<T>> splitAt(int where) {
		return SequenceM
				.<FastFuture<T>> fromStream(
						(Stream<FastFuture<T>>) (Stream) this.getLastActive()
								.injectFutures()).splitAt(where)
				.map1(s -> fromStreamOfFutures(s))
				.map2(s -> fromStreamOfFutures(s));
	}
	
	
	/**
	 * Zip two LazyFutureStreams by combining the underlying Futures
	 * 
	 * <pre>
	 * {@code 
	 * 
	 * List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).actOnFutures().zipLfs(of(100,200,300,400))
													.peek(it -> System.out.println(it)).collect(Collectors.toList());
	 * 
	 * // [1,100],[2,200],[3,300],[4,400]
	 * }
	 * </pre>
	 * 
	 * @param other
	 * @return
	 */
	default <R> LazyFutureStream<Tuple2<T, R>> zipLfs(LazyFutureStream<R> other) {

		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().injectFuturesSeq()
				.map(f -> f.toCompletableFuture())
				.zipStream(other.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()))
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2.join())))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));
	}
	/**
	 * Zip two LazyFutureStreams using the provided combiner
	 * 
	 * <pre>
	 * {@code 
	 *  BiFunction<CompletableFuture<Integer>,CompletableFuture<Integer>,CompletableFuture<Tuple2<Integer,Integer>>> combiner = 
					 			(cf1,cf2)->cf1.<Integer,Tuple2<Integer,Integer>>thenCombine(cf2, (v1,v2)->Tuple.tuple(v1,v2));
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).actOnFutures().zipLfs(of(100,200,300,400), combiner)
													.peek(it -> System.out.println(it)).collect(Collectors.toList());
			
			List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
			assertThat(right,hasItem(100));
			assertThat(right,hasItem(200));
			assertThat(right,hasItem(300));
			assertThat(right,hasItem(400));
	 * 
	 * 
	 * }
	 * </pre>
	 * 
	 */
	default <R,T2> LazyFutureStream<R> zipLfs(LazyFutureStream<T2> other, BiFunction<CompletableFuture<T>,CompletableFuture<T2>,CompletableFuture<R>> combiner) {

		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().injectFuturesSeq()
				.map(f -> f.toCompletableFuture())
				.zipStream(other.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()))
				.map(t -> combiner.apply(t.v1,t.v2))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));
	}
	
	
	/**
	 * Zip two Streams. Futures from this Stream will be paired with data from provided Stream (if the other Stream is also a LazyFutureStream this operator will pair based on
	 * Futures from this Stream with results from the other].
	 * 
	 * <pre>
	 * {@code
	 * List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).actOnFutures().zip(of(100,200,300,400))
													.peek(it -> System.out.println(it)).collect(Collectors.toList());
	 * 
	 * // [1,100],[2,200],[3,300],[4,400]
	 * }
	 * </pre>
	 * 
	 * Example with two LazyFutureStreams
	 * <pre>
	 * {@code
	 * List<Tuple2<Integer,Integer>> list =
					LazyFutureStream.of(slow,fast,med).actOnFutures().zip(LazyFutureStream.of(slow,fast,med))
													.peek(it -> System.out.println(it)).collect(Collectors.toList());
	 * 
	 * // [slow,fast],[fast,med],[med,slow]
	 * }
	 * </pre>
	 */
	default <R> LazyFutureStream<Tuple2<T, R>> zip(Stream<R> other) {
		
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().injectFuturesSeq()
				.map(f -> f.toCompletableFuture())
				.zipStream(other)
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2)))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));

	}

	/**
	 * zip 3 Streams into one
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Tuple3&lt;Integer, Integer, Character&gt;&gt; list = of(1, 2, 3, 4, 5, 6).zip3(
	 * 			of(100, 200, 300, 400), of('a', 'b', 'c')).collect(
	 * 			Collectors.toList());
	 * 
	 * 	// [[1,100,'a'],[2,200,'b'],[3,300,'c']]
	 * }
	 * 
	 * </pre>
	 */
	default <S, U> LazyFutureStream<Tuple3<T, S, U>> zip3(
			Stream<? extends S> second, Stream<? extends U> third) {

		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().injectFuturesSeq()
				.map(f -> f.toCompletableFuture()).zip3(second, third)
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2, t.v3)))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));

	}
	default <S, U> LazyFutureStream<Tuple3<T, S, U>> zip3Lfs(
			LazyFutureStream<? extends S> second, LazyFutureStream<? extends U> third) {

		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().injectFuturesSeq()
				.map(f -> f.toCompletableFuture()).zip3(second.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()), third.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()))
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2.join(), t.v3.join())))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));

	}

	/**
	 * zip 4 Streams into 1
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;Tuple4&lt;Integer, Integer, Character, String&gt;&gt; list = of(1, 2, 3, 4, 5,
	 * 			6).zip4(of(100, 200, 300, 400), of('a', 'b', 'c'),
	 * 			of(&quot;hello&quot;, &quot;world&quot;)).collect(Collectors.toList());
	 * 
	 * }
	 * // [[1,100,'a',&quot;hello&quot;],[2,200,'b',&quot;world&quot;]]
	 * </pre>
	 */
	default <T2, T3, T4> SequenceM<Tuple4<T, T2, T3, T4>> zip4(
			Stream<T2> second, Stream<T3> third, Stream<T4> fourth) {
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive()
				.injectFuturesSeq()
				.map(f -> f.toCompletableFuture())
				.zip4(second, third, fourth)
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2, t.v3, t.v4)))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));
	}
	default <T2, T3, T4> SequenceM<Tuple4<T, T2, T3, T4>> zip4Lfs(
			LazyFutureStream<T2> second, LazyFutureStream<T3> third, LazyFutureStream<T4> fourth) {
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive()
				.injectFuturesSeq()
				.map(f -> f.toCompletableFuture())
				.zip4(second.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()), third.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()), fourth.getLastActive().injectFuturesSeq()
						.map(f -> f.toCompletableFuture()))
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2.join(), t.v3.join(), t.v4.join())))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));
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
	default LazyFutureStream<Tuple2<T, Long>> zipWithIndex() {
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().injectFuturesSeq()
				.map(f -> f.toCompletableFuture()).zipWithIndex()
				.map(t -> t.v1.thenApply(r -> Tuple.tuple(r, t.v2)))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));
	}

	/**
	 * Create a sliding view over this Sequence
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = anyM(Stream.of(1, 2, 3, 4, 5, 6)).asSequence()
	 * 			.sliding(2).collect(Collectors.toList());
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
	default LazyFutureStream<List<T>> sliding(int windowSize) {

		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive()
				.<T> injectFuturesSeq()
				.sliding(windowSize)
				.map(list-> Tuple.tuple(list,list.stream().map(f->f.toCompletableFuture())))
				
				.map(tuple-> FastFuture.fromCompletableFuture(CompletableFuture.allOf(tuple.v2.collect(Collectors.toList()).toArray(new CompletableFuture[0]))
						.thenApply(v->tuple.v1.stream().map(f -> safeJoin(f)).collect(Collectors.toList())))));
	}

	/**
	 * Create a sliding view over this Sequence
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;List&lt;Integer&gt;&gt; list = anyM(Stream.of(1, 2, 3, 4, 5, 6)).asSequence()
	 * 			.sliding(3, 2).collect(Collectors.toList());
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
	default LazyFutureStream<List<T>> sliding(int windowSize,
			int increment) {
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive()
				.<T> injectFuturesSeq()
				.sliding(windowSize, increment)
				.map(list-> Tuple.tuple(list,list.stream().map(f->f.toCompletableFuture())))
				
				.map(tuple-> FastFuture.fromCompletableFuture(CompletableFuture.allOf(tuple.v2.collect(Collectors.toList()).toArray(new CompletableFuture[0]))
						.thenApply(v->tuple.v1.stream().map(f -> safeJoin(f)).collect(Collectors.toList())))));
				
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
	default SequenceM<List<T>> grouped(int groupSize) {
		
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive()
				.<T> injectFuturesSeq()
				.grouped(groupSize)
				.map(list-> Tuple.tuple(list,list.stream().map(f->f.toCompletableFuture())))
				
				.map(tuple-> FastFuture.fromCompletableFuture(CompletableFuture.allOf(tuple.v2.collect(Collectors.toList()).toArray(new CompletableFuture[0]))
						.thenApply(v->tuple.v1.stream().map(f -> safeJoin(f)).collect(Collectors.toList())))));
			
				
	}

	/**
	 * <pre>
	 * {@code assertThat(LazyFutureStream.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
	 * </pre>
	 * 
	 * 
	 * 
	 * @param num
	 *            Number of elemenets to skip
	 * @return Monad converted to Stream with specified number of elements
	 *         skipped
	 */
	default SequenceM<T> skip(long n) {
		Continueable sub = this.getSubscription();
		sub.registerSkip(n);
		LazyStreamWrapper lastActive = getLastActive();
		LazyStreamWrapper limited = lastActive.withStream(lastActive.stream()
				.skip(n));
		return this.withLastActive(limited);
	}

	/**
	 * 
	 * 
	 * <pre>
	 * {@code assertThat(LazyFutureStream.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
	 * </pre>
	 * 
	 * @param num
	 *            Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	default LazyFutureStream<T> limit(long maxSize) {
		Continueable sub = this.getSubscription();
		sub.registerLimit(maxSize);
		LazyStreamWrapper lastActive = getLastActive();
		LazyStreamWrapper limited = lastActive.withStream(lastActive.stream()
				.limit(maxSize));
		return this.withLastActive(limited);
	}

	/**
	 * extract head and tail together, where head is expected to be present
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SequenceM&lt;String&gt; helloWorld = LazyFutureStream
	 * 			.of(&quot;hello&quot;, &quot;world&quot;, &quot;last&quot;);
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
	default HeadAndTail<T> headAndTail() {
		return this
				.getLastActive()
				.injectFuturesSeq()
				.headAndTailOptional()
				.map(hat -> new HeadAndTail(safeJoin(hat.head()),
						fromStreamOfFutures(hat.tail()))).get();
	}

	/**
	 * extract head and tail together, where no head or tail may be present
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SequenceM&lt;String&gt; helloWorld = LazyFutureStream.of();
	 * 	Optional&lt;HeadAndTail&lt;String&gt;&gt; headAndTail = helloWorld
	 * 			.headAndTailOptional();
	 * 	assertTrue(!headAndTail.isPresent());
	 * 
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	default Optional<HeadAndTail<T>> headAndTailOptional() {
		return this
				.getLastActive()
				.injectFuturesSeq()
				.headAndTailOptional()
				.map(hat -> new HeadAndTail(safeJoin(hat.head()),
						fromStreamOfFutures(hat.tail())));
	}

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	default LazyFutureStream<T> intersperse(T value) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.intersperse(FastFuture.completedFuture(value)));
	}

	default LazyFutureStream<T> intersperse(CompletableFuture<T> value) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.intersperse(FastFuture.fromCompletableFuture(value)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#shuffle()
	 */
	default SequenceM<T> shuffle() {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.shuffle());
	}

	/**
	 * Append Stream to this SequenceM
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = LazyFutureStream.of(1, 2, 3)
	 * 			.appendStream(LazyFutureStream.of(100, 200, 300))
	 * 			.map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;, &quot;100!!&quot;,
	 * 			&quot;200!!&quot;, &quot;300!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            to append
	 * @return SequenceM with Stream appended
	 */
	default LazyFutureStream<T> appendStream(Stream<T> stream) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.appendStream(stream.map(v -> FastFuture.completedFuture(v))));
	}

	/**
	 * Prepend Stream to this SequenceM
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = LazyFutureStream.of(1, 2, 3)
	 * 			.prependStream(of(100, 200, 300)).map(it -&gt; it + &quot;!!&quot;)
	 * 			.collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;, &quot;1!!&quot;,
	 * 			&quot;2!!&quot;, &quot;3!!&quot;)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            to Prepend
	 * @return SequenceM with Stream prepended
	 */
	default LazyFutureStream<T> prependStream(Stream<T> stream) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.prependStream(stream.map(v -> FastFuture.completedFuture(v))));
	}

	default LazyFutureStream<T> prependStreamFutures(
			Stream<CompletableFuture<T>> stream) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.prependStream(
						stream.map(v -> FastFuture.fromCompletableFuture(v))));
	}

	/**
	 * Append values to the end of this SequenceM
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = LazyFutureStream.of(1, 2, 3).append(100, 200, 300)
	 * 			.map(it -&gt; it + &quot;!!&quot;).collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;2!!&quot;, &quot;3!!&quot;, &quot;100!!&quot;,
	 * 			&quot;200!!&quot;, &quot;300!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param values
	 *            to append
	 * @return SequenceM with appended values
	 */
	default LazyFutureStream<T> append(T... values) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.appendStream(
						Stream.of(values).map(
								v -> FastFuture.completedFuture(v))));
	}

	/**
	 * Prepend given values to the start of the Stream
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	LazyFutureStream.of(1,2,3)
	 * 									 .prepend(100,200,300)
	 * 													 .map(it ->it+"!!")
	 * 													 .collect(Collectors.toList());
	 * 
	 * 						assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
	 * }
	 * @param values to prepend
	 * @return SequenceM with values prepended
	 */
	default LazyFutureStream<T> prepend(T... values) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.prependStream(
						Stream.of(values).map(
								v -> FastFuture.completedFuture(v))));
	}

	default LazyFutureStream<T> prependFutures(CompletableFuture<T>... values) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.prependStream(
						Stream.of(values).map(
								v -> FastFuture.fromCompletableFuture(v))));
	}

	/**
	 * Insert data into a stream at given position
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = LazyFutureStream.of(1, 2, 3)
	 * 			.insertAt(1, 100, 200, 300).map(it -&gt; it + &quot;!!&quot;)
	 * 			.collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;,
	 * 			&quot;2!!&quot;, &quot;3!!&quot;)));
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
	default LazyFutureStream<T> insertAt(int pos, T... values) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.insertStreamAt(
						pos,
						Stream.of(values).map(
								v -> FastFuture.completedFuture(v))));
	}

	/**
	 * Delete elements between given indexes in a Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = LazyFutureStream.of(1, 2, 3, 4, 5, 6)
	 * 			.deleteBetween(2, 4).map(it -&gt; it + &quot;!!&quot;)
	 * 			.collect(Collectors.toList());
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
	default LazyFutureStream<T> deleteBetween(int start, int end) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.deleteBetween(start, end));
	}

	/**
	 * Insert a Stream into the middle of this stream at the specified position
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	List&lt;String&gt; result = LazyFutureStream.of(1, 2, 3)
	 * 			.insertStreamAt(1, of(100, 200, 300)).map(it -&gt; it + &quot;!!&quot;)
	 * 			.collect(Collectors.toList());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(&quot;1!!&quot;, &quot;100!!&quot;, &quot;200!!&quot;, &quot;300!!&quot;,
	 * 			&quot;2!!&quot;, &quot;3!!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param pos
	 *            to insert Stream at
	 * @param stream
	 *            to insert
	 * @return newly conjoined SequenceM
	 */
	default LazyFutureStream<T> insertStreamAt(int pos, Stream<T> stream) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.insertStreamAt(pos,
						stream.map(v -> FastFuture.completedFuture(v))));
	}

	default LazyFutureStream<T> insertStreamFuturesAt(int pos,
			Stream<CompletableFuture<T>> stream) {
		return fromStreamOfFutures(this
				.getLastActive()
				.injectFuturesSeq()
				.insertStreamAt(pos,
						stream.map(v -> FastFuture.fromCompletableFuture(v))));
	}

	/**
	 * assertThat(LazyFutureStream.of(1,2,3,4,5) .skipLast(2)
	 * .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	 * 
	 * @param num
	 * @return
	 */
	default SequenceM<T> skipLast(int num) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.skipLast(num));
	}

	/**
	 * Limit results to the last x elements in a SequenceM
	 * 
	 * <pre>
	 * {@code 
	 * 	assertThat(LazyFutureStream.of(1,2,3,4,5)
	 * 										.limitLast(2)
	 * 										.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	 * 
	 * }
	 * 
	 * @param num of elements to return (last elements)
	 * @return SequenceM limited to last num elements
	 */
	default SequenceM<T> limitLast(int num) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.limitLast(num));
	}

	/**
	 * Return the elementAt index or Optional.empty
	 * 
	 * <pre>
	 * {@code
	 * 	assertThat(LazyFutureStream.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
	 * }
	 * </pre>
	 * 
	 * @param index
	 *            to extract element from
	 * @return elementAt index
	 */
	default Optional<T> elementAt(long index) {
		return this.getLastActive().injectFuturesSeq().zipWithIndex()
				.filter(t -> t.v2 == index).findFirst()
				.map(t -> safeJoin(t.v1()));
	}

	/**
	 * Gets the element at index, and returns a Tuple containing the element (it
	 * must be present) and a lazy copy of the Sequence for further processing.
	 * 
	 * <pre>
	 * {@code 
	 * LazyFutureStream.of(1,2,3,4,5).get(2).v1
	 * //3
	 * }
	 * </pre>
	 * 
	 * @param index
	 *            to extract element from
	 * @return Element and Sequence
	 */
	default Tuple2<T, LazyFutureStream<T>> get(long index) {
		Tuple2<SequenceM<FastFuture<T>>, SequenceM<FastFuture<T>>> tuple = this
				.getLastActive().injectFuturesSeq().duplicateSequence();
		return tuple.map1(
				s -> s.zipWithIndex().filter(t -> t.v2 == index)
						.map(f -> safeJoin(f.v1)).findFirst().get()).map2(
				s -> fromStreamOfFutures(s));
	}

	default <R> LazyFutureStream<R> thenCombine(BiFunction<T, T, R> combiner) {
		return (LazyFutureStream) fromStreamOfFutures((Stream) this
				.getLastActive().<T> injectFuturesSeq()
				.map(f -> f.toCompletableFuture()).grouped(2)
				.filter(list -> list.size() == 2)
				.map(list -> list.get(0).thenCombine(list.get(1), combiner))
				.map(cf -> FastFuture.fromCompletableFuture(cf)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#concat(java.util.stream.Stream)
	 */
	default SequenceM<T> concat(Stream<T> other) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.concat(other.map(t -> FastFuture.completedFuture(t))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#concat(java.lang.Object)
	 */
	default SequenceM<T> concat(T other) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.concat(FastFuture.completedFuture(other)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#concat(java.lang.Object[])
	 */
	default SequenceM<T> concat(T... other) {
		return concat(Stream.of(other));
	}

	default SequenceM<T> concatFutures(CompletableFuture<T>... other) {
		return concatFutures(Stream.of(other));
	}

	default SequenceM<T> concatFutures(Stream<CompletableFuture<T>> other) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.concat(other.map(t -> FastFuture.fromCompletableFuture(t))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#shuffle(java.util.Random)
	 */
	default SequenceM<T> shuffle(Random random) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.shuffle(random));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	default SequenceM<T> slice(long from, long to) {
		return fromStreamOfFutures(this.getLastActive().injectFuturesSeq()
				.slice(from, to));
	}

	
	default SequenceM<CompletableFuture<T>> toStream(){
		return this.getLastActive().injectFuturesSeq().map(f->f.toCompletableFuture());
	}
	default Set<CompletableFuture<T>> toSet() {
		
		return toStream().collect(Collectors.toSet());
	}

	default <R, A> R collect(Collector<? super CompletableFuture<T>, A, R> collector){
		return toStream().collect(collector);
	}
	
	default List<CompletableFuture<T>> toList() {
		
		return toStream().collect(Collectors.toList());
	}

	
	default <C extends Collection<CompletableFuture<T>>> C toCollection(
			Supplier<C> collectionFactory) {
		
		return toStream().collect(Collectors.toCollection(collectionFactory));
	}
	default Optional<CompletableFuture<T>> reduce(BinaryOperator<CompletableFuture<T>> accumulator){
		return toStream().reduce(accumulator);
	}
	default CompletableFuture<T> foldRight(CompletableFuture<T> identity,  BinaryOperator<CompletableFuture<T>> accumulator){
		
		return toStream().foldRight(identity,accumulator);
	}
	
	default CompletableFuture<T> foldLeft(CompletableFuture<T> identity,  BinaryOperator<CompletableFuture<T>> accumulator){
		return toStream().foldLeft(identity,accumulator);
	}
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	default CompletableFuture<T> reduce(CompletableFuture<T> identity, BinaryOperator<CompletableFuture<T>> accumulator){
		return toStream().reduce(identity,accumulator);
	}
	 /* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	default <U> CompletableFuture<U> reduce(CompletableFuture<U> identity,
            BiFunction<CompletableFuture<U>, ? super CompletableFuture<T>, CompletableFuture<U>> accumulator,
            BinaryOperator<CompletableFuture<U>> combiner){
		return toStream().reduce(identity, accumulator, combiner);
	}
}
