package com.aol.cyclops.comprehensions;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
public class FreeFormTest {


	@Test
	public void freeForm(){
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = ForComprehensions.foreachX(c -> c.$("hello",list)
										.yield(()-> c.<Integer>$("hello")+2));
		
		assertThat(Arrays.asList(3,4,5),equalTo(stream.collect(Collectors.toList())));
										
	}
	@Test
	public void freeFormCustom(){
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = ForComprehensions.foreachX(Custom.class,  
									c-> c.myVar(list)
										.yield(()->c.myVar()+3)
									);
		
		assertThat(Arrays.asList(4,5,6),equalTo(stream.collect(Collectors.toList())));
										
	}
	static interface Custom extends CustomForComprehension<Stream<Integer>,Custom>{
		Integer myVar();
		Custom myVar(List<Integer> value);
	}
	
}
