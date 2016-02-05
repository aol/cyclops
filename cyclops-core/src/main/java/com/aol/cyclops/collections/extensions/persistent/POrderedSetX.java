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
import org.pcollections.OrderedPSet;
import org.pcollections.PBag;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;

import com.aol.cyclops.collections.PBags;
import com.aol.cyclops.collections.POrderedSets;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface POrderedSetX<T> extends POrderedSet<T>, PersistentCollectionX<T>{
	//after module merge, move to reducers
	public static <T> Monoid<POrderedSetX<T>> toPOrderedSetX() { 
					return	Monoid.<POrderedSetX<T>>of(POrderedSetX.<T>empty(), 
											(POrderedSetX<T> a) -> b -> a.plusAll(b),
											(T x) -> POrderedSetX.singleton(x));
	}
	public static <T> POrderedSetX<T> of(T...values){
		return new POrderedSetXImpl<>(OrderedPSet.from(Arrays.asList(values)));
	}
	public static <T> POrderedSetX<T> empty(){
		return new POrderedSetXImpl<>(OrderedPSet.empty());
	}
	public static <T> POrderedSetX<T> singleton(T value){
		return new POrderedSetXImpl<>(OrderedPSet.singleton(value));
	}
	public static<T> POrderedSetX<T> fromCollection(Collection<T> stream){
		if(stream instanceof POrderedSetX)
			return (POrderedSetX)(stream);
		if(stream instanceof POrderedSet)
			return new  POrderedSetXImpl<>((POrderedSet)(stream));
		return new  POrderedSetXImpl<>(OrderedPSet.from(stream));
	}
	public static<T> POrderedSetX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof POrderedSetX)
			return (POrderedSetX)iterable;
		if(iterable instanceof POrderedSet)
			return new POrderedSetXImpl<>((POrderedSet)(iterable));
		POrderedSet<T> res = OrderedPSet.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new POrderedSetXImpl<>(res);
	}
	public static<T> POrderedSetX<T> toPOrderedSet(Stream<T> stream){
		return new POrderedSetXImpl<>((POrderedSet<T>)POrderedSets.toPOrderedSet().mapReduce(stream));
	}
	@Override
	default<R> POrderedSetX<R> unit(Collection<R> col){
		return fromCollection(col);
	}
	@Override
	default <R> POrderedSetX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default <R> POrderedSetX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	@Override
	default<R> POrderedSetX<R> emptyUnit(){
		return empty();
	}
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	default POrderedSet<T> toPOrderedSet(){
		return this;
	}
	
	default <X> POrderedSetX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Monoid<PBag<T>> monoid(){
		return PBags.toPBag();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public POrderedSetX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public POrderedSetX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public POrderedSetX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public POrderedSetX<T> minusAll(Collection<?> list);

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default POrderedSetX<T> reverse() {
		return (POrderedSetX<T>)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> filter(Predicate<? super T> pred) {
		return (POrderedSetX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> map(Function<? super T, ? extends R> mapper) {
		return (POrderedSetX<R>)PersistentCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		return (POrderedSetX<R>)PersistentCollectionX.super.flatMap(mapper);
	}
	default POrderedSetX<T> takeRight(int num){
		return (POrderedSetX<T>)PersistentCollectionX.super.takeRight(num);
	}
	default POrderedSetX<T> dropRight(int num){
		return (POrderedSetX<T>)PersistentCollectionX.super.dropRight(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default POrderedSetX<T> limit(long num) {
		return (POrderedSetX<T>)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default POrderedSetX<T> skip(long num) {
		return (POrderedSetX<T>)PersistentCollectionX.super.skip(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> takeWhile(Predicate<? super T> p) {
		return (POrderedSetX<T>)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> dropWhile(Predicate<? super T> p) {
		return (POrderedSetX<T>)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> takeUntil(Predicate<? super T> p) {
		return (POrderedSetX<T>)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> dropUntil(Predicate<? super T> p) {
		return (POrderedSetX<T>)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (POrderedSetX<R>)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default POrderedSetX<T> slice(long from, long to) {
		return (POrderedSetX<T>)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> POrderedSetX<T> sorted(Function<? super T, ? extends U> function) {
		return (POrderedSetX<T>)PersistentCollectionX.super.sorted(function);
	}
	default POrderedSetX<ListX<T>> grouped(int groupSize){
		return  (POrderedSetX<ListX<T>>)PersistentCollectionX.super.grouped(groupSize);
	}
	default <K, A, D> POrderedSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return  (POrderedSetX)PersistentCollectionX.super.grouped(classifier,downstream);
	}
	default <K> POrderedSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return  (POrderedSetX)PersistentCollectionX.super.grouped(classifier);
	}
	default <U> POrderedSetX<Tuple2<T, U>> zip(Iterable<U> other){
		return  (POrderedSetX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	default POrderedSetX<ListX<T>> sliding(int windowSize){
		return  (POrderedSetX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize);
	}
	default POrderedSetX<ListX<T>> sliding(int windowSize, int increment){
		return  (POrderedSetX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize,increment);
	}
	default POrderedSetX<T> scanLeft(Monoid<T> monoid){
		return  (POrderedSetX<T>)PersistentCollectionX.super.scanLeft(monoid);
	}
	default <U> POrderedSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return  (POrderedSetX<U>)PersistentCollectionX.super.scanLeft(seed,function);
	}
	default POrderedSetX<T> scanRight(Monoid<T> monoid){
		return  (POrderedSetX<T>)PersistentCollectionX.super.scanRight(monoid);
	}
	default <U> POrderedSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return  (POrderedSetX<U>)PersistentCollectionX.super.scanRight(identity,combiner);
	}

}
