package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.trampoline.Trampoline;

public interface SortedSetX<T> extends SortedSet<T>, MutableCollectionX<T> {
	
	
	
	public <T>Collector<T,?,SortedSet<T>> getCollector();
	
	default <T1> SortedSetX<T1> from(Collection<T1> c){
		return new SortedSetXImpl<T1>(c.stream().collect(getCollector()),getCollector());
	}
	
	default <X> SortedSetX<X> fromStream(Stream<X> stream){
		return new SortedSetXImpl<>(stream.collect(getCollector()),getCollector());
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default SortedSetX<T> reverse() {
		return (SortedSetX)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> filter(Predicate<? super T> pred) {
		
		return (SortedSetX)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (SortedSetX)MutableCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		
		return (SortedSetX)MutableCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default SortedSetX<T> limit(long num) {
		return (SortedSetX)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default SortedSetX<T> skip(long num) {
		
		return (SortedSetX)MutableCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> takeWhile(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> dropWhile(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> takeUntil(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default SortedSetX<T> dropUntil(Predicate<? super T> p) {
		
		return (SortedSetX)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> SortedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (SortedSetX)MutableCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default SortedSetX<T> slice(long from, long to) {
		
		return (SortedSetX)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> SortedSetX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (SortedSetX)MutableCollectionX.super.sorted(function);
	}

	
}
