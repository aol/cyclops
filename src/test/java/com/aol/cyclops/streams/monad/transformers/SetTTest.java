package com.aol.cyclops.streams.monad.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.SetT;


public class SetTTest {

	String result = null;
	@Test
	public void optionAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.ofValue(Optional.of(nums));
		
		Set<Integer> results = optTAdd2.apply(SetT.fromStream(stream))
										.unwrap()
										.<Optional<Set<Integer>>>unwrap().get();
		
		assertThat(results,equalTo(asSet(3,4)));
		
	}
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<SetT<Integer>,SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift2(add);
		
		Streamable<Integer> threeValues = Streamable.of(1,2,3);
		AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
		AnyM<Set<Integer>> streamOpt = stream.map(this::asSet);
		
		CompletableFuture<Set<Integer>> two = CompletableFuture.completedFuture(asSet(2));
		AnyM<Set<Integer>> future=  AnyM.fromCompletableFuture(two);
		Set<Integer> results = optTAdd2.apply(SetT.of(streamOpt),SetT.of(future))
										.unwrap()
										.<Streamable<Set<Integer>>>unwrap()
										.flatMap(i->Streamable.fromStream(i.stream()))
										.collect(Collectors.toSet());
		
		assertThat(results,equalTo(asSet(3,4,5)));
		
	}
	
	@Test
	public void filterFail(){
		SetT<Integer> streamT = SetT.of(AnyM.fromOptional(Optional.of(asSet(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<Set<String>>>unwrap()
						.get(),  equalTo(asSet()));
	}
	@Test
	public void filterSuccess(){
		SetT<Integer> streamT = SetT.of(AnyM.fromOptional(Optional.of(asSet(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<Set<String>>>unwrap()
						.get(),  equalTo(asSet(10)));
	}
	@Test
	public void peek() {
		result = null;
		SetT<Integer> streamT = SetT.of(AnyM.fromOptional(Optional.of(asSet(10))));
		
		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<Set<String>>>unwrap().get();
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		SetT<Integer> streamT = SetT.of(AnyM.fromOptional(Optional.of(asSet(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<Set<String>>>unwrap()
						.get(),  equalTo(this.asSet("hello world10")));
	}
	private  <T> Set<T> asSet(T... elements){
	   	return new HashSet<T>(Arrays.asList(elements));
   }

}