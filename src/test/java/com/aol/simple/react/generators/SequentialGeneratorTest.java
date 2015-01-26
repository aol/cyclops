package com.aol.simple.react.generators;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SequentialGeneratorTest {

	SequentialGenerator generator;
	
	int count =1;
	
	@Test
	public void testGenerateEquivalentToListNoOffset()  {
		
		count =1;
		List<Integer> list = Arrays.asList(1,2,3,4);
		
		System.out.println(list.stream().skip(2).limit(1).collect(Collectors.toList()).get(0));
		 generator = new  SequentialGenerator(4,0);
			Stream<CompletableFuture<Integer>> result1 = generator.generate(() -> count++);
			List<Integer> list1 = result1.map( cf -> get(cf) ).collect(Collectors.toList());
			assertThat(list1,is(list));
		
	}
	@Test
	public void testGenerateEquivalentToListWithOffset()  {
		
		count =1;
		List<Integer> list = Arrays.asList(1,2,3,4);
		
		list = list.stream().skip(2).limit(2).collect(Collectors.toList());
		 generator = new  SequentialGenerator(2,2);
			Stream<CompletableFuture<Integer>> result1 = generator.generate(() -> count++);
			assertThat(count,is(1));
			List<Integer> list1 = result1.map( cf -> get(cf) ).collect(Collectors.toList());
			assertThat(list1.size(),is(list.size()));
			
		
	}
	private Integer get(CompletableFuture<Integer> cf) {
		
		try {
			return cf.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

}
