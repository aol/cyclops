package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.trampoline.Trampoline;

public interface QueueX<T> extends Queue<T>, MutableCollectionX<T> {
	
	public <T> Collector<T,?,Queue<T>> getCollector();
	
	default <T1> QueueX<T1> from(Collection<T1> c){
		
		return new QueueXImpl<T1>(c.stream().collect(getCollector()),getCollector());
	}
	
	default <X> QueueX<X> fromStream(Stream<X> stream){
		return new QueueXImpl<>(stream.collect(getCollector()),getCollector());
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default QueueX<T> reverse() {
		
		return (QueueX)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> filter(Predicate<? super T> pred) {
		
		return (QueueX)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (QueueX)MutableCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		
		return (QueueX)MutableCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default QueueX<T> limit(long num) {
	
		return (QueueX)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default QueueX<T> skip(long num) {
		
		return (QueueX)MutableCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> takeWhile(Predicate<? super T> p) {
		
		return (QueueX)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> dropWhile(Predicate<? super T> p) {
		
		return (QueueX)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> takeUntil(Predicate<? super T> p) {
		
		return (QueueX)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default QueueX<T> dropUntil(Predicate<? super T> p) {
		
		return (QueueX)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> QueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (QueueX)MutableCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default QueueX<T> slice(long from, long to) {
		
		return (QueueX)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> QueueX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (QueueX)MutableCollectionX.super.sorted(function);
	}
}
