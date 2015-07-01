package com.aol.cyclops.comprehensions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;

public class OptionalTest {

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forComprehension(){
		Optional<Integer> two = Optional.of(2);
		Optional<Integer> three = Optional.of(3);
		Optional<Integer> four = Optional.of(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	
		Optional<Integer> result =  Do.add(two)
										.add(four)
										.add(three)
										.yield(v1->v2->v3 -> f2.apply(v1, v2))).unwrap();
		
		assertThat(result,equalTo(Optional.of(8)));

	}
	@Test
	public void testForComphrensions4Null(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = null;
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result = Do.add(one)
							.add(empty)
							.withOptional(v1->v2->Optional.empty())
							.add(Optional.empty())
							.yield(v1->v2->v3->v4-> f2.apply(v1, v2)).unwrap();
		
		assertThat(result,equalTo(Optional.empty()));

	}
	@Test
	public void testForComphrensions4(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result =  Do.add(one)
							.withOptional(v1->empty)
							.withOptional(v1->v2->Optional.empty())
							.withOptional(v1->v2->v3->Optional.empty())
							.yield(v1->v2->v3->v4-> f2.apply(v1, v2))).unwrap();
		
		assertThat(result,equalTo(Optional.empty()));

	}
	
	@Test
	public void test1(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		Object result = Do.add(one)
						   .yield(v->f2.apply(v, 10)).unwrap();

		assertThat(result,equalTo(Optional.of(10)));

	}
	@Test
	public void test2(){
		Optional<Integer> one = Optional.of(3);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
				
		
		Object result =  Do.add(one)
							.withOptional(v1->Optional.of(v1))
							.filter(v1->v2->v1>2)
							.yield(v1->v2-> f2.apply(v1, v2)).unwrap();
		
		assertThat(result,equalTo(Optional.of(9)));

	}
}
