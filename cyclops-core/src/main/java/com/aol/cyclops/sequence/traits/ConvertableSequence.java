package com.aol.cyclops.sequence.traits;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.PMapX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.sequence.CyclopsCollectors;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.types.Value;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public interface ConvertableSequence<T> extends Iterable<T>{
	
	default SequenceM<T> stream(){
		return SequenceM.fromIterable(this);
	}
	
	default LazyFutureStream<T> toFutureStream(LazyReact reactor){
		return reactor.fromIterable(this);
	}
	default LazyFutureStream<T> toFutureStream(){
		return new LazyReact().fromIterable(this);
	}
	default SimpleReactStream<T> toSimpleReact(SimpleReact reactor){
		return reactor.fromIterable(this);
	}
	default SimpleReactStream<T> toSimpleReact(){
		return new SimpleReact().fromIterable(this);
	}
	default Streamable<T> toStreamable(){
		return stream().toStreamable();
	}
	default DequeX<T> toDequeX(){
		return DequeX.fromIterable(this);
	}
	default QueueX<T> toQueueX(){
		return QueueX.fromIterable(this);
	}
	default SetX<T> toSetX(){
		return SetX.fromIterable(this);
		
	}
	default SortedSetX<T> toSortedSetX(){
		return SortedSetX.fromIterable(this);
	}
	default ListX<T> toListX(){
		return ListX.fromIterable(this);
	}
	default <K,V> PMapX<K,V> toPMapX(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper){
		
		SequenceM<Tuple2<K,V>> stream = stream().map(t-> Tuple.tuple(keyMapper.apply(t),valueMapper.apply(t)));
		return stream.mapReduce(Reducers.toPMapX());		
	}
	default <K, V> MapX<K, V> toMapX(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper){
		return MapX.fromMap(stream().toMap(keyMapper,valueMapper));
	}
	default PStackX<T> toPStackX(){
		return PStackX.fromIterable(this);
	}
	default PVectorX<T> toPVectorX(){
		return PVectorX.fromIterable(this);
	}
	default PQueueX<T> toPQueueX(){
		return PQueueX.fromIterable(this);
	}
	default PBagX<T> toPBagX(){
		return PBagX.fromIterable(this);
	}
	default PSetX<T> toPSetX(){
		return PSetX.fromIterable(this);
	}
	default POrderedSetX<T> toPOrderedSetX(){
		return POrderedSetX.fromIterable(this);
	}
	/**
	 *  
	 * <pre>
	 * {@code 
	 * Optional<List<String>> stream = anyM("hello","world")
											.asSequence()
											.unwrapOptional();
		assertThat(stream.get(),equalTo(Arrays.asList("hello","world")));
	 * }
	 * 
	 * </pre>
	 * @return
	 */
	default Optional<ListX<T>> toOptional(){
		ListX<T> list = toListX();
		if(list.size()==0)
			return Optional.empty();
		return Optional.of(list);
	}
	default Value<ListX<T>> toValue(){
		return ()-> ListX.fromIterable(StreamUtils.stream(this).collect(Collectors.toList()));
	}
	default Value<SetX<T>> toValueSet(){
		return ()-> SetX.fromIterable(StreamUtils.stream(this).collect(Collectors.toSet()));
	}
	default <K,V> Value<MapX<K,V>> toValueMap(Function<? super T, ? extends K> keyMapper,
								Function<? super T, ? extends V> valueMapper){
		return ()-> MapX.fromMap(StreamUtils.stream(this).collect(Collectors.toMap(keyMapper,valueMapper)));
	}
	default Maybe<ListX<T>> toMaybe(){
		return Maybe.fromOptional(toOptional());
	}
	default <ST> Xor<ST,ListX<T>> toXor(){
		return toValue().toXor();
	}
	default <PT> Xor<ListX<T>,PT> toXorSecondary(){
		return toValue().toXorSecondary();
	}
	default  Try<ListX<T>,NoSuchElementException> toTry(){
		return toValue().toTry();
	}
	default <ST> Ior<ST,ListX<T>> toIor(){
		return toValue().toIor();
	}
	default <PT> Ior<ListX<T>,PT> toIorSecondary(){
		return toValue().toIorSecondary();
	}
	default Eval<ListX<T>> toEvalNow(){
		return toValue().toEvalNow();
	}
	default Eval<ListX<T>> toEvalLater(){
		return toValue().toEvalLater();
	}
	default Eval<ListX<T>> toEvalAlways(){
		return toValue().toEvalAlways();
	}
	/**
	 * <pre>
	 * {@code 
	 * CompletableFuture<List<String>> cf = anyM("hello","world")
											.asSequence()
											.unwrapCompletableFuture();
		assertThat(cf.join(),equalTo(Arrays.asList("hello","world")));
	 * }
	 * </pre>
	 * @return
	 */
	default CompletableFuture<ListX<T>> toCompletableFuture(){
		return toValue().toCompletableFuture();
	}
	default FutureW<ListX<T>> toFutureW(){
		return FutureW.of(toCompletableFuture());
	}
}
