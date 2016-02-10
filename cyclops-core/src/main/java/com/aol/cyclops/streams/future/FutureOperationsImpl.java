package com.aol.cyclops.streams.future;

import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.collections.extensions.standard.MapX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.reactivestreams.ReactiveTask;
import com.aol.cyclops.streams.FutureStreamUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;



@AllArgsConstructor
public class FutureOperationsImpl<T> implements  DoubleOperatorsMixin<T>, 
						IntOperatorsMixin<T>, LongOperatorsMixin<T>,FutureOperations<T>{

	@Getter
	private final Executor exec;
	@Getter
	private final SequenceM<T> stream;
	
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
	public  <U extends Comparable<? super U>> CompletableFuture<Optional<T>> minBy(Function<? super T,? extends U> function){
		return CompletableFuture.supplyAsync(()->stream.minBy(function));
	}
	/**
	 * Asynchronously capture the maximum value in this stream using the provided function
	 * 
	 *  @see org.jooq.lambda.Seq#maxBy(Function)
	 */
	public  <U extends Comparable<? super U>> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function){
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
	
	@Override
	public void forEach(Consumer<T> c) {
		 CompletableFuture.runAsync(()->stream.forEach(c),exec);
		
	}
	
	
	@Override
	public <X extends Throwable> ReactiveTask forEachX(long numberOfElements, Consumer<? super T> consumer) {
		return new ReactiveTask( exec,FutureStreamUtils.forEachX(stream,numberOfElements, consumer)
								.map2(r->CompletableFuture.runAsync(r,exec)));
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachXWithError(long numberOfElements, Consumer<? super T> consumer,
			Consumer<? super Throwable> consumerError) {
		return new ReactiveTask(exec,FutureStreamUtils.forEachXWithError(stream,numberOfElements, consumer, consumerError)
								.map2(r->CompletableFuture.runAsync(r,exec)));

		
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachXEvents(long numberOfElements, Consumer<? super T> consumer,
			Consumer<? super Throwable> consumerError, Runnable onComplete) {
		return new ReactiveTask(exec,FutureStreamUtils.forEachXEvents(stream,numberOfElements, consumer, consumerError, onComplete)
								.map2(r->CompletableFuture.runAsync(r,exec)));
		
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachWithError(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError) {
		
		return new ReactiveTask( exec,FutureStreamUtils.forEachWithError(stream,consumerElement, consumerError)
							.map2(r->CompletableFuture.runAsync(r,exec)));
	}
	@Override
	public <X extends Throwable> ReactiveTask forEachEvent(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError,
			Runnable onComplete) {
		return new ReactiveTask(exec,FutureStreamUtils.forEachEvent(stream,consumerElement, consumerError,onComplete)
									.map2(r->CompletableFuture.runAsync(r,exec)));
	}
	@Override
	public CompletableFuture<T> single(Predicate<T> predicate) {
		return CompletableFuture.supplyAsync(()-> stream.filter(predicate).single(),exec);
				
	}
	@Override
	public CompletableFuture<Optional<T>> singleOptional() {
		return CompletableFuture.supplyAsync(()-> stream.singleOptional(),exec);
	}
	
	
	
	@Override
	public CompletableFuture<Long> countDistinct() {
		return CompletableFuture.supplyAsync(()->stream.countDistinct(),exec);
	}
	@Override
	public <U> CompletableFuture<Long> countDistinctBy(Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.countDistinctBy(function),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> mode() {
		return CompletableFuture.supplyAsync(()->stream.mode(),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> sum() {
		return CompletableFuture.supplyAsync(()->stream.sum(),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<U>> sum(Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.sum(function),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> avg() {
		return CompletableFuture.supplyAsync(()->stream.avg(),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<U>> avg(Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.avg(function),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> min() {
		return CompletableFuture.supplyAsync(()->stream.min(),exec);
	}
	@Override
	public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> min(Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.min(function),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<U>> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return CompletableFuture.supplyAsync(()->stream.min(function,comparator),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<T>> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return CompletableFuture.supplyAsync(()->stream.minBy(function,comparator),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> max() {
		return CompletableFuture.supplyAsync(()->stream.max(),exec);
	}
	@Override
	public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> max(Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.max(function),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<U>> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return CompletableFuture.supplyAsync(()->stream.max(function,comparator),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return CompletableFuture.supplyAsync(()->stream.maxBy(function,comparator),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> median() {
		return CompletableFuture.supplyAsync(()->stream.median(),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> median(Comparator<? super T> comparator) {
		return CompletableFuture.supplyAsync(()->stream.median(comparator),exec);
	}
	@Override
	public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> medianBy(Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.medianBy(function),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<T>> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return CompletableFuture.supplyAsync(()->stream.medianBy(function,comparator),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> percentile(double percentile) {
		return CompletableFuture.supplyAsync(()->stream.percentile(percentile),exec);
	}
	@Override
	public CompletableFuture<Optional<T>> percentile(double percentile, Comparator<? super T> comparator) {
		return CompletableFuture.supplyAsync(()->stream.percentile(percentile,comparator),exec);
	}
	@Override
	public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> percentileBy(double percentile,
			Function<? super T, ? extends U> function) {
		return CompletableFuture.supplyAsync(()->stream.percentileBy(percentile,function),exec);
	}
	@Override
	public <U> CompletableFuture<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function,
			Comparator<? super U> comparator) {
		return CompletableFuture.supplyAsync(()->stream.percentileBy(percentile,function,comparator),exec);
	}
	@Override
	public <L extends List<T>> CompletableFuture<L> toList(Supplier<L> factory) {
		return CompletableFuture.supplyAsync(()->stream.toList(factory),exec);
	}
	@Override
	public <S extends Set<T>> CompletableFuture<S> toSet(Supplier<S> factory) {
		return CompletableFuture.supplyAsync(()->stream.toSet(factory),exec);
	}
	@Override
	public <K, V> CompletableFuture<MapX<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		return CompletableFuture.supplyAsync(()->stream.toMapX(keyMapper,valueMapper),exec);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#sumDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	public CompletableFuture<Double> sumDouble(ToDoubleFunction<? super T> fn) {
		return DoubleOperatorsMixin.super.sumDouble(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.LongOperatorsMixin#sumLong(java.util.function.ToLongFunction)
	 */
	@Override
	public CompletableFuture<Long> sumLong(ToLongFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return LongOperatorsMixin.super.sumLong(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.LongOperatorsMixin#maxLong(java.util.function.ToLongFunction)
	 */
	@Override
	public CompletableFuture<OptionalLong> maxLong(ToLongFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return LongOperatorsMixin.super.maxLong(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.LongOperatorsMixin#minLong(java.util.function.ToLongFunction)
	 */
	@Override
	public CompletableFuture<OptionalLong> minLong(ToLongFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return LongOperatorsMixin.super.minLong(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.LongOperatorsMixin#averageLong(java.util.function.ToLongFunction)
	 */
	@Override
	public CompletableFuture<OptionalDouble> averageLong(ToLongFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return LongOperatorsMixin.super.averageLong(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.LongOperatorsMixin#summaryStatisticsLong(java.util.function.ToLongFunction)
	 */
	@Override
	public CompletableFuture<LongSummaryStatistics> summaryStatisticsLong(ToLongFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return LongOperatorsMixin.super.summaryStatisticsLong(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.IntOperatorsMixin#sumInt(java.util.function.ToIntFunction)
	 */
	@Override
	public CompletableFuture<Integer> sumInt(ToIntFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return IntOperatorsMixin.super.sumInt(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.IntOperatorsMixin#maxInt(java.util.function.ToIntFunction)
	 */
	@Override
	public CompletableFuture<OptionalInt> maxInt(ToIntFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return IntOperatorsMixin.super.maxInt(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.IntOperatorsMixin#minInt(java.util.function.ToIntFunction)
	 */
	@Override
	public CompletableFuture<OptionalInt> minInt(ToIntFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return IntOperatorsMixin.super.minInt(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.IntOperatorsMixin#averageInt(java.util.function.ToIntFunction)
	 */
	@Override
	public CompletableFuture<OptionalDouble> averageInt(ToIntFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return IntOperatorsMixin.super.averageInt(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.IntOperatorsMixin#summaryStatisticsInt(java.util.function.ToIntFunction)
	 */
	@Override
	public CompletableFuture<IntSummaryStatistics> summaryStatisticsInt(ToIntFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return IntOperatorsMixin.super.summaryStatisticsInt(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#maxDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	public CompletableFuture<OptionalDouble> maxDouble(ToDoubleFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return DoubleOperatorsMixin.super.maxDouble(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#minDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	public CompletableFuture<OptionalDouble> minDouble(ToDoubleFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return DoubleOperatorsMixin.super.minDouble(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#averageDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	public CompletableFuture<OptionalDouble> averageDouble(ToDoubleFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return DoubleOperatorsMixin.super.averageDouble(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.streams.future.DoubleOperatorsMixin#summaryStatisticsDouble(java.util.function.ToDoubleFunction)
	 */
	@Override
	public CompletableFuture<DoubleSummaryStatistics> summaryStatisticsDouble(ToDoubleFunction<? super T> fn) {
		// TODO Auto-generated method stub
		return DoubleOperatorsMixin.super.summaryStatisticsDouble(fn);
	}
	
	
	
	
}