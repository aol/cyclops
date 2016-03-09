package com.aol.cyclops.trycatch;

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
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Try.Failure;
import com.aol.cyclops.control.Try.Success;
import com.aol.cyclops.control.monads.transformers.TryT;
public class TryTTest {

	String result = null;
	
	@Test
	public void tryAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<TryT<Integer,RuntimeException>, TryT<Integer,RuntimeException>> optTAdd2 = TryT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
		List<Integer> results = optTAdd2.apply(TryT.of(streamOpt))
										.unwrap()
										.<Stream<Try<Integer,RuntimeException>>>unwrap()
										.filter(Try::isSuccess)
										.map(Try::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	private Try<Integer,RuntimeException> toTry(Integer b){
		return  b!=null ? Success.of(b) : Failure.of(new NullPointerException());
	}
	@Test
	public void tryAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<TryT<Integer,RuntimeException>,TryT<Integer,RuntimeException>, TryT<Integer,RuntimeException>> optTAdd2 = TryT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
		
		CompletableFuture<Try<Integer,RuntimeException>> two = CompletableFuture.completedFuture(Try.of(2));
		AnyM<Try<Integer,RuntimeException>> future=  AnyM.ofValue(two);
		List<Integer> results = optTAdd2.apply(TryT.of(streamOpt),TryT.of(future))
										.unwrap()
										.<Stream<Try<Integer,RuntimeException>>>unwrap()
										.filter(Try::isSuccess)
										.map(Try::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4)));
		
	}
	
	
	@Test
	public void filterFail(){
		TryT<Integer,RuntimeException> optionT = TryT.of(AnyM.ofValue(Stream.of(Try.of(10))));
		assertThat(optionT.filter(num->num<10).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.none()));
	}
	@Test
	public void filterSuccess(){
		TryT<Integer,RuntimeException> optionT = TryT.of(AnyM.fromStream(Stream.of(Try.of(10))));
		assertThat(optionT.filter(num->num==10).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.of(10)));
	}
	@Test
	public void peek() {
		result = null;
		TryT<Integer,RuntimeException> optionT = TryT.of(AnyM.ofValue(Stream.of(Try.of(10))));
		optionT.peek(num->result = "hello world"+num)
				.unwrap().<Stream<Try<String,RuntimeException>>>unwrap().collect(Collectors.toList());
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		TryT<Integer,RuntimeException> optionT = TryT.of(AnyM.ofSeq(Stream.of(Try.of(10))));
		assertThat(optionT.map(num->"hello world"+num).unwrap().<Stream<Try<String,RuntimeException>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Try.of("hello world10")));
	}
	@Test
	public void flatMap() {
		TryT<Integer,RuntimeException> optionT = TryT.of(AnyM.ofSeq(Stream.of(Try.of(10))));
		
		assertThat(optionT.flatMap(num->TryT.fromAnyM(AnyM.ofSeq(Stream.of("hello world"+num))))
				.unwrap().<Stream<Try<String,RuntimeException>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Try.of("hello world10")));
	}

}
