package com.aol.cyclops.lambda.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
public class OptionTTest {

	String result = null;
	
	@Test
	public void filterFail(){
		OptionalT<Integer> optionT = new OptionalT<>(AnyM.ofMonad(Stream.of(Optional.of(10))));
		assertThat(optionT.filter(num->num<10).getRun().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.empty()));
	}
	@Test
	public void filterSuccess(){
		OptionalT<Integer> optionT = new OptionalT<>(AnyM.ofMonad(Stream.of(Optional.of(10))));
		assertThat(optionT.filter(num->num==10).getRun().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.of(10)));
	}
	@Test
	public void peek() {
		result = null;
		OptionalT<Integer> optionT = new OptionalT<>(AnyM.ofMonad(Stream.of(Optional.of(10))));
		optionT.peek(num->result = "hello world"+num)
				.getRun().<Stream<Optional<String>>>unwrap().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		OptionalT<Integer> optionT = new OptionalT<>(AnyM.ofMonad(Stream.of(Optional.of(10))));
		assertThat(optionT.map(num->"hello world"+num).getRun().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.of("hello world10")));
	}
	@Test
	public void flatMap() {
		OptionalT<Integer> optionT = new OptionalT<>(AnyM.ofMonad(Stream.of(Optional.of(10))));
		
		assertThat(optionT.flatMap(num->OptionalT.fromAnyM(AnyM.ofMonad(Stream.of("hello world"+num))))
						.getRun().<Stream<Optional<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Optional.of("hello world10")));
	}

}
