package com.aol.cyclops.lambda.tuple;


import static com.aol.cyclops.lambda.tuple.PowerTuples.tuple;
import static java.util.stream.Collectors.counting;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class CollectorTest {

	@Test
	public void collector(){
		 assertThat(Stream.of(1, 2, 3)
		                  .collect(tuple(counting(),Collectors.toList()).asCollector())
		                  ,equalTo(tuple(3,Arrays.asList(1,2,3))));
	}
	@Test
	public void collector2(){
		PTuple2<Set<Integer>,List<Integer>> res = Stream.of(1, 2, 3)
                .collect(tuple(Collectors.toSet(),Collectors.toList()).asCollector());
		
		 assertThat(res,equalTo(tuple(3,Arrays.asList(1,2,3))));
	}
	
}
