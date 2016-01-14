package com.aol.cyclops.sequence.future;



import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

import com.sun.javafx.collections.MappingChange.Map;

public interface FutureCollectable<T>{

	
	public <R, A> CompletableFuture<R> collect(Collector<? super T, A, R> collector);

	
	public CompletableFuture<Long> count();

	
	public CompletableFuture<Long> countDistinct();

	
	public <U> CompletableFuture<Long> countDistinctBy(Function<? super T, ? extends U> function);

	
	public CompletableFuture<Optional<T>> mode() ;

	
	public CompletableFuture<Optional<T>> sum() ;

	
	public <U> CompletableFuture<Optional<U>> sum(Function<? super T, ? extends U> function) ;

	
	public CompletableFuture<Integer> sumInt(ToIntFunction<? super T> function);

	
	public CompletableFuture<Long> sumLong(ToLongFunction<? super T> function);

	
	public CompletableFuture<Double> sumDouble(ToDoubleFunction<? super T> function);

	
	public CompletableFuture<Optional<T>> avg() ;

	
	public <U> CompletableFuture<Optional<U>> avg(Function<? super T, ? extends U> function) ;

	
	
	public CompletableFuture<Optional<T>> min() ;

	
	public CompletableFuture<Optional<T>> min(Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> min(Function<? super T, ? extends U> function) ;

	
	public <U> CompletableFuture<Optional<U>> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> minBy(Function<? super T, ? extends U> function) ;

	
	public <U> CompletableFuture<Optional<T>> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public CompletableFuture<Optional<T>> max() ;

	
	public CompletableFuture<Optional<T>> max(Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> CompletableFuture<Optional<U>> max(Function<? super T, ? extends U> function) ;

	
	public <U> CompletableFuture<Optional<U>> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function) ;

	
	public <U> CompletableFuture<Optional<T>> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public CompletableFuture<Optional<T>> median() ;

	
	public CompletableFuture<Optional<T>> median(Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> medianBy(Function<? super T, ? extends U> function) ;

	
	public <U> CompletableFuture<Optional<T>> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public CompletableFuture<Optional<T>> percentile(double percentile) ;

	
	public CompletableFuture<Optional<T>> percentile(double percentile, Comparator<? super T> comparator) ;

	
	public <U extends Comparable<? super U>> CompletableFuture<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function) ;

	
	public <U> CompletableFuture<Optional<T>> percentileBy(double percentile, Function<? super T, ? extends U> function, Comparator<? super U> comparator) ;

	
	public CompletableFuture<Boolean> allMatch(Predicate<? super T> predicate);

	
	public CompletableFuture<Boolean> anyMatch(Predicate<? super T> predicate);

	
	public CompletableFuture<Boolean> noneMatch(Predicate<? super T> predicate);

	
	public CompletableFuture<List<T>> toList() ;

	
	public <L extends List<T>> CompletableFuture<L> toList(Supplier<L> factory) ;

	
	public CompletableFuture<Set<T>> toSet() ;

	
	public <S extends Set<T>> CompletableFuture<S> toSet(Supplier<S> factory) ;

	
	public <C extends Collection<T>> CompletableFuture<C> toCollection(Supplier<C> factory) ;

	
	public <K, V> CompletableFuture<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) ;

	
	

}
