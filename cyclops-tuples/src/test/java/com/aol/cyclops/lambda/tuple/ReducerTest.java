package com.aol.cyclops.lambda.tuple;


import static com.aol.cyclops.lambda.tuple.PowerTuples.tuple;
import static java.util.stream.Collectors.counting;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.lambda.monads.Monoid;

public class ReducerTest {

	@Test
	public void reducer(){
		Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		Monoid<CachedValues> reducer = tuple(concat,join).asReducer(); 
		 assertThat(
		  
		            Stream.of("hello", "world", "woo!").map(s ->(CachedValues)PowerTuples.tuple(s))
		                  .reduce(reducer.zero(),reducer.reducer())
		                  ,equalTo(tuple("helloworldwoo!","hello,world,woo!"))
		        );
	}
	
}
