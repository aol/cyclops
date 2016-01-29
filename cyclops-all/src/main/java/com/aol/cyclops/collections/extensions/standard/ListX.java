package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.FluentSequenceX;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface ListX<T> extends List<T>, MutableCollectionX<T>, MutableSequenceX<T> {
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	public <T> Collector<T,?,List<T>> getCollector();
	
	default <T1> ListX<T1> from(Collection<T1> c){
		return new ListXImpl<T1>(c.stream().collect(getCollector()),getCollector());
	}
	
	default <X> ListX<X> fromStream(Stream<X> stream){
		return new ListXImpl<>(stream.collect(getCollector()),getCollector());
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	default ListX<T> reverse() {
		
		return (ListX)MutableCollectionX.super.reverse();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> filter(Predicate<? super T> pred) {
		
		return (ListX)MutableCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (ListX)MutableCollectionX.super.map(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
	
		return (ListX)MutableCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	default ListX<T> limit(long num) {
		
		return (ListX)MutableCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	default ListX<T> skip(long num) {
		
		return (ListX)MutableCollectionX.super.skip(num);
	}
	default  ListX<T> takeRight(int num){
		return (ListX)MutableCollectionX.super.takeRight(num);
	}
	default  ListX<T> dropRight(int num){
		return  (ListX)MutableCollectionX.super.dropRight(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> takeWhile(Predicate<? super T> p) {
		
		return (ListX)MutableCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> dropWhile(Predicate<? super T> p) {
		
		return (ListX)MutableCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> takeUntil(Predicate<? super T> p) {
		
		return (ListX)MutableCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default ListX<T> dropUntil(Predicate<? super T> p) {
		return (ListX)MutableCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> ListX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		return (ListX)MutableCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	default ListX<T> slice(long from, long to) {
		return (ListX)MutableCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> ListX<T> sorted(Function<? super T, ? extends U> function) {
		
		return (ListX)MutableCollectionX.super.sorted(function);
	}
	
	/* Makes a defensive copy of this ListX replacing the value at i with the specified element
	 *  (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
	 */
	default ListX<T> with(int i,T element){
		return from(stream().deleteBetween(i, i+1).insertAt(i,element).collect(getCollector()));
	}
	public ListX<T> subList(int start, int end);
	default ListX<T> plus(T e){
		add(e);
		return this;
	}
	
	default ListX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	default ListX<T> minus(int pos){
		remove(pos);
		return this;
	}
	
	default ListX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default ListX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
	default ListX<T> plus(int i, T e){
		add(i,e);
		return this;
	}
	
	
	default ListX<T> plusAll(int i, Collection<? extends T> list){
		addAll(i,list);
		return this;
	}
}
