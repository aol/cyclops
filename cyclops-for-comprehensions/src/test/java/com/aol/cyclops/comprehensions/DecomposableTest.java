package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;

import lombok.Value;
import lombok.val;

import org.jooq.lambda.Seq;
import org.junit.Test;

import com.aol.cyclops.lambda.api.Decomposable;

public class DecomposableTest {

	@Test
	public void decomposable(){
		val one = new  MyCase("hello",20);
		val two  = new MyCase2("France");
		
		Seq<String> result = ForComprehensions.foreach2(c -> c.flatMapAs$1(one)
										 .mapAs$2(v->two)
										 .yield(v-> v.$1().toString() 
												 	+ v.$2().toString())
				);
		
	
		assertThat(result.toList(),equalTo(Arrays.asList("helloFrance","20France")));
	}
	
	@Value static class MyCase implements Decomposable{ String name; int value;}
	@Value static class MyCase2 implements Decomposable{ String country;}
}
