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

import com.aol.cyclops.types.anyM.Witness;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.ListT;


public class ListTTest {

	String result = null;

	
	@Test
	public void filterFail(){
		ListT<Witness.optional,Integer> streamT = ListT.ofList(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList()));
	}
	@Test
	public void filterSuccess(){
		ListT<Witness.optional,Integer> streamT = ListT.ofList(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.filter(num->num==10).unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList(10)));
	}
	@Test
	public void peek() {
		result = null;
		ListT<Witness.optional,Integer> streamT = ListT.ofList(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		
		streamT.peek(num->result = "hello world"+num)
				.unwrap().<Optional<List<String>>>unwrap().get();
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		ListT<Witness.optional,Integer> streamT = ListT.ofList(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.map(num->"hello world"+num)
						.unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList("hello world10")));
	}
	

}