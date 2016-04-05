package com.aol.cyclops.comprehensions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.For;



public class OptionalRunTest {
	Integer result ;
	
	@Before
	public void setup(){
		result = null;
	}
	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forComprehension(){
		Optional<Integer> two = Optional.of(2);
		Optional<Integer> three = Optional.of(3);
		Optional<Integer> four = Optional.of(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	
		Optional<Integer> result =  For.optional(two)
										.optional(a->four)
										.optional(a->b->three)
										.yield(v1->v2->v3-> f2.apply(v1, v2)).unwrap();
		
		assertThat(result.get(),equalTo(8));

	}
		
	@Test
	public void test1(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		Optional<Integer> result  = For.optional(one)
								.yield(v-> f2.apply(v, 10)).unwrap();

		assertThat(result.get(),equalTo(10));

	}
	
}
