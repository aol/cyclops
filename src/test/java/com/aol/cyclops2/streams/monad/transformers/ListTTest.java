package com.aol.cyclops2.streams.monad.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.aol.cyclops2.data.collections.extensions.IndexedSequenceX;
import cyclops.Converters;
import cyclops.monads.Witness;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.monads.transformers.ListT;


public class ListTTest {

	String result = null;

	
	@Test
	public void filterFail(){
		ListT<Witness.optional,Integer> streamT = ListT.ofList(AnyM.fromOptional(Optional.of(Arrays.asList(10))));
		assertThat(streamT.filter(num->num<10).unwrap().<Optional<List<String>>>unwrap()
						.get(),  equalTo(Arrays.asList()));

        AnyM<Witness.optional, IndexedSequenceX<Integer>> anyM = streamT.unwrap();
        Optional<IndexedSequenceX<Integer>> opt = Witness.optional(anyM);
        Optional<LinkedList<Integer>> list = opt.map(s -> s.toX(Converters::LinkedList));

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
				.unwrap().<Optional<List<String>>>unwrap().get().get(0);
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