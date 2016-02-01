package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import com.aol.cyclops.collections.PSets;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface PSetX<T> extends PSet<T>, PersistentCollectionX<T>{
	//after module merge, move to reducers
	public static <T> Monoid<PSetX<T>> toPSetX() { 
				return	Monoid.<PSetX<T>>of(PSetX.empty(), 
										(PSetX<T> a) -> b -> a.plusAll(b),
										(T x) -> PSetX.singleton(x));
	}
	public static <T> PSetX<T> of(T...values){
		
		return new PSetXImpl<>(HashTreePSet.from(Arrays.asList(values)));
	}
	public static <T> PSetX<T> empty(){
		return new PSetXImpl<>(HashTreePSet .empty());
	}
	public static <T> PSetX<T> singleton(T value){
		return new PSetXImpl<>(HashTreePSet.singleton(value));
	}
	public static<T> PSetX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PSetX)
			return (PSetX)(stream);
		if(stream instanceof PSet)
			return new PSetXImpl<>((PSet)(stream));
		return new PSetXImpl<>(HashTreePSet.from(stream));
	}
	public static<T> PSetX<T> fromStream(Stream<T> stream){
		return new PSetXImpl<>((PSet<T>)PSets.toPSet().mapReduce(stream));
	}
	@Override
	default<R> PSetX<R> unit(Collection<R> col){
		return fromCollection(col);
	}
	@Override
	default<R> PSetX<R> emptyUnit(){
		return empty();
	}
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	default PSet<T> toPSet(){
		return this;
	}
	
	default <X> PSetX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Monoid<PSet<T>> monoid(){
		return PSets.toPSet();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public PSetX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public PSetX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public PSetX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public PSetX<T> minusAll(Collection<?> list);

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PSetX<T> reverse() {
		return (PSetX<T>)PersistentCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> filter(Predicate<? super T> pred) {
		return (PSetX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> map(Function<? super T, ? extends R> mapper) {
		return (PSetX<R>)PersistentCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		return (PSetX<R>)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PSetX<T> limit(long num) {
		return (PSetX<T>)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PSetX<T> skip(long num) {
		return (PSetX<T>)PersistentCollectionX.super.skip(num);
	}
	default PSetX<T> takeRight(int num){
		return (PSetX<T>)PersistentCollectionX.super.takeRight(num);
	}
	default PSetX<T> dropRight(int num){
		return (PSetX<T>)PersistentCollectionX.super.dropRight(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> takeWhile(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> dropWhile(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> takeUntil(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> dropUntil(Predicate<? super T> p) {
		return (PSetX<T>)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (PSetX<R>)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PSetX<T> slice(long from, long to) {
		return (PSetX<T>)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PSetX<T> sorted(Function<? super T, ? extends U> function) {
		return (PSetX<T>)PersistentCollectionX.super.sorted(function);
	}
	default PSetX<ListX<T>> grouped(int groupSize){
		return  (PSetX<ListX<T>>)PersistentCollectionX.super.grouped(groupSize);
	}
	default <K, A, D> PSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return  (PSetX)PersistentCollectionX.super.grouped(classifier,downstream);
	}
	default <K> PSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return  (PSetX)PersistentCollectionX.super.grouped(classifier);
	}
	default <U> PSetX<Tuple2<T, U>> zip(Iterable<U> other){
		return  (PSetX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	default PSetX<ListX<T>> sliding(int windowSize){
		return  (PSetX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize);
	}
	default PSetX<ListX<T>> sliding(int windowSize, int increment){
		return  (PSetX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize,increment);
	}
	default PSetX<T> scanLeft(Monoid<T> monoid){
		return  (PSetX<T>)PersistentCollectionX.super.scanLeft(monoid);
	}
	default <U> PSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return  (PSetX<U>)PersistentCollectionX.super.scanLeft(seed,function);
	}
	default PSetX<T> scanRight(Monoid<T> monoid){
		return  (PSetX<T>)PersistentCollectionX.super.scanRight(monoid);
	}
	default <U> PSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return  (PSetX<U>)PersistentCollectionX.super.scanRight(identity,combiner);
	}
	

}
