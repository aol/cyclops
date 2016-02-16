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
import com.aol.cyclops.control.AnyM;
public class OptionTTest {

	String result = null;
	
	@Test
	public void optionAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyM<Integer> stream = AnyM.ofMonad(withNulls);
		AnyM<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt))
										.unwrap()
										.<Stream<Optional<Integer>>>unwrap()
										.filter(Optional::isPresent)
										.map(Optional::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<OptionalT<Integer>,OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyM<Integer> stream = AnyM.ofMonad(withNulls);
		AnyM<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		
		CompletableFuture<Optional<Integer>> two = CompletableFuture.completedFuture(Optional.of(2));
		AnyM<Optional<Integer>> future=  AnyM.ofMonad(two);
		List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt),OptionalT.of(future))
										.unwrap()
										.<Stream<Optional<Integer>>>unwrap()
										.filter(Optional::isPresent)
										.map(Optional::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	
	
	@Test
	public void filterFail(){
		OptionalT<Integer> optionT = OptionalT.of(AnyM.ofMonad(Stream.of(Optional.of(10))));
		assertThat(optionT.filter(num->num<10).unwrap().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.empty()));
	}
	@Test
	public void filterSuccess(){
		OptionalT<Integer> optionT = OptionalT.of(AnyM.fromStream(Stream.of(Optional.of(10))));
		assertThat(optionT.filter(num->num==10).unwrap().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.of(10)));
	}
	@Test
	public void peek() {
		result = null;
		OptionalT<Integer> optionT = OptionalT.of(AnyM.ofMonad(Stream.of(Optional.of(10))));
		optionT.peek(num->result = "hello world"+num)
				.unwrap().<Stream<Optional<String>>>unwrap().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		OptionalT<Integer> optionT = OptionalT.of(AnyM.ofMonad(Stream.of(Optional.of(10))));
		assertThat(optionT.map(num->"hello world"+num).unwrap().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.of("hello world10")));
	}
	@Test
	public void flatMap() {
		OptionalT<Integer> optionT = OptionalT.of(AnyM.ofMonad(Stream.of(Optional.of(10))));
		
		assertThat(optionT.flatMap(num->OptionalT.fromAnyM(AnyM.ofMonad(Stream.of("hello world"+num))))
				.unwrap().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.of("hello world10")));
	}

}
