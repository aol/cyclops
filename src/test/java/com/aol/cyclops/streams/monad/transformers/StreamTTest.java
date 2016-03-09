package com.aol.cyclops.streams.monad.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.util.stream.Streamable;
import com.aol.cyclops.control.AnyM;


public class StreamTTest {

	String result = null;
	@Test
	public void optionAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<StreamT<Integer>, StreamT<Integer>> optTAdd2 = StreamT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.fromOptional(Optional.of(nums));
		
		List<Integer> results = optTAdd2.apply(StreamT.of(stream))
										.unwrap()
										.<Optional<Stream<Integer>>>unwrap()
										.get()
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	
	
	@Test
	public void filterFail(){
		StreamT<Integer> streamT = StreamT.of(AnyM.ofValue(Optional.of(Stream.of(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<Stream<String>>>unwrap()
						.get().collect(Collectors.toList()),  equalTo(Arrays.asList()));
		
		
	}
	@Test
	public void filterSuccess(){
		StreamT<Integer> streamT = StreamT.of(AnyM.ofValue(Optional.of(Stream.of(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<Stream<String>>>unwrap()
						.get().collect(Collectors.toList()),  equalTo(Arrays.asList(10)));
	}
	@Test
	public void peek() {
		result = null;
		StreamT<Integer> streamT = StreamT.of(AnyM.ofValue(Optional.of(Stream.of(10))));
		
		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<Stream<String>>>unwrap().get().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		StreamT<Integer> streamT = StreamT.of(AnyM.ofValue(Optional.of(Stream.of(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<Stream<String>>>unwrap()
						.get().collect(Collectors.toList()),  equalTo(Arrays.asList("hello world10")));
	}
	

}