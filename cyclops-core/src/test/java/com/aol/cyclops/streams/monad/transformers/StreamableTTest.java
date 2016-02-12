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
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.util.Streamable;


public class StreamableTTest {

	String result = null;
	@Test
	public void optionAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.ofMonad(Optional.of(nums));
		
		List<Integer> results = optTAdd2.apply(StreamableT.fromStream(stream))
										.unwrap()
										.<Optional<Streamable<Integer>>>unwrap()
										.get()
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<StreamableT<Integer>,StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift2(add);
		
		Streamable<Integer> threeValues = Streamable.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofMonad(threeValues);
		AnyM<Streamable<Integer>> streamOpt = stream.map(Streamable::of);
		
		CompletableFuture<Streamable<Integer>> two = CompletableFuture.completedFuture(Streamable.of(2));
		AnyM<Streamable<Integer>> future=  AnyM.fromCompletableFuture(two);
		List<Integer> results = optTAdd2.apply(StreamableT.of(streamOpt),StreamableT.of(future))
										.unwrap()
										.<Stream<Streamable<Integer>>>unwrap()
										.flatMap(i->i.sequenceM())
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	
	@Test
	public void filterFail(){
		StreamableT<Integer> streamT = StreamableT.of(AnyM.ofMonad(Optional.of(Streamable.of(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<Streamable<String>>>unwrap()
						.get().collect(Collectors.toList()),  equalTo(Arrays.asList()));
	}
	@Test
	public void filterSuccess(){
		StreamableT<Integer> streamT = StreamableT.of(AnyM.ofMonad(Optional.of(Streamable.of(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<Streamable<String>>>unwrap()
						.get().collect(Collectors.toList()),  equalTo(Arrays.asList(10)));
	}
	@Test
	public void peek() {
		result = null;
		StreamableT<Integer> streamT = StreamableT.of(AnyM.ofMonad(Optional.of(Streamable.of(10))));
		
		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<Streamable<String>>>unwrap().get().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		StreamableT<Integer> streamT = StreamableT.of(AnyM.ofMonad(Optional.of(Streamable.of(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<Streamable<String>>>unwrap()
						.get().collect(Collectors.toList()),  equalTo(Arrays.asList("hello world10")));
	}
	

}