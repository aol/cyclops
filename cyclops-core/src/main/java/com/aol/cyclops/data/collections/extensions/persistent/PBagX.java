package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.HashTreePBag;
import org.pcollections.MapPBag;
import org.pcollections.PBag;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.PBags;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.matcher2.Case;
import com.aol.cyclops.matcher2.CheckValues;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicative;

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
		
		return (PBagX<R>)(PBagX<T>)PersistentCollectionX.super.ap1(ap);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default PBagX<T> toPBagX() {
		return this;
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
	default <T> Reducer<PBag<T>> monoid(){
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
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> PBagX<R> zip(Iterable<U> other,
			BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (PBagX<R>)PersistentCollectionX.super.zip(other, zipper);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#permutations()
	 */
	@Override
	default PBagX<SequenceM<T>> permutations() {
		
		return ( PBagX<SequenceM<T>>)PersistentCollectionX.super.permutations();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations(int)
	 */
	@Override
	default PBagX<SequenceM<T>> combinations(int size) {
		
		return (PBagX<SequenceM<T>>)PersistentCollectionX.super.combinations(size);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations()
	 */
	@Override
	default PBagX<SequenceM<T>> combinations() {
		
		return (PBagX<SequenceM<T>>)PersistentCollectionX.super.combinations();
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


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
	 */
	@Override
	default PBagX<T> plusInOrder(T e) {
		
		return (PBagX<T>)PersistentCollectionX.super.plusInOrder(e);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(int)
	 */
	@Override
	default PBagX<T> cycle(int times) {
		
		return (PBagX<T>)PersistentCollectionX.super.cycle(times);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default PBagX<T> cycle(Monoid<T> m, int times) {
		
		return (PBagX<T>)PersistentCollectionX.super.cycle(m, times);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (PBagX<T>)PersistentCollectionX.super.cycleWhile(predicate);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (PBagX<T>)PersistentCollectionX.super.cycleUntil(predicate);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> PBagX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (PBagX<Tuple2<T, U>>)PersistentCollectionX.super.zipStream(other);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> PBagX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (PBagX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> PBagX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (PBagX)PersistentCollectionX.super.zip3(second, third);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> PBagX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (PBagX<Tuple4<T, T2, T3, T4>>)PersistentCollectionX.super.zip4(second, third, fourth);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
	 */
	@Override
	default PBagX<Tuple2<T, Long>> zipWithIndex() {
		
		return (PBagX<Tuple2<T, Long>>)PersistentCollectionX.super.zipWithIndex();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#distinct()
	 */
	@Override
	default PBagX<T> distinct() {
		
		return (PBagX<T>)PersistentCollectionX.super.distinct();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted()
	 */
	@Override
	default PBagX<T> sorted() {
		
		return (PBagX<T>)PersistentCollectionX.super.sorted();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default PBagX<T> sorted(Comparator<? super T> c) {
		
		return (PBagX<T>)PersistentCollectionX.super.sorted(c);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> skipWhile(Predicate<? super T> p) {
		
		return (PBagX<T>)PersistentCollectionX.super.skipWhile(p);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> skipUntil(Predicate<? super T> p) {
		
		return (PBagX<T>)PersistentCollectionX.super.skipUntil(p);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> limitWhile(Predicate<? super T> p) {
		
		return (PBagX<T>)PersistentCollectionX.super.limitWhile(p);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> limitUntil(Predicate<? super T> p) {
		
		return (PBagX<T>)PersistentCollectionX.super.limitUntil(p);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default PBagX<T> intersperse(T value) {
		
		return (PBagX<T>)PersistentCollectionX.super.intersperse(value);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle()
	 */
	@Override
	default PBagX<T> shuffle() {
		
		return (PBagX<T>)PersistentCollectionX.super.shuffle();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
	 */
	@Override
	default PBagX<T> skipLast(int num) {
		
		return (PBagX<T>)PersistentCollectionX.super.skipLast(num);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
	 */
	@Override
	default PBagX<T> limitLast(int num) {
		
		return (PBagX<T>)PersistentCollectionX.super.limitLast(num);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default PBagX<T> onEmpty(T value) {
		
		return (PBagX<T>)PersistentCollectionX.super.onEmpty(value);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default PBagX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (PBagX<T>)PersistentCollectionX.super.onEmptyGet(supplier);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> PBagX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (PBagX<T>)PersistentCollectionX.super.onEmptyThrow(supplier);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default PBagX<T> shuffle(Random random) {
		
		return (PBagX<T>)PersistentCollectionX.super.shuffle(random);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> PBagX<U> ofType(Class<U> type) {
		
		return (PBagX<U>)PersistentCollectionX.super.ofType(type);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default PBagX<T> filterNot(Predicate<? super T> fn) {
		
		return (PBagX<T>)PersistentCollectionX.super.filterNot(fn);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#notNull()
	 */
	@Override
	default PBagX<T> notNull() {
		
		return (PBagX<T>)PersistentCollectionX.super.notNull();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default PBagX<T> removeAll(Stream<T> stream) {
		
		return (PBagX<T>)PersistentCollectionX.super.removeAll(stream);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default PBagX<T> removeAll(Iterable<T> it) {
		
		return (PBagX<T>)PersistentCollectionX.super.removeAll(it);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default PBagX<T> removeAll(T... values) {
		
		return (PBagX<T>)PersistentCollectionX.super.removeAll(values);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default PBagX<T> retainAll(Iterable<T> it) {
		
		return (PBagX<T>)PersistentCollectionX.super.retainAll(it);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default PBagX<T> retainAll(Stream<T> stream) {
		
		return (PBagX<T>)PersistentCollectionX.super.retainAll(stream);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default PBagX<T> retainAll(T... values) {
		
		return (PBagX<T>)PersistentCollectionX.super.retainAll(values);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PBagX<T> retainMatches(Matcher<T> m) {
		
		return (PBagX<T>)PersistentCollectionX.super.retainMatches(m);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PBagX<T> removeMatches(Matcher<T> m) {
		
		return (PBagX<T>)PersistentCollectionX.super.removeMatches(m);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> PBagX<U> cast(Class<U> type) {
		
		return (PBagX<U>)PersistentCollectionX.super.cast(type);
	}


	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> PBagX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (PBagX<R>)PersistentCollectionX.super.patternMatch(defaultValue, case1);
	}


	

}
