package com.aol.cyclops.sequence.future;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsTerminalFutureOperations;
import com.aol.cyclops.streams.future.DoubleOperators;
import com.aol.cyclops.streams.future.FutureCollectable;
import com.aol.cyclops.streams.future.IntOperators;
import com.aol.cyclops.streams.future.LongOperators;

public interface FutureOperations<T> extends
					IntOperators<T>,DoubleOperators<T>, LongOperators<T>,
						ReactiveStreamsTerminalFutureOperations<T>,
						FutureCollectable<T>
					{

	
	
	/**
	 * Asynchronously convert  this Stream into a List
	 *  <pre>
	 * {@code
	 *  CompletableFuture<List<Data>> myList = EagerFutureStream.of(1,2,3,4)
	 *  														.map(this::loadFromDb)
	 *  														.withTaskExecutor(parallelBuilder().getExecutor())
	 *  														.map(this::processOnDifferentExecutor)
	 *  														.toList();
	 * }
	 * </pre>
	 * 
	 * @return Future List
	 */
	public CompletableFuture<List<T>> toList();
	/**
	 * @return Last value in this Stream (must be non-empty)
	 */
	public CompletableFuture<T> lastValue();
	
	/**
	 * @return the only entry in this Stream if it is a single entry Stream,
	 *         otherwise throws an UnsupportedOperationException
	 */
	public CompletableFuture<T> single();
	/**
	 * @return the only entry in this Stream if it is a single entry Stream,
	 *         otherwise throws an UnsupportedOperationException
	 */
	public CompletableFuture<T> single(Predicate<T> predicate);
	
	/**
	 * @return the only entry in this Stream if it is a single entry Stream,
	 *         otherwise throws an UnsupportedOperationException
	 */
	public CompletableFuture<Optional<T>> singleOptional();
	
	
	/**
	 * Asynchronously convert  this Stream into a List
	 *  <pre>
	 * {@code
	 *  CompletableFuture<Set<Data>> myList = LazyFutureStream.of(1,2,3,4)
	 *  														.map(this::loadFromDb)
	 *  														.withTaskExecutor(parallelBuilder().getExecutor())
	 *  														.map(this::processOnDifferentExecutor)
	 *  														.toSet();
	 * }
	 * </pre>
	 * 
	 * @return Future Set
	 */
	public CompletableFuture<Set<T>> toSet();
	
	/**
	 * Asynchronously capture the minimum value in this stream using the provided function
	 * 
	 * @see org.jooq.lambda.Seq#minBy(Function)
	 */
	public  <U extends Comparable<? super U>> CompletableFuture<Optional<T>> minBy(Function<? super T, ? extends U> function);
	/**
	 * Asynchronously capture the maximum value in this stream using the provided function
	 * 
	 *  @see org.jooq.lambda.Seq#maxBy(Function)
	 */
	public  <U extends Comparable<? super U>> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function);
	
	/**
	 * Asynchronously perform a Stream collection
	 * 
	 * @see java.util.stream.Stream#collect(Collector)
	 * 
	 */
	public <R, A> CompletableFuture<R> collect(Collector<? super T, A, R> collector);
	
	
	/**
	 *  Asynchronously perform a Stream collection
	 * @see org.jooq.lambda.Seq#toCollection(Supplier)
	 */
	public <C extends Collection<T>> CompletableFuture<C> toCollection(Supplier<C> collectionFactory);
	/**
	 * Asyncrhonously generate an Array
	 * 
	 * @see java.util.stream.Stream#toArray(IntFunction)
	 */
	public <A> CompletableFuture<A[]> toArray(IntFunction<A[]> generator);
	/**
	 * Asyncrhonously generate an Array
	 * 
	 * @see java.util.stream.Stream#toArray(IntFunction)
	 */
	public CompletableFuture<Object[]> toArray() ;
	
	/**
	 * Perform an asyncrhonous groupBy operation
	 * @see org.jooq.lambda.Seq#groupBy(Function)
	 */
	public <K> CompletableFuture<Map<K, List<T>>> groupBy(Function<? super T, ? extends K> classifier);
	/**
	 * Perform an asyncrhonous groupBy operation
	 * @see org.jooq.lambda.Seq#groupBy(Function, Collector)
	 */
	public <K, A, D> CompletableFuture<Map<K, D>> groupBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) ;
	/**
	 * Perform an asyncrhonous groupBy operation
	 * @see org.jooq.lambda.Seq#groupBy(Function, Supplier, Collector)
	 */
	public  <K, D, A, M extends Map<K, D>> CompletableFuture<M> groupBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream);
	
	/**
	 * Perform an asynchronous foldLeft operation
	 * 	 @see org.jooq.lambda.Seq#foldLeft(Object,BiFunction)
	 * */
	public  <U> CompletableFuture<U> foldLeft(U seed, BiFunction<U, ? super T, U> function);
	/**
	 * Perform an asynchronous foldRight operation
	 * 	 @see org.jooq.lambda.Seq#foldRight(Object,BiFunction)
	 * */
	public  <U> CompletableFuture<U> foldRight(U seed, BiFunction<? super T, U, U> function);
	
	/**
	 * Perform an asyncrhonous min operation
	 *  @see java.util.stream.Stream#min(Comparator)
	 */
	public CompletableFuture<Optional<T>> min(Comparator<? super T> comparator);
	/**
	 * Perform an asyncrhonous min operation
	 *  @see java.util.stream.Stream#max(Comparator)
	 */
	public CompletableFuture<Optional<T>> max(Comparator<? super T> comparator);
	/**
	 * Asynchronously perform a Stream collection
	 * 
	 * @see java.util.stream.Stream#collect(Supplier, BiConsumer, BiConsumer)
	 * 
	 */
	public  <R> CompletableFuture<R> collect(Supplier<R> supplier,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R, R> combiner);
	/**
	 * Asynchronously perform a Stream reduction
	 * 
	 * @see java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)
	 * 
	 */
	public <U> CompletableFuture<U> reduce(U identity, BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner);
	/**
	 * Asynchronously perform a Stream reduction
	 * 
	 * @see java.util.stream.Stream#reduce(BinaryOperator)
	 * 
	 */
	public CompletableFuture<Optional<T>> reduce(BinaryOperator<T> accumulator);
	/**
	 * Asynchronously perform a Stream reduction
	 * 
	 * @see java.util.stream.Stream#reduce(Object, BinaryOperator)
	 * 
	 */
	public   CompletableFuture<T> reduce(T identity, BinaryOperator<T> accumulator);
	/**
	 * Asynchronously perform a Stream count
	 * 
	 * @see java.util.stream.Stream#count()
	 * 
	 */
	public CompletableFuture<Long> count();
	
	public  CompletableFuture<String> join(CharSequence sep);
	/**
	 * Perform an asynchronous join operation
	 * 	 @see org.jooq.lambda.Seq#join()
	 * */
	public  CompletableFuture<String> join();
	/**
	 * Perform an asynchronous join operation
	 * 	 @see org.jooq.lambda.Seq#join(CharSequence)
	 * */
	public  CompletableFuture<String> join(CharSequence delimiter, CharSequence prefix,
												CharSequence suffix);
	/**
	 * Perform an asynchronous findAny operation
	 * 	 @see java.util.stream.Stream#findAny()
	 * */
	public CompletableFuture<Optional<T>> findAny();
	/**
	 * Perform an asynchronous findAny operation
	 * 	 @see java.util.stream.Stream#findFirst()
	 * */
	public CompletableFuture<Optional<T>> findFirst();
	
	/**
	 * Perform an asynchronous findAny operation
	 * 	 @see java.util.stream.Stream#findFirst()
	 * */
	public CompletableFuture<T> firstValue();
	
	/**
	 * Perform an asynchronous All Match operation
	 * 	@see java.util.stream.Stream#allMatch(Predicate)
	 * */
	public CompletableFuture<Boolean> allMatch(Predicate<? super T> predicate);
	/**
	 * Perform an asynchronous Any Match operation
	 * 	 @see java.util.stream.Stream#anyMatch(Predicate)
	 * */
	public CompletableFuture<Boolean>   anyMatch(Predicate<? super T> predicate);
	/**
	 * Perform an asynchronous Any Match operation
	 * 	 @see java.util.stream.Stream#noneMatch(Predicate)
	 * */
	public CompletableFuture<Boolean>   noneMatch(Predicate<? super T> predicate);
	public void forEach(Consumer<T> object);
	
}