package com.aol.simple.react.stream.traits;

import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.Queue.QueueReader;
import com.aol.simple.react.async.Queue.QueueTimeoutException;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.traits.future.operators.ToLazyCollection;
import com.aol.simple.react.stream.traits.operators.BatchBySize;
import com.aol.simple.react.stream.traits.operators.BatchByTime;
import com.aol.simple.react.stream.traits.operators.BatchByTimeAndSize;
import com.aol.simple.react.stream.traits.operators.Debounce;
import com.aol.simple.react.stream.traits.operators.SlidingWindow;
import com.aol.simple.react.util.SimpleTimer;

public interface FutureStream<U> extends Seq<U>, SimpleReactStreamInterface<U>, ToQueue<U> {

	static final ExceptionSoftener softener = ExceptionSoftener.singleton.factory
			.getInstance();
	
	static <T,R> Function<FutureStream<T>,FutureStream<R>> lift(Function<T,R> fn){
		return fs -> fs.map(v->fn.apply(v));
	}
	static <T1,T2,R> BiFunction<FutureStream<T1>,FutureStream<T2>,FutureStream<R>> lift2(BiFunction<T1,T2,R> fn){
		return (fs1,fs2) -> fs1.flatMap( v1-> (FutureStream)fs2.map(v2->fn.apply(v1,v2)));
	}
	
	/**
	 * 
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = LazyFutureStream.of(1,2,3,4,5,6)
				.map(i->i+2)
				.toLazyCollection();
	 * }
	 * </pre>
	 * 
	 * @return a LazyCollection from this Stream
	 * 			Collection will not be populated until methods are accessed.
	 * 			(Eager streams may populate a LazyColleciton in the background however)
	 */
	default Collection<U> toLazyCollection(){
		return ToLazyCollection.toLazyCollection(this.iterator());
	}
	/**
	 * Create a LazyCollection access to which is synchronized
	 * <pre>
	 * {@code 
	 * Collection<Integer> col = LazyFutureStream.of(1,2,3,4,5,6)
				.map(i->i+2)
				.toConcurrentLazyCollection();
	 * }
	 * </pre>
	 * 
	 * @return a LazyCollection from this Stream
	 * 			Collection will not be populated until methods are accessed.
	 * 			(Eager streams may populate a LazyColleciton in the background however)
	 */
	default Collection<U> toConcurrentLazyCollection(){
		return ToLazyCollection.toConcurrentLazyCollection(this.iterator());
	}
	/**
	 * <pre>
	 * {@code 
	 * 	 int num  =	LazyFutureStream.of(1).single();
	 * 
	 *  //num is 1
	 * }
	 * </pre>
	 * 
	 * @return single element from this Stream if it is a single element Stream
	 * 			otherwise throw an UnsupportedOperationException
	 */
	default U single(){
		 List<U> l= toList(); 
		 if(l.size()==1){ 
			 return l.get(l.size()-1); 
			 }
		throw new UnsupportedOperationException("single only works for Streams with a single value");
	}
	/**
	 * Zip two Streams, zipping against the underlying futures of this stream
	 * 
	 * @param other
	 * @return
	 */
	 <R> FutureStream<Tuple2<U,R>> zipFutures(Stream<R> other);/** {
		if(other instanceof FutureStream)
			return zipFutures((FutureStream)other);
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other));
		Seq<Tuple2<CompletableFuture<U>,R>> withType = (Seq<Tuple2<CompletableFuture<U>,R>>)seq;
		Stream futureStream = fromStreamOfFutures((Stream)withType.map(t ->t.v1.thenApply(v -> Tuple.tuple(t.v1.join(),t.v2)))
				);

		
		return (FutureStream<Tuple2<U,R>>)futureStream;

	
	}**/
	
	/* 
	 * @see org.jooq.lambda.Seq#crossJoin(java.util.stream.Stream)
	 */
	@Override
	default <T> FutureStream<Tuple2<U, T>> crossJoin(Stream<T> other) {
	        return fromStream(Seq.crossJoin(this, other));
	}
	
	
	/**
     * Produce this stream, or an alternative stream from the
     * {@code value}, in case this stream is empty.
     */
	@Override
	default FutureStream<U> onEmpty(U value){
		return fromStream(Seq.super.onEmpty(value));
	}
	/**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	@Override
	default FutureStream<U> onEmptyGet(Supplier<U> supplier){
		return fromStream(Seq.super.onEmptyGet(supplier));
	}
	 /**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     */
	@Override
	default <X extends Throwable> FutureStream<U> onEmptyThrow(Supplier<X> supplier) {
			return fromStream(Seq.super.onEmptyThrow(supplier));
	}
    
