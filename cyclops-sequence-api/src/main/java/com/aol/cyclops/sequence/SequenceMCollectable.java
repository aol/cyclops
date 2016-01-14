package com.aol.cyclops.sequence;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.sequence.streamable.ConvertableToSequenceM;
import com.aol.cyclops.sequence.streamable.ToStream;



public interface SequenceMCollectable<T> extends Collectable<T>,ConvertableToSequenceM<T> {
	
	
	

	

	

	@Override
	default long countDistinct() {
		return sequenceM().countDistinct();
	}

	@Override
	default <U> long countDistinctBy(Function<? super T, ? extends U> function) {
		return sequenceM().countDistinctBy(function);
	}

	@Override
	default Optional<T> mode() {
		return sequenceM().mode();
	}

	@Override
	default Optional<T> sum() {
		return sequenceM().sum();
	}

	@Override
	default <U> Optional<U> sum(Function<? super T, ? extends U> function) {
		return sequenceM().sum(function);
	}

	@Override
	default int sumInt(ToIntFunction<? super T> function) {
		return sequenceM().sumInt(function);
	}

	@Override
	default long sumLong(ToLongFunction<? super T> function) {
		return sequenceM().sumLong(function);
	}

	@Override
	default double sumDouble(ToDoubleFunction<? super T> function) {
		return sequenceM().sumDouble(function);
	}

	@Override
	default Optional<T> avg() {
		return sequenceM().avg();
	}

	@Override
	default <U> Optional<U> avg(Function<? super T, ? extends U> function) {
		return sequenceM().avg(function);
	}

	@Override
	default double avgInt(ToIntFunction<? super T> function) {
		return sequenceM().avgInt(function);
	}

	@Override
	default double avgLong(ToLongFunction<? super T> function) {
		return sequenceM().avgLong(function);
	}

	@Override
	default double avgDouble(ToDoubleFunction<? super T> function) {
		return sequenceM().avgDouble(function);
	}

	@Override
	default Optional<T> min() {
		return sequenceM().min();
	}

	@Override
	default Optional<T> min(Comparator<? super T> comparator) {
		return sequenceM().min(comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<U> min(Function<? super T, ? extends U> function) {
		return sequenceM().min(function);
	}

	@Override
	default <U> Optional<U> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return sequenceM().min(function,comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function) {
		return sequenceM().minBy(function);
	}

	@Override
	default <U> Optional<T> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return sequenceM().minBy(function,comparator);
	}

	@Override
	default Optional<T> max() {
		return sequenceM().max();
	}

	@Override
	default Optional<T> max(Comparator<? super T> comparator) {
		return sequenceM().max(comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<U> max(Function<? super T, ? extends U> function) {
		return sequenceM().max(function);
	}

	@Override
	default <U> Optional<U> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return sequenceM().max(function,comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> maxBy(Function<? super T, ? extends U> function) {
		return sequenceM().maxBy(function);
	}

	@Override
	default <U> Optional<T> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return sequenceM().maxBy(function,comparator);
	}

	@Override
	default Optional<T> median() {
		return sequenceM().median();
	}

	@Override
	default Optional<T> median(Comparator<? super T> comparator) {
		return sequenceM().median(comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> medianBy(Function<? super T, ? extends U> function) {
		return sequenceM().medianBy(function);
	}

	@Override
	default <U> Optional<T> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return sequenceM().medianBy(function,comparator);
	}

	@Override
	default Optional<T> percentile(double percentile) {
		return sequenceM().percentile(percentile);
	}

	@Override
	default Optional<T> percentile(double percentile, Comparator<? super T> comparator) {
		return sequenceM().percentile(percentile,comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> percentileBy(double percentile, Function<? super T, ? extends U> function) {
		return sequenceM().percentileBy(percentile, function);
	}

	@Override
	default <U> Optional<T> percentileBy(double percentile, Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return sequenceM().percentileBy(percentile, function, comparator);
	}

	@Override
	default boolean allMatch(Predicate<? super T> predicate) {
		return sequenceM().allMatch(predicate);
	}

	@Override
	default boolean anyMatch(Predicate<? super T> predicate) {
		return sequenceM().anyMatch(predicate);
	}

	@Override
	default boolean noneMatch(Predicate<? super T> predicate) {
		return sequenceM().noneMatch(predicate);
	}

	@Override
	default List<T> toList() {
		return sequenceM().toList();
	}

	@Override
	default <L extends List<T>> L toList(Supplier<L> factory) {
		return sequenceM().toList(factory);
	}

	@Override
	default Set<T> toSet() {
		return sequenceM().toSet();
	}

	@Override
	default <S extends Set<T>> S toSet(Supplier<S> factory) {
		return sequenceM().toSet(factory);
	}

	@Override
	default <C extends Collection<T>> C toCollection(Supplier<C> factory) {
		return sequenceM().toCollection(factory);
	}

	@Override
	default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return sequenceM().toMap(keyMapper, valueMapper);
	}

	@Override
	default String toString(CharSequence delimiter) {
		return sequenceM().toString(delimiter);
	}

	@Override
	default String toString(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
		return sequenceM().toString(delimiter, prefix, suffix);
	}

}
