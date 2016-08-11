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

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.util.stream.Streamable;

public class StreamTTest {

	@Test
	public void flatMap() {
		StreamableTValue<Integer> streamT = StreamableT.fromOptional(Optional.of(Streamable.of(10)));
		
		
		assertThat(streamT.flatMapT(num->StreamableT.fromAnyMValue(AnyM.ofValue(Stream.of("hello world"+num))))
						.unwrap()
						.<Optional<Streamable<String>>>unwrap()
						.get()
						.collect(Collectors.toList()),  equalTo(Arrays.asList("hello world10")));
						
	}
	
	
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<StreamableT<Integer>,StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift2(add);
		
		Streamable<Integer> nums = Streamable.of(1,2);
		AnyM<Streamable<Integer>> stream = AnyM.fromOptional(Optional.of(nums));
		
		CompletableFuture<Streamable<Integer>> two = CompletableFuture.completedFuture(Streamable.of(2));
		AnyM<Streamable<Integer>> future=  AnyM.fromCompletableFuture(two);
		List<Integer> results = optTAdd2.apply(StreamableT.of(stream),StreamableT.of(future))
										.unwrap()
										.<Optional<Streamable<Integer>>>unwrap()
										.get()
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	
}