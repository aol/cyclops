package com.aol.simple.react.stream.traits;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.aol.simple.react.stream.traits.future.operators.DoubleOperators;
import com.aol.simple.react.stream.traits.future.operators.IntOperators;
import com.aol.simple.react.stream.traits.future.operators.LongOperators;
@Getter
@AllArgsConstructor
public class FutureOps<T> implements IntOperators<T>,DoubleOperators<T>, LongOperators<T> {

	private final Executor exec;
	private final FutureStream<T> stream;
	
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
	public CompletableFuture<List<T>> toList(){
		return CompletableFuture.supplyAsync(()->stream.toList(),exec);
	}
	/**
	 * @return Last value in this Stream (must be non-empty)
	 */
	public CompletableFuture<T> lastValue(){
		return CompletableFuture.supplyAsync(()->{ List<T> l= stream.toList(); return l.get(l.size()-1);},exec);
	}
	
	/**
	 * @return the only entry in this Stream if it is a single entry Stream,
	 *         otherwise throws an UnsupportedOperationException
	 */
	public CompletableFuture<T> single(){
		return CompletableFuture.supplyAsync(()->{ List<T> l= stream.toList(); if(l.size()==1){ return l.get(l.size()-1); }
			throw new UnsupportedOperationException("single only works for Streams with a single value");},exec);
	}
	
	
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
	public CompletableFuture<Set<T>> toSet(){
		return CompletableFuture.supplyAsync(()->stream.toSet(),exec);
	}
	
	/**
	 * Asynchronously capture the minimum value in this stream using the provided function
	 * 
	 * @see org.jooq.lambda.Seq#minBy(Function)
	 */
	public  <U extends Comparable<U>> CompletableFuture<Optional<T>> minBy(Function<T, U> function){
		return CompletableFuture.supplyAsync(()->stream.minBy(function));
	}
	/**
	 * Asynchronously capture the maximum value in this stream using the provided function
	 * 
	 *  @see org.jooq.lambda.Seq#maxBy(Function)
	 */
	public  <U extends Comparable<U>> CompletableFuture<Optional<T>> maxBy(Function<T, U> function){
		return CompletableFuture.supplyAsync(()->stream.maxBy(function));
	}
	
	/**
	 * Asynchronously perform a Stream collection
	 * 
	 * @see java.util.stream.Stream#collect(Collector)
	 * 
	 */
	public <R, A> CompletableFuture<R> collect(Collector<? super T, A, R> collector){
		return CompletableFuture.supplyAsync(()->stream.collect(collector));
	}
	
	
	/**
	 *  Asynchronously perform a Stream collection
	 * @see org.jooq.lambda.Seq#toCollection(Supplier)
	 */
	public <C extends Collection<T>> CompletableFuture<C> toCollection(Supplier<C> collectionFactory){
		return CompletableFuture.supplyAsync(()->stream.toCollection(collectionFactory),exec);
	}
	
	/**
	 * Asyncrhonously generate an Array
	 * 
	 * @see java.util.stream.Stream#toArray(IntFunction)
	 */
	public <A> CompletableFuture<A[]> toArray(IntFunction<A[]> generator){
		return CompletableFuture.supplyAsync(()->stream.toArray(generator),exec);
	}
	/**
	 * Asyncrhonously generate an Array
	 * 
	 * @see java.util.stream.Stream#toArray(IntFunction)
	 */
	public CompletableFuture<Object[]> toArray() {
		return CompletableFuture.supplyAsync(()->stream.toArray(),exec);
	}
	
