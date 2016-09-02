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

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.ListT;


public class ListTTest {

	String result = null;
	@Test
	public void optionAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<ListT<Integer>, ListT<Integer>> optTAdd2 = ListT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.ofSeq(Optional.of(nums));
		
		List<Integer> results = optTAdd2.apply(ListT.fromStreamAnyM(stream))
										.unwrap()
										.<Optional<List<Integer>>>unwrap().get();
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<ListT<Integer>,ListT<Integer>, ListT<Integer>> optTAdd2 = ListT.lift2(add);
		
		Streamable<Integer> threeValues = Streamable.of(1,2,3);
		AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
		AnyM<List<Integer>> streamOpt = stream.map(Arrays::asList);
		
		CompletableFuture<List<Integer>> two = CompletableFuture.completedFuture(Arrays.asList(2));
		AnyM<List<Integer>> future=  AnyM.fromCompletableFuture(two);
		
			
		List<Integer> results = optTAdd2.apply(ListT.of(streamOpt),ListT.of(future))
										.unwrap()
										.<Streamable<List<Integer>>>unwrap()
										.flatMap(i->Streamable.fromStream(i.stream()))
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	
	@Test
	public void filterFail(){
		ListT<Integer> streamT = ListT.of(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList()));
	}
	@Test
	public void filterSuccess(){
		ListT<Integer> streamT = ListT.of(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList(10)));
	}
	@Test
	public void peek() {
		result = null;
		ListT<Integer> streamT = ListT.of(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		
		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<List<String>>>unwrap().get();
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		ListT<Integer> streamT = ListT.of(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList("hello world10")));
	}
	

}