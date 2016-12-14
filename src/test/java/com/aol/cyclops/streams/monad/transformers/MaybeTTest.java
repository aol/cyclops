package com.aol.cyclops.streams.monad.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
public class MaybeTTest {

	String result = null;
	
	@Test
	public void optionAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
		List<Integer> results = optTAdd2.apply(MaybeT.of(streamOpt))
										.unwrap()
										.<Stream<Maybe<Integer>>>unwrap()
										.filter(Maybe::isPresent)
										.map(Maybe::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	@Test
	public void optionAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<MaybeT<Integer>,MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
		
		CompletableFuture<Maybe<Integer>> two = CompletableFuture.completedFuture(Maybe.of(2));
		AnyM<Maybe<Integer>> future=  AnyM.ofValue(two);
		List<Integer> results = optTAdd2.apply(MaybeT.of(streamOpt),MaybeT.of(future))
										.unwrap()
										.<Stream<Maybe<Integer>>>unwrap()
										.filter(Maybe::isPresent)
										.map(Maybe::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	
	
	@Test
	public void filterFail(){
		MaybeT<Integer> optionT = MaybeT.of(AnyM.ofSeq(Stream.of(Maybe.of(10))));
		assertThat(optionT.filter(num->num<10).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.none()));
	}
	@Test
	public void filterSuccess(){
		MaybeT<Integer> optionT = MaybeT.of(AnyM.fromStream(Stream.of(Maybe.of(10))));
		assertThat(optionT.filter(num->num==10).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.of(10)));
	}
	@Test
	public void peekNull() {
		result = null;
		MaybeT<Integer> optionT = MaybeT.of(AnyM.ofSeq(Stream.of(Maybe.of(10))));
		optionT.peek(num->result = "hello world"+num)
				.unwrap().<Stream<Maybe<Integer>>>unwrap().collect(Collectors.toList());
		assertTrue(result==null); //maybe is lazy
		
	}
	
	@Test
	public void map() {
		MaybeT<Integer> optionT = MaybeT.of(AnyM.ofSeq(Stream.of(Maybe.of(10))));
		assertThat(optionT.map(num->"hello world"+num).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.of("hello world10")));
	}
	@Test
	public void flatMap() {
		MaybeTSeq<Integer> optionT = MaybeT.fromStream(Stream.of(Maybe.of(10)));
		
		assertThat(optionT.flatMapT(num->MaybeT.fromStream(Stream.of(Maybe.just("hello world"+num))))
				.unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.of("hello world10")));
	}

}
