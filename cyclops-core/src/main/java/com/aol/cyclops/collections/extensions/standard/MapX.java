package com.aol.cyclops.collections.extensions.standard;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.lambda.monads.BiFunctor;
import com.aol.cyclops.lambda.monads.Foldable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Traversable;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMCollectable;
import com.aol.cyclops.streams.StreamUtils;


public interface MapX<K,V> extends Map<K, V>, BiFunctor<K, V>, Functor<V>, Traversable<Tuple2<K, V>>, Foldable<Tuple2<K,V>>,
												SequenceMCollectable<Tuple2<K,V>>{

	static <K,V> Collector<Tuple2<K,V>,?,Map<K,V>> defaultCollector(){
		return Collectors.toMap(t->t.v1, t->t.v2);
	}
	static <K,V> Collector<Tuple2<K,V>,?,Map<K,V>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), Collections::unmodifiableMap);

	}
	static <K,V> Collector<Tuple2<K,V>,?,Map<K,V>> toMapX(){
		return Collectors.collectingAndThen(defaultCollector(), (Map<K,V> d)->new MapXImpl<>(d,defaultCollector()));
		
	}
	public <K,V> Collector<Tuple2<K,V>,?,Map<K,V>> getCollector();
	default SequenceM<Tuple2<K, V>> stream(){
		return SequenceM.fromIterable(this.entrySet())
					    .map(e->Tuple.tuple(e.getKey(),e.getValue()));
	}
	
	public static <K,V> MapX<K,V> fromMap(Map<K,V> it){
		return fromMap(defaultCollector(),it);
	}
	public static <K,V> MapX<K,V> fromMap(Collector<Tuple2<K,V>,?,Map<K,V>>  collector,Map<K,V> it){
		if(it instanceof MapX)
			return (MapX)it;
		if(it instanceof Map)
			return new MapXImpl<K,V>( (Map)it, collector);
		return new MapXImpl<K,V>(StreamUtils.stream(it).map(e->Tuple.tuple(e.getKey(),e.getValue()))
				.collect(collector),collector);
	}
	default MapX<K,V> fromStream(SequenceM<Tuple2<K,V>> stream){
		return new MapXImpl<>(stream.toMap(t->t.v1, t->t.v2),getCollector());
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
	default <KR,VR> MapX<KR,VR> flatMap(BiFunction<? super K,? super V, ? extends MapX<KR,VR>> fn) {
		
		SequenceM<Tuple2<KR,VR>> s = stream().flatMap(t -> fn.apply(t.v1,t.v2).stream());
		return new MapXImpl<>(s.<KR,VR>toMap(t->t.v1, t->t.v2),getCollector());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <R> MapX<K,R> map(Function<? super V, ? extends R> fn) {
		
		SequenceM<Tuple2<K,R>> s = stream().map(t->t.map2(v->fn.apply(v)));
		return new MapXImpl<>(s.<K,R>toMap(t->t.v1, t->t.v2),getCollector());
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bimap(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R1, R2> BiFunctor<R1, R2> bimap(Function<? super K, ? extends R1> fn1,
			Function<? super V, ? extends R2> fn2) {
		SequenceM<Tuple2<R1,V>> s1 = stream().map(t->t.map1(v->fn1.apply(v)));
		SequenceM<Tuple2<R1,R2>> s2 =	s1.map(t->t.map2(v->fn2.apply(v)));
		return new MapXImpl<>(s2.<R1,R2>toMap(t->t.v1, t->t.v2),getCollector());
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
