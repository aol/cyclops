package com.aol.cyclops.comprehensions;


import com.aol.cyclops.util.Optionals;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class OptionalTest {

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forComprehension(){
		Optional<Integer> two = Optional.of(2);
		Optional<Integer> three = Optional.of(3);
		Optional<Integer> four = Optional.of(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	
		Optional<Integer> result =  Optionals.forEach3(two, a->four,(a, b)->three,(v1, v2, v3) -> f2.apply(v1, v2));
		
		assertThat(result,equalTo(Optional.of(8)));

	}
	@Test
	public void testForComphrensions4Null(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = null;
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result = Optionals.forEach4(one,
										   a->empty,
											(v1,v2)->Optional.empty(),
											(a,b,c)->Optional.empty(),
											(v1,v2,v3,v4)-> f2.apply(v1, v2));
		
		assertThat(result,equalTo(Optional.empty()));

	}



	


	@Test
	public void test1(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		Optional<Integer> result = Optionals.forEach2(one,a->Optional.of(20),(a,b)->f2.apply(a, 10));

		assertThat(result,equalTo(Optional.of(10)));

	}
	@Test
	public void test2(){
		Optional<Integer> one = Optional.of(3);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
				
		
		Object result =  Optionals.forEach2(one,
									v1-> { System.out.println(v1); return Optional.of(v1);},
									(v1,v2)->v1>2,
									(v1,v2)-> f2.apply(v1, v2));
		
		assertThat(result,equalTo(Optional.of(9)));

	}
}
