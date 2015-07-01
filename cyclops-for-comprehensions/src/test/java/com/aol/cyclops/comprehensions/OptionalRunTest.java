package com.aol.cyclops.comprehensions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;



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

	
		Optional<Integer> result =  Do.add(two)
										.add(four)
										.add(three)
										.yield(v1->v2->v3-> f2.apply(v1, v2)).unwrap();
		
		assertThat(result.get(),equalTo(8));

	}
	
	@Test
	public void testForComphrensions4(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		ForComprehensions.foreach4(c -> c.flatMapAs$1(one)
														.flatMapAs$2((Vars4<Integer,Integer,Integer,Integer> v)->empty)
														.flatMapAs$3(v->Optional.empty())
														.mapAs$4(v->Optional.empty())
														.run(v->{ result= f2.apply(v.$1(), v.$2());}));
		
		assertThat(result,equalTo(null));

	}
	
	@Test
	public void test1(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		ForComprehensions.foreach1(c ->  c.mapAs$1(one)
										  .run((Vars1<Integer> v)->{result =f2.apply(v.$1(), 10);}));

		assertThat(result,equalTo(10));

	}
	@Test
	public void test2(){
		Optional<Integer> one = Optional.of(3);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
				
		
		ForComprehensions.foreach2(c -> c.flatMapAs$1(one)
										.mapAs$2((Vars2<Integer,Integer> v)->Optional.of(v.$1()))
										.filter(v->v.<Integer>$1()>2)
										.run(v->{result = f2.apply(v.$1(), v.$2());}));
		
		assertThat(result,equalTo(9));

	}
}
