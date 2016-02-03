package com.aol.cyclops.collections.extensions.standard;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.lambda.monads.BiFunctor;
import com.aol.cyclops.lambda.monads.Foldable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Traversable;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMCollectable;


public interface MapX<K,V> extends Map<K, V>, BiFunctor<K, V>, Functor<V>, Traversable<Tuple2<K, V>>, Foldable<Tuple2<K,V>>,
												SequenceMCollectable<Tuple2<K,V>>{

	
	default SequenceM<Tuple2<K, V>> stream(){
		return SequenceM.fromIterable(this.entrySet())
					    .map(e->Tuple.tuple(e.getKey(),e.getValue()));
	}
	
	default MapX<K,V> fromStream(SequenceM<Tuple2<K,V>> stream){
		stream.toMap(t->t.v1, t->t.v2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	default Iterator<Tuple2<K, V>> iterator() {
		return stream().iterator();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.SequenceMCollectable#collectable()
	 */
	@Override
	default Collectable<Tuple2<K, V>> collectable() {
		return stream();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <R> MapX<K,R> map(Function<? super V, ? extends R> fn) {
		
		return stream().map(t->t.map2(v->fn.apply(v)))
				.toMap(t->t.v1, t->t.v2);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bimap(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R1, R2> BiFunctor<R1, R2> bimap(Function<? super K, ? extends R1> fn1,
			Function<? super V, ? extends R2> fn2) {
		return fromStream(stream().map(t->t.map1(fn1).map2(fn2)));
	}

	

	@Override
	default int size() {
		return Traversable.super.size();
	}

	@Override
	default boolean allMatch(Predicate<? super Tuple2<K, V>> c) {
		return Traversable.super.allMatch(c);
	}

	@Override
	default boolean anyMatch(Predicate<? super Tuple2<K, V>> c) {
		return Traversable.super.anyMatch(c);
	}

	@Override
	default boolean noneMatch(Predicate<? super Tuple2<K, V>> c) {
		return Traversable.super.noneMatch(c);
	}

	@Override
	default Optional<Tuple2<K, V>> max(Comparator<? super Tuple2<K, V>> comparator) {
		return Traversable.super.max(comparator);
	}

	@Override
	default Optional<Tuple2<K, V>> min(Comparator<? super Tuple2<K, V>> comparator) {
		return Traversable.super.min(comparator);
	}
	

}