	/* 
	 * @see org.jooq.lambda.Seq#innerJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	@Override
    default <T> FutureStream<Tuple2<U, T>> innerJoin(Stream<T> other, BiPredicate<U, T> predicate) {

       
        RepeatableStream<T> s = new RepeatableStream<>(ToLazyCollection.toLazyCollection(other.iterator()));

        return flatMap(t -> s.stream()
                           .filter(u -> predicate.test(t, u))
                           .map(u -> tuple(t, u)));
    }

    
    /* 
     * @see org.jooq.lambda.Seq#leftOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
     */
	@Override
    default <T> FutureStream<Tuple2<U, T>> leftOuterJoin(Stream<T> other, BiPredicate<U, T> predicate) {

    	 RepeatableStream<T> s = new RepeatableStream<>(ToLazyCollection.toLazyCollection(other.iterator()));

        return flatMap(t -> Seq.seq(s.stream())
                           .filter(u -> predicate.test(t, u))
                           .onEmpty(null)
                           .map(u -> tuple(t, u)));
    }

	/* 
	 * @see org.jooq.lambda.Seq#rightOuterJoin(java.util.stream.Stream, java.util.function.BiPredicate)
	 */
	@Override
    default <T> FutureStream<Tuple2<U, T>> rightOuterJoin(Stream<T> other, BiPredicate<U, T> predicate) {
        return fromStream(Seq.super.rightOuterJoin(other, predicate));
    }
   
	
	
	
	
	
	/**
	 * Zip two Streams, zipping against the underlying futures of both Streams
	 * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
	 * will be populated asyncrhonously
	 * 
	 * @param other  Another FutureStream to zip Futures with
	 * @return New Sequence of CompletableFutures
	 */

	<R> FutureStream<Tuple2<U,R>> zipFutures(FutureStream<R> other);
	/**{
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other.getLastActive().stream()));
		Seq<Tuple2<CompletableFuture<U>,CompletableFuture<R>>> withType = (Seq<Tuple2<CompletableFuture<U>,CompletableFuture<R>>>)seq;
		Stream futureStream =  fromStreamOfFutures((Stream)withType.map(t ->CompletableFuture.allOf(t.v1,t.v2).thenApply(v -> Tuple.tuple(t.v1.join(),t.v2.join())))
				);
		
		return (FutureStream<Tuple2<U,R>>)futureStream;
		

	}**/
	
