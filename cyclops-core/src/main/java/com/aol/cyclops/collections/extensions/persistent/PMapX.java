package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PMap;

import com.aol.cyclops.collections.extensions.FluentMapX;
import com.aol.cyclops.lambda.monads.BiFunctor;
import com.aol.cyclops.lambda.monads.Foldable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.IterableCollectable;
import com.aol.cyclops.lambda.monads.Traversable;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.traits.SequenceMCollectable;
import com.aol.cyclops.trampoline.Trampoline;

public interface PMapX<K, V> extends PMap<K, V>, FluentMapX<K,V>,BiFunctor<K, V>, Functor<V>, 
													Traversable<Tuple2<K, V>>, Foldable<Tuple2<K,V>>,
													SequenceMCollectable<Tuple2<K,V>>,
													IterableCollectable<Tuple2<K,V>>{

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
	default SequenceM<Tuple2<K, V>> stream() {
		// TODO Auto-generated method stub
		return Traversable.super.stream();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.BiFunctor#bimap(java.util.function.Function, java.util.function.Function)
	 */
	@Override
	default <R1, R2> PMapX<R1, R2> bimap(Function<? super K, ? extends R1> fn1,
			Function<? super V, ? extends R2> fn2) {
		// TODO Auto-generated method stub
		return null;
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


	

	
}
