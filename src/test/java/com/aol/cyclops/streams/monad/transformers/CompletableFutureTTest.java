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

import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.AnyM;
public class CompletableFutureTTest {

	String result = null;
	
	@Test
	public void futureAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<CompletableFutureT<Integer>, CompletableFutureT<Integer>> optTAdd2 = CompletableFutureT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.fromStream(withNulls);
		AnyM<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
		List<Integer> results = optTAdd2.apply(CompletableFutureT.of(streamOpt))
										.unwrap()
										.<Stream<CompletableFuture<Integer>>>unwrap()
										.map(CompletableFuture::join)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	@Test
	public void futureAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<CompletableFutureT<Integer>,CompletableFutureT<Integer>,CompletableFutureT<Integer>> optTAdd2 = CompletableFutureT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
		
		CompletableFuture<CompletableFuture<Integer>> two = CompletableFuture.completedFuture(CompletableFuture.completedFuture(2));
		AnyM<CompletableFuture<Integer>> future=  AnyM.fromCompletableFuture(two);
		List<Integer> results = optTAdd2.apply(CompletableFutureT.of(streamOpt),CompletableFutureT.of(future))
										.unwrap()
										.<Stream<CompletableFuture<Integer>>>unwrap()
										.map(CompletableFuture::join)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	
	
	
	@Test
	public void peek() {
		result = null;
		CompletableFutureT<Integer> optionT = CompletableFutureT.of(AnyM.fromStream(Stream.of(CompletableFuture.completedFuture(10))));
		optionT.peek(num->result = "hello world"+num)
				.unwrap().<Stream<Optional<String>>>unwrap().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		CompletableFutureT<Integer> optionT = CompletableFutureT.of(AnyM.fromStream(Stream.of(CompletableFuture.completedFuture(10))));
		assertThat(optionT.map(num->"hello world"+num).unwrap().<Stream<CompletableFuture<String>>>unwrap()
						.collect(Collectors.toList()).get(0).join(),  equalTo("hello world10"));
	}
	@Test
	public void flatMap() {
		CompletableFutureT<Integer> optionT = CompletableFutureT.of(AnyM.fromStream(Stream.of(CompletableFuture.completedFuture(10))));
		
		assertThat(optionT.flatMap(num->CompletableFutureT.fromAnyM(AnyM.fromStream(Stream.of("hello world"+num))))
				.unwrap().<Stream<CompletableFuture<String>>>unwrap()
						.collect(Collectors.toList()).get(0).join(),  equalTo("hello world10"));
	}

}
