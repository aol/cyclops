package com.aol.cyclops.types.stream;

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
import java.util.stream.Collector;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.control.ReactiveSeq;



public interface CyclopsCollectable<T> extends Collectable<T>, 
                                                Iterable<T> {
	

	

	/* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#modeBy(java.util.function.Function)
     */
    @Override
    default <U> Optional<T> modeBy(Function<? super T, ? extends U> function) {
        
        return collectable().modeBy(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#modeAll()
     */
    @Override
    default ReactiveSeq<T> modeAll() {
        
        return ReactiveSeq.fromStream(collectable().modeAll());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#modeAllBy(java.util.function.Function)
     */
    @Override
    default <U> ReactiveSeq<T> modeAllBy(Function<? super T, ? extends U> function) {
        
        return ReactiveSeq.fromStream(collectable().modeAllBy(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll()
     */
    @Override
    default ReactiveSeq<T> minAll() {
        
        return ReactiveSeq.fromStream(collectable().minAll());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll(java.util.Comparator)
     */
    @Override
    default ReactiveSeq<T> minAll(Comparator<? super T> comparator) {
        
        return ReactiveSeq.fromStream(collectable().minAll(comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ReactiveSeq<U> minAll(Function<? super T, ? extends U> function) {
        
        return ReactiveSeq.fromStream(collectable().minAll(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll(java.util.function.Function, java.util.Comparator)
     */
    @Override
    default <U> ReactiveSeq<U> minAll(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        
        return ReactiveSeq.fromStream(collectable().minAll(function,comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAllBy(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ReactiveSeq<T> minAllBy(Function<? super T, ? extends U> function) {
        
        return ReactiveSeq.fromStream(collectable().minAllBy(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAllBy(java.util.function.Function, java.util.Comparator)
     */
    @Override
    default <U> ReactiveSeq<T> minAllBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        
        return ReactiveSeq.fromStream(collectable().minAllBy(function,comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll()
     */
    @Override
    default ReactiveSeq<T> maxAll() {
        
        return ReactiveSeq.fromStream(collectable().maxAll());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll(java.util.Comparator)
     */
    @Override
    default ReactiveSeq<T> maxAll(Comparator<? super T> comparator) {
        
        return ReactiveSeq.fromStream(collectable().maxAll(comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ReactiveSeq<U> maxAll(Function<? super T, ? extends U> function) {
        
        return ReactiveSeq.fromStream(collectable().maxAll(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll(java.util.function.Function, java.util.Comparator)
     */
    @Override
    default <U> ReactiveSeq<U> maxAll(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        
        return ReactiveSeq.fromStream(collectable().maxAll(function,comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAllBy(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> ReactiveSeq<T> maxAllBy(Function<? super T, ? extends U> function) {
        
        return ReactiveSeq.fromStream(collectable().maxAllBy(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAllBy(java.util.function.Function, java.util.Comparator)
     */
    @Override
    default <U> ReactiveSeq<T> maxAllBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        
        return ReactiveSeq.fromStream(collectable().maxAllBy(function,comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAnd()
     */
    @Override
    default Optional<T> bitAnd() {
        
        return collectable().bitAnd();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAnd(java.util.function.Function)
     */
    @Override
    default <U> Optional<U> bitAnd(Function<? super T, ? extends U> function) {
        
        return collectable().bitAnd(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAndInt(java.util.function.ToIntFunction)
     */
    @Override
    default int bitAndInt(ToIntFunction<? super T> function) {
        
        return collectable().bitAndInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAndLong(java.util.function.ToLongFunction)
     */
    @Override
    default long bitAndLong(ToLongFunction<? super T> function) {
        
        return collectable().bitAndLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOr()
     */
    @Override
    default Optional<T> bitOr() {
        
        return collectable().bitOr();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOr(java.util.function.Function)
     */
    @Override
    default <U> Optional<U> bitOr(Function<? super T, ? extends U> function) {
        
        return collectable().bitOr(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOrInt(java.util.function.ToIntFunction)
     */
    @Override
    default int bitOrInt(ToIntFunction<? super T> function) {
        
        return collectable().bitOrInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOrLong(java.util.function.ToLongFunction)
     */
    @Override
    default long bitOrLong(ToLongFunction<? super T> function) {
        
        return collectable().bitOrLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toMap(java.util.function.Function)
     */
    @Override
    default <K> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper) {
        
        return collectable().toMap(keyMapper);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonPrefix()
     */
    @Override
    default String commonPrefix() {
        
        return collectable().commonPrefix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonSuffix()
     */
    @Override
    default String commonSuffix() {
        
        return collectable().commonSuffix();
    }

    /* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
	 */
	@Override
	default <R, A> R collect(Collector<? super T, A, R> collector) {
		return collectable().collect(collector);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#count()
	 */
	@Override
	default long count() {
		return collectable().count();
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#count(java.util.function.Predicate)
	 */
	@Override
	default long count(Predicate<? super T> predicate) {
		return collectable().count(predicate);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#countDistinct(java.util.function.Predicate)
	 */
	@Override
	default long countDistinct(Predicate<? super T> predicate) {
		return collectable().countDistinct(predicate);
	}

	/* (non-Javadoc)
	 * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function, java.util.function.Predicate)
	 */
	@Override
	default <U> long countDistinctBy(Function<? super T, ? extends U> function, Predicate<? super U> predicate) {
		return collectable().countDistinctBy(function, predicate);
	}

	/**
	 * Narrow this class to a Collectable
	 * 
	 * @return Collectable
	 */
	Collectable<T> collectable();

	@Override
	default long countDistinct() {
		return collectable().countDistinct();
	}

	@Override
	default <U> long countDistinctBy(Function<? super T, ? extends U> function) {
		return collectable().countDistinctBy(function);
	}

	@Override
	default Optional<T> mode() {
		return collectable().mode();
	}

	@Override
	default Optional<T> sum() {
		return collectable().sum();
	}

	@Override
	default <U> Optional<U> sum(Function<? super T, ? extends U> function) {
		return collectable().sum(function);
	}

	@Override
	default int sumInt(ToIntFunction<? super T> function) {
		return collectable().sumInt(function);
	}

	@Override
	default long sumLong(ToLongFunction<? super T> function) {
		return collectable().sumLong(function);
	}

	@Override
	default double sumDouble(ToDoubleFunction<? super T> function) {
		return collectable().sumDouble(function);
	}

	@Override
	default Optional<T> avg() {
		return collectable().avg();
	}

	@Override
	default <U> Optional<U> avg(Function<? super T, ? extends U> function) {
		return collectable().avg(function);
	}

	@Override
	default double avgInt(ToIntFunction<? super T> function) {
		return collectable().avgInt(function);
	}

	@Override
	default double avgLong(ToLongFunction<? super T> function) {
		return collectable().avgLong(function);
	}

	@Override
	default double avgDouble(ToDoubleFunction<? super T> function) {
		return collectable().avgDouble(function);
	}

	@Override
	default Optional<T> min() {
		return collectable().min();
	}

	@Override
	default Optional<T> min(Comparator<? super T> comparator) {
		return collectable().min(comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<U> min(Function<? super T, ? extends U> function) {
		return collectable().min(function);
	}

	@Override
	default <U> Optional<U> min(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return collectable().min(function,comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function) {
		return collectable().minBy(function);
	}

	@Override
	default <U> Optional<T> minBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return collectable().minBy(function,comparator);
	}

	@Override
	default Optional<T> max() {
		return collectable().max();
	}

	@Override
	default Optional<T> max(Comparator<? super T> comparator) {
		return collectable().max(comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<U> max(Function<? super T, ? extends U> function) {
		return collectable().max(function);
	}

	@Override
	default <U> Optional<U> max(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return collectable().max(function,comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> maxBy(Function<? super T, ? extends U> function) {
		return collectable().maxBy(function);
	}

	@Override
	default <U> Optional<T> maxBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return collectable().maxBy(function,comparator);
	}

	@Override
	default Optional<T> median() {
		return collectable().median();
	}

	@Override
	default Optional<T> median(Comparator<? super T> comparator) {
		return collectable().median(comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> medianBy(Function<? super T, ? extends U> function) {
		return collectable().medianBy(function);
	}

	@Override
	default <U> Optional<T> medianBy(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return collectable().medianBy(function,comparator);
	}

	@Override
	default Optional<T> percentile(double percentile) {
		return collectable().percentile(percentile);
	}

	@Override
	default Optional<T> percentile(double percentile, Comparator<? super T> comparator) {
		return collectable().percentile(percentile,comparator);
	}

	@Override
	default <U extends Comparable<? super U>> Optional<T> percentileBy(double percentile, Function<? super T, ? extends U> function) {
		return collectable().percentileBy(percentile, function);
	}

	@Override
	default <U> Optional<T> percentileBy(double percentile, Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
		return collectable().percentileBy(percentile, function, comparator);
	}
	

	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
	 * }
	 * </pre>
	 * 
	 * @param c
	 *            Predicate to check if all match
	 */
	default boolean allMatch(Predicate<? super T> c){
		return collectable().allMatch(c);
	}

	/**
	 * True if a single element matches when Monad converted to a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
	 * }
	 * </pre>
	 * 
	 * @param c
	 *            Predicate to check if any match
	 */
	default boolean anyMatch(Predicate<? super T> c){
		return collectable().anyMatch(c);
	}

	default boolean noneMatch(Predicate<? super T> c){
		return collectable().noneMatch(c);
	}

	

	@Override
	default List<T> toList() {
		if(this instanceof List)
			return (List)this;
		return collectable().toList();
	}

	@Override
	default <L extends List<T>> L toList(Supplier<L> factory) {
		return collectable().toList(factory);
	}

	@Override
	default Set<T> toSet() {
		if(this instanceof Set)
			return (Set)this;
		return collectable().toSet();
	}

	@Override
	default <S extends Set<T>> S toSet(Supplier<S> factory) {
		return collectable().toSet(factory);
	}

	@Override
	default <C extends Collection<T>> C toCollection(Supplier<C> factory) {
		return collectable().toCollection(factory);
	}

	@Override
	default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return collectable().toMap(keyMapper, valueMapper);
	}

	@Override
	default String toString(CharSequence delimiter) {
		return collectable().toString(delimiter);
	}

	@Override
	default String toString(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
		return collectable().toString(delimiter, prefix, suffix);
	}
	
}
