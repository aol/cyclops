package com.aol.simple.react.stream.traits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.util.SimpleTimer;
import com.google.common.collect.Lists;

public interface FutureStream<U> extends Seq<U>, ConfigurableStream<U>,
		LazyStream<U>, BlockingStream<U>, SimpleReactStream<U>, ToQueue<U> {

	static final ExceptionSoftener softener = ExceptionSoftener.singleton.factory
			.getInstance();
	
	
	/**
	 * Zip two Streams, zipping against the underlying futures of this stream
	 * 
	 * @param other
	 * @return
	 */
	default <R> FutureStream<Tuple2<U,R>> zipFutures(Stream<R> other) {
		if(other instanceof FutureStream)
			return zipFutures((FutureStream)other);
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other));
		Seq<Tuple2<CompletableFuture<U>,R>> withType = (Seq<Tuple2<CompletableFuture<U>,R>>)seq;
		Stream futureStream = fromStream(withType.map(t ->t.v1.thenApply(v -> Tuple.tuple(t.v1.join(),t.v2)))
				.map(CompletableFuture::join));

		
		return (FutureStream<Tuple2<U,R>>)futureStream;

	}
	/**
	 * Zip two Streams, zipping against the underlying futures of both Streams
	 * Placeholders (Futures) will be populated immediately in the new zipped Stream and results
	 * will be populated asyncrhonously
	 * 
	 * @param other  Another FutureStream to zip Futures with
	 * @return New Sequence of CompletableFutures
	 */

	default <R> FutureStream<Tuple2<U,R>> zipFutures(FutureStream<R> other) {
		Seq seq = Seq.seq(getLastActive().stream()).zip(Seq.seq(other.getLastActive().stream()));
		Seq<Tuple2<CompletableFuture<U>,CompletableFuture<R>>> withType = (Seq<Tuple2<CompletableFuture<U>,CompletableFuture<R>>>)seq;
		Stream futureStream =  fromStream(withType.map(t ->CompletableFuture.allOf(t.v1,t.v2).thenApply(v -> Tuple.tuple(t.v1.join(),t.v2.join())))
				.map(CompletableFuture::join));
		
		return (FutureStream<Tuple2<U,R>>)futureStream;
		

	}
	
	/**
	 * @return an Iterator that chunks all completed elements from this stream since last it.next() call into a collection
	 */
	default Iterator<Collection<U>> chunkLastReadIterator(){
		
		Queue.QueueReader reader =  new Queue.QueueReader(toQueue(q->q.withTimeout(100).withTimeUnit(TimeUnit.MICROSECONDS)),null);
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
						return Lists.newArrayList();
					}catch(QueueTimeoutException e){
						LockSupport.parkNanos(0l);
					}
				}
				return Lists.newArrayList();
				

			}
		}
		return new Chunker();
	}
	/**
	 * @return a Stream that batches all completed elements from this stream since last read attempt into a collection
	 */
	default FutureStream<Collection<U>> chunkSinceLastRead(){
		Queue queue = toQueue();
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
		return fromStream(queue.streamBatch(getSubscription(), fn));
		
		
	}
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
	 * 
	 * Batch the elements in this stream into Lists of specified size
	 * 
	 * @param size Size of lists elements should be batched into
	 * @return Stream of Lists
	 */
	default FutureStream<Collection<U>> batchBySize(int size) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
			return () -> {List<U> list = new ArrayList<>();
			try {
				for (int i = 0; i < size; i++) {
					
					list.add(s.get());
					getSubscription().closeQueueIfFinished(queue);
				}
			} catch (ClosedQueueException e) {
				if(list.size()>0)
					throw new ClosedQueueException(list);
				else
					throw new ClosedQueueException();
			}
			return list;
			};
		};
		return fromStream(queue.streamBatch(getSubscription(), fn));
	}
	/**
	 * Batch elements into a Stream of collections with user defined function
	 * @param fn Function takes a supplier, which can be used repeatedly to get the next value from the Stream. If there are no more values, a ClosedQueueException will be thrown.
	 *           This function should return a Supplier which creates a collection of the batched values
	 * @return Stream of batched values
	 */
	default FutureStream<Collection<U>> batch(Function<Supplier<U>, Supplier<Collection<U>>> fn){
		Queue queue = toQueue();
		return fromStream(queue.streamBatch(getSubscription(), fn));
	}
	
	/**
	 * Batch the elements in this stream into Collections of specified size
	 * The type of Collection is determined by the specified supplier
	 * 
	 * @param size Size of batch
	 * @param supplier Create the batch holding collection
	 * @return Stream of Collections
	 */
	default FutureStream<Collection<U>> batchBySize(int size, Supplier<Collection<U>> supplier) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
			return () -> {Collection<U> list = supplier.get();
			try {
				for (int i = 0; i < size; i++) {
					
					list.add(s.get());
					getSubscription().closeQueueIfFinished(queue);
				}
			} catch (ClosedQueueException e) {
				if(list.size()>0)
					throw new ClosedQueueException(list);
				else
					throw new ClosedQueueException();
			}
			return list;
			};
		};
		return fromStream(queue.streamBatch(getSubscription(), fn));
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
		Function<Supplier<U>, Supplier<U>> fn = s -> {
			
			return () -> {
				SimpleTimer timer=  new SimpleTimer();
				Optional<U> result = Optional.empty();
				try {
					long elapsedNanos= 1;
					while(elapsedNanos>0){
					
						result = Optional.of(s.get());
						elapsedNanos= unit.toNanos(time) - timer.getElapsedNanoseconds();
					
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
	 * 
	 * @param time Time period during which all elements should be collected
	 * @param unit Time unit during which all elements should be collected
	 * @return Stream of Lists
	 */
	default FutureStream<Collection<U>> batchByTime(long time, TimeUnit unit) {
		Queue queue = toQueue();
		Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
			return () -> {
				SimpleTimer timer = new SimpleTimer();
				List<U> list = new ArrayList<>();
				try {
					do {
						list.add(s.get());
					} while (timer.getElapsedNanoseconds()<unit.toNanos(time));
				} catch (ClosedQueueException e) {
					
					throw new ClosedQueueException(list);
				}
				return list;
			};
		};
		return fromStream(queue.streamBatch(getSubscription(), fn));
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
		Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
			return () -> {
				SimpleTimer timer = new SimpleTimer();
				Collection<U> list = factory.get();
				try {
					do {
						list.add(s.get());
					} while (timer.getElapsedNanoseconds()<unit.toNanos(time));
				} catch (ClosedQueueException e) {
					
					throw new ClosedQueueException(list);
				}
				return list;
			};
		};
		return fromStream(queue.streamBatch(getSubscription(), fn));
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
		return fromStream(withLatest(this, s));
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
		return fromStream(combineLatest(this, s));
	}
	/**
	 * Return a Stream with the same values as this Stream, but with all values omitted until the provided stream starts emitting values.
	 * Provided Stream ends the stream of values from this stream.
	 * 
	 * @param s Stream that will start the emission of values from this stream
	 * @return Next stage in the Stream but with all values skipped until the provided Stream starts emitting
	 */
	default<T>  FutureStream<U> skipUntil(FutureStream<T> s) {
		return fromStream(skipUntil(this, s));
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
		return fromStream(takeUntil(this, s));
	}

	/**
	 * Close all queues except the active one
	 * 
	 * @param active Queue not to close
	 * @param all All queues potentially including the active queue
	 */
	static void closeOthers(Queue active, List<Queue> all){
		all.stream().filter(next -> next!=active).forEach(Queue::closeAndClear);
		
	}
	/**
	 * Close all streams except the active one
	 * 
	 * @param active Stream not to close
	 * @param all  All streams potentially including the active stream
	 */
	static void closeOthers(FutureStream active, List<FutureStream> all){
		all.stream().filter(next -> next!=active).filter(s -> s.isEager()).forEach(FutureStream::cancel);
		
	}
	
	/**
	 * Return first Stream out of provided Streams that starts emitted results 
	 * 
	 * @param futureStreams Streams to race
	 * @return First Stream to start emitting values
	 */
	static <U> FutureStream<U> firstOf(FutureStream<U>... futureStreams) {
		List<Tuple2<FutureStream<U>, QueueReader>> racers = Stream
				.of(futureStreams)
				.map(s -> Tuple.tuple(s,new Queue.QueueReader(s.toQueue(),null))).collect(Collectors.toList());
		while(true){
		for(Tuple2<FutureStream<U>,Queue.QueueReader> q: racers){
			if(q.v2.notEmpty()){
				closeOthers(q.v2.getQueue(),racers.stream().map(t -> t.v2.getQueue()).collect(Collectors.toList()));
				closeOthers(q.v1,racers.stream().map(t -> t.v1).collect(Collectors.toList()));
				return q.v1.fromStream(q.v2.getQueue().stream(q.v1.getSubscription()));
			}
				
		}
		LockSupport.parkNanos(1l);
		}

		

	}

	/**
	 * Zip two streams into one. Uses the latest values from each rather than waiting for both
	 * 
	 */
	static <T1, T2> Seq<Tuple2<T1, T2>> combineLatest(FutureStream<T1> left,
			FutureStream<T2> right) {
		return combineLatest(left, right, Tuple::tuple);
	}
	@AllArgsConstructor
	static class Val<T>{
		enum Pos { left,right};
		Pos pos;
		T val;
	}

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting. 
	 * values. Uses the latest values from each rather than waiting for both.
	 * 
	 */
	static <T1, T2, R> Seq<R> combineLatest(FutureStream<T1> left,
			FutureStream<T2> right, BiFunction<T1, T2, R> zipper) {
		
		Queue q = left.map(it->new Val(Val.Pos.left,it)).merge(right.map(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		

		class Zip implements Iterator<R> {
			T1 lastLeft = null;
			T2 lastRight = null;
			@Override
			public boolean hasNext() {

				return it.hasNext();
			}

			@Override
			public R next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left)
					lastLeft = (T1)v.val;
				else
					lastRight = (T2)v.val;
			
				return zipper.apply(lastLeft, lastRight);
				

			}
		}

		return Seq.seq(new Zip());
	}
	
	/**
	 * Zip two streams into one. Uses the latest values from each rather than waiting for both
	 * 
	 */
	static <T1, T2> Seq<Tuple2<T1, T2>> withLatest(FutureStream<T1> left,
			FutureStream<T2> right) {
		return withLatest(left, right, Tuple::tuple);
	}
	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting. 
	 * values. Uses the latest values from each rather than waiting for both.
	 * 
	 */
	static <T1, T2, R> Seq<R> withLatest(FutureStream<T1> left,
			FutureStream<T2> right, BiFunction<T1, T2, R> zipper) {
		
		Queue q = left.map(it->new Val(Val.Pos.left,it)).merge(right.map(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		

		class Zip implements Iterator<R> {
			T1 lastLeft = null;
			T2 lastRight = null;
			@Override
			public boolean hasNext() {

				return it.hasNext();
			}

			@Override
			public R next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left){
					lastLeft = (T1)v.val;
					return zipper.apply(lastLeft, lastRight);
				}
				else
					lastRight = (T2)v.val;
			
				return (R)Optional.empty();
				

			}
		}

		return Seq.seq(new Zip()).filter(next->!(next instanceof Optional));
	}
	
	static <T1, T2> Seq<T1> skipUntil(FutureStream<T1> left,
			FutureStream<T2> right) {
		
		Queue q = left.map(it->new Val(Val.Pos.left,it)).merge(right.map(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		
		final Object missingValue = new Object();
		class Zip implements Iterator<T1> {
			Optional<T1> lastLeft = Optional.empty();
			Optional<T2> lastRight = Optional.empty();
			@Override
			public boolean hasNext() {

				return it.hasNext();
			}

			@Override
			public T1 next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left){
					if(lastRight.isPresent())
						lastLeft = Optional.of((T1)v.val);
				}
				else
					lastRight = Optional.of((T2)v.val);
				if(!lastRight.isPresent())
					return (T1)Optional.empty();
				if(lastLeft.isPresent())
					return lastLeft.get();
				else
					return (T1)Optional.empty();
				
				

			}
		}

		return Seq.seq(new Zip()).filter(next->!(next instanceof Optional));
	}
	static <T1, T2> Seq<T1> takeUntil(FutureStream<T1> left,
			FutureStream<T2> right) {
		
		Queue q = left.map(it->new Val(Val.Pos.left,it)).merge(right.map(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		
		final Object missingValue = new Object();
		class Zip implements Iterator<T1> {
			Optional<T1> lastLeft = Optional.empty();
			Optional<T2> lastRight = Optional.empty();
			boolean closed= false;
			@Override
			public boolean hasNext() {
				
				return !closed && it.hasNext();
			}

			@Override
			public T1 next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left)
					lastLeft = Optional.of((T1)v.val);
				else
					lastRight = Optional.of((T2)v.val);
				
				if(!lastRight.isPresent() && lastLeft.isPresent())
					return lastLeft.get();
				else{
					closed= true;
					return (T1)Optional.empty();
				}
				
				

			}
		}

		return Seq.seq(new Zip()).filter(next->!(next instanceof Optional));
	}

	
	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#retry(java.util.
	 * function.Function)
	 */
	default <R> FutureStream<R> retry(final Function<U, R> fn) {
		return (FutureStream) SimpleReactStream.super.retry(fn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#allOf(java.util.
	 * stream.Collector, java.util.function.Function)
	 */
	default <T, R> FutureStream<R> allOf(final Collector collector,
			final Function<T, R> fn) {
		return (FutureStream) SimpleReactStream.super.allOf(collector, fn);
	}

	default <R> FutureStream<R> anyOf(final Function<U, R> fn) {
		return (FutureStream) SimpleReactStream.super.anyOf(fn);
	}

	/*
	 * @see com.aol.simple.react.stream.traits.SimpleReactStream#
	 * fromStreamCompletableFuture(java.util.stream.Stream)
	 */
	default <R> FutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {
		return (FutureStream) SimpleReactStream.super
				.fromStreamCompletableFuture(stream);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#then(java.util.function
	 * .Function)
	 */
	default <R> FutureStream<R> then(final Function<U, R> fn) {
		return (FutureStream) SimpleReactStream.super.then(fn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#fromStream(java.
	 * util.stream.Stream)
	 */
	default <R> FutureStream<R> fromStream(Stream<R> stream) {
		return (FutureStream) SimpleReactStream.super.fromStream(stream);
	}

	/*
	 * @see org.jooq.lambda.Seq#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> FutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {
		return (FutureStream) SimpleReactStream.super.flatMap(flatFn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#merge(com.aol.simple
	 * .react.stream.traits.SimpleReactStream)
	 */
	@Override
	default FutureStream<U> merge(SimpleReactStream<U> s) {
		return (FutureStream) SimpleReactStream.super.merge(s);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#onFail(java.util
	 * .function.Function)
	 */
	@Override
	default FutureStream<U> onFail(
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (FutureStream) SimpleReactStream.super.onFail(fn);
	}

	/*
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#onFail(java.lang
	 * .Class, java.util.function.Function)
	 */
	default FutureStream<U> onFail(Class<? extends Throwable> exceptionClass,
			final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (FutureStream) SimpleReactStream.super
				.onFail(exceptionClass, fn);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#capture(java.util
	 * .function.Consumer)
	 */
	@Override
	default FutureStream<U> capture(
			final Consumer<? extends Throwable> errorHandler) {
		return (FutureStream) SimpleReactStream.super.capture(errorHandler);
	}

	/*
	 * @see
	 * com.aol.simple.react.stream.traits.SimpleReactStream#allOf(java.util.
	 * function.Function)
	 */
	@Override
	default <T, R> FutureStream<R> allOf(final Function<List<T>, R> fn) {
		return (FutureStream) SimpleReactStream.super.allOf(fn);
	}

	/*
	 * @see org.jooq.lambda.Seq#peek(java.util.function.Consumer)
	 */
	@Override
	default FutureStream<U> peek(final Consumer<? super U> consumer) {
		return (FutureStream) SimpleReactStream.super.peek(consumer);
	}

	/*
	 * @see org.jooq.lambda.Seq#filter(java.util.function.Predicate)
	 */
	default FutureStream<U> filter(final Predicate<? super U> p) {
		return (FutureStream) SimpleReactStream.super.filter(p);
	}

	/**
	 * Stream supporting methods
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#forEach(java.util.function.Consumer)
	 */
	@Override
	default void forEach(Consumer<? super U> action) {
		toQueue().stream(getSubscription()).forEach((Consumer) action);

	}

	/*
	 * (non-Javadoc)
	 * 
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
	default long count() {

		return getLastActive().stream().count();
	}

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

		if (getSubscription().closed())
			return new CloseableIterator<>(Arrays.<U> asList().iterator(),
					getSubscription());
		return new CloseableIterator<>(toQueue().stream(getSubscription())
				.iterator(), getSubscription());
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
	default boolean isParallel() {
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
	default Seq<U> sequential() {
		Queue q = toQueue();
		q.fromStream(getLastActive().stream().map(it -> it.join()));
		q.close();
		return q.stream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#parallel()
	 */
	@Override
	default FutureStream<U> parallel() {
		return this;
	}

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
	default Seq<U> onClose(Runnable closeHandler) {

		return Seq.seq(getLastActive().stream().onClose(closeHandler)
				.map(it -> (U) it.join()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#close()
	 */
	@Override
	default void close() {
		getLastActive().stream().close();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#map(java.util.function.Function)
	 */
	@Override
	default <R> FutureStream<R> map(Function<? super U, ? extends R> mapper) {
		return then((Function) mapper);
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

	/**
	 * Merge this reactive dataflow with another - recommended for merging
	 * different types. To merge flows of the same type the instance method
	 * merge is more appropriate.
	 * 
	 * @param s1
	 *            Reactive stage builder to merge
	 * @param s2
	 *            Reactive stage builder to merge
	 * @return Merged dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> FutureStream<R> merge(FutureStream s1, FutureStream s2) {
		List merged = Stream
				.of(s1.getLastActive().list(), s2.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (FutureStream<R>) s1.withLastActive(new StreamWrapper(merged));
	}

}
