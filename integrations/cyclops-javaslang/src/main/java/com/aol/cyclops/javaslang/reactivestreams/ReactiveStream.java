package com.aol.cyclops.javaslang.reactivestreams;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collector;

import javaslang.Lazy;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.LazyStream;
import javaslang.collection.Stream;
import javaslang.collection.Traversable;
import javaslang.control.Option;

import org.jooq.lambda.Seq;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.javaslang.Javaslang;
import com.aol.cyclops.javaslang.streams.JavaslangHotStream;
import com.aol.cyclops.javaslang.streams.StreamUtils;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsTerminalOperations;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.trampoline.Trampoline;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public interface ReactiveStream<T> extends LazyStream<T>, Publisher<T>, ReactiveStreamsTerminalOperations<T>{

	    default <U> U foldRight2(U zero, BiFunction<? super U,? super T, ? extends U> f) {
	        Objects.requireNonNull(f, "f is null");
	        return reverse().foldLeft(zero, (xs, x) -> f.apply(xs, x));
	    }
	/** creational methods **/
	/**
	 * Constructs a ReactiveStream of a head element and a tail supplier.
	 *
	 * @param head
	 *            The head element of the Stream
	 * @param tailSupplier
	 *            A supplier of the tail values. To end the stream, return
	 *            {@link LazyStream#empty}.
	 * @param <T>
	 *            value type
	 * @return A new Stream
	 */
	@SuppressWarnings("unchecked")
	static <T> ReactiveStream<T> cons(Supplier<T> head, Supplier<? extends LazyStream<? extends T>> tailSupplier) {
		return fromStream(LazyStream.cons(head, tailSupplier));
	}

	static ReactiveStream<Integer> from(int value) {

		return fromStream(LazyStream.from(value));
	}

	static ReactiveStream<Long> from(long value) {

		return fromStream(LazyStream.from(value));
	}

	static <T> ReactiveStream<T> of(T... values) {

		return fromStream(LazyStream.of(values));
	}

	static <T> ReactiveStream<T> empty() {
		return fromStream(LazyStream.empty());
	}

	static <T> ReactiveStream<T> iterate(T seed, Function<? super T, ? extends T> gen) {
		return fromStream(LazyStream.gen(seed, gen));
	}

	static ReactiveStream<Integer> range(int start, int end) {
		return fromStream(LazyStream.range(start, end));
	}

	static ReactiveStream<Long> range(long start, long end) {
		return fromStream(LazyStream.range(start, end));
	}

	static <T> ReactiveStream<T> generate(Supplier<T> gen) {
		return fromStream( (LazyStream<T>)Lazy.val(()->LazyStream.gen(gen),LazyStream.class));
	}

	static <T> ReactiveStream<T> fromStream(Stream<T> stream) {
		
		if (stream instanceof ReactiveStream)
			return (ReactiveStream<T>) stream;
		return new ReactiveStreamImpl<T>(LazyStream.fromStream(stream));
	}

	static <T> ReactiveStream<T> fromStreamable(Streamable<T> stream) {
		return fromIterable(stream);
	}

	static <T> ReactiveStream<T> fromJDK(BaseStream<T, ? extends BaseStream<T, ?>> stream) {
		return fromIterable(() -> stream.iterator());
	}

	static <T> ReactiveStream<T> fromAnyM(AnyM<T> stream) {
		return fromJDK(stream.stream());
	}

	static <T> ReactiveStream<T> fromIterable(Iterable<T> stream) {
		return fromStream(LazyStream.ofAll(stream));
	}
	static <T> ReactiveStream<T> fromIterator(Iterator<T> it) {
		return fromStream(LazyStream.ofAll(()->it));
	}


	static <T> ReactiveStream<T> fromAsyncQueue(com.aol.simple.react.async.Queue<T> q) {

		return fromJDK(q.stream(new com.aol.simple.react.async.subscription.Subscription()));

	}

	static <T> JavaslangReactiveStreamsSubscriber<T> subscriber() {
		return new JavaslangReactiveStreamsSubscriber<>();
	}

	
	default <U> ReactiveStream<U> cast(Class<U> type){
		return fromStream(StreamUtils.cast(this, type));
		
	}
	default <U> ReactiveStream<U> ofType(Class<U> type){
		return fromStream(StreamUtils.ofType(this, type));
	}
	
	default <R> ReactiveStream<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		
		 return  map(in-> mapper.apply(in).result());
	 }
	
	/** subscribe **/
	@Override
	default void subscribe(Subscriber<? super T> s) {
		JavaslangReactiveStreamsPublisher.ofSync(this).subscribe(s);

	}

	default void subscribeAsync(Executor ex, Subscriber<? super T> s) {
		JavaslangReactiveStreamsPublisher.ofAsync(this, ex).subscribe(s);

	}

	/** async execution **/
	default FutureOperations<T> futureOperations(Executor ex) {

		return StreamUtils.futureOperations(this, ex);
	}

	default JavaslangHotStream<T> pausedHotStream(Executor ex) {
		return StreamUtils.pausedHotStream(this, ex);
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
	/**
     * An iterator by means of head() and tail(). Subclasses may want to override this method.
     *
     * @return A new Iterator of this Traversable elements.
     */
    @Override
    default javaslang.collection.Iterator<T> iterator() {
        final Traversable<T> that = this;
        return new javaslang.collection.Iterator<T>() {

            Traversable<T> traversable = that;

            @Override
            public boolean hasNext() {
                return !traversable.isEmpty();
            }

            @Override
            public T next() {
            	
                if (traversable.isEmpty()) {
                    throw new NoSuchElementException();
                } else {
                	T result = null;;
                	try{
                		
                	
                	result = traversable.head();
                	
                	
                		
                	}catch(Throwable t){
                		
                		throw ExceptionSoftener.throwSoftenedException(t);
                		
                	}
                	finally{
                		traversable = traversable.tail();
                	}
                    return result;
                }
            }
        };
    }  
   
	default ReactiveStream<T> recover(Function<Throwable, ? extends T> fn) {
		Iterator<T> it = iterator();
		
		Class type =Throwable.class;
		return fromIterator(new Iterator<T>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
				
			}
			@Override
			public T next() {
				try{
					return it.next();
				}catch(Throwable t){
					t.printStackTrace();
					if(type.isAssignableFrom(t.getClass())){
						return fn.apply(t);
						
					}
					throw ExceptionSoftener.throwSoftenedException(t);
					
				}
				
				
			}
			
		});
	}

	default <EX extends Throwable> ReactiveStream<T> recover(Class<EX> type, Function<EX, ? extends T> fn) {
		return fromStream(StreamUtils.recover(this, type, fn));
	}

	default <R> ReactiveStream<R> retry(Function<? super T, ? extends R> fn) {

		return fromJDK(sequenceM().retry(fn));
	}

	
	
	/** time based operations **/
	default ReactiveStream<T> debounce(long time, TimeUnit t) {
		return fromStream(StreamUtils.debounce(this, time, t));
	}

	default ReactiveStream<T> onePer(long time, TimeUnit t) {
		return fromStream(StreamUtils.onePer(this, time, t));
	}

	default ReactiveStream<T> jitter(long jitterInNanos) {
		return fromStream(StreamUtils.jitter(this, jitterInNanos));
	}

	default ReactiveStream<T> fixedDelay(long time, TimeUnit unit) {
		return fromStream(StreamUtils.fixedDelay(this, time, unit));
	}

	default ReactiveStream<T> xPer(int x, long time, TimeUnit t) {
		return fromStream(StreamUtils.xPer(this, x, time, t));
	}
	/**
	 * <pre>
	 * {@code 
	 * ReactiveStream.of(1,2,3,4,5)
				 .elapsed()
				 .forEach(System.out::println);
	 * }
	 * </pre>
	 * 
	 * @return Stream that adds the time between elements in millis to each element
	 */
	default ReactiveStream<Tuple2<T,Long>> elapsed(){
		AtomicLong last = new AtomicLong(System.currentTimeMillis());
		
		return zip(ReactiveStream.generate( ()->{
		long now =System.currentTimeMillis();
		
		long result =  now-last.get();
		last.set(now);
		return result;
		} ));
	}
	/**
	 * <pre>
	 * {@code
	 *    ReactiveStream.of(1,2,3,4,5)
				   .timestamp()
				   .forEach(System.out::println)
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @return Stream that adds a timestamp to each element
	 */
	default ReactiveStream<Tuple2<T,Long>> timestamp(){
		return zip(ReactiveStream.generate( ()->System.currentTimeMillis()));
	}

	/** batching & windowing **/
	default ReactiveStream<ReactiveStream<T>> slidingWindow(int windowSize, int increment) {
		return fromStream(StreamUtils.sliding(this, windowSize, increment).map(s -> fromIterable(s)));
	}

	default ReactiveStream<ReactiveStream<T>> slidingWindow(int windowSize) {
		
		return fromStream(StreamUtils.sliding(this, windowSize, 1).map(s -> fromIterable(s)));
	}

	default ReactiveStream<ReactiveStream<T>> windowByTime(long time, TimeUnit t) {
		return fromStream(StreamUtils.windowByTime(this, time, t).map(s -> fromStreamable(s)));
	}

	default ReactiveStream<ReactiveStream<T>> windowStatefullyWhile(BiPredicate<Streamable<? super T>, ? super T> predicate) {
		return fromStream(StreamUtils.windowStatefullyWhile(this, predicate).map(s -> fromStreamable(s)));
	}

	default ReactiveStream<ReactiveStream<T>> windowWhile(Predicate<? super T> predicate) {
		return fromStream(StreamUtils.windowWhile(this, predicate).map(s -> fromStreamable(s)));
	}

	default ReactiveStream<ReactiveStream<T>> windowUntil(Predicate<? super T> predicate) {
		return fromStream(StreamUtils.windowWhile(this, predicate.negate()).map(s -> fromStreamable(s)));
	}

	default ReactiveStream<ReactiveStream<T>> windowBySizeAndTime(int size, long time, TimeUnit t) {
		return fromStream(StreamUtils.windowBySizeAndTime(this, size, time, t).map(s -> fromStreamable(s)));
	}
	default ReactiveStream<ReactiveStream<T>> windowBySize(int size) {
		return fromStream(StreamUtils.batchBySize(this, size).map(s -> fromIterable(s)));
	}
	
	/**
	 * Return a Stream with elements before the provided start index removed, and elements after the provided
	 * end index removed
	 * 
	 * <pre>
	 * {@code 
	 *   ReactiveStream.of(1,2,3,4,5,6).subStream(1,3);
	 *   
	 *   
	 *   //ReactiveStream[2,3]
	 * }
	 * </pre>
	 * 
	 * @param start index inclusive
	 * @param end index exclusive
	 * @return Sequence between supplied indexes of original Sequence
	 */
	default ReactiveStream<T> subStream(int start, int end){
		return this.take(end).removeBetween(0, start);
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
	default ReactiveStream<T> cycleUntil(LazyStream<T> stream, Predicate<? super T> predicate) {

		return fromStream(StreamUtils.cycleUntil(this, predicate));
	}

	/** scanLeft & scanRight **/
	default <U> LazyStream<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {

		return reverse().scanLeft(identity, (u, t) -> combiner.apply(t, u));
	}

	default ReactiveStream<T> scanRight(Monoid<T> monoid) {
		return reverse().scanLeft(monoid.zero(), (u, t) -> monoid.combiner().apply(t, u));

	}

	default <U> ReactiveStream<U> scanLeft(U identity, BiFunction<? super U, ? super T, ? extends U> combiner) {
		return fromStream(LazyStream.super.scanLeft(identity, combiner));
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
	default boolean xMatch(int num, Predicate<? super T> c){
		return StreamUtils.xMatch(this, num, c);
	}

	/** take & drop **/

	default ReactiveStream<T> take(long time, TimeUnit unit) {
		return fromStream(StreamUtils.limit(this, time, unit));
	}

	default ReactiveStream<T> drop(long time, TimeUnit unit) {
		return fromStream(StreamUtils.skip(this, time, unit));
		
	}
	/** If this SequenceM is empty replace it with a another Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(ReactiveStream.of(4,5,6)
							.onEmptySwitch(()->SequenceM.of(1,2,3))
							.toJavaList(),
							equalTo(Arrays.asList(4,5,6)));
	 * }
	 * </pre>
	 * @param switchTo Supplier that will generate the alternative Stream
	 * @return ReactiveStream that will switch to an alternative Stream if empty
	 */
	default ReactiveStream<T> onEmptySwitch(Supplier<LazyStream<T>> switchTo){
		AtomicBoolean called=  new AtomicBoolean(false);
		return ReactiveStream.fromStream(onEmptyGet((Supplier)()->{
				called.set(true); 
				return switchTo.get();
		}).flatMap(s->{
			if(called.get())
				return (LazyStream)s;
			return LazyStream.of(s);
		}));
	}

	default ReactiveStream<T> onEmpty(T value){
		return fromJDK(sequenceM().onEmpty(value));
	}

	
	default ReactiveStream<T> onEmptyGet(Supplier<T> supplier){
		return fromJDK(sequenceM().onEmptyGet(supplier));
	}

	
	default <X extends Throwable> ReactiveStream<T> onEmptyThrow(Supplier<X> supplier){
		return fromJDK(sequenceM().onEmptyThrow(supplier));
	}
	
	/** for-comprehensions **/
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
	<R1, R2, R> ReactiveStream<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, ? extends Iterable<R2>>> stream2,
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
	<R1, R2, R> ReactiveStream<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, ? extends Iterable<R2>>> stream2,
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
	<R1, R> ReactiveStream<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
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
	<R1, R> ReactiveStream<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1, Function<? super T, Function<? super R1, Boolean>> filterFunction,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);

	/** reduction **/
	/**
	 * Simultaneously reduce a stream with multiple reducers
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Monoid&lt;Integer&gt; sum = Monoid.of(0, (a, b) -&gt; a + b);
	 * 	Monoid&lt;Integer&gt; mult = Monoid.of(1, (a, b) -&gt; a * b);
	 * 	val result = StreamUtils.reduce(Stream.ofAll(1, 2, 3, 4), Arrays.asList(sum, mult));
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(10, 24)));
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            Stream to reduce
	 * @param reducers
	 *            Reducers to reduce Stream
	 * @return Reduced Stream values as List entries
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default List<T> reduce(Iterable<Monoid<T>> reducers) {
		return StreamUtils.reduce(this, reducers);

	} */

	/**
	 * Simultanously reduce a stream with multiple reducers
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Monoid&lt;String&gt; concat = Monoid.of(&quot;&quot;, (a, b) -&gt; a + b);
	 * 	Monoid&lt;String&gt; join = Monoid.of(&quot;&quot;, (a, b) -&gt; a + &quot;,&quot; + b);
	 * 	assertThat(StreamUtils.reduce(Stream.ofAll(&quot;hello&quot;, &quot;world&quot;, &quot;woo!&quot;), Stream.ofAll(concat, join)),
	 * 			equalTo(Arrays.asList(&quot;helloworldwoo!&quot;, &quot;,hello,world,woo!&quot;)));
	 * }
	 * </pre>
	 * 
	 * @param stream
	 *            Stream to reduce
	 * @param reducers
	 *            Reducers to reduce Stream
	 * @return Reduced Stream values as List entries
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default List<T> reduce(LazyStream<Monoid<T>> reducers) {
		return StreamUtils.reduce(this, reducers);

	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Monoid<R> reducer) {
		return StreamUtils.mapReduce(this, reducer);
	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid, using
	 * supplied function Then use Monoid to reduce values
	 * 
	 * @param mapper
	 *            Function to map Monad type
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer) {
		return StreamUtils.mapReduce(this, mapper,reducer);
	}

	/**
	 * 
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	default T reduce(Monoid<T> reducer) {
		return StreamUtils.foldLeft(this, reducer);
	}

	/**
	 * 
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	default T foldRight(Monoid<T> reducer) {
		return StreamUtils.foldRight(this, reducer);

	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 
	default T foldRightMapToType(Monoid<T> reducer) {
		return StreamUtils.foldRightMapToType(this, reducer);
	}
*/
	/** Zipping **/

	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Stream&lt;List&lt;Integer&gt;&gt; zipped = StreamUtils.zip(Stream.ofAll(1, 2, 3), anyM(Optional.of(2)), (a, b) -&gt; Arrays.asList(a, b));
	 * 
	 * 	List&lt;Integer&gt; zip = zipped.collect(Collectors.toList()).get(0);
	 * 	assertThat(zip.get(0), equalTo(1));
	 * 	assertThat(zip.get(1), equalTo(2));
	 * 
	 * }
	 * </pre>
	 */
	default <S, R> LazyStream<R> zipAnyM(AnyM<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper) {
		return fromStream(StreamUtils.zipAnyM(this, second, zipper));
	}

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
	 *    //Some[1]
	 *    ReactiveStream.of(1).singleOption(); 
	 *    
	 *    //None
	 *    ReactiveStream.of().singleOpion();
	 *     
	 *    //None
	 *    ReactiveStream.of(1,2,3).singleOption();
	 * }
	 * </pre>
	 * 
	 * @return An Optional with single value if this Stream has exactly one
	 *         element, otherwise Optional Empty
	 */
	default Option<T> singleOption() {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			T result = it.next();
			if (!it.hasNext())
				return Option.of(result);
		}
		return Option.none();

	}

	@Override
	default ReactiveStream<T> append(T element) {
		return fromStream(toStream().append(element));
	}

	default ReactiveStream<T> append(T... elements) {
		return fromStream(toStream().appendAll(Arrays.asList(elements)));
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
	default ReactiveStream<Stream<T>> crossProduct(int power) {
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
	default ReactiveStream<T> drop(long n) {
		return fromStream(toStream().drop(n));
	}

	@Override
	default ReactiveStream<T> dropRight(long n) {
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

	default <U> ReactiveStream<U> flatten() {
		return fromJDK(sequenceM().flatten());
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
	
	
	
	 default <U> ReactiveStream<U> map(Function<? super T, ? extends U> mapper) {
	        Objects.requireNonNull(mapper, "mapper is null");
	        if (isEmpty()) {
	            return fromStream(Empty.instance());
	        } else {
	        	 return fromStream(LazyStream.super.map(mapper));
	        	
	        }
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

	default ReactiveStream<T> prepend(T... elements) {
		return fromStream(toStream().prependAll(Arrays.asList(elements)));
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

	default ReactiveStream<T> removeBetween(int start, int end) {
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
	default ReactiveStream<T> slice(long beginIndex,long endIndex) {
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
	default ReactiveStream<T> take(long n) {
		return fromStream(toStream().take(n));
	}

	@Override
	default ReactiveStream<T> takeRight(long n) {
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
	default ReactiveStream<Tuple2<T, Long>> zipWithIndex() {
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
