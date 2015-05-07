package com.aol.cyclops.comprehensions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Test;

public class OptionalTest {

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forComprehension(){
		Optional<Integer> two = Optional.of(2);
		Optional<Integer> three = Optional.of(3);
		Optional<Integer> four = Optional.of(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	
		Optional<Integer> result =  (Optional)ForComprehensions.foreach3(c -> c.flatMapAs$1(two)
														.flatMapAs$2(four)
														.mapAs$3(three)
														.yield(()->{return f2.apply(c.$1(), c.$2());}));
		
		assertThat(result,equalTo(Optional.of(8)));

	}
	
	public void testForComphrensions4(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result =  LessTypingForComprehension4.foreach(c -> c.flatMapAs$1(one)
														.flatMapAs$2(empty)
														.flatMapAs$3(Optional.empty())
														.mapAs$4(Optional.empty())
													//	.guard(()->c.<Integer>$1()>2)
														.yield(()->{return f2.apply(c.$1(), c.$2());}));
		
		assertThat(result,equalTo(Optional.empty()));

	}
}
