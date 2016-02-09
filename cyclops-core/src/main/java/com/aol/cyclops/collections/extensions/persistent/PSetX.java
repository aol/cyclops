package com.aol.cyclops.collections.extensions.persistent;

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
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.collections.PSets;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface PSetX<T> extends PSet<T>, PersistentCollectionX<T>{
	
	public static <T> PSetX<T> of(T...values){
		
		return new PSetXImpl<>(HashTreePSet.from(Arrays.asList(values)));
	}
	public static <T> PSetX<T> empty(){
		return new PSetXImpl<>(HashTreePSet .empty());
	}
	public static <T> PSetX<T> singleton(T value){
		return new PSetXImpl<>(HashTreePSet.singleton(value));
	}
	public static<T> PSetX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PSetX)
			return (PSetX)iterable;
		if(iterable instanceof PSet)
			return new PSetXImpl<>((PSet)(iterable));
		PSet<T> res = HashTreePSet.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PSetXImpl<>(res);
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
	default PSetX<T> toPSetX() {
		return this;
	}
	@Override
	default <R> PSetX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (PSetX<R>)PersistentCollectionX.super.ap1(ap);
	}
	@Override
	default<R> PSetX<R> unit(Collection<R> col){
		return fromCollection(col);
	}
	@Override
	default <R> PSetX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default <R> PSetX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
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
	default <T> Reducer<PSet<T>> monoid(){
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
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> PSetX<R> zip(Iterable<U> other,
			BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (PSetX<R>)PersistentCollectionX.super.zip(other, zipper);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#permutations()
	 */
	@Override
	default PSetX<SequenceM<T>> permutations() {
		
		return ( PSetX<SequenceM<T>>)PersistentCollectionX.super.permutations();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations(int)
	 */
	@Override
	default PSetX<SequenceM<T>> combinations(int size) {
		
		return (PSetX<SequenceM<T>>)PersistentCollectionX.super.combinations(size);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations()
	 */
	@Override
	default PSetX<SequenceM<T>> combinations() {
		
		return (PSetX<SequenceM<T>>)PersistentCollectionX.super.combinations();
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
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
	 */
	@Override
	default PSetX<T> plusInOrder(T e) {
		
		return (PSetX<T>)PersistentCollectionX.super.plusInOrder(e);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(int)
	 */
	@Override
	default PSetX<T> cycle(int times) {
		
		return (PSetX<T>)PersistentCollectionX.super.cycle(times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default PSetX<T> cycle(Monoid<T> m, int times) {
		
		return (PSetX<T>)PersistentCollectionX.super.cycle(m, times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (PSetX<T>)PersistentCollectionX.super.cycleWhile(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (PSetX<T>)PersistentCollectionX.super.cycleUntil(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> PSetX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (PSetX<Tuple2<T, U>>)PersistentCollectionX.super.zipStream(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> PSetX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (PSetX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> PSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (PSetX)PersistentCollectionX.super.zip3(second, third);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> PSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (PSetX<Tuple4<T, T2, T3, T4>>)PersistentCollectionX.super.zip4(second, third, fourth);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
	 */
	@Override
	default PSetX<Tuple2<T, Long>> zipWithIndex() {
		
		return (PSetX<Tuple2<T, Long>>)PersistentCollectionX.super.zipWithIndex();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#distinct()
	 */
	@Override
	default PSetX<T> distinct() {
		
		return (PSetX<T>)PersistentCollectionX.super.distinct();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted()
	 */
	@Override
	default PSetX<T> sorted() {
		
		return (PSetX<T>)PersistentCollectionX.super.sorted();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default PSetX<T> sorted(Comparator<? super T> c) {
		
		return (PSetX<T>)PersistentCollectionX.super.sorted(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> skipWhile(Predicate<? super T> p) {
		
		return (PSetX<T>)PersistentCollectionX.super.skipWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> skipUntil(Predicate<? super T> p) {
		
		return (PSetX<T>)PersistentCollectionX.super.skipUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> limitWhile(Predicate<? super T> p) {
		
		return (PSetX<T>)PersistentCollectionX.super.limitWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> limitUntil(Predicate<? super T> p) {
		
		return (PSetX<T>)PersistentCollectionX.super.limitUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default PSetX<T> intersperse(T value) {
		
		return (PSetX<T>)PersistentCollectionX.super.intersperse(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle()
	 */
	@Override
	default PSetX<T> shuffle() {
		
		return (PSetX<T>)PersistentCollectionX.super.shuffle();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
	 */
	@Override
	default PSetX<T> skipLast(int num) {
		
		return (PSetX<T>)PersistentCollectionX.super.skipLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
	 */
	@Override
	default PSetX<T> limitLast(int num) {
		
		return (PSetX<T>)PersistentCollectionX.super.limitLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default PSetX<T> onEmpty(T value) {
		
		return (PSetX<T>)PersistentCollectionX.super.onEmpty(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default PSetX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (PSetX<T>)PersistentCollectionX.super.onEmptyGet(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> PSetX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (PSetX<T>)PersistentCollectionX.super.onEmptyThrow(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default PSetX<T> shuffle(Random random) {
		
		return (PSetX<T>)PersistentCollectionX.super.shuffle(random);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> PSetX<U> ofType(Class<U> type) {
		
		return (PSetX<U>)PersistentCollectionX.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default PSetX<T> filterNot(Predicate<? super T> fn) {
		
		return (PSetX<T>)PersistentCollectionX.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#notNull()
	 */
	@Override
	default PSetX<T> notNull() {
		
		return (PSetX<T>)PersistentCollectionX.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default PSetX<T> removeAll(Stream<T> stream) {
		
		return (PSetX<T>)PersistentCollectionX.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default PSetX<T> removeAll(Iterable<T> it) {
		
		return (PSetX<T>)PersistentCollectionX.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default PSetX<T> removeAll(T... values) {
		
		return (PSetX<T>)PersistentCollectionX.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default PSetX<T> retainAll(Iterable<T> it) {
		
		return (PSetX<T>)PersistentCollectionX.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default PSetX<T> retainAll(Stream<T> stream) {
		
		return (PSetX<T>)PersistentCollectionX.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default PSetX<T> retainAll(T... values) {
		
		return (PSetX<T>)PersistentCollectionX.super.retainAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PSetX<T> retainMatches(Matcher<T> m) {
		
		return (PSetX<T>)PersistentCollectionX.super.retainMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PSetX<T> removeMatches(Matcher<T> m) {
		
		return (PSetX<T>)PersistentCollectionX.super.removeMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> PSetX<U> cast(Class<U> type) {
		
		return (PSetX<U>)PersistentCollectionX.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#matchesCases(com.aol.cyclops.matcher.Case[])
	 */
	@Override
	default <R> PSetX<Optional<R>> matchesCases(Case<T, R, Function<T, R>>... cases) {
		
		return (PSetX<Optional<R>>)PersistentCollectionX.super.matchesCases(cases);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (PSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, case1);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case2) {
		
		return (PSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, case1, case2);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3) {
		
		return (PSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn4) {
		
		return (PSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3, fn4);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> PSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn4,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn5) {
		
		return (PSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3, fn4, fn5);
	}
	
	

}
