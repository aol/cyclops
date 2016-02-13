package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentMapX;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.BiFunctor;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableCollectable;
import com.aol.cyclops.types.IterableFilterable;
import com.aol.cyclops.types.stream.SequenceMCollectable;

public interface PMapX<K, V> extends PMap<K, V>, 
									 FluentMapX<K,V>,
									 BiFunctor<K, V>, 
									 Functor<V>, 
									 IterableFilterable<Tuple2<K, V>>,				
									 ExtendedTraversable<Tuple2<K, V>>, 
									 Foldable<Tuple2<K,V>>,
									 SequenceMCollectable<Tuple2<K,V>>,
									 IterableCollectable<Tuple2<K,V>>{

	public static<K,V> PMapX<K,V> empty(){
		return new PMapXImpl<K,V>(HashTreePMap.empty());
	}
	public static<K,V> PMapX<K,V> singleton(K key, V value){
		return new PMapXImpl<K,V>(HashTreePMap.singleton(key, value));
	}
	public static<K,V> PMapX<K,V> fromMap(Map<? extends K,? extends V> map){
		return new PMapXImpl<K,V>(HashTreePMap.from(map));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	default Iterator<Tuple2<K, V>> iterator() {
		return stream().iterator();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.SequenceMCollectable#collectable()
	 */
	@Override
	default Collectable<Tuple2<K, V>> collectable() {
		
		return stream();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PMap#plus(java.lang.Object, java.lang.Object)
	 */
	@Override
	PMapX<K, V> plus(K key, V value) ;

	/* (non-Javadoc)
	 * @see org.pcollections.PMap#plusAll(java.util.Map)
	 */
	@Override
	PMapX<K, V> plusAll(Map<? extends K, ? extends V> map);

	/* (non-Javadoc)
	 * @see org.pcollections.PMap#minus(java.lang.Object)
	 */
	@Override
	PMapX<K, V> minus(Object key);

	/* (non-Javadoc)
	 * @see org.pcollections.PMap#minusAll(java.util.Collection)
	 */
	@Override
	PMapX<K, V> minusAll(Collection<?> keys);

	@Override
	default ReactiveSeq<Tuple2<K, V>> stream() {
		// TODO Auto-generated method stub
		return ExtendedTraversable.super.stream();
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <R> PMapX<K,R> map(Function<? super V, ? extends R> fn) {
		return stream().map(t->t.map2(v->fn.apply(v))).toPMapX(t->t.v1, t->t.v2);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bimap(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R1, R2> PMapX<R1, R2> bimap(Function<? super K, ? extends R1> fn1,
			Function<? super V, ? extends R2> fn2) {
		
		return stream().map(t->t.map2(v->fn2.apply(v)).map1(k->fn1.apply(k))).toPMapX(t->t.v1, t->t.v2);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bipeek(java.util.function.Consumer, java.util.function.Consumer)
	 */
	@Override
	default PMapX<K, V> bipeek(Consumer<? super K> c1, Consumer<? super V> c2) {
		// TODO Auto-generated method stub
		return (PMapX<K, V>)BiFunctor.super.bipeek(c1, c2);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bicast(java.lang.Class, java.lang.Class)
	 */
	@Override
	default <U1, U2> PMapX<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {
		// TODO Auto-generated method stub
		return (PMapX<U1, U2>)BiFunctor.super.bicast(type1, type2);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bitrampoline(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R1, R2> PMapX<R1, R2> bitrampoline(Function<? super K, ? extends Trampoline<? extends R1>> mapper1,
			Function<? super V, ? extends Trampoline<? extends R2>> mapper2) {
		
		return (PMapX)BiFunctor.super.bitrampoline(mapper1, mapper2);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> PMapX<K,U> cast(Class<U> type) {
		
		return (PMapX<K,U>)Functor.super.cast(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	@Override
	default PMapX<K,V> peek(Consumer<? super V> c) {
		
		return (PMapX<K,V>)Functor.super.peek(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PMapX<K,R> trampoline(Function<? super V, ? extends Trampoline<? extends R>> mapper) {
		
		return (PMapX<K,R>)Functor.super.trampoline(mapper);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
	 */
	@Override
	default PMapX<K, V> filter(Predicate<? super Tuple2<K, V>> fn) {
		return stream().filter(fn).toPMapX(t->t.v1,t->t.v2);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default PMapX<K, V> filterNot(Predicate<? super Tuple2<K, V>> fn) {
	
		return (PMapX<K, V>)IterableFilterable.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default PMapX<K, V> notNull() {
		
		return (PMapX<K, V>)IterableFilterable.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
	 */
	@Override
	default PMapX<K, V> removeAll(Stream<Tuple2<K, V>> stream) {
		
		return (PMapX<K, V>)IterableFilterable.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Iterable)
	 */
	@Override
	default PMapX<K, V> removeAll(Iterable<Tuple2<K, V>> it) {
		
		return (PMapX<K, V>)IterableFilterable.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Object[])
	 */
	@Override
	default PMapX<K, V> removeAll(Tuple2<K, V>... values) {
		
		return (PMapX<K, V>)IterableFilterable.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Iterable)
	 */
	@Override
	default PMapX<K, V> retainAll(Iterable<Tuple2<K, V>> it) {
		
		return (PMapX<K, V>)IterableFilterable.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.util.stream.Stream)
	 */
	@Override
	default PMapX<K, V> retainAll(Stream<Tuple2<K, V>> stream) {
		
		return (PMapX<K, V>)IterableFilterable.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Object[])
	 */
	@Override
	default PMapX<K, V> retainAll(Tuple2<K, V>... values) {
		
		return (PMapX<K, V>)IterableFilterable.super.retainAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PMapX<K, V> retainMatches(Matcher<Tuple2<K, V>> m) {
		
		return (PMapX<K, V>)IterableFilterable.super.retainMatches(m);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeMatches(org.hamcrest.Matcher)
	 */
	@Override
	default PMapX<K, V> removeMatches(Matcher<Tuple2<K, V>> m) {
	
		return (PMapX<K, V>)IterableFilterable.super.removeMatches(m);
	}

	

	
}