	/**
	 * @return an Iterator that chunks all completed elements from this stream since last it.next() call into a collection
	 */
	default Iterator<Collection<U>> chunkLastReadIterator(){
		
		Queue.QueueReader reader =  new Queue.QueueReader(this.withQueueFactory(QueueFactories.unboundedQueue())
																	.toQueue(q->q.withTimeout(100)
																	.withTimeUnit(TimeUnit.MICROSECONDS))
																	,null);
		class Chunker implements Iterator<Collection<U>> {
			volatile boolean open =true;
			@Override
			public boolean hasNext() {

				return open == true && reader.isOpen();
			}

			@Override
			public Collection<U> next() {
				
				while(hasNext()){
					try{
						return reader.drainToOrBlock();
					}catch(ClosedQueueException e){
						open =false;
						return new ArrayList<>();
					}catch(QueueTimeoutException e){
						LockSupport.parkNanos(0l);
					}
				}
				return new ArrayList<>();
				

			}
		}
		return new Chunker();
	}
	/**
	 * @return a Stream that batches all completed elements from this stream since last read attempt into a collection
	 */
	default FutureStream<Collection<U>> chunkSinceLastRead(){
		Queue queue = this.withQueueFactory(QueueFactories.unboundedQueue()).toQueue();
		Queue.QueueReader reader =  new Queue.QueueReader(queue,null);
		class Chunker implements Iterator<Collection<U>> {
			
			@Override
			public boolean hasNext() {

				return reader.isOpen();
			}

			@Override
			public Collection<U> next() {
				return reader.drainToOrBlock();

			}
		}
		Chunker chunker = new Chunker();
		Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
			return () -> {
				
				try {
					return chunker.next();
				} catch (ClosedQueueException e) {
					
					throw new ClosedQueueException();
				}
				
			};
		};
		return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
		
		
	}
	FutureStream<U> withQueueFactory(
			QueueFactory<U> qf);
	/**
	 * Break a stream into multiple Streams based of some characteristic of the elements of the Stream
	 * 
	 * e.g. 
	 * 
	 * EagerFutureStream.of(10,20,25,30,41,43).shard(ImmutableMap.of("even",new Queue(),"odd",new Queue(),element-&gt; element%2==0? "even" : "odd");
	 * 
	 * results in 2 Streams
	 * "even": 10,20,30
	 * "odd" : 25,41,43
	 * 
	 * @param shards Map of Queue's keyed by shard identifier
	 * @param sharder Function to split split incoming elements into shards
	 * @return Map of new sharded Streams
	 */
	default <K> Map<K, ? extends FutureStream<U>> shard(
			Map<K, Queue<U>> shards, Function<U, K> sharder) {
		toQueue(shards, sharder);
		return shards
				.entrySet()
				.stream()
				.collect(
						Collectors.toMap(e -> e.getKey(), e -> fromStream(e
								.getValue().stream(getSubscription()))));
	}
	
	/**
	 * Cancel the CompletableFutures in this stage of the stream
	 */
	default void cancel(){
		this.streamCompletableFutures().forEach(next-> next.cancel(true));
	}
	
	/**
	 * Batch the elements in the Stream by a combination of Size and Time
	 * If batch exceeds max size it will be split
	 * If batch exceeds max time it will be split
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * @return
	 */
	default FutureStream<List<U>> batchBySizeAndTime(int size,long time, TimeUnit unit) { 
	 
	    Queue<U> queue = toQueue();
	    Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTimeAndSize<>(size,time,unit,()->new ArrayList<>());
	    return (FutureStream)fromStream(queue.streamBatch(getSubscription(), (Function)fn)).filter(c->!((Collection)c).isEmpty());
	}
	
	/**
	 * Batch the elements in the Stream by a combination of Size and Time
	 * If batch exceeds max size it will be split
	 * If batch exceeds max time it will be split
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * @return
	 */
	default <C extends Collection<U>> FutureStream<C> batchBySizeAndTime(int size,long time, TimeUnit unit, Supplier<C> factory) { 
	 
	    Queue<U> queue = toQueue();
	    Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTimeAndSize(size,time,unit,factory);
	    return (FutureStream)fromStream(queue.streamBatch(getSubscription(), (Function)fn));
	}
	/**
	 * 
	 * Batch the elements in this stream into Lists of specified size
	 * 
	 * @param size Size of lists elements should be batched into
	 * @return Stream of Lists
	 */
	default FutureStream<List<U>> batchBySize(int size) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<List<U>>> fn = new BatchBySize(size,this.getSubscription(),queue,()->new ArrayList<>());
		return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
	}
	/**
	 * Batch elements into a Stream of collections with user defined function
	 * @param fn Function takes a supplier, which can be used repeatedly to get the next value from the Stream. If there are no more values, a ClosedQueueException will be thrown.
	 *           This function should return a Supplier which creates a collection of the batched values
	 * @return Stream of batched values
	 */
	default <C extends Collection<U>>FutureStream<C> batch(Function<Supplier<U>, Supplier<C>> fn){
		Queue queue = toQueue();
		return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
	}
	
	/**
	 * Batch the elements in this stream into Collections of specified size
	 * The type of Collection is determined by the specified supplier
	 * 
	 * @param size Size of batch
	 * @param supplier Create the batch holding collection
	 * @return Stream of Collections
	 */
	default <C extends Collection<U>>FutureStream<C> batchBySize(int size, Supplier<C> supplier) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<Collection<U>>> fn = new BatchBySize(size,this.getSubscription(),queue,supplier);
		return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
	}
	/**
	 * Introduce a random delay between events in a stream
	 * Can be used to prevent behaviour synchronizing within a system
	 * 
	 * @param jitterInNanos Max number of nanos for jitter (random number less than this will be selected)/
	 * @return Next stage in Stream with jitter applied
	 */
	default FutureStream<U> jitter(long jitterInNanos){
		Queue queue = toQueue();
		Random r = new Random();
		Function<Supplier<U>, Supplier<U>> fn = s -> {
			return () -> {
				SimpleTimer timer = new SimpleTimer();
				Optional<U> result = Optional.empty();
				try {
					
						result = Optional.of(s.get());
						try {
							long elapsedNanos= (long)(jitterInNanos * r.nextDouble());
							long millis = elapsedNanos/1000000;
							int nanos = (int)(elapsedNanos - millis*1000000);
							Thread.sleep(Math.max(0,millis),Math.max(0,nanos));
						} catch (InterruptedException e) {
							softener.throwSoftenedException(e);
						}
				} catch (ClosedQueueException e) {
					if(result.isPresent())
						throw new ClosedQueueException(result);
					else
						throw new ClosedQueueException();
				}
				return result.get();
			};
		};
		return fromStream(queue.streamControl(getSubscription(), fn));
	}
	/**
	 * Apply a fixed delay before emitting elements to the next phase of the Stream.
	 * Note this doesn't neccessarily imply a fixed delay between element creation (although it may do).
	 * e.g.
	 * 
	 * EagerFutureStream.of(1,2,3,4).fixedDelay(1,TimeUnit.hours);
	 * 
	 * Will emit 1 on start, then 2 after an hour, 3 after 2 hours and so on.
	 * 
	 * However all 4 numbers will be populated in the Stream immediately.
	 * 
	 * LazyFutureStream.of(1,2,3,4).withQueueFactories(QueueFactories.boundedQueue(1)).fixedDelay(1,TimeUnit.hours);
	 * 
	 * Will populate each number in the Stream an hour apart.
	 * 
	 * @param time amount of time between emissions
	 * @param unit TimeUnit for emissions
	 * @return Next Stage of the Stream
	 */
	default FutureStream<U> fixedDelay(long time, TimeUnit unit) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<U>> fn = s -> {
			return () -> {
				SimpleTimer timer = new SimpleTimer();
				Optional<U> result = Optional.empty();
				try {
					
						result = Optional.of(s.get());
						try {
							long elapsedNanos= unit.toNanos(time);
							long millis = elapsedNanos/1000000;
							int nanos = (int)(elapsedNanos - millis*1000000);
							Thread.sleep(Math.max(0,millis),Math.max(0,nanos));
							
						} catch (InterruptedException e) {
							softener.throwSoftenedException(e);
						}
				} catch (ClosedQueueException e) {
					if(result.isPresent())
						throw new ClosedQueueException(result);
					else
						throw new ClosedQueueException();
				}
				return result.get();
			};
		};
		return fromStream(queue.streamControl(getSubscription(), fn));
	}
	/**
	 * Allows clients to control the emission of data for the next phase of the Stream.
	 * The user specified function can delay, drop, or change elements
	 * 
	 * @param fn Function takes a supplier, which can be used repeatedly to get the next value from the Stream. If there are no more values, a ClosedQueueException will be thrown.
	 *           This function should return a Supplier which returns the desired result for the next element (or just the next element).
	 * @return Next stage in Stream
	 */
	default FutureStream<U> control(Function<Supplier<U>, Supplier<U>> fn){
		Queue queue = toQueue();
		return fromStream(queue.streamControl(getSubscription(), fn));
	}
	
	/**
	 * Can be used to debounce (accept a single data point from a unit of time) data.
	 * This drops data. For a method that slows emissions and keeps data #see#onePer
	 * 
	 * @param time Time from which to accept only one element
	 * @param unit Time unit for specified time
	 * @return Next stage of stream, with only 1 element per specified time windows
	 */
	default FutureStream<U> debounce(long time, TimeUnit unit) {
		Queue queue = toQueue();
		long timeNanos =  unit.toNanos(time);
		Function<Supplier<U>, Supplier<U>> fn = new Debounce<>(timeNanos,1l,true);
		return fromStream(queue.streamControl(getSubscription(), fn));
	}
	/**
	 * Slow emissions down, emiting one element per specified time period
	 * 
	 * @param time Frequency period of element emission
	 * @param unit Time unit for frequency period
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	default FutureStream<U> onePer(long time, TimeUnit unit) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<U>> fn = s -> {
			
			return () -> {
				SimpleTimer timer=  new SimpleTimer();
				Optional<U> result = Optional.empty();
				try {
					
						
						try {
							long elapsedNanos= unit.toNanos(time)- timer.getElapsedNanoseconds();
							long millis = elapsedNanos/1000000;
							int nanos = (int)(elapsedNanos - millis*1000000);
							Thread.sleep(Math.max(0,millis),Math.max(0,nanos));
						} catch (InterruptedException e) {
							softener.throwSoftenedException(e);
						}
						result = Optional.of(s.get());
						
				} catch (ClosedQueueException e) {
					if(result.isPresent())
						throw new ClosedQueueException(result);
					else
						throw new ClosedQueueException();
				}
				return result.get();
			};
		};
		return fromStream(queue.streamControl(getSubscription(), fn));
	}
	/**
	 * Allows x (specified number of) emissions with a time period before stopping emmissions until specified time has elapsed since last emission
	 * 
	 * @param x Number of allowable emissions per time period
	 * @param time Frequency time period
	 * @param unit Frequency time unit
	 * @return Stream with emissions slowed down by specified emission frequency
	 */
	default FutureStream<U> xPer(int x,long time, TimeUnit unit) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<U>> fn = s -> {
			SimpleTimer[] timer=  {new SimpleTimer()};
			final int[]count={0};
			return () -> {
				
				Optional<U> result = Optional.empty();
				try {
					
						
						try {
							if(count[0]==x){
								long elapsedNanos= unit.toNanos(time)-timer[0].getElapsedNanoseconds();
								long millis = elapsedNanos/1000000;
								int nanos = (int)(elapsedNanos - millis*1000000);
								Thread.sleep(Math.max(0,millis),nanos);
								count[0]=0;
								timer[0]= new SimpleTimer();
							}
						} catch (InterruptedException e) {
							softener.throwSoftenedException(e);
						}
						result = Optional.of(s.get());
						
				} catch (ClosedQueueException e) {
					if(result.isPresent())
						throw new ClosedQueueException(result);
					else
						throw new ClosedQueueException();
				}
				return result.get();
			};
		};
		return fromStream(queue.streamControl(getSubscription(), fn));
	}

	/**
	 * Organise elements in a Stream into a Collections based on the time period they pass through this stage
	 * Excludes Null values (neccessary for timeout handling)
	 * 
	 * @param time Time period during which all elements should be collected
	 * @param unit Time unit during which all elements should be collected
	 * @return Stream of Lists
	 */
	default FutureStream<Collection<U>> batchByTime(long time, TimeUnit unit) {
		Queue queue = toQueue();
		Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTime<U>(time,unit,
				this.getSubscription(),queue,()->new ArrayList<>());
		
		return fromStream(queue.streamBatch(getSubscription(), fn)).filter(c->!((Collection)c).isEmpty());
	}
	/**
	 * Organise elements in a Stream into a Collections based on the time period they pass through this stage
	 * 
	 * @param time Time period during which all elements should be collected
	 * @param unit Time unit during which all elements should be collected
	 * @param factory Instantiates the collections used in the batching
	 * @return Stream of collections
	 */
	default FutureStream<Collection<U>> batchByTime(long time, TimeUnit unit,Supplier<Collection<U>> factory) {
		Queue queue = toQueue();
		Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTime<U>(time,unit, this.getSubscription(),queue,factory);
		return fromStream(queue.streamBatch(getSubscription(), fn)).filter(c->!((Collection)c).isEmpty());
	}

	/**
	 * 
	 * Similar to zip and combineLatest, except will always take the latest from this Stream while taking the last available value from the provided stream.
	 * By contrast zip takes new / latest values from both Streams and combineLatest takes the latest from either Stream (merged with last available from the other).
	 * 
	 * @param s Stream to merge with
	 * @return Stream of Tuples with the latest values from this stream
	 */
	default <T> FutureStream<Tuple2<U, T>> withLatest(FutureStream<T> s) {
		return fromStream(FutureStreamFunctions.withLatest(this, s));
	}
	/**
	 * Similar to zip and withLatest, except will always take the latest from either Stream (merged with last available from the other).
	 * By contrast zip takes new / latest values from both Streams and withLatest will always take the latest from this Stream while 
	 * taking the last available value from the provided stream.
	 * 
	 * @param s Stream to merge with
	 * @return  Stream of Tuples with the latest values from either stream
	 */
	default <T> FutureStream<Tuple2<U, T>> combineLatest(FutureStream<T> s) {
		return fromStream(FutureStreamFunctions.combineLatest(this, s));
	}
	/**
	 * Return a Stream with the same values as this Stream, but with all values omitted until the provided stream starts emitting values.
	 * Provided Stream ends the stream of values from this stream.
	 * 
	 * @param s Stream that will start the emission of values from this stream
	 * @return Next stage in the Stream but with all values skipped until the provided Stream starts emitting
	 */
	default<T>  FutureStream<U> skipUntil(FutureStream<T> s) {
		return fromStream(FutureStreamFunctions.skipUntil(this, s));
	}
	/**
	 * Return a Stream with the same values, but will stop emitting values once the provided Stream starts to emit values.
	 * e.g. if the provided Stream is asynchronously refreshing state from some remote store, this stream can proceed until
	 * the provided Stream succeeds in retrieving data.
	 * 
	 * @param s Stream that will stop the emission of values from this stream
	 * @return Next stage in the Stream but will only emit values until provided Stream starts emitting values
	 */
	default<T>  FutureStream<U> takeUntil(FutureStream<T> s) {
		return fromStream(FutureStreamFunctions.takeUntil(this, s));
	}

	<R> FutureStream<R> fromStream(Stream<R> stream);
	 <R> FutureStream<R> flatMap(
				Function<? super U, ? extends Stream<? extends R>> flatFn);
	
	 FutureStream<U> filter(final Predicate<? super U> p) ;
	 FutureStream<U> peek(final Consumer<? super U> consumer);


	/**
	 * Stream supporting methods
	 */

	
	/*
	 * Sequentially iterate through the LazyFutureStream
	 * To run a parallel Stream in parallel use run or run on current
	 *  
	 *@param action Consumer for each element in the Stream
	 * @see org.jooq.lambda.Seq#forEach(java.util.function.Consumer)
	 */
	@Override
	default void forEach(Consumer<? super U> action) {
		toQueue().stream(getSubscription()).forEach((Consumer) action);

	}

	
	/* 
	 * Sequentially iterate through the LazyFutureStream
	 * To run a parallel Stream in parallel use run or run on current
	 * 
	 *	@param action Consumer for each element
	 * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
	 */
	@Override
	default void forEachOrdered(Consumer<? super U> action) {
		toQueue().stream(getSubscription()).forEachOrdered((Consumer) action);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#toArray()
	 */
	@Override
	default Object[] toArray() {
		return toQueue().stream(getSubscription()).toArray();
	}
	
	/**
	 * Create a sliding view over this Stream
	 * 
	 * <pre>
	 * {@code 
	 * //futureStream of [1,2,3,4,5,6]
	 *		 
	 * List<List<Integer>> list = futureStream.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	 * }
	 * </pre>
	 * @param size
	 *            Size of sliding window
	 * @return Stream with sliding view over data in this stream
	 */
	default FutureStream<List<U>> sliding(int size){
		return this.fromStream(SlidingWindow.sliding(this,size, 1));
	}
	/**
	 * Create a sliding view over this Stream
	 * 
	 * <pre>
	 * {@code 
	 * //futureStream of [1,2,3,4,5,6,7,8]
	 *		 
	 * List<List<Integer>> list = futureStream.sliding(3,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	 * }
	 * </pre>
	 * @param size
	 *            Size of sliding window
	 * @return Stream with sliding view over data in this stream
	 */
	default FutureStream<List<U>> sliding(int size, int increment){
		return this.fromStream(SlidingWindow.sliding(this,size, increment));
	}
	
	/**
	 * @return An extensive set of asyncrhonous terminal operations
	 */
	default FutureOps<U> futureOperations(){
		return new FutureOps<>(this.getTaskExecutor(),this);
	}
	
	/**
	 * @param executor to execute terminal ops asynchronously on
	 * @return An extensive set of asyncrhonous terminal operations
	 */
	default FutureOps<U> futureOperations(Executor executor){
		return new FutureOps<>(executor,this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
	 */
	@Override
	default <A> A[] toArray(IntFunction<A[]> generator) {
		return toQueue().stream(getSubscription()).toArray(generator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BinaryOperator)
	 */
	@Override
	default U reduce(U identity, BinaryOperator<U> accumulator) {

		return (U) toQueue().stream(getSubscription()).reduce(identity,
				accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.util.function.BinaryOperator)
	 */
	@Override
	default Optional<U> reduce(BinaryOperator<U> accumulator) {
		return toQueue().stream(getSubscription()).reduce(accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#collect(java.util.function.Supplier,
	 * java.util.function.BiConsumer, java.util.function.BiConsumer)
	 */
	@Override
	default <R> R collect(Supplier<R> supplier,
			BiConsumer<R, ? super U> accumulator, BiConsumer<R, R> combiner) {

		return (R) toQueue().stream(getSubscription()).collect(supplier,
				accumulator, combiner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	@Override
	default Optional<U> min(Comparator<? super U> comparator) {

		return toQueue().stream(getSubscription()).min(comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	@Override
	default Optional<U> max(Comparator<? super U> comparator) {
		return toQueue().stream(getSubscription()).max(comparator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#count()
	 */
	@Override
	long count();/** {

		return getLastActive().stream().count();
	}**/

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean anyMatch(Predicate<? super U> predicate) {
		return toQueue().stream(getSubscription()).anyMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean allMatch(Predicate<? super U> predicate) {
		return toQueue().stream(getSubscription()).allMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#noneMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean noneMatch(Predicate<? super U> predicate) {
		return toQueue().stream(getSubscription()).noneMatch(predicate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findFirst()
	 */
	@Override
	default Optional<U> findFirst() {
		return toQueue().stream(getSubscription()).findFirst();
	}
	/**
	 * <pre>
	 * {@code 
	 * 	int first = LazyFutureStream.of(1,2,3,4)
	 * 					.firstValue();
	 *  
	 *   //first is 1
	 * }
	 * </pre>
	 * 
	 * @return the firstValue in this stream - same as findFirst but doesn't 
	 * 				return an Optional. Will throw an exception if empty
	 */
	default U firstValue() {
		return toQueue().stream(getSubscription()).findFirst().get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#findAny()
	 */
	@Override
	default Optional<U> findAny() {
		return toQueue().stream(getSubscription()).findAny();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	@Override
	default <R> R reduce(R identity, BiFunction<R, ? super U, R> accumulator,
			BinaryOperator<R> combiner) {

		return toQueue().stream(getSubscription()).reduce(identity,
				accumulator, combiner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.BaseStream#iterator()
	 */
	@Override
	default CloseableIterator<U> iterator() {

		Queue<U> q = toQueue();
		if (getSubscription().closed())
			return new CloseableIterator<>(Arrays.<U> asList().iterator(),
					getSubscription(),null);
		
		return new CloseableIterator<>(q.stream(getSubscription())
				.iterator(), getSubscription(),q);
	}
	
 
	
	 /* 
	  * More efficient reverse implementation than Seq version
	  * 
	  * 
	  *	@return
	  * @see org.jooq.lambda.Seq#reverse()
	  */
	default FutureStream<U> reverse() {
	        return fromStream(Seq.seq(reversedIterator()));
	   }
	default Iterator<U> reversedIterator(){
		Queue<U> q = toQueue();
		if (getSubscription().closed())
			return new CloseableIterator<>(Arrays.<U> asList().iterator(),
					getSubscription(),null);
		List l=  q.stream(getSubscription()).toList();
		ListIterator iterator = l.listIterator(l.size());
		return new CloseableIterator<>(new Iterator(){

			@Override
			public boolean hasNext() {
				return iterator.hasPrevious();
			}

			@Override
			public Object next() {
				return iterator.previous();
			}
			
		}, getSubscription(),q);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#spliterator()
	 */
	@Override
	default Spliterator<U> spliterator() {
		return toQueue().stream(getSubscription()).spliterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.BaseStream#isParallel()
	 */
	@Override
	default boolean isParallel(){
		return true;
	}

	/*
	 * Creates a sequential instance by populating an async Queue from the
	 * current stream, and reading sequentially from that Stream.
	 * 
	 * For an alternative approach change the task executors to single thread
	 * model, via withTaskExecutor and withRetrier
	 * 
	 * @return Sequential Stream
	 * 
	 * @see com.aol.simple.react.stream.traits.FutureStream#sequential()
	 */
	@Override
	 Seq<U> sequential();/** {
		Queue q = toQueue();
		q.fromStream(getLastActive().stream().map(it -> it.join()));
		q.close();
		return q.stream();
	}**/



	@Override
	default Stream<U> stream() {
		return toQueue().stream(getSubscription());
	}

	/*
	 * Seq supporting methods
	 */

	/**
	 * native Seq
	 * 
	 */

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
	 * 
	 *
	 * @see #intersperse(Stream, Object)
	 */
	default FutureStream<U> intersperse(U value) {
		return intersperse(this, value);
	}

	/*
	 * 
	 * @param type
	 * 
	 * @return
	 * 
	 * @see org.jooq.lambda.Seq#cast(java.lang.Class)
	 */

	@Override
	default <U> FutureStream<U> cast(Class<U> type) {
		return (FutureStream<U>) cast(this, type);
	}

	@Override
	default <U> FutureStream<U> ofType(Class<U> type) {
		return (FutureStream<U>) ofType(this, type);
	}

	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * // (1, 2, 3) EagerFutureStream.of(1, "a", 2, "b",
	 * 3).ofType(Integer.class)
	 * 
	 */
	@SuppressWarnings("unchecked")
	static <T, U> FutureStream<U> ofType(FutureStream<T> stream, Class<U> type) {
		return stream.filter(type::isInstance).map(t -> (U) t);
	}

	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException LazyFutureStream.of(1, "a", 2, "b",
	 * 3).cast(Integer.class)
	 * 
	 */
	static <T, U> FutureStream<U> cast(FutureStream<T> stream, Class<U> type) {
		return stream.map(type::cast);
	}

	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * 
	 * // (1, 0, 2, 0, 3, 0, 4) Seq.of(1, 2, 3, 4).intersperse(0)
	 * 
	 */
	static <T> FutureStream<T> intersperse(FutureStream<T> stream, T value) {
		return stream.flatMap(t -> Stream.of(value, t).skip(1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#unordered()
	 */
	@Override
	default FutureStream<U> unordered() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
	 */
	@Override
	Seq<U> onClose(Runnable closeHandler);/** {

		return Seq.seq(getLastActive().stream().onClose(closeHandler)
				.map(it -> (U) it.join()));
	}**/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#close()
	 */
	@Override
	void close();/** {
		getLastActive().stream().close();

	}**/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#map(java.util.function.Function)
	 */
	@Override
	default <R> FutureStream<R> map(Function<? super U, ? extends R> mapper) {
		return (FutureStream)then((Function) mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#mapToInt(java.util.function.ToIntFunction)
	 */
	@Override
	default IntStream mapToInt(ToIntFunction<? super U> mapper) {
		return toQueue().stream(getSubscription()).mapToInt(mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#mapToLong(java.util.function.ToLongFunction)
	 */
	@Override
	default LongStream mapToLong(ToLongFunction<? super U> mapper) {
		return toQueue().stream(getSubscription()).mapToLong(mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#mapToDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	default DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) {
		return toQueue().stream(getSubscription()).mapToDouble(mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMapToInt(java.util.function.Function)
	 */
	@Override
	default IntStream flatMapToInt(
			Function<? super U, ? extends IntStream> mapper) {
		return toQueue().stream(getSubscription()).flatMapToInt(mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMapToLong(java.util.function.Function)
	 */
	@Override
	default LongStream flatMapToLong(
			Function<? super U, ? extends LongStream> mapper) {
		return toQueue().stream(getSubscription()).flatMapToLong(mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMapToDouble(java.util.function.Function)
	 */
	@Override
	default DoubleStream flatMapToDouble(
			Function<? super U, ? extends DoubleStream> mapper) {
		return toQueue().stream(getSubscription()).flatMapToDouble(mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted()
	 */
	@Override
	default Seq<U> sorted() {
		return toQueue().stream(getSubscription()).sorted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
	 */
	@Override
	default Seq<U> sorted(Comparator<? super U> comparator) {
		return toQueue().stream(getSubscription()).sorted(comparator);
	}

	/**
	 * Give a function access to the current stage of a SimpleReact Stream
	 * 
	 * @param consumer
	 *            Consumer that will recieve current stage
	 * @return Self (current stage)
	 */
	default FutureStream<U> self(Consumer<FutureStream<U>> consumer) {
		return (FutureStream<U>) then((t) -> {
			consumer.accept(this);
			return (U) t;
		});

	}

	/**
	 * Returns a limited interval from a given Stream.
	 * 
	 * 
	 * // (4, 5) Seq.of(1, 2, 3, 4, 5, 6).slice(3, 5)
	 * 
	 *
	 * @see #slice(Stream, long, long)
	 */
	@Override
	default Seq<U> slice(long from, long to) {
		return Seq.super.slice(from, to);
	}

	/**
	 * Returns a limited interval from a given Stream.
	 * 
	 * 
	 * // (4, 5) EagerFutureStream.of(1, 2, 3, 4, 5, 6).slice(3, 5)
	 * 
	 */
	static <T> Seq<T> slice(FutureStream<T> stream, long from, long to) {
		long f = Math.max(from, 0);
		long t = Math.max(to - f, 0);

		return stream.skip(f).limit(t);
	}

	

	
	

	
	 Continueable getSubscription();
	 Executor getTaskExecutor();
	
}