	/**
	 * Perform an asyncrhonous groupBy operation
	 * @see org.jooq.lambda.Seq#groupBy(Function)
	 */
	public <K> CompletableFuture<Map<K, List<T>>> groupBy(Function<? super T, ? extends K> classifier){
		return CompletableFuture.supplyAsync(()->stream.groupBy(classifier),exec);
	}
	/**
	 * Perform an asyncrhonous groupBy operation
	 * @see org.jooq.lambda.Seq#groupBy(Function, Collector)
	 */
	public <K, A, D> CompletableFuture<Map<K, D>> groupBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
		return CompletableFuture.supplyAsync(()->stream.groupBy(classifier, downstream),exec);
	}
	/**
	 * Perform an asyncrhonous groupBy operation
	 * @see org.jooq.lambda.Seq#groupBy(Function, Supplier, Collector)
	 */
	public  <K, D, A, M extends Map<K, D>> CompletableFuture<M> groupBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream){
		return CompletableFuture.supplyAsync(()->stream.groupBy(classifier, mapFactory, downstream),exec);
	}
	
	/**
	 * Perform an asynchronous foldLeft operation
	 * 	 @see org.jooq.lambda.Seq#foldLeft(Object,BiFunction)
	 * */
	public  <U> CompletableFuture<U> foldLeft(U seed, BiFunction<U, ? super T, U> function){
		return CompletableFuture.supplyAsync(()->stream.foldLeft(seed, function),exec);
	}
	/**
	 * Perform an asynchronous foldRight operation
	 * 	 @see org.jooq.lambda.Seq#foldRight(Object,BiFunction)
	 * */
	public  <U> CompletableFuture<U> foldRight(U seed, BiFunction<? super T, U, U> function){
		return CompletableFuture.supplyAsync(()->stream.foldRight(seed, function),exec);
	}
	
	/**
	 * Perform an asyncrhonous min operation
	 *  @see java.util.stream.Stream#min(Comparator)
	 */
	public CompletableFuture<Optional<T>> min(Comparator<? super T> comparator){
		return CompletableFuture.supplyAsync(()->stream.min(comparator),exec);
	}
	/**
	 * Perform an asyncrhonous min operation
	 *  @see java.util.stream.Stream#max(Comparator)
	 */
	public CompletableFuture<Optional<T>> max(Comparator<? super T> comparator){

		return CompletableFuture.supplyAsync(()->stream.max(comparator),exec);
	}
	/**
	 * Asynchronously perform a Stream collection
	 * 
	 * @see java.util.stream.Stream#collect(Supplier, BiConsumer, BiConsumer)
	 * 
	 */
	public  <R> CompletableFuture<R> collect(Supplier<R> supplier,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R, R> combiner){
		return CompletableFuture.supplyAsync(()->stream.collect(supplier, accumulator, combiner),exec);
	}
	/**
	 * Asynchronously perform a Stream reduction
	 * 
	 * @see java.util.stream.Stream#reduce(Object, BiFunction, BinaryOperator)
	 * 
	 */
	public <U> CompletableFuture<U> reduce(U identity, BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner){
		return CompletableFuture.supplyAsync(()->stream.reduce(identity, accumulator,combiner),exec);
	}
	/**
	 * Asynchronously perform a Stream reduction
	 * 
	 * @see java.util.stream.Stream#reduce(BinaryOperator)
	 * 
	 */
	public CompletableFuture<Optional<T>> reduce(BinaryOperator<T> accumulator){
		
		return CompletableFuture.supplyAsync(()->stream.reduce(accumulator));
		
	}
	/**
	 * Asynchronously perform a Stream reduction
	 * 
	 * @see java.util.stream.Stream#reduce(Object, BinaryOperator)
	 * 
	 */
	public   CompletableFuture<T> reduce(T identity, BinaryOperator<T> accumulator){
		return CompletableFuture.supplyAsync(()->stream.reduce(identity, accumulator),exec);
	}
	/**
	 * Asynchronously perform a Stream count
	 * 
	 * @see java.util.stream.Stream#count()
	 * 
	 */
	public CompletableFuture<Long> count(){
		return CompletableFuture.supplyAsync(()->stream.count(),exec);
	}
	public  CompletableFuture<String> join(CharSequence sep){
		return CompletableFuture.supplyAsync(()->stream.join(sep),exec);
	}
	/**
	 * Perform an asynchronous join operation
	 * 	 @see org.jooq.lambda.Seq#join()
	 * */
	public  CompletableFuture<String> join(){
		return CompletableFuture.supplyAsync(()->stream.join(),exec);
	}
	/**
	 * Perform an asynchronous join operation
	 * 	 @see org.jooq.lambda.Seq#join(CharSequence)
	 * */
	public  CompletableFuture<String> join(CharSequence delimiter, CharSequence prefix,
												CharSequence suffix){
		return CompletableFuture.supplyAsync(()->stream.join(delimiter,prefix,suffix),exec);
	}
	/**
	 * Perform an asynchronous findAny operation
	 * 	 @see java.util.stream.Stream#findAny()
	 * */
	public CompletableFuture<Optional<T>> findAny(){
		return CompletableFuture.supplyAsync(()->stream.findAny(),exec);
	}
	/**
	 * Perform an asynchronous findAny operation
	 * 	 @see java.util.stream.Stream#findFirst()
	 * */
	public CompletableFuture<Optional<T>> findFirst(){
		return CompletableFuture.supplyAsync(()->stream.findFirst(),exec);
	}
	
	/**
	 * Perform an asynchronous findAny operation
	 * 	 @see java.util.stream.Stream#findFirst()
	 * */
	public CompletableFuture<T> firstValue(){
		return CompletableFuture.supplyAsync(()->stream.firstValue(),exec);
	}
	
	/**
	 * Perform an asynchronous All Match operation
	 * 	@see java.util.stream.Stream#allMatch(Predicate)
	 * */
	public CompletableFuture<Boolean> allMatch(Predicate<? super T> predicate){
		return CompletableFuture.supplyAsync(()->stream.allMatch(predicate),exec);
	}
	/**
	 * Perform an asynchronous Any Match operation
	 * 	 @see java.util.stream.Stream#anyMatch(Predicate)
	 * */
	public CompletableFuture<Boolean>   anyMatch(Predicate<? super T> predicate){
		return CompletableFuture.supplyAsync(()->stream.anyMatch(predicate),exec);
	}
	/**
	 * Perform an asynchronous Any Match operation
	 * 	 @see java.util.stream.Stream#noneMatch(Predicate)
	 * */
	public CompletableFuture<Boolean>   noneMatch(Predicate<? super T> predicate){
		return CompletableFuture.supplyAsync(()->stream.noneMatch(predicate),exec);
	}
	
}
