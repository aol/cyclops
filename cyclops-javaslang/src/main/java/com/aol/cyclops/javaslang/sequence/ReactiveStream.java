package com.aol.cyclops.javaslang.sequence;

import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collector;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.IndexedSeq;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Stream;
import javaslang.control.Option;

import org.jooq.lambda.Seq;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.javaslang.Javaslang;
import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsPublisher;
import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsSubscriber;
import com.aol.cyclops.javaslang.streams.JavaslangHotStream;
import com.aol.cyclops.javaslang.streams.StreamUtils;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsTerminalOperations;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public interface ReactiveStream<T> extends Stream<T>, Publisher<T>, ReactiveStreamsTerminalOperations<T> {

	/** creational methods **/
	
	static <T> ReactiveStream<T> of(T... values) {
					
		return fromStream(Stream.ofAll(values));
	}
	static <T> ReactiveStream<T> empty() {
		return fromStream(Stream.empty());
	}
	
	static <T> ReactiveStream<T> iterate(T seed, Function<? super T, ? extends T> gen) {
		return fromStream(Stream.gen(seed, gen));
	}

	static <T> ReactiveStream<T> generate(Supplier<T> gen) {
		return fromStream(Stream.gen(gen));
	}

	static <T> ReactiveStream<T> fromStream(Stream<T> stream) {
		if(stream instanceof ReactiveStream)
			return (ReactiveStream<T>)stream;
		return new ReactiveStreamImpl<T>(stream);
	}
	static <T> ReactiveStream<T> fromStreamable(Streamable<T> stream) {
		return fromIterable(stream);
	}

	static <T> ReactiveStream<T> fromJDK(BaseStream<T, ? extends BaseStream<T, ?>> stream) {
		return fromIterable(()->stream.iterator());
	}
	static <T> ReactiveStream<T> fromAnyM(AnyM<T> stream) {
		return fromJDK(stream.stream());
	}

	static <T> ReactiveStream<T> fromIterable(Iterable<T> stream) {
		return fromStream(Stream.ofAll(stream));
	}

	static <T> ReactiveStream<T> fromAsyncQueue(com.aol.simple.react.async.Queue<T> q) {
		
		return fromJDK(q.stream(new com.aol.simple.react.async.subscription.Subscription()));

	}
	
	static <T> JavaslangReactiveStreamsSubscriber<T> subscriber(){
		return new JavaslangReactiveStreamsSubscriber<>();
	}
	/** To Transfer Queue **/
	default com.aol.simple.react.async.Queue<T> toAsyncBlockingQueue(int boundSize) {
		return new com.aol.simple.react.async.Queue<>(new LinkedBlockingQueue<T>(boundSize));
	}
	default com.aol.simple.react.async.Queue<T> toAsyncQueue() {
		return QueueFactories.<T>unboundedNonBlockingQueue().build();
	}

	default com.aol.simple.react.async.Queue<T> toAsyncQueue(int boundSize){
		return QueueFactories.<T>boundedNonBlockingQueue(boundSize).build();
	}
	
	/** JDK Collect **/
	default <R, A> R collect(Collector<? super T, A, R> collector){
		return this.toJavaStream().collect(collector);
	}

	/** conversions **/
	default AnyM<T> anyM() {
		return Javaslang.anyM(this);
	}

	default SequenceM<T> sequenceM() {
		return SequenceM.fromIterable(this);
	}

	default Seq<T> seq(){
		return Seq.seq(this);
	}
	default Streamable<T> streamable() {
		return Streamable.fromIterable(this);
	}

	default LazyFutureStream<T> futureStream() {
		return LazyFutureStream.lazyFutureStreamFromIterable(this);
	}

	default LazyFutureStream<T> futureStream(LazyReact react) {
		return react.fromIterable(this);
	}

	default SimpleReactStream<T> futures() {
		return (SimpleReactStream<T>) new SimpleReact().fromIterable(this);
	}

	default SimpleReactStream<T> futures(SimpleReact react) {
		return (SimpleReactStream<T>) react.fromIterable(this);
	}
	/** subscribe **/
	@Override
	default void subscribe(Subscriber<? super T> s) {
		 JavaslangReactiveStreamsPublisher.ofSync(this)
		 									.subscribe(s);
		
	}
	default void subscribeAsync(Executor ex,Subscriber<? super T> s) {
		 JavaslangReactiveStreamsPublisher.ofAsync(this,ex)
		 									.subscribe(s);
		
	}
	/** async execution **/
	default FutureOperations<T> futureOperations(Executor ex) {

		return StreamUtils.futureOperations(this, ex);
	}

	default JavaslangHotStream<T> pausedHotStream(Executor ex) {
		return StreamUtils.hotStream(this, ex);
	}
	default JavaslangHotStream<T> hotStream(Executor ex) {
		return StreamUtils.hotStream(this, ex);
	}

	/** fan out operators **/
	default <R> LazyFutureStream<R> asyncMap(LazyReact react, Function<? super T, ? extends R> fn) {
		return this.futureStream(react).map((Function) fn);
	}

	default <R> LazyFutureStream<R> asyncRetry(LazyReact react, Function<? super T, ? extends R> fn) {
		return this.futureStream(react).retry((Function) fn);
	}

	/** recover & retry **/
	default Stream<T> recover(Function<Throwable, ? extends T> fn) {
		return StreamUtils.recover(this, fn);
	}

	default <EX extends Throwable> Stream<T> recover(Class<EX> type, Function<EX, ? extends T> fn) {
		return StreamUtils.recover(this, type, fn);
	}

	default <R> ReactiveStream<R> retry(Function<? super T, ? extends R> fn) {

		return fromJDK(sequenceM().retry(fn));
	}

	/** time based operations **/
	  default ReactiveStream<T> debounce(long time, TimeUnit t){
		  return fromStream(StreamUtils.debounce(this, time, t));
	  }
	  default ReactiveStream<T> onePer(Stream<T> stream, long time, TimeUnit t){
		  return fromStream(StreamUtils.onePer(this, time, t));
	  }
	  default ReactiveStream<T> jitter(Stream<T> stream,long jitterInNanos){
		  return fromStream(StreamUtils.jitter(this,jitterInNanos));
	  }
	  default ReactiveStream<T> fixedDelay(Stream<T> stream,long time, TimeUnit unit){
		  return fromStream(StreamUtils.fixedDelay(this, time, unit));
	  }
	  default ReactiveStream<T> xPer(Stream<T> stream,int x, long time, TimeUnit t){
		  return fromStream(StreamUtils.xPer(this, x,time,t));
	  }
	  /** batching & windowing **/
	  default ReactiveStream<ReactiveStream<T>> slidingWindow(int windowSize,int increment){
		  return fromStream(StreamUtils.sliding(this, windowSize,increment)).map(s->fromIterable(s));
	  }
	  default ReactiveStream<ReactiveStream<T>> slidingWindow(int windowSize){
		  return fromStream(StreamUtils.sliding(this, windowSize,1)).map(s->fromIterable(s));
	  }
	  default ReactiveStream<ReactiveStream<T>>  windowByTime(long time, TimeUnit t){
			return fromStream(StreamUtils.windowByTime(this, time, t).map(s->fromStreamable(s)));
	  }
	  
	  default ReactiveStream<ReactiveStream<T>>  windowStatefullyWhile(BiPredicate<Streamable<? super T>,? super T> predicate){
		  return fromStream(StreamUtils.windowStatefullyWhile(this, predicate).map(s->fromStreamable(s)));
	  }
	  default ReactiveStream<ReactiveStream<T>>  windowWhile(Predicate<? super T> predicate){
		  return fromStream(StreamUtils.windowWhile(this, predicate).map(s->fromStreamable(s)));
	  }
	  default ReactiveStream<ReactiveStream<T>>  windowUntil(Predicate<? super T> predicate){
		  return fromStream(StreamUtils.windowWhile(this, predicate.negate()).map(s->fromStreamable(s)));
	  }
	 
	  
	  default ReactiveStream<ReactiveStream<T>>  windowBySizeAndTime(Stream<T> stream,int size, long time, TimeUnit t){
		  return fromStream(StreamUtils.windowBySizeAndTime(this, size,time,t).map(s->fromStreamable(s)));
	  }
	/** cycle **/
	/**
	 * Create a new Stream that infiniteable cycles the provided Stream
	 * 
	 * <pre>
	 * {@code 		
	 * assertThat(Stream.ofAll(1,2,3)
	 *                  .cycle()
	 * 					.limit(6)
	 * 				    .toList(),
	 * 					equalTo(List.ofAll(1,2,3,1,2,3)));
	 * 		}
	 * </pre>
	 * 
	 * @param s
	 *            Stream to cycle
	 * @return New cycling stream
	 */
	default ReactiveStream<T> cycle() {

		return fromStream(StreamUtils.cycle(this));
	}

	default ReactiveStream<T> cycle(int times) {

		return fromStream(StreamUtils.cycle(times, streamable()));
	}

	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * 	int count = 0;
	 * 
	 * 	assertThat(Stream.ofAll(1, 2, 2, 3)
	 *                   .cycleWhile(next -> count++ < 6)
	 *                   .toList()), equalTo(Arrays.asList(1, 2, 2, 1, 2, 2));
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	default ReactiveStream<T> cycleWhile(Predicate<? super T> predicate) {
		return fromStream(StreamUtils.cycleWhile(this, predicate));
	}

	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * 	count =0;
	 * 		assertThat(Stream.ofAll(1,2,2,3)
	 * 						 .cycleUntil(next -> count++>10 )
	 * 						 .toList(),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	default ReactiveStream<T> cycleUntil(Stream<T> stream, Predicate<? super T> predicate) {
	
		return fromStream(StreamUtils.cycleUntil(this, predicate));
	}
	
	/** scanLeft & scanRight **/
	default <U> Stream<U> scanRight(U identity,BiFunction<? super T, U, U>  combiner){
		return fromJDK(sequenceM().scanRight(identity,combiner));
	}
	default  ReactiveStream<T> scanRight(Monoid<T> monoid){
		return fromJDK(sequenceM().scanRight(monoid));
	}
	default <U> ReactiveStream<U> scanLeft(U identity, BiFunction<U, ? super T, U> combiner){
		return fromJDK(sequenceM().scanLeft(identity,combiner));
	}
	/**
	 * Scan left using supplied Monoid
	 * 
	 * 
	 * @param monoid
	 * @return
	 */
	default ReactiveStream<T> scanLeft(Monoid<T> monoid) {
		return fromStream(StreamUtils.scanLeft(this, monoid));
	}

	/** take & drop **/
	
	default ReactiveStream<T> take(long time, TimeUnit unit){
		return fromJDK(sequenceM().limit(time, unit));
	}
	default ReactiveStream<T> drop(long time, TimeUnit unit){
		return fromJDK(sequenceM().skip(time, unit));
	}

	/** for-comprehensions **/
	/** 
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	  *<pre>
	 * {@code 
	 * SequenceM.of(1,2)
						.forEach3(a->IntStream.range(10,13),
						        a->b->Stream.of(""+(a+b),"hello world"),
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
	<R1,R2,R> ReactiveStream<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1, 
													Function<? super T,Function<? super R1,? extends Iterable<R2>>> stream2,
													Function<? super T,Function<? super R1,Function<? super R2,? extends R>>> yieldingFunction );
	

	/**
	 * Perform a three level nested internal iteration over this Stream and the supplied streams
	 * 
	 *<pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
						.forEach3(a->IntStream.range(10,13),
						      a->b->Stream.of(""+(a+b),"hello world"),
						         a->b->c-> c!=3,
									a->b->c->c+":"a+":"+b);
									
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre> 
	 * 
	 * 
	 * @param stream1 Nested Stream to iterate over
	 * @param stream2 Nested Stream to iterate over
	 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	<R1,R2,R> ReactiveStream<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1, 
											Function<? super T,Function<? super R1,? extends Iterable<R2>>> stream2,
															Function<? super T,Function<? super R1,Function<? super R2,Boolean>>> filterFunction,
													Function<? super T,Function<? super R1,Function<? super R2,? extends R>>> yieldingFunction );
	
	

	
	
	
	
	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
						.forEach2(a->IntStream.range(10,13),
									a->b->a+b);
									
	 * 
	 *  //SequenceM[11,14,12,15,13,16]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1 Nested Stream to iterate over
	 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	<R1,R> ReactiveStream<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1, 
													Function<? super T,Function<? super R1,? extends R>> yieldingFunction );

	
	/**
	 * Perform a two level nested internal iteration over this Stream and the supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
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
	<R1,R> ReactiveStream<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1, 
												Function<? super T, Function<? super R1, Boolean>> filterFunction,
													Function<? super T,Function<? super R1,? extends R>> yieldingFunction );
	/** reduction **/
	/**
	 * Simultaneously reduce a stream with multiple reducers
	 * 
	 * <pre>
	 * {@code
	 * 
	 *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = StreamUtils.reduce(Stream.ofAll(1,2,3,4),Arrays.asList(sum,mult));
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
		}</pre>
	 * 
	 * @param stream Stream to reduce
	 * @param reducers Reducers to reduce Stream
	 * @return Reduced Stream values as List entries
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default List<T> reduce(Iterable<Monoid<T>> reducers) {
		return StreamUtils.reduce(this, reducers);

	}

	/**
	 * Simultanously reduce a stream with multiple reducers
	 * 
	 * <pre>
	 * {@code 
	 *  Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		assertThat(StreamUtils.reduce(Stream.ofAll("hello", "world", "woo!"),Stream.ofAll(concat,join))
		                 ,equalTo(Arrays.asList("helloworldwoo!",",hello,world,woo!")));
	 * }
	 * </pre>
	 * 
	 *  @param stream Stream to reduce
	 * @param reducers Reducers to reduce Stream
	 * @return Reduced Stream values as List entries
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default  List<T> reduce(Stream<Monoid<T>> reducers) {
		return StreamUtils.reduce(this, reducers);

	}
	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Monoid<R> reducer){
		return StreamUtils.mapReduce(this,reducer);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
	 *  Then use Monoid to reduce values
	 *  
	 * @param mapper Function to map Monad type
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <R>  R mapReduce(Function<? super T,? extends R> mapper, Monoid<R> reducer){
		return StreamUtils.mapReduce(this,reducer);
	}

	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	default  T reduce(Monoid<T> reducer){
		return StreamUtils.foldLeft(this,reducer);
	}
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	default T foldRight(Monoid<T> reducer){
		return StreamUtils.foldRight(this,reducer);
		
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default T foldRightMapToType(Monoid<T> reducer){
		return StreamUtils.foldRightMapToType(this,reducer);
	}
	/** Zipping **/
	
	/**
	 *  Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {@code
	 * Stream<List<Integer>> zipped = StreamUtils.zip(Stream.ofAll(1,2,3)
										,anyM(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
	 * 
	 * }
	 * </pre>
	 
	 */
	default <S, R> Stream<R> zipAnyM(AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return fromStream(StreamUtils.zipAnyM(this, second, zipper));
	}

	@Override
	default ReactiveStream<T> append(T element) {
		return fromStream(toStream().append(element));
	}

	@Override
	default ReactiveStream<T> appendAll(Iterable<? extends T> elements) {
		return fromStream(toStream().appendAll(elements));
	}

	@Override
	default ReactiveStream<T> clear() {
		return fromStream(toStream().clear());
	}

	@Override
	default ReactiveStream<Tuple2<T, T>> crossProduct() {
		return fromStream(toStream().crossProduct());
	}

	@Override
	default ReactiveStream<IndexedSeq<T>> crossProduct(int power) {
		return fromStream(toStream().crossProduct(power));
	}

	@Override
	default <U> ReactiveStream<Tuple2<T, U>> crossProduct(Iterable<? extends U> that) {
		return fromStream(toStream().crossProduct(that));
	}

	@Override
	default ReactiveStream<Stream<T>> combinations() {
		return fromStream(toStream().combinations());
	}

	@Override
	default ReactiveStream<Stream<T>> combinations(int k) {
		return fromStream(toStream().combinations(k));
	}

	@Override
	default ReactiveStream<T> distinct() {
		return fromStream(toStream().distinct());
	}

	@Override
	default ReactiveStream<T> distinctBy(Comparator<? super T> comparator) {
		return fromStream(toStream().distinctBy(comparator));
	}

	@Override
	default <U> ReactiveStream<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
		return fromStream(toStream().distinctBy(keyExtractor));
	}

	@Override
	default ReactiveStream<T> drop(int n) {
		return fromStream(toStream().drop(n));
	}

	@Override
	default ReactiveStream<T> dropRight(int n) {
		return fromStream(toStream().dropRight(n));
	}

	@Override
	default ReactiveStream<T> dropWhile(Predicate<? super T> predicate) {
		return fromStream(toStream().dropWhile(predicate));
	}

	@Override
	default ReactiveStream<T> filter(Predicate<? super T> predicate) {
		return fromStream(toStream().filter(predicate));
	}

	@Override
	default <U> ReactiveStream<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
		return fromStream(toStream().flatMap(mapper));
	}

	@Override
	default <U> ReactiveStream<U> flatten() {
		return fromStream(toStream().flatten());
	}

	default <C> Map<C, ? extends ReactiveStream<T>> groupByReactive(Function<? super T, ? extends C> classifier) {
		return toStream().groupBy(classifier).map((i, s) -> Tuple.of((C) i, fromStream(s)));
	}

	@Override
	default ReactiveStream<T> init() {
		return fromStream(toStream().init());
	}

	default Option<? extends ReactiveStream<T>> initOptionReactive() {
		return toStream().initOption().map(s -> fromStream(s));
	}

	@Override
	default ReactiveStream<T> insert(int index, T element) {
		return fromStream(toStream().insert(index, element));
	}

	@Override
	default ReactiveStream<T> insertAll(int index, Iterable<? extends T> elements) {
		return fromStream(toStream().insertAll(index, elements));
	}

	@Override
	default ReactiveStream<T> intersperse(T element) {
		return fromStream(toStream().intersperse(element));
	}

	@Override
	default <U> ReactiveStream<U> map(Function<? super T, ? extends U> mapper) {
		return fromStream(toStream().map(mapper));
	}

	@Override
	default ReactiveStream<T> padTo(int length, T element) {
		return fromStream(toStream().padTo(length, element));
	}

	@Override
	default ReactiveStream<T> patch(int from, Iterable<? extends T> that, int replaced) {
		return fromStream(toStream().patch(from, that, replaced));
	}

	default Tuple2<? extends ReactiveStream<T>, ? extends ReactiveStream<T>> partitionReactive(Predicate<? super T> predicate) {

		return toStream().partition(predicate).map(i -> fromStream(i), s -> fromStream(s));
	}

	@Override
	default ReactiveStream<T> peek(Consumer<? super T> action) {
		return fromStream(toStream().peek(action));
	}

	@Override
	default ReactiveStream<Stream<T>> permutations() {
		return fromStream(toStream().permutations());
	}

	@Override
	default ReactiveStream<T> prepend(T element) {
		return fromStream(toStream().prepend(element));
	}

	@Override
	default ReactiveStream<T> prependAll(Iterable<? extends T> elements) {
		return fromStream(toStream().prependAll(elements));
	}

	@Override
	default ReactiveStream<T> remove(T element) {
		return fromStream(toStream().remove(element));
	}

	@Override
	default ReactiveStream<T> removeFirst(Predicate<T> predicate) {
		return fromStream(toStream().removeFirst(predicate));
	}

	@Override
	default ReactiveStream<T> removeLast(Predicate<T> predicate) {
		return fromStream(toStream().removeLast(predicate));
	}

	@Override
	default ReactiveStream<T> removeAt(int index) {
		return fromStream(toStream().removeAt(index));
	}
	
	default ReactiveStream<T> removeBetween(int start,int end) {
		return fromStream(StreamUtils.deleteBetween(this, start, end));
	}

	@Override
	default ReactiveStream<T> removeAll(T element) {
		return fromStream(toStream().removeAll(element));
	}

	@Override
	default ReactiveStream<T> removeAll(Iterable<? extends T> elements) {
		return fromStream(toStream().removeAll(elements));
	}

	@Override
	default ReactiveStream<T> replace(T currentElement, T newElement) {
		return fromStream(toStream().replace(currentElement, newElement));
	}

	@Override
	default ReactiveStream<T> replaceAll(T currentElement, T newElement) {
		return fromStream(toStream().replaceAll(currentElement, newElement));
	}

	@Override
	default ReactiveStream<T> retainAll(Iterable<? extends T> elements) {
		return fromStream(toStream().retainAll(elements));
	}

	@Override
	default ReactiveStream<T> reverse() {
		return fromStream(toStream().reverse());
	}

	@Override
	default ReactiveStream<T> slice(int beginIndex, int endIndex) {
		return fromStream(toStream().slice(beginIndex, endIndex));
	}

	@Override
	default ReactiveStream<T> sort() {
		return fromStream(toStream().sort());
	}

	@Override
	default ReactiveStream<T> sort(Comparator<? super T> comparator) {
		return fromStream(toStream().sort(comparator));
	}

	@Override
	default <U extends Comparable<? super U>> ReactiveStream<T> sortBy(Function<? super T, ? extends U> mapper) {
		return fromStream(toStream().sortBy(mapper));
	}

	@Override
	default <U> ReactiveStream<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
		return fromStream(toStream().sortBy(comparator, mapper));
	}

	default Tuple2<? extends ReactiveStream<T>, ? extends ReactiveStream<T>> spanReactive(Predicate<? super T> predicate) {
		return toStream().span(predicate).map(s1 -> fromStream(s1), s2 -> fromStream(s2));
	}

	@Override
	default ReactiveStream<T> subSequence(int beginIndex) {
		return fromStream(toStream().subSequence(beginIndex));
	}

	@Override
	default ReactiveStream<T> subSequence(int beginIndex, int endIndex) {
		return fromStream(toStream().subSequence(beginIndex, endIndex));
	}

	default Option<? extends ReactiveStream<T>> tailOptionReactive() {
		return toStream().tailOption().map(s -> fromStream(s));
	}

	@Override
	default ReactiveStream<T> take(int n) {
		return fromStream(toStream().take(n));
	}

	@Override
	default ReactiveStream<T> takeRight(int n) {
		return fromStream(toStream().takeRight(n));
	}

	@Override
	default ReactiveStream<T> takeUntil(Predicate<? super T> predicate) {
		return fromStream(toStream().takeUntil(predicate));
	}

	@Override
	default ReactiveStream<T> takeWhile(Predicate<? super T> predicate) {
		return fromStream(toStream().takeWhile(predicate));
	}

	@Override
	default <U> ReactiveStream<U> unit(Iterable<? extends U> iterable) {
		return fromStream(toStream().unit(iterable));
	}

	default <T1, T2> Tuple2<? extends ReactiveStream<T1>, ? extends ReactiveStream<T2>> unzipReactive(
			Function<? super T, Tuple2<? extends T1, ? extends T2>> unzipper) {
		return toStream().unzip(unzipper).map(s1 -> fromStream(s1), s2 -> fromStream(s2));
	}

	@Override
	default ReactiveStream<T> update(int index, T element) {
		return fromStream(toStream().update(index, element));
	}

	@Override
	default <U> ReactiveStream<Tuple2<T, U>> zip(Iterable<U> that) {
		return fromStream(toStream().zip(that));
	}

	@Override
	default <U> ReactiveStream<Tuple2<T, U>> zipAll(Iterable<U> that, T thisElem, U thatElem) {
		return fromStream(toStream().zipAll(that, thisElem, thatElem));
	}

	@Override
	default ReactiveStream<Tuple2<T, Integer>> zipWithIndex() {
		return fromStream(toStream().zipWithIndex());
	}

	default Tuple2<? extends ReactiveStream<T>, ? extends ReactiveStream<T>> splitAtReactive(int n) {
		return toStream().splitAt(n).map(s1 -> fromStream(s1), s2 -> fromStream(s2));
	}

	default Tuple2<? extends ReactiveStream<T>, ? extends ReactiveStream<T>> splitAtReactive(Predicate<? super T> predicate) {
		return toStream().splitAt(predicate).map(s1 -> fromStream(s1), s2 -> fromStream(s2));
	}

	default Tuple2<? extends ReactiveStream<T>, ? extends ReactiveStream<T>> splitAtInclusiveReactive(Predicate<? super T> predicate) {
		return toStream().splitAtInclusive(predicate).map(s1 -> fromStream(s1), s2 -> fromStream(s2));
	}

	default <T1, T2, T3> Tuple3<? extends ReactiveStream<T1>, ? extends ReactiveStream<T2>, ? extends ReactiveStream<T3>> unzip3Reactive(
			Function<? super T, Tuple3<? extends T1, ? extends T2, ? extends T3>> unzipper) {
		return toStream().unzip3(unzipper).map(s1 -> fromStream(s1), s2 -> fromStream(s2), s3 -> fromStream(s3));
	}

	@Override
	default ReactiveStream<T> tail() {
		return fromStream(toStream().tail());
	}

	/**
	 * Execute this Stream on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run at 8PM every night
	 *  ReactiveStream.generate(()->"next job:"+formatDate(new Date()))
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
	 * 	HotStream&lt;Data&gt; dataStream = ReactiveStream.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
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
	default JavaslangHotStream<T> schedule(String cron, ScheduledExecutorService ex) {
		return StreamUtils.schedule(this, cron, ex);
	}

	/**
	 * Execute this Stream on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run every 60 seconds after last job completes
	 *  ReactiveStream.generate(()->"next job:"+formatDate(new Date()))
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
	 * 	HotStream&lt;Data&gt; dataStream = ReactiveStream.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
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
	default JavaslangHotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex) {
		return StreamUtils.scheduleFixedDelay(this, delay, ex);

	}

	/**
	 * Execute this Stream on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run every 60 seconds
	 *  ReactiveStream.generate(()->"next job:"+formatDate(new Date()))
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
	 * 	HotStream&lt;Data&gt; dataStream = ReactiveStream.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
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
	default JavaslangHotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex) {
		return StreamUtils.scheduleFixedRate(this, rate, ex);
	}

}
