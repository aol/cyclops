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
import org.pcollections.OrderedPSet;
import org.pcollections.PBag;
import org.pcollections.POrderedSet;

import com.aol.cyclops.collections.PBags;
import com.aol.cyclops.collections.POrderedSets;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicative;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface POrderedSetX<T> extends POrderedSet<T>, PersistentCollectionX<T>{
	
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
	default <R> POrderedSetX<R> ap1( ZippingApplicative<T,R, ?> ap){
		
		return (POrderedSetX<R>)PersistentCollectionX.super.ap1(ap);
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
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#plusInOrder(java.lang.Object)
	 */
	@Override
	default POrderedSetX<T> plusInOrder(T e) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.plusInOrder(e);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(int)
	 */
	@Override
	default POrderedSetX<T> cycle(int times) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.cycle(times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default POrderedSetX<T> cycle(Monoid<T> m, int times) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.cycle(m, times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.cycleWhile(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.cycleUntil(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> POrderedSetX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (POrderedSetX<Tuple2<T, U>>)PersistentCollectionX.super.zipStream(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> POrderedSetX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (POrderedSetX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> POrderedSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (POrderedSetX)PersistentCollectionX.super.zip3(second, third);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> POrderedSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (POrderedSetX<Tuple4<T, T2, T3, T4>>)PersistentCollectionX.super.zip4(second, third, fourth);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
	 */
	@Override
	default POrderedSetX<Tuple2<T, Long>> zipWithIndex() {
		
		return (POrderedSetX<Tuple2<T, Long>>)PersistentCollectionX.super.zipWithIndex();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#distinct()
	 */
	@Override
	default POrderedSetX<T> distinct() {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.distinct();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted()
	 */
	@Override
	default POrderedSetX<T> sorted() {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.sorted();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default POrderedSetX<T> sorted(Comparator<? super T> c) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.sorted(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> skipWhile(Predicate<? super T> p) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.skipWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> skipUntil(Predicate<? super T> p) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.skipUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> limitWhile(Predicate<? super T> p) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.limitWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> limitUntil(Predicate<? super T> p) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.limitUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default POrderedSetX<T> intersperse(T value) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.intersperse(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle()
	 */
	@Override
	default POrderedSetX<T> shuffle() {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.shuffle();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
	 */
	@Override
	default POrderedSetX<T> skipLast(int num) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.skipLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
	 */
	@Override
	default POrderedSetX<T> limitLast(int num) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.limitLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default POrderedSetX<T> onEmpty(T value) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.onEmpty(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default POrderedSetX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.onEmptyGet(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> POrderedSetX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.onEmptyThrow(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default POrderedSetX<T> shuffle(Random random) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.shuffle(random);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> POrderedSetX<U> ofType(Class<U> type) {
		
		return (POrderedSetX<U>)PersistentCollectionX.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default POrderedSetX<T> filterNot(Predicate<? super T> fn) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#notNull()
	 */
	@Override
	default POrderedSetX<T> notNull() {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default POrderedSetX<T> removeAll(Stream<T> stream) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default POrderedSetX<T> removeAll(Iterable<T> it) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default POrderedSetX<T> removeAll(T... values) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default POrderedSetX<T> retainAll(Iterable<T> it) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default POrderedSetX<T> retainAll(Stream<T> stream) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default POrderedSetX<T> retainAll(T... values) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.retainAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default POrderedSetX<T> retainMatches(Matcher<T> m) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.retainMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default POrderedSetX<T> removeMatches(Matcher<T> m) {
		
		return (POrderedSetX<T>)PersistentCollectionX.super.removeMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> POrderedSetX<U> cast(Class<U> type) {
		
		return (POrderedSetX<U>)PersistentCollectionX.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#matchesCases(com.aol.cyclops.matcher.Case[])
	 */
	@Override
	default <R> POrderedSetX<Optional<R>> matchesCases(Case<T, R, Function<T, R>>... cases) {
		
		return (POrderedSetX<Optional<R>>)PersistentCollectionX.super.matchesCases(cases);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (POrderedSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, case1);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case2) {
		
		return (POrderedSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, case1, case2);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3) {
		
		return (POrderedSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn4) {
		
		return (POrderedSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3, fn4);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R> POrderedSetX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn1,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn2,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn3,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn4,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> fn5) {
		
		return (POrderedSetX<R>)PersistentCollectionX.super.patternMatch(defaultValue, fn1, fn2, fn3, fn4, fn5);
	}
	

}
