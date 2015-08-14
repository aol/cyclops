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
import java.util.stream.Stream;

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
	
	public CompletableFuture<List<T>> toList(){
		return CompletableFuture.supplyAsync(()->stream.toList(),exec);
	}
	public CompletableFuture<Set<T>> toSet(){
		return CompletableFuture.supplyAsync(()->stream.toSet(),exec);
	}
	
	public  <U extends Comparable<U>> CompletableFuture<Optional<T>> minBy(Function<T, U> function){
		return CompletableFuture.supplyAsync(()->stream.minBy(function));
	}
	public  <U extends Comparable<U>> CompletableFuture<Optional<T>> maxBy(Function<T, U> function){
		return CompletableFuture.supplyAsync(()->stream.maxBy(function));
	}
	
	public <R, A> CompletableFuture<R> collect(Collector<? super T, A, R> collector){
		return CompletableFuture.supplyAsync(()->stream.collect(collector));
	}
	public CompletableFuture<Optional<T>> reduce(BinaryOperator<T> accumulator){
		
		return CompletableFuture.supplyAsync(()->stream.reduce(accumulator));
		
	}
	
	public <C extends Collection<T>> CompletableFuture<C> toCollection(Supplier<C> collectionFactory){
		return CompletableFuture.supplyAsync(()->stream.toCollection(collectionFactory),exec);
	}
	
	public <A> CompletableFuture<A[]> toArray(IntFunction<A[]> generator){
		return CompletableFuture.supplyAsync(()->stream.toArray(generator),exec);
	}
	public CompletableFuture<Object[]> toArray() {
		return CompletableFuture.supplyAsync(()->stream.toArray(),exec);
	}
	
	public <K> CompletableFuture<Map<K, List<T>>> groupBy(Function<? super T, ? extends K> classifier){
		return CompletableFuture.supplyAsync(()->stream.groupBy(classifier),exec);
	}
	
	public <K, A, D> CompletableFuture<Map<K, D>> groupBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
		return CompletableFuture.supplyAsync(()->stream.groupBy(classifier, downstream),exec);
	}
	
	public  <K, D, A, M extends Map<K, D>> CompletableFuture<M> groupBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream){
		return CompletableFuture.supplyAsync(()->stream.groupBy(classifier, mapFactory, downstream),exec);
	}
	
	public  <U> CompletableFuture<U> foldLeft(U seed, BiFunction<U, ? super T, U> function){
		return CompletableFuture.supplyAsync(()->stream.foldLeft(seed, function),exec);
	}
	public  <U> CompletableFuture<U> foldRight(U seed, BiFunction<? super T, U, U> function){
		return CompletableFuture.supplyAsync(()->stream.foldRight(seed, function),exec);
	}
	
	public CompletableFuture<Optional<T>> min(Comparator<? super T> comparator){
		return CompletableFuture.supplyAsync(()->stream.min(comparator),exec);
	}
	
	public CompletableFuture<Optional<T>> max(Comparator<? super T> comparator){

		return CompletableFuture.supplyAsync(()->stream.max(comparator),exec);
	}
	
	public  <R> CompletableFuture<R> collect(Supplier<R> supplier,
            BiConsumer<R, ? super T> accumulator,
            BiConsumer<R, R> combiner){
		return CompletableFuture.supplyAsync(()->stream.collect(supplier, accumulator, combiner),exec);
	}
	public <U> CompletableFuture<U> reduce(U identity, BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner){
		return CompletableFuture.supplyAsync(()->stream.reduce(identity, accumulator,combiner),exec);
	}
	
	public   CompletableFuture<T> reduce(T identity, BinaryOperator<T> accumulator){
		return CompletableFuture.supplyAsync(()->stream.reduce(identity, accumulator),exec);
	}
	
	public CompletableFuture<Long> count(){
		return CompletableFuture.supplyAsync(()->stream.count(),exec);
	}
	public  CompletableFuture<String> join(CharSequence sep){
		return CompletableFuture.supplyAsync(()->stream.join(sep),exec);
	}
	public  CompletableFuture<String> join(){
		return CompletableFuture.supplyAsync(()->stream.join(),exec);
	}
	public  CompletableFuture<String> join(CharSequence delimiter, CharSequence prefix,
												CharSequence suffix){
		return CompletableFuture.supplyAsync(()->stream.join(delimiter,prefix,suffix),exec);
	}
	
	public CompletableFuture<Optional<T>> findAny(){
		return CompletableFuture.supplyAsync(()->stream.findAny(),exec);
	}
	public CompletableFuture<Optional<T>> findFirst(){
		return CompletableFuture.supplyAsync(()->stream.findFirst(),exec);
	}
	
	public CompletableFuture<Boolean> allMatch(Predicate<? super T> predicate){
		return CompletableFuture.supplyAsync(()->stream.allMatch(predicate),exec);
	}
	
	public CompletableFuture<Boolean>   anyMatch(Predicate<? super T> predicate){
		return CompletableFuture.supplyAsync(()->stream.anyMatch(predicate),exec);
	}
	public CompletableFuture<Boolean>   noneMatch(Predicate<? super T> predicate){
		return CompletableFuture.supplyAsync(()->stream.noneMatch(predicate),exec);
	}
	
}
