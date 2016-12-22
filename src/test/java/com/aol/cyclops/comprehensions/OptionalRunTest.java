package com.aol.cyclops.comprehensions;

import cyclops.Optionals;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;



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

	
		Optional<Integer> result = Optionals.forEach3(two,a->four,(a,b)->three,(v1,v2,v3)-> f2.apply(v1, v2));
		
		assertThat(result.get(),equalTo(8));

	}
		
	@Test
	public void test1(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		Optional<Integer> result  = Optionals.forEach2(one,a->empty,(a,b)-> f2.apply(a, 10));

		assertThat(result.isPresent(),equalTo(true));

	}
	@Test
	public void testEmpty(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.empty();
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;


		Optional<Integer> result  = Optionals.forEach2(one,a->empty,(a,b)-> f2.apply(a, 10));

		assertThat(result.isPresent(),equalTo(false));

	}
	
}
