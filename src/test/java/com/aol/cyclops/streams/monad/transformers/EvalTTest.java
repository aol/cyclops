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
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;

public class EvalTTest {

	String result = null;
	
	@Test
	public void EvalAndStream(){
		Function<Integer,Integer> add2 = i -> i+2;
		Function<EvalT<Integer>, EvalT<Integer>> optTAdd2 = EvalT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofValue(withNulls);
		AnyM<Eval<Integer>> streamOpt = stream.map(this::toEval);
		List<Integer> results = optTAdd2.apply(EvalT.of(streamOpt))
										.unwrap()
										.<Stream<Eval<Integer>>>unwrap()
									//	.filter(Eval::isSuccess)
										.map(Eval::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	private Eval<Integer> toEval(Integer b){
		return Eval.now(b);
	}
	@Test
	public void EvalAndStreamAndFuture(){
		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<EvalT<Integer>,EvalT<Integer>, EvalT<Integer>> optTAdd2 = EvalT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofSeq(withNulls);
		AnyM<Eval<Integer>> streamOpt = stream.map(this::toEval);
		
		CompletableFuture<Eval<Integer>> two = CompletableFuture.completedFuture(Eval.now(2));
		AnyM<Eval<Integer>> future=  AnyM.ofValue(two);
		List<Integer> results = optTAdd2.apply(EvalT.of(streamOpt),EvalT.of(future))
										.unwrap()
										.<Stream<Eval<Integer>>>unwrap()
										//.filter(Eval::isSuccess)
										.map(Eval::get)
										.collect(Collectors.toList());
		
		assertThat(results,equalTo(Arrays.asList(3,4,5)));
		
	}
	
	
	@Test
	public void filterFail(){
		EvalT<Integer> optionT = EvalT.of(AnyM.ofSeq(Stream.of(Eval.now(10))));
		assertThat(optionT.filter(num->num<10).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.none()));
	}
	@Test
	public void filterSuccess(){
		EvalT<Integer> optionT = EvalT.of(AnyM.fromStream(Stream.of(Eval.now(10))));
		assertThat(optionT.filter(num->num==10).unwrap().<Stream<Maybe<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Maybe.of(10)));
	}
	@Test
	public void peek() {

		result = null;
		EvalT<Integer> optionT = EvalT.of(AnyM.ofSeq(Stream.of(Eval.now(10))));
		optionT.peek(num->result = "hello world"+num).map(i->i+2)
				.unwrap().<Stream<Eval<String>>>unwrap().forEach(System.out::println);
		assertThat(result,  equalTo("hello world10"));
	}
	@Test
	public void map() {
		EvalT<Integer> optionT = EvalT.of(AnyM.ofSeq(Stream.of(Eval.now(10))));
		assertThat(optionT.map(num->"hello world"+num).unwrap().<Stream<Eval<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Eval.now("hello world10")));
	}
	@Test
	public void flatMap() {
		EvalTSeq<Integer> optionT = EvalT.fromStream(Stream.of(Eval.now(10)));
		
		assertThat(optionT.flatMapT(num->EvalT.fromStream(Stream.of(Eval.now("hello world"+num))))
				.unwrap()
				.<Stream<Eval<String>>>unwrap()
						.collect(Collectors.toList()).get(0),  equalTo(Eval.now("hello world10")));
	}

}
