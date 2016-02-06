package com.aol.cyclops.sequence.traits;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.collections.extensions.standard.MapX;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trycatch.Try;
import com.aol.cyclops.value.Value;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public interface ConvertableSequence<T> extends Iterable<T>{
	
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
		return toValue().toOptional();
	}
	default Value<ListX<T>> toValue(){
		return ()-> ListX.fromIterable(StreamUtils.stream(this).collect(Collectors.toList()));
	}
	default Value<ListX<T>> toValueSet(){
		return ()-> ListX.fromIterable(StreamUtils.stream(this).collect(Collectors.toSet()));
	}
	default <K,V> Value<MapX<K,V>> toValueMap(Function<? super T, ? extends K> keyMapper,
								Function<? super T, ? extends V> valueMapper){
		return ()-> MapX.fromMap(StreamUtils.stream(this).collect(Collectors.toMap(keyMapper,valueMapper)));
	}
	default Maybe<ListX<T>> toMaybe(){
		return toValue().toMaybe();
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
}
