package com.aol.cyclops.streams.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.lambda.monads.transformers.StreamT;
import com.aol.cyclops.monad.AnyM;

public class StreamTTest {

	@Test
	public void flatMap() {
		StreamT<Integer> streamT = new StreamT<>(AnyM.ofMonad(Optional.of(Stream.of(10))));
		
		
		assertThat(streamT.flatMap(num->StreamT.fromAnyM(AnyM.ofMonad(Stream.of("hello world"+num))))
						.getRun()
						.<Optional<Stream<String>>>unwrap()
						.get()
						.collect(Collectors.toList()),  equalTo(Arrays.asList("hello world10")));
						
	}
	
	/** Streamable only
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<StreamT<Integer>,StreamT<Integer>, StreamT<Integer>> optTAdd2 = StreamT.lift2(add);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.ofMonad(Optional.of(nums));
		
		CompletableFuture<Stream<Integer>> two = CompletableFuture.completedFuture(Stream.of(2));
		AnyM<Stream<Integer>> future=  AnyM.ofMonad(two);
		List<Integer> results = optTAdd2.apply(StreamT.of(stream),StreamT.of(future))
										.getRun()
										.<Optional<Stream<Integer>>>unwrap()
										.get()
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	**/
}
