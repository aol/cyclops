package com.aol.cyclops.sequence;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.IterableFilterable;

public interface JoolManipulation<T> extends IterableFilterable<T>, Seq<T>{
	
	default SequenceM<T> removeAll(Stream<T> stream){
		return (SequenceM<T>)(IterableFilterable.super.removeAll(stream));
	}
	default  SequenceM<T> removeAll(Iterable<T> it){
		return (SequenceM<T>)(IterableFilterable.super.removeAll(it));
	}
	default  SequenceM<T> removeAll(Seq<T> seq){
		return (SequenceM<T>)(IterableFilterable.super.removeAll((Stream)seq));
	}
	default  SequenceM<T> removeAll(T... values){
		return (SequenceM<T>)(IterableFilterable.super.removeAll(values));
		
	}
	default  SequenceM<T> retainAll(Iterable<T> it){
		return (SequenceM<T>)(IterableFilterable.super.retainAll(it));
	}
	default  SequenceM<T> retainAll(Seq<T> seq){
		return (SequenceM<T>)(IterableFilterable.super.retainAll((Stream)seq));
	}
	default  SequenceM<T> retainAll(Stream<T> stream){
		return (SequenceM<T>)(IterableFilterable.super.retainAll(stream));
	}
	default  SequenceM<T> retainAll(T... values){
		return (SequenceM<T>)(IterableFilterable.super.retainAll(values));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default SequenceM<T> filterNot(Predicate<? super T> fn) {
		
		return (SequenceM<T>)IterableFilterable.super.filterNot(fn);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default SequenceM<T> notNull() {
		
		return (SequenceM<T>)IterableFilterable.super.notNull();
	}
	@Override
	default <U> SequenceM<U> ofType(Class<U> type) {
		
		return (SequenceM<U>)IterableFilterable.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
	 */
	@Override
	SequenceM<T> filter(Predicate<? super T> fn) ;
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
	 */
	@Override
	default void forEachOrdered(Consumer<? super T> action) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray()
	 */
	@Override
	default Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
	 */
	@Override
	default <A> A[] toArray(IntFunction<A[]> generator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
	 */
	@Override
	default T reduce(T identity, BinaryOperator<T> accumulator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.util.function.BinaryOperator)
	 */
	@Override
	default Optional<T> reduce(BinaryOperator<T> accumulator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	@Override
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#collect(java.util.function.Supplier, java.util.function.BiConsumer, java.util.function.BiConsumer)
	 */
	@Override
	default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#collect(java.util.stream.Collector)
	 */
	@Override
	default <R, A> R collect(Collector<? super T, A, R> collector) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#min(java.util.Comparator)
	 */
	@Override
	default Optional<T> min(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#max(java.util.Comparator)
	 */
	@Override
	default Optional<T> max(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean anyMatch(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean allMatch(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#noneMatch(java.util.function.Predicate)
	 */
	@Override
	default boolean noneMatch(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#findFirst()
	 */
	@Override
	default Optional<T> findFirst() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.Stream#findAny()
	 */
	@Override
	default Optional<T> findAny() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#iterator()
	 */
	@Override
	default Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see java.util.stream.BaseStream#isParallel()
	 */
	@Override
	default boolean isParallel() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
