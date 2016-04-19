package com.aol.cyclops.streams.monad.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.control.monads.transformers.seq.XorTSeq;

public class XorTTest {

	String result = null;
	
	@Test
	public void XorAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<XorT<RuntimeException,Integer>, XorT<RuntimeException,Integer>> optTAdd2 = XorT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Xor<RuntimeException,Integer>> streamOpt = stream.map(this::toXor);
		List<Integer> results = optTAdd2.apply(XorT.of(streamOpt))
										.unwrap()
										.<Stream<Xor<?,Integer>>>unwrap()
										.filter(Xor::isPrimary)
										.map(Xor::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	private Xor<RuntimeException,Integer> toXor(Integer b){
		return  b!=null ? Xor.primary(b) : Xor.secondary(new NullPointerException());
	}
	@Test
	public void XorAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<XorT<RuntimeException,Integer>,XorT<RuntimeException,Integer>, XorT<RuntimeException,Integer>> optTAdd2 = XorT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Xor<RuntimeException,Integer>> streamOpt = stream.map(this::toXor);
		
		CompletableFuture<Xor<RuntimeException,Integer>> two = CompletableFuture.completedFuture(Xor.primary(2));
		AnyM<Xor<RuntimeException,Integer>> future=  AnyM.ofValue(two);
		List<Integer> results = optTAdd2.apply(XorT.of(streamOpt),XorT.of(future))
										.unwrap()
										.<Stream<Xor<?,Integer>>>unwrap()
										.filter(Xor::isPrimary)
										.map(Xor::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	
	
	@Test
	public void filterFail(){
		XorT<RuntimeException,Integer> optionT = XorT.of(AnyM.ofSeq(Stream.of(Xor.primary(10))));
		assertThat(optionT.filter(num->num<10).unwrap().<Stream<Xor<RuntimeException,Integer>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Xor.secondary(null)));
	}
	@Test
	public void filterSuccess(){
		XorT<RuntimeException,Integer> optionT = XorT.of(AnyM.fromStream(Stream.of(Xor.primary(10))));
		assertThat(optionT.filter(num->num==10).unwrap().<Stream<Xor<RuntimeException,Integer>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Xor.primary(10)));
	}
	@Test
	public void peek() {
		result = null;
		XorT<RuntimeException,Integer> optionT = XorT.of(AnyM.ofSeq(Stream.of(Xor.primary(10))));
		optionT.peek(num->result = "hello world"+num)
				.unwrap().<Stream<Xor<?,String>>>unwrap().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		XorT<RuntimeException,Integer> optionT = XorT.of(AnyM.ofSeq(Stream.of(Xor.primary(10))));
		assertThat(optionT.map(num->"hello world"+num).unwrap().<Stream<Xor<?,String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Xor.primary("hello world10")));
	}
	@Test
	public void flatMap() {
		XorTSeq<RuntimeException,Integer> optionT = XorT.fromStream(Stream.of(Xor.primary(10)));
		
		assertThat(optionT.flatMapT(num->XorT.fromStream(Stream.of(Xor.primary("hello world"+num))))
				.unwrap().<Stream<Xor<String,RuntimeException>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Xor.primary("hello world10")));
	}

}
