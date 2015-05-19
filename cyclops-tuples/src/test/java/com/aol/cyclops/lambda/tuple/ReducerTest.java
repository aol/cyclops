package com.aol.cyclops.lambda.tuple;


import static com.aol.cyclops.lambda.tuple.PowerTuples.tuple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.api.Monoid;
 
public class ReducerTest {

	@Test
	public void reducer(){
		Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		Monoid<CachedValues> reducer = tuple(concat,join).asReducer(); 
		 System.out.println(Stream.of("hello", "world", "woo!").reduce("", (a,b)->a+","+b));
		 assertThat(Stream.of("hello", "world", "woo!").map(CachedValues::of)
		                  .reduce(reducer.zero(),reducer.reducer())
		                  ,equalTo(tuple("helloworldwoo!",",hello,world,woo!")));
	}
	@Test
	public void reducer2(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = tuple(sum,mult).<PTuple2<Integer,Integer>>asReducer()
											.mapReduce(Stream.of(1,2,3,4)); 
		 
		assertThat(result,equalTo(tuple(10,24)));
	}
	
}
