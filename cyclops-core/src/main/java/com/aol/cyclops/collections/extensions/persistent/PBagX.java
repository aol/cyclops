package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.HashTreePBag;
import org.pcollections.MapPBag;
import org.pcollections.PBag;

import com.aol.cyclops.collections.PBags;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface PBagX<T> extends PBag<T>, PersistentCollectionX<T>{
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	

	public static<T> PBagX<T> of(T...values){
		return new PBagXImpl<>(HashTreePBag.from(Arrays.asList(values)));
	}
	
	public static<T> PBagX<T> empty(){
		return new PBagXImpl<>(HashTreePBag .empty());
	}
	public static<T> PBagX<T> singleton(T value){
		return new PBagXImpl<>(HashTreePBag.singleton(value));
	}
	public static<T> PBagX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PBagX)
			return (PBagX)iterable;
		if(iterable instanceof PBag)
			return new PBagXImpl<>((PBag)(iterable));
		MapPBag<T> res = HashTreePBag.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PBagXImpl<>(res);
	}
	public static<T> PBagX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PBagX)
			return (PBagX)stream;
		if(stream instanceof PBag)
			return new PBagXImpl<>((PBag)(stream));
		
		return new PBagXImpl<>(HashTreePBag.from(stream));
	}
	public static<T> PBagX<T> fromStream(Stream<T> stream){
		return new PBagXImpl<>((PBag<T>)PBags.toPBag().mapReduce(stream));
	}
	@Override
	default <R> PBagX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (PBagX<R>)PersistentCollectionX.super.ap1(ap);
	}
	@Override
	default <R> PBagX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default <R> PBagX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	@Override
	default<R> PBagX<R> unit(Collection<R> col){
		return fromCollection(col);
	}
	@Override
	default<R> PBagX<R> emptyUnit(){
		return empty();
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
	default <R> PBagX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
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
	default PBagX<T> takeRight(int num){
		return (PBagX<T>)PersistentCollectionX.super.takeRight(num);
	}
	default PBagX<T> dropRight(int num){
		return (PBagX<T>)PersistentCollectionX.super.dropRight(num);
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
	default PBagX<ListX<T>> grouped(int groupSize){
		return  (PBagX<ListX<T>>)PersistentCollectionX.super.grouped(groupSize);
	}
	default <K, A, D> PBagX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return  (PBagX)PersistentCollectionX.super.grouped(classifier,downstream);
	}
	default <K> PBagX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return  (PBagX)PersistentCollectionX.super.grouped(classifier);
	}
	default <U> PBagX<Tuple2<T, U>> zip(Iterable<U> other){
		return  (PBagX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	default PBagX<ListX<T>> sliding(int windowSize){
		return  (PBagX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize);
	}
	default PBagX<ListX<T>> sliding(int windowSize, int increment){
		return  (PBagX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize,increment);
	}
	default PBagX<T> scanLeft(Monoid<T> monoid){
		return  (PBagX<T>)PersistentCollectionX.super.scanLeft(monoid);
	}
	default <U> PBagX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return  (PBagX<U>)PersistentCollectionX.super.scanLeft(seed,function);
	}
	default PBagX<T> scanRight(Monoid<T> monoid){
		return  (PBagX<T>)PersistentCollectionX.super.scanRight(monoid);
	}
	default <U> PBagX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return  (PBagX<U>)PersistentCollectionX.super.scanRight(identity,combiner);
	}
	

}
