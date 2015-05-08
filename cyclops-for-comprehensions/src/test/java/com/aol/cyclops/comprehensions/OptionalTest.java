package com.aol.cyclops.comprehensions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.BiFunction;

import org.junit.Test;

import com.aol.cyclops.comprehensions.LessTypingForComprehension1.Vars1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension3.Vars3;
import com.aol.cyclops.comprehensions.LessTypingForComprehension4.Vars4;

public class OptionalTest {

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forComprehension(){
		Optional<Integer> two = Optional.of(2);
		Optional<Integer> three = Optional.of(3);
		Optional<Integer> four = Optional.of(4);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

	
		Optional<Integer> result =  (Optional)ForComprehensions.foreach3(c -> c.flatMapAs$1(two)
														.flatMapAs$2((Vars3<Integer,Integer,Integer> v)->four)
														.mapAs$3(v->three)
														.yield(v->{return f2.apply(v.$1(), v.$2());}));
		
		assertThat(result,equalTo(Optional.of(8)));

	}
	
	@Test
	public void testForComphrensions4(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		Object result =  ForComprehensions.foreach4(c -> c.flatMapAs$1(one)
														.flatMapAs$2((Vars4<Integer,Integer,Integer,Integer> v)->empty)
														.flatMapAs$3(v->Optional.empty())
														.mapAs$4(v->Optional.empty())
														.yield(v->{return f2.apply(v.$1(), v.$2());}));
		
		assertThat(result,equalTo(Optional.empty()));

	}
	
	@Test
	public void test1(){
		Optional<Integer> one = Optional.of(1);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer, Integer, Integer> f2 = (a, b) -> a * b;

		
		Object result = ForComprehensions.foreach1(c ->  c.mapAs$1(one)
														
														.yield((Vars1<Integer> v)->{return f2.apply(v.$1(), 10);}));

		assertThat(result,equalTo(Optional.of(10)));

	}
	@Test
	public void test2(){
		Optional<Integer> one = Optional.of(3);
		Optional<Integer> empty = Optional.of(3);
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
				
		
		Object result =  ForComprehensions.foreach2(c -> c.flatMapAs$1(one)
																		.mapAs$2((Vars2<Integer,Integer> v)->Optional.of(v.$1()))
																		.filter(v->v.<Integer>$1()>2)
																		.yield(v->{return f2.apply(v.$1(), v.$2());}));
		
		assertThat(result,equalTo(Optional.of(9)));

	}
}
