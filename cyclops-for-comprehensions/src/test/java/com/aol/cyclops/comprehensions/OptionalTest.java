package com.aol.cyclops.comprehensions;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Test;

import com.aol.cyclops.comprehensions.notype.LessTypingForComprehension3;

public class OptionalTest {

	@Test
	public void forComprehension(){
		Optional<Integer> two = Optional.of(2);
		Optional<Integer> three = Optional.of(3);
		Optional<Integer> four = Optional.of(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Optional<Integer> result =  (Optional)ForComprehensions.foreach3(c -> c.flatMapAs$1(two)
														.flatMapAs$2(four)
														.mapAs$3(three)
													//	.guard(()->c.<Integer>$1()>2)
														.yield(()->{return f2.apply(c.$1(), c.$2());}));
		
		assertThat(result,equalTo(Optional.of(8)));

	}
}
