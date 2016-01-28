package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.PStack;

import com.aol.cyclops.collections.PStacks;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PStackX<T> extends PStack<T>, PersistentCollectionX<T>{

	public static <T> PStackX<T> of(PStack<T> stack){
		return new PStackXImpl<T>(stack);
	}
	default PStack<T> toPStack(){
		return this;
	}
	
	default <X> PStackX<X> from(Collection<X> col){
		return PStacks.fromCollection(col);
	}
	default <T> Monoid<PStack<T>> monoid(){
		return PStacks.toPStack();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PStackX<T> reverse() {
		
		return (PStackX)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> filter(Predicate<? super T> pred) {
		
		return (PStackX)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (PStackX)PersistentCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		
		return (PStackX)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PStackX<T> limit(long num) {
		
		return (PStackX)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PStackX<T> skip(long num) {
		
		return (PStackX)PersistentCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> takeWhile(Predicate<? super T> p) {
		
		return (PStackX)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> dropWhile(Predicate<? super T> p) {
		
		return (PStackX)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> takeUntil(Predicate<? super T> p) {
		
		return (PStackX)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> dropUntil(Predicate<? super T> p) {
		return (PStackX)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (PStackX)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PStackX<T> slice(long from, long to) {
		return (PStackX)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PStackX<T> sorted(Function<? super T, ? extends U> function) {
		return (PStackX)PersistentCollectionX.super.sorted(function);
	}

	

}
