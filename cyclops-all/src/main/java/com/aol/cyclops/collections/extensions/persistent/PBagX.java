package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.HashTreePBag;
import org.pcollections.PBag;
import org.pcollections.PSet;

import com.aol.cyclops.collections.PBags;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PBagX<T> extends PBag<T>, PersistentCollectionX<T>{

	public static<T> PBagX<T> of(T...values){
		return new PBagXImpl<>(HashTreePBag.from(Arrays.asList(values)));
	}
	
	public static<T> PBagX<T> empty(){
		return new PBagXImpl<>(HashTreePBag .empty());
	}
	public static<T> PBagX<T> singleton(T value){
		return new PBagXImpl<>(HashTreePBag.singleton(value));
	}
	
	public static<T> PBagX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PBag)
			return new PBagXImpl<>((PBag)(stream));
		return new PBagXImpl<>(HashTreePBag.from(stream));
	}
	public static<T> PBagX<T> fromStream(Stream<T> stream){
		return new PBagXImpl<>((PBag<T>)PBags.toPBag().mapReduce(stream));
	}
	
	default PBag<T> toPBag(){
		return this;
	}
	
	default <X> PBagX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Monoid<PBag<T>> monoid(){
		return PBags.toPBag();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public PBagX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public PBagX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public PBagX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public PBagX<T> minusAll(Collection<?> list);

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PBagX<T> reverse() {
		return (PBagX<T>)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> filter(Predicate<? super T> pred) {
		return (PBagX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PBagX<R> map(Function<? super T, ? extends R> mapper) {
		return (PBagX<R>)PersistentCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PBagX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return (PBagX<R>)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PBagX<T> limit(long num) {
		return (PBagX<T>)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PBagX<T> skip(long num) {
		return (PBagX<T>)PersistentCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> takeWhile(Predicate<? super T> p) {
		return (PBagX<T>)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> dropWhile(Predicate<? super T> p) {
		return (PBagX<T>)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> takeUntil(Predicate<? super T> p) {
		return (PBagX<T>)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> dropUntil(Predicate<? super T> p) {
		return (PBagX<T>)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PBagX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (PBagX<R>)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PBagX<T> slice(long from, long to) {
		return (PBagX<T>)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PBagX<T> sorted(Function<? super T, ? extends U> function) {
		return (PBagX<T>)PersistentCollectionX.super.sorted(function);
	}
	

}